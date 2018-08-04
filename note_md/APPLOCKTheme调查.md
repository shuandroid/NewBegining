
## App 动态加载 Theme 实现 和对 classLoader 的理解

一个应用程序 app 内，想要有多种主题，目前来说主要有两种实现方式：

1. 应用内 res 内置资源：

	这样好处是便于实现，坏处是会增大包的体积，当主题十分多时，会把主程序拖死，所以这种方式基本只适用于主题非常少，且不会再增多的情况，例如，夜间模式，这时便可以 把 夜间模式的 资源放入主程序的 res 下。
	
2. 利用插件技术，读取 theme Apk 里面的资源:

	这样的好处是，主程序不需要包含这些主题的资源，减少包的提交，且主题个数可以有无限多个，但是在实现上会比第一种方式难度大些。
	
**综上可知**： 为了theme主题的多变和主程序的包体积，选择第二种方式是常用的解决方式。

下面主要讲解如何利用第二种方式，实现 app 动态换肤。

### 如何从主 `app` 中拿到 `theme app` 中的资源？ 即资源访问
	
这是实现过程中最重要的一点，如何能拿到 theme apk 下的资源。

其实在 android 中，我们通常去拿一些资源，总是通过 `getContext().getResources()`去获取到我们想要使用的资源。所以，当我们可以获取到 theme app 中的 context，按照道理来讲，我们便可以访问 theme app 中的任何资源了。

> 注： 如果深入代码去找，会发现 最后是通过 `AssetManager.addAssetPath()` 去加载资源的， 

1. **获取到 theme app 的 context:**

	```
	Context context = null;
   try {
        context = createPackageContext(packageName,
               Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
   } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
   }
	```
	
	android  给我们提供了 `createPackageContext(String packageName, @CreatePackageOptions int flags)` 这个方法，可以获取到一个 theme app 的 context ， 该 context 和该 theme app 正常 `launched` 启动时的 context 是同样的。
	
	获取到该 themeContext 后，我们便可以去拿到我们想要的资源了。
	
	```
	// 这是 Resources 里的一个方法，返回该资源对应的 ID
	public int getIdentifier(String name, String defType, String defPackage) {
		...
	}
	
	// 第一个参数 name : 我们想要的资源的名字, 即在theme app 中 该资源的ID
	// 第二个参数 defType:  想要资源的类型， drawable, dimen, color ...
	// 第三个参数 defPackage: 当 context 找不到时，默认去寻找的包名，可以为null
	
	```
	获取9个资源 bitmap， 这些 drawable  的 名字分别为 `gesture_dot_1_connecting` ~ `gesture_dot_9_connecting `
	
	```
	Resources themeResource = themeContext.getResources();
	List<Bitmap> bitmapList = new ArrayList<>();

   try {
       for (int i = 1; i <= 9; i++) {
            String drawableName = "gesture_dot_" + i + "_connecting";

            bitmapList.add(DisplayUtils.drawable2Bitmap(themeResource
                        .getDrawable(themeResource.getIdentifier(drawableName, "drawable", packageName))));
        }

   } catch (Exception e) {
        e.printStackTrace();
   }
	
	```

### 利用获取到的 `context` 我们还可以做哪些事？

context 里面有个 `getClassLoader()` 的方法:

```
/**
* Return a class loader you can use to retrieve classes in this package.
*/
public abstract ClassLoader getClassLoader();
```

返回一个可用于检索此包中的类的类加载器.

这个类加载器可以做什么？？？

#### 有关 类加载器 ClassLoader

所有类加载到程序中，都是通过 `classLoader` 实现的

在 android 中， `ClassLoader` 分为两种，分别是系统 `ClassLoader` 和 自定义 `ClassLoader`. 其中系统 `ClassLoader` 包含三种，分别是: `BootClassLoader`, `PathClassLoader`, `DexClassLoader`.

在 android 运行一个 app 时，其实不止一个 classLoader 在运行，而是两个以上。

一个为 Java.lang.BootClassLoader， 是用于加载一些系统 framework 层级需要的类

一个为 dalvik.system.PathClassLoader, 是用来加载  apk dex 文件里面的类，包含 dexPathList ( 它里面显示具体的 classLoader).

利用如下代码可实现：

```
ClassLoader classLoader = baseContext.getClassLoader();
if (classLoader != null) {

    Log.i(TAG, "classLoader is " + classLoader.toString() + " --->from Log");

    while (classLoader.getParent() != null) {
          classLoader = classLoader.getParent();
          Log.i(TAG, "classLoader is " + classLoader.toString() + " --->from Log  in while");
    }
}
```

打印的结果为：

