# Android app 启动时 Application、类加载器的初始化过程


当 app 启动时 ActivityManagerService.startProcessLocked() 是 app 启动时启动进程的地方

### ActivityManagerService.startProcessLocked() 的具体过程

1. **ActivityManagerService.startProcessLocked() 的具体过程**

	```
	// 
	// 这里是真正启动的代码；
	// entryPoint 是 "android.app.ActivityThread" 是 activityThread 的 包名路径，
	//便于后面进行反射调用它的 main() 方法
	Process.ProcessStartResult startResult = Process.start(entryPoint,
                    app.processName, uid, uid, gids, debugFlags, mountExternal,
                    app.info.targetSdkVersion, app.info.seinfo, requiredAbi, instructionSet,
                    app.info.dataDir, entryPointArgs); 
	```
	
	`app.processName` 是 进程名
	
	`Process.Start()` ---> `ZygoteProcess.startViaZygote` ---> 通过 socket 连接 发送给 zygote, ZygoteInit.main()---> `ZygoteInit.startSystemServer()`
	---> `Zygote.forkSystemServer()`
	
	
2.  **ZygoteInit.java 的过程**
		
		
	```
	//ZygoteInit.java 
	
	/**
	* Prepare the arguments and fork for the system server process.
	* 准备一些必要的参数，并且 从 system server process  fork 一个 进程 
	*/
	private static boolean startSystemServer(String abiList, String socketName)
            throws MethodAndArgsCaller, RuntimeException {
    	
    	....
    	
    	/* Request to fork the system server process */
    	pid = Zygote.forkSystemServer(
    					...
    					);
    	
    	....
    	
    	// 由 systemserver 第一次创建我们的app 进程时 走不到这里
    	/* For child process */
        if (pid == 0) {
            if (hasSecondZygote(abiList)) {
                waitForSecondaryZygote(socketName);
            }

            handleSystemServerProcess(parsedArgs);
        }
    	       
   }
	```
	
	这里 对 **Zygote.forkSystemServer()** 这个方法做个说明，它的 返回值为三种：
	
	- 0 : 代表着 当前是在新建的子进程中执行的，， 当由我们 app 里面的 mainProcess 开启一个  workProcess 时， 返回值为 0
		
	- pid of the child : 如何使在 父进程中执行的（即最刚开始的进程）， 则会返回 新建的子进程的 pid
		
	- -1 ： 代表出错
	
3. **分析一下 pid == 0 后的代码走向** 
	
	*********这是 pid== 0 开始分析的分割线**********
	
	以下部分代码，是程序走到 if (pid == 0) 里面的逻辑
	
	start() 方法 通过 socket 的方式 向 Zygote 进程 发起进程创建请求，在 `Zygote.forkSystemServer()` 之后 会调用 `handleSystemServerProcess()` 

	```
	//ZygoteInit.java 
	private static void handleSystemServerProcess(
	ZygoteConnection.Arguments parsedArgs) 
	throws ZygoteInit.MethodAndArgsCaller {
		
		// 关闭 socket 服务
		closeServerSocket();
		....
		....
		
		// 创建 classLoader 并且把它设为 currentThread() 的 classLoader
		ClassLoader cl = null;
       if (systemServerClasspath != null) {
       
       		// createSystemServerClassLoader() 创建的是一个 PathClassLoader
           cl = createSystemServerClassLoader(systemServerClasspath,
                                   parsedArgs.targetSdkVersion);

           Thread.currentThread().setContextClassLoader(cl);
       }
		....
	}
	```
	
	当运行到这里时，代表当前是 system server 进程，所以去创建了 system server 的 classLoader, 设置了 Thread.current().setContextClassLoader() 类加载器，后面的应用中所有的类默认是通过上面的类加载器加载.
	
	*********这是上面 pid == 0 的结束线**********

4. **问题：application 的 类加载器是哪个？**
	
	**现在引出另外一个问题，Application 的 类加载器 是哪个？？？咱们接着看**
	
	*attach() 引起的一系列事件 ……*

	
	在后面就是 通过socket 反射调用了  ActivityThread.main() 方法， 创建了 ActivityThread 后，进行 attach() 操作
	
	```
	//ActivityThread.java
	/**
	* 把这段代码写出来，只是想让大家看到 Looper.prepareMainLooper()、 attach() 、 Looper.loop() 的位置关系， 
	* 我有点说不出来，但感觉有些东西是与这个相关的
	*/ 
	public static void main(String[] args) {
		
		...
		
		Looper.prepareMainLooper();
		ActivityThread thread = new ActivityThread();
		thread.attach(false);
		...
		
		Looper.loop();
		
		throw new RuntimeException("Main thread loop unexpectedly exited");
	}
	
	```
	
	在 thread.attach() 里面进行了 attachApplication(mAppThread) 同时会调用 thread.bindApplication()  这里是 `ActivityThread.bindApplication()`
		
	```
	//ActivityThread.bindApplication()
	public final void bindApplication(...) {
		
		...
		//最后一句， 发送了一条 message 消息，
		sendMessage(H.BIND_APPLICATION, data);
	}
	
	```
	
	 在 `ActivityThread.handleMessage()` 中 对 BIND_APPLICATION 的接收后调用了 `handleBindApplication()` 
	 
	 ```
	 // ActivityThread.java
	 
	 /**
	 * 真正绑定 application 的地方 
	 */ 
	 private void handleBindApplication(AppBindData data) {
	 	
	 	...
	 	...
	 	...
	 	
	 	//If the app is being launched for full backup or restore, 
	 	// bring it up in a restricted environment with the base application class.
	 	
	 	Application app = data.info.makeApplication(data.restrictedBackupMode, null);
	 	mInitialApplication = app;
	 	...
	 }
	 
	 ```

5. **调用 `LoadedApk.java`, `ApplicationLoaders.java` 部分**
	 
	 这里还会涉及到其他的类，`LoadedApk.java`, `ApplicationLoaders.java` 等相关类;
	 
	 在 `LoadedApk.makeApplication()` 里面的代码 会去获取 classLoader, 并且创建 appContext, 再通过 classLoader 和 appContext 去创建 application 对象;
	 
	 ```
	 // LoadedApk.java 
	 public Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation) {
     	
     	...
     	// getClassLoader() 是去获取一个 与当前 apk 相关联的 pathClassLoader， 
     	java.lang.ClassLoader cl = getClassLoader();
     	
     	ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
     	
     	// app 为 application
     	app = mActivityThread.mInstrumentation.newApplication(
                    cl, appClass, appContext);
     	
     	...
     	// 通过 instrumentation.callApplicationOnCreate() 去调用 app 的 onCreate() 方法，即 application 的 onCreate()；
     	instrumentation.callApplicationOnCreate(app);
     	       
    }
	 ```