```
ThemeLayoutContainer: classLoader is dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.example.chenzhao.thememaintest-1/base.apk"],nativeLibraryDirectories=[/data/app/com.example.chenzhao.thememaintest-1/lib/arm, /data/app/com.example.chenzhao.thememaintest-1/base.apk!/lib/armeabi-v7a, /vendor/lib, /system/lib]]] --->from Log
ThemeLayoutContainer: classLoader is java.lang.BootClassLoader@f42ce93 --->from Log  in while
```

可以发现 java.lang.BootClassLoader 是 dalvik.system.PathClassLoader 的 parent.

**parent 是个什么概念呢？**

在 ClassLoader 机制中，采用了**双亲委托模型**。

即 当新建一个 classLoader 时，会如下：

```
//构造
ClassLoader(ClassLoader parentLoader, boolean nullAllowed) {
	if(parentLoader == null && ! nullAllowed) {
		throw new NullPointerException("parentLoader == null && !nullAllowed");
	}
	
	parent = parentLoader;
}
```
可以看到， 创建一个 classLoader 时，需要使用一个 现有的 ClassLoader 实例作为 parent，这样一来，所有的 classLoader 都可以利用一颗树联系起来，这是 classLoader 的双亲委托模型。
 
双亲加载类时 是通过 loadClass() 方法，一个classloader 加载类时的特点和步骤：

1. 会先查询当前 ClassLoader 是否加载过此类，加载过就返回；

2. 如果没有，查询 它的 parent 是否已经加载过此类，如果有，就直接返回 parent 加载过的类；

3. 如果继承路线上的 classLoader 都没有加载，则会有它去加载该类工作；

当一个类被位于树根 的 classLoader 加载过，那么， 在以后整个系统的生命周期内，这个类永远不会被重新加载。

所以 `BootClassLoader` 用于加载系统 framework 层的类，是所有 app 内其他 classloader 的 parent。

> 如何唯一标识一个类，即它在加载过程中的唯一性？
> 由一个 classLoader 加载，并且该 classLoader 里没有和它一样的目录下的文件名，那么它就是唯一的。
> 
> 当由同一个 classloader 加载，并且是在相同的目录下，相同的文件名， 如果有两个时，加载时便会出错。
> 
> 同一个class =  相同的 className + 相同的 packageName + 相同的 classLoader

#### PathClassLoader

在android中，当应用启动时，PathClassLoader 会自动创建，从 /data/app/包名/...中加载 apk 文件. 

> PathClassLoader 其实加载的都是我们自己编写的类或我们依赖的 库的类，在 data/app/当前目录/目录下的类, apk 里面的 class.dex 都是通过 pathClassLoader 去加载的。

#### BootClassLoader

而 `BootClassLoader` 是 PathClassLoader 的父加载器，加载一些系统 framework 层级需要的类，在系统启动时创建，在 app 启动时 会将该对象传进来，（我的理解是， 所有 app 启动时都会用到这个 BootClassLoader, 并且为同一个对象，实验证实。）

```
if (loader == null) {
	loader  = BootClassLoader.getInstance();
}

```

> bootClassLoader 加载一些系统 framework 层级需要的类，以后任何地方用到都不需要重新加载, 有共享作用


### 实现一个 main app 对应 多个 主题 theme apk, 需要良好的规则

成功获取到 context 后，我们便可以去拿到这些资源了，但是，在后面，假设我们会再次更新主题，这些新开发的 theme 必然要和第一套theme 里的资源名字，id 都是一样的，才可以获取成功。那在不改动 main app  的情况下，要做到多个主题任意替换，就需要我们在刚开始去设计到主题变化的范围，并且把所有可变的元素都罗列出来，在每个 theme apk 中，这些相对应的 元素都有对应的资源存在. 一定要切记切记！！！

需要一个规则存在，每每添加新的 theme 时，都要遵循这些规则.


### 更近一步，释放出更多的可变内容，一个可以思考的方向

上面的实现主要是资源的更换，可变的范围往往是背景图片的改变，每个 view 的大小及背景变化，但是，做不到对每个view 做一些特殊化处理。例如，如果我们想要去在 A theme 中 实现对 一个 view 的缩放动画， 在另外一个 B theme  里 实现对这个 view 的旋转动画，那上述的方法是做不到的。 甚至是， 在 a theme 中 和 b theme 中的界面都是完全不同的，这样的效果，目前来说，只替换资源是达不到的。

**我们可以在一个 theme apk 中获取一整个布局， 然后加载在 main app
上面**

**或是直接去 theme apk 获取一个 自定义 view 展示在我们 main app 上面**

代码如下：