6.  **总结： 创建 application 的过程**
	
	1. mActivityThread.mInstrumentation.newApplication(cl, appClass, appContext)
	
	2. 第一个参数 cl, 是 当前 app 的 classLoader, 
	3. 第二个参数是 appClass, 是 当前 app application 的名字，检索的包名
	4. appContext  是新建的 context， 也是当前这个 app 对应的 context； 当作为参数去创建 application 时， 在 application.attach(context), 把该 context 作为 application 的 context 
	5. 在 newApplication() 中，会利用 cl.loadClass(appClass) 的得到当前 app 的 application

	6. **补充** loadClass 后得到的 clazz, 下一步进行了 

		```
		Application app = (Application)clazz.newInstance();
		
		app.attach(context);
		return app;
		```
	
		其中 app.attach(context)， 会调用 application 的 attachBaseContext();
		
		而后会 调用 instrumentation.callApplicationOnCreate(app)， 会调用 application.onCreate();
		
		所以 application 的 attachBaseContext() 时机 早于 application.onCreate()；
	
	7. **mInstrumentation 又是个啥？？？**

		Instrumentation 是 android 系统里面的一套控制方法或者“钩子”，我理解的是，它可以在正常的生命周期（正常是有系统控制的）之外控制 android 的运行， 拿到一些流程控制的时机，然后我们就可以在这些时机里面写一些处理我们想要的东西。它提供了各种流程控制方法，例如下面的方法：
		
		```
		callActivityOnCreate()-----> 对应着系统的 onCreate();
		
		callActivityOnStart()-----> 对应着系统的 onStart();
		
		callActivityOnDestroy()-----> 对应着系统的 onDestroy();
		``` 
 
 		所以 当我们去新建一个类， 继承与 Instrumentation, 重写一些方法，在这些方法里面我们就可以自由的做一些控制的事情，例如，添加自动测试模块等。
 

8. **application 创建出来后，那么在它之前创建的 classLoader 是个什么样的过程？**
 
	到目前为止，由 thread.attach(); 引起了一系列的事件，现在已经正式的创建了 application， 并且去执行了 application 的 onCreate() 方法；
	 
	 **那么 app apk 什么时候与 classLoader 什么时候关联的呢？**
	 
	 看这一行代码 ： java.lang.ClassLoader cl = getClassLoader();
	 
	 ```
	 public ClassLoader getClassLoader() {
	 	synchronized (this) {
	 		if (mClassLoader == null) {
	 			createOrUpdateClassLoaderLocked(null /*addedPaths*/);
	 		}
	 		
	 		return mClassLoader;
	 	}
	 }
	 ```
	 在看一下 createOrUpdateClassLoaderLocked() 的实现：
	 
	 ```
	 //LoadedApk.java
	 
	 private void createOrUpdateClassLoaderLocked(List<String> addedPaths) {
	 	...
	 	// 主要关键的地方
	 	
	 	mClassLoader = ApplicationLoaders.getDefault().getClassLoader(
                    "" /* codePath */, mApplicationInfo.targetSdkVersion, isBundledApp,
                    librarySearchPath, libraryPermittedPath, mBaseClassLoader);
                    
	 }
	 ```
	
	对 mClassLoader 进行了赋值，那在ApplicationLoaders.getDefault().getClassLoader() 是如何操作的呢？
	
	对于 `ApplicationLoaders` 这个类，它里面维护了一个 mLoaders， 它是一个map， key 为 string （可以看做是 包名）， value 为 ClassLoader(类加载器)，
	
	看一下 getClassLoader() 的实现：
	
	```
	public ClassLoader getClassLoader(String zip, ... ) {
		
		...
		// 首先 检查 mLoaders map 里 是否有该 loader， 有即返回，
		//没有则创建一个新的 pathClassLoader, 并把新建的 loader 加入 mLoaders
		
		ClassLoader loader = mLoaders.get(zip);
       if (loader != null) {
           return loader;
       }
       
       PathClassLoader pathClassloader = PathClassLoaderFactory.createClassLoader(...);
       ...
       mLoaders.put(zip, pathClassloader);
       
       return pathClassloader;
		
		...
	}
	```
	
	每个 app 进程都有唯一的 ApplicationLoaders 实例， 后续则通过 apk 的路径 （zip 参数）查询返回 classLoader, 因为 ApplicationLoaders 里 维护了  classLoader 的一个 map 常量 mLoaders, 所以 一个进程可以对应多个 apk!
	
	> 不知道还记不记得 app lock theme 里面的实现，就是利用 主 app 去加载 theme app 里面的代码，就是说 主 app 里持有 theme apk 的 classLoader!!!
	

8. **总结总结！！ app 启动时发生的事情**	

	大概可以总结一下了，在上述过程中，我们知道当我们的程序运行起来时
	
	1. 先去从 zygote fork 一个 进程 
	
	2. fork 进程之后，会调用 `ActivityThread.main()`， new 出一个 ActivityThread 对象 并且会把它与 application 关联起来，即 attach();
	3. attach() 会引发一系列事件，这个过程中 会先后创建 classLoader, appContext, 和 application, 把 application 和 activityThread 关联起来
	4. 调用 instrumentation.callApplicationOnCreate(app)， 触发 application 的 onCreate() 方法


	我的老哥，美滋滋
	

9. **上述过程，是 application 的创建时机，其他部分呢？**

	上面的绝大部分都是分析 在 app 启动时 application 的创建时机 和 classLoader 的关系， 那么其他部分的类，例如 四大组件 和 普通的类 的 classLoader 是哪个呢？


	可以经过同样差不多的分析，发现 最终他们都是同一个 classLoader, 会去 ApplicationLoaders.getDefault().getClassLoader() 拿到该 apk 对应的那个 classLoader
	
10. **ClassLoader.getSystemClassLoader()， 这又是一个什么东西呢？**

	在这个过程中，我看到了在 ApplicationLoaders 里调用了 :
	
	ClassLoader.getSystemClassLoader(); 
	
	我实际测试了一下，它与 application 的 classLoader 都是属于 PathClassLoader，他们两有相同的 parent : bootClassLoader,  但却是不一样的对象，那么它是做什么的呢？
	
	打印出来，看看：
	
	```
	ClassLoader classLoader = getClassLoader();
	HSLog.i(TAG, "onCreate() classLoader is " + classLoader);
	HSLog.i(TAG, "onCreate() ClassLoader.getSystemClassLoader() is " + ClassLoader.getSystemClassLoader());
	```

	log 如下：
	
	```
	onCreate() classLoader is dalvik.system.PathClassLoader[
	DexPathList[
	[zip file "/data/app/包名/base.apk"],
	nativeLibraryDirectories=[/data/app/包名/lib/arm, /data/app/包名/base.apk!/lib/armeabi, /vendor/lib, /system/lib]
	]
	]
	
	
	onCreate() ClassLoader.getSystemClassLoader() is dalvik.system.PathClassLoader[
	DexPathList[
	[directory "."],
	nativeLibraryDirectories=[/vendor/lib, /system/lib]
	]
	]
	
	```
	
	可以看到，这两个 classLoader 是不一样的，官方对 ClassLoader.getSystemClassLoader() 的解释是：
	`返回委托的系统类加载器， 通常是用于启动应用程序的类加载器`, 我有点不懂它的意思, 在  createSystemClassloader() 时， classPath 的默认值就是 ".", 上面的打印也说明了（directory "."）；同时它和 classLoader 同时都加载了 native 下的 `/vendor/lib, /system/lib` 的 代码
	
	ClassLoader.getSystemClassLoader() 是做什么的呢？？？
	
	网上有一个说法是这样的: "当我们自定义 classLoader 时，假设是一个插件工程，想与host工程不冲突，独立运行，关注插件工程中的类的加载，而不关注host工程中的类的加载造成的冲突，此时可以将自定义类加载器的 parent 指定为此 classLoader， 即systemClassLoader。"
	
	我再找找答案吧，搜了很多，没找到具体的说明。
	
	
	
参考链接：

1. [以 ClassLoader 为视角看 Android 应用的启动过程](https://www.jianshu.com/p/6e695471bd08)

2. [ClassLoader的来源](http://linjiang.tech/2016/10/31/App's%20ClassLoader%E7%9A%84%E6%9D%A5%E6%BA%90/)

3. [理解Android进程创建流程](http://gityuan.com/2016/03/26/app-process-create/)

4. android 源码, `Zygote.java`, `ActivityManagerServer.java`, `Process`, `ZygoteInit.java`, `ActivityThread.java`, `LoadedApk.java`, `ApplicationLoaders.java`