```
// packageName 是指 要取资源的apk的包名， layoutName 是要取得对应的 layout 的名字
private View getRemoteLayout(String packageName, String layoutName, ViewGroup parent) {
    Context context = null;
     try {
         context = baseContext.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
     } catch (PackageManager.NameNotFoundException e) {
         e.printStackTrace();
     }
     
     if (context != null) {
     	int resId = context.getResources().getIdentifier(layoutName, "layout", packageName);
     	return LayoutInflater.from(context).inflate(resId, parent, false);
     }
     
     return null;
}     

```

获取到该 view 后，可通过反射的方法去调用该 view 里面的任何方法：

```
	// object 是上面我们获取到的view， methodName, 是我们想要调用的方法
    public Object invokeObjectMethod(Object object, String methodName) {

        Log.i(TAG, "invokeObjectMethod() ");
        if (object == null) {
            return null;
        }

        Object returnValue = null;
        try {
            Class<?> themeAnimationContainerClass = object.getClass();
            returnValue = themeAnimationContainerClass.getMethod(methodName).invoke(object);
            Log.i(TAG, "invokeObjectMethod() returnValue is " + returnValue);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return returnValue;
    }
```
注意：这里  method.invoke(object) 的返回值 是 这个对象调用该方法后 返回的结果！！！

为什么要设置 returnValue 为 Object 类型？ 因为不确定调用该方法后会返回何种类型的 结果。

如果我们想 传递一个参数给 我们想要通过反射调用的类的那个方法，该如何实现呢？

如下：

```
        try {
            Class<?> objectClass = object.getClass();
            Method method = objectClass.getMethod(methodName, paramArrayOfClass);
            returnValue = method.invoke(object, paramArrayOfObject);

            Log.i(TAG, "invokeObjectMethod() returnValue is " + returnValue);
        }
```

objectClass.getMethod(methodName, paramArrayOfClass);

第一个参数为 要调用的方法名字，第二个为 参数的类型，可以是多个参数的类型， 例如: `new Class[]{ Context.class }`

method.invoke(object, paramArrayOfObject);

第一个参数为 要反射调用的 对象，第二个参数为 具体的参数，可以为多个, 例如 `new Object[]{ baseContext }` 

> **注意：** 这里我们要传递的参数，必须为 framework 中的类，才会匹配到该方法。因为 其实 在 我们的 main app 的类 A 中去调用 theme app 里面的类B，那么其实，加载 类 A 的是一个 classLoader， 加载 类 B 的是另外一个 classLoader！！！， 即使是同一个类名，同一个包名， 传递过去的参数 类C，也不会匹配到 theme app 中的 参数类C的！！！为什么？？？ 在上面，我们知道， pathClassLoader 只会加载 当前这个 apk 下的资源文件，当去加载另外一个 apk 下的文件时，必然是另外一个 classLoader，两者不会是同一个对象，所以导致，参数不匹配。 同时我们也知道，加载 framework 的是同一个 BootClassLoader, 所以当参数为 framework 中的类时，是会正确匹配到的
> 

**还有一个问题** 该如何去处理一些时机上的行为？

例如  在 main app 类 A 中调用了 theme app 类B 中的 方法`setAClassListener()`, 当 在该方法中，时机到了，可以通知给 类A 监听已经生效了，可以开始回调了， 该怎么做好呢？

提供两种思考方式：

1. 参数为 handler, 在 theme app 类 B 中通过 handler 发送消息，在 main app 类 A 中去接受这些消息;

	这种方法，没有实际测过，但是别人实际过，是可行的，但也有一些问题，handler  处理可能并不是完全同步的

2. 在 在 theme app 类 B  中，在时机到了的地方，再通过反射去回调 main app 类A 中的方法；

	这种方式，实际测过，可达到预期的效果。其实本质上还是利用反射的方式去调用其他 apk 下的方法

通过第二种方式可顺利的实现一些功能。

#### 总结

这种进一步开放出去更多内容的方式， 要慎重，一旦把更多的内容交给外部 apk 去处理，同时也会伴随着更多的问题会出现，它的优势大，风险也大，这里只是给大家提供一种实现的思路，具体做时，要慎之又慎！


-------------	

### 全篇总结

以上是实践过程中的一些心得，对 classLoader 并没有很详细的说明，水平有限，classLoader 内容比较多，涉及的地方也比较多，哪里不对的地方，大家一起讨论，研究。


> chenzhao
> 
> 参考链接：[APK 动态加载基础 classloader](https://segmentfault.com/a/1190000004062880)
> 
> [android 动态加载机制 ](https://blog.csdn.net/u012439416/article/details/70473515)



		