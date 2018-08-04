## ONE YEAR

我的 android 开发一年总结。

2017年6月21号，是我在华中科技大学的毕业典礼的日子，也是对我来说颇为重要的一天，我大学毕业。距今，2018年6月，已经快要一年了。这一年中，在技术方面遇到很多有趣的东西，同时弥补了之前很多一知半解的东西，在整体方面，我感觉自己越来越好了，如果说自己越来越差了，是不是也不太对啊……（尴尬ing）

下面是一篇极为枯燥的文章……

下面是一篇主观性偏多的文章……

下面是一篇 长长长长长～～～的文章……

### 1. 是不是每一个走向工作的 dev 都是从动画开始？

我是去年6月份底毕业的，7月3号入职。 入职做的第一个 feature 是一个动画，有关 junkScan 的动画，属于一个扫描类的动画。当时看到这个动画的 gif 图，第一瞬的感觉是有种血脉喷张的感觉，有种跃跃欲试的感觉，同时，这也是一个比较复杂的动画，之前在学校里面，这些动画，恩，说实话没想去做过，所以也会有些紧张，刚一开始不知道从何处入手。也不得不承认，公司这个环境会让你迅速成长，迅速掌握一些知识。也是那个时候正式详细的接触 `ValueAnimator` 和 `ObjectAnimator` ，没办法呀，大学里我太菜，很多知识没有接触到。这个动画是一个大的扫描动画，下面是三个小动画，伴随着背景的变化。慢慢的学会了，从慢去看动画，一帧一帧的看动画，才分析的详细，在会把这个时间段所发生的操作全部了解。所以我理解动画，就是把它一点点剖析，在慢的角度去看它，去分析它，然后什么时间应该做哪些操作，按照这样就可以达到，动画其实很简单。

后面又不同的做了很多种类的动画，当你对动画熟悉以后，一个动画其实在你刚去做时，你就已经知道应该在哪里做哪些事情，下面是我认为一些需要注意的地方：

#### 1.1 ValueAnimator, ObjectAnimator 需要注意的地方：

1. `onAnimationUpdate`, `onAnimationEnd`, `onAnimationStart` 三个方法，当然还需要去设置插值器，默认并不是线性匀速加速器，而是 `AccelerateDecelerateInterpolator `,  这个自行设置就好。


2. 设置了 `setRepeatCount` 后

	 当设置了 `setRepeatCount` 后，它对 `onAnimationEnd` 和 `onAnimationStart ` 的 调用只有一次了，理解起来就是 这两个方法分别在动画的开始和动画的结束调用，可当你设置了 repeatCount 为 5 后，这个整体的动画开始就是第一遍动画的开始，动画的结束就是 第五次动画的结束，所以，这两个方法均只会调用一次，

	但是如果是你在 `onAnimationEnd ` 中重新 调用了这个 animator 的 start ，则 每次 `onAnimationEnd` 和 `onAnimationStart` 都会调用；

 
3. 在 animtor.cancel() 时 会调用 `onAnimationEnd()`, 如果不想这个方法被调用，需要先 removeAllListeners();

4. 尤其要注意设置了 `setRepeatCount(ValueAnimator.INFINITE)`

	这个地方要多注意，因为一旦设置了这个属性，动画会不断的执行下去，如果不及时释放会造成内存泄漏，所以在 `onPause()` 和 `onDestroy()` 要判断下，如果该 animttor  != null 要释放该动画；
	
#### 1.2 ValueAnimator, ObjectAnimator 动画与自定义 View

很多动画都不只是对一个 原生的 view  进行操作就可以达到所有需求的，动画也往往与自定义 view 联系在一起， 下面是一些个人对 自定义 view 动画的感触：

1. 预留一个方法接口去开启动画，也需要预留一个方法接口去停止动画，在编程里很多都是对称的
	`startAnim()`, `stopAnim()`

2. 往往都是通过 `invalidate()` 去调动 onDraw() 重新绘制
 

3. 在 `onDraw()` 中，不外乎几种方式:

	* canvas.drawArc(@NonNull RectF oval, float startAngle, float sweepAngle, boolean useCenter,
            @NonNull Paint paint)
            
   		 `useCenter`, 这个参数比较重要，为 true 时 是一个扇形的弧， 为 false 时是一个一段弧
   		 
   * canvas.drawCircle()
   * canvas.drawBitmap()

4. paint.setXfermode()

	有一些效果，只是简单的绘制可能达不到效果，Xfermode 是作用在两个 Bitmap（或其他形式的元素上） 相互交合的部分的效果，它有很多效果，我只记得简单的 例如 src_in 等效果
	
	参考链接： [自定义控件三部曲之绘图篇（十）——Paint之setXfermode(一)](https://blog.csdn.net/harvic880925/article/details/51264653)
 
 	* 擦除效果：

 		擦除效果类的动画，可以理解为 两个图层在相互进行 xfermode 效果
 
 	* 高光闪过效果：

 		好像很多设计比较喜欢添加 高光闪烁的效果，往往，这个高光不太好加，多试试 xfermode
 
5. 动画的卡顿问题

	可能是因为动画开始时布局文件尚未绘制好，导致的丢帧情况（一下子,顿一下，运行或直接接跳过一段时间），这时需要`postDelay` 一下，尽可能不会发生 draw 丢帧情况(丢帧是可能会跳过 view 的部分 `draw()` 方法)

#### 1.3 动画的思考

在动画方面，其实还有很多，我没有去接触，下面是一些需要加强的部分

1. `OpenGL` 这一块，我还没机会去涉足，感觉这是一个大的部分，内容很多；

2. 对于动画的原理，不够清楚, 有时候我思考时会有限制，会有一些理所当然，我会想 `onAnimationUpdate()` 是每次值刷新后回调的方法，我在这里做相应的操作，可到底是谁去回调的 `onAnimationUpdate()` 呢？我有时会忽略这一点，想当然的背后就是不足够理解。

3. `ValueAnimator`, `ObjectAnimator` 的插值器，

	插值器影响的是 `animation.getAnimatedValue()`, 和 `animation.getAnimatedFraction()` 的值，其实插值器是影响这些返回值的变化的速率。深入代码里， `ValueAnimator.animateValue()` 	
	
	```
	/**
	* this method is called with the elapsed fraction of the animation
	* during every animation frame
	*/
	@CallSuper
	void animateValue(float fraction) {
        fraction = mInterpolator.getInterpolation(fraction);
        mCurrentFraction = fraction;
        int numValues = mValues.length;
        for (int i = 0; i < numValues; ++i) {
            mValues[i].calculateValue(fraction);
        }
        if (mUpdateListeners != null) {
            int numListeners = mUpdateListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                mUpdateListeners.get(i).onAnimationUpdate(this);
            }
        }
    }
	```
	
	会根据当前的插值器，1. 计算动画的进度(0 ~ 1), `fraction ` ；2.同时计算动画的实际进度值--->映射到 我们想要的值, `mValues` ；3. 通知动画的进度回调, `mUpdateListeners`


4. 谁触发的 `onAnimationUpdate()`
 
 	追踪代码，会找到 `doAnimationFrame()`---> `animateBasedOnTime()`--->`animateValue ()` ---> `onAnimationUpdate()`, 
 	
 	而 `ValueAnimator.doAnimationFrame()` 是被 `AnimationHandler.doAnimationFrame()` 里被调用的， 
 	
 	```
 	private void doAnimationFrame(long frameTime) {
 		...
 	}
 	```
 	是私有的，它的被调用是在 `Choreographer` 的回调 ---> `doFrame()` 里被调用的;
 	
 	 `AnimationHandler`  是一个单例， 用来处理所有活着的 `ValueAnimator (active ValueAnimator)`.
 	 
 	 与 `Choreographer` 的关系
 	 
 	 ```
 	 public class AnimationHandler {
 	 
 	 	...
 	 	private final Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        	@Override
        	public void doFrame(long frameTimeNanos) {
        		
        		//因为 AnimationHandler. doAnimationFrame() 是私有的，所以它的调用均在它自身里面
            	doAnimationFrame(getProvider().getFrameTime());
            	if (mAnimationCallbacks.size() > 0) {
                	getProvider().postFrameCallback(this);
            	}
        	}
    	};
 	 	... 
 	 }
 	 ```
 	 
 	 
5. 一个重要的类 `Choreographer`， 屏幕刷新信号
 
 	我对这个类还没深入进行了解，它是屏幕刷新的关键, `ViewRootImpl`, `scheduleTraversals()` , 都是大把大把的好玩的.
 	
 	一个 View 发起刷新的操作时，会层层通知到 `ViewRootImpl` 的 `scheduleTraversals()` 里去，然后这个方法会将遍历绘制 View 树的操作 `performTraversals()` 封装到 `Runnable` 里，传给 `Choreographer`
 
> 以上是我对动画的一些理解， 细细琢磨来，动画的实现可能不是那么难，但是动画涉及到很多知识，在对动画的刷新上还需要进一步加强理解；
> 
> `View` 的绘制，或是 `findViewById()` 均是深度优先；

### 2. 开发随笔 - 日常拾壳

动画是一个刚开始阶段， 后面大大小小做了一些其他方面的事，在这个过程中，发现很多对之前的我不了解的部分，捡一些好玩的部分记录一下：

1. 相对布局 `PercentRelativeLayout`

	这个主要做屏幕的适配，在众多的机型上，挺好用的。
	
2. 更好用的一种布局 `ConstrainLayout`

	这个就比较好玩了，可以完全只用一个父布局 `ConstrainLayout ` 完成布局的书写，而且同时支持百分比布局，它里面还有很多新奇的特性，可以完成之前在开发过程中不太好实现的效果，这个布局总会给我很多惊喜，我觉着它还会有很多惊奇的点待发现。

      在后面做任务时，所有的布局我都是利用的 `ConstrainLayout` 实现的，虽然有些界面完全是重新写了一遍，但我感觉这个过程还蛮有意思的，做的点滴好处，说不定在某个时候会积累成一个大的优势。
	
3. 当点击 Home键退出时， 从后台启动 activity, android 系统做的 5 秒限制

	情景是这样的，我监听了其他的某些具体的app A， 当从这个 app A退出，回到桌面时， 我从自己的 app 后台进程那里启动了一个 activity， 退出 app A ， 回到桌面有两种方式：
	
	1. 点击 back 键退出， 回到桌面，

	2. 点击 home 键退出， 回到桌面

	当是第一种方式时，可以立马从我的 app 后台启动 一个 activity， 但是当是第二种方式时， 会延迟 5 秒才会启动 我的这个 activity ！
	
	为什么会这样呢？ 发现：
	
	 Google 特意提醒开发者，当用户点击 home 键退出时，不要从后台（包括 service 或者 broadcastReceiver）启动 activity，任何在后台 `startActivity` 的操作都将会延迟 5 秒, 除非获取到了 "android.permission.STOP_APP_SWITCHES" 权限.
	 
	 在 `ActivityManagerService.java` 的代码里，我们可以发现：
	 
	 ```	 
	@Override
    public void stopAppSwitches() {
    	...// 检查是否为 STOP_APP_SWITCHES 权限
    	
    	if (checkCallingPermission(android.Manifest.permission.STOP_APP_SWITCHES)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("viewquires permission "
                    + android.Manifest.permission.STOP_APP_SWITCHES);
        }
    	
    	// APP_SWITCH_DELAY_TIME 为 5000 （5 秒）
    	synchronized(this) {
            mAppSwitchesAllowedTime = SystemClock.uptimeMillis()
                    + APP_SWITCH_DELAY_TIME;
            mDidAppSwitch = false;
            mHandler.removeMessages(DO_PENDING_ACTIVITY_LAUNCHES_MSG);
            Message msg = mHandler.obtainMessage(DO_PENDING_ACTIVITY_LAUNCHES_MSG);
            mHandler.sendMessageDelayed(msg, APP_SWITCH_DELAY_TIME);
        }
    	
    	// 加锁，并且，延迟5秒发送消息, 在接收消息（DO_PENDING_ACTIVITY_LAUNCHES_MSG）的地方  会去调用 
    	//mActivityStarter.doPendingActivityLaunchesLocked(true);
    }
	 ```
	 
	 其实这里面还有很多细节，我对这个的理解，当用户点击 Home 键回到桌面时， 是想退出 app 回到桌面，不继续操作事情的，可如果你 从后台去开启一个 activity, 进入到一个app内 这就会影响用户的体验，所以 Google 有了 5秒的限制，让你即使能在 后台开启 activity 也会延迟 5秒，这样至少用户会先看到 桌面，等过了5秒，才会开启你的 activity，
	 
	 如何解决这个问题？ 如果获取不到 "android.permission.STOP_APP_SWITCHES" 这个权限，就只好不从后台开启 activity， 可以去 show 一个 floating window, 但并不是所有的机型都支持 floating window.
	 
	 参考链接: [后台启动 activity 的 限制](https://blog.csdn.net/QQxiaoqiang1573/article/details/77015379)
	 
4.  一些沙滩上的贝壳：

	在日常的开发中，我有时会发现一些好玩的点，很简单的一部分，我把它们记录在了下面这篇文章里面了：
	
	[那些好玩的 android 小事](https://www.jianshu.com/p/702b950491fc)
	
5. 等待补充

### 3. 第一个模块任务 clean whatsapp

起步于动画和一些小的需求任务，慢慢的做熟了，后来开始了一个整模块的需求，一个新的模块，放手给我去做，当知道这个任务时，蛮开心的。

在这里，很想对在公司带我的 mentor 和 leader 感谢， 感谢你们一年来对我技术上的帮助，感谢你们一年来对我在公司的帮助，谢谢你们！ 我的 mentor 是一个 很好相处也超级厉害的大佬，很有开发经验， 我经常会给他说，mentor 你写本书吧，写本有关 debug 心得的书，绝对大卖！我的 leader，这是一位特别负责的 leader，刚开始在公司时，我写的代码，先被 mentor review 后， 再由 leader review，刚开始，他会一行一行代码的 review，告诉我哪里不太对，告诉哪里逻辑缺少，告诉我更好的实现方法，并给我说明原因，他对代码的要求极高，一个变量名，一个方法名，都要尽可能达到能表述清楚的程度，谢谢 leader 对我技术的帮助和引导。

#### 3.1 clean whatsapp 的思考

clean whatsapp 这个模块很多地方都是需要对文件，对数据进行操作。下面是一些在这个过程中有感触的东西：

1. 完完全全，在很短的时间内自定义了一个很复杂的动画，，其实剖析起来，慢慢的实现，并不是很复杂

2. 对文件 file 的读取和移除， 对各种格式文件 根据后缀去分类
	
	这个部分，我想把那些常用的对文件的操作的方式集中写一写，有很多可以借鉴的部分。
	
3. `ImageView` 的设置 `setBackground...()` 与 `setImage...()` 的区别

	 这个地方，虽然之前了解，但在写代码时，曾经也犯了写错。
	 
	 setBackgroundResource()、setBackgroundDrawable()  是 ImageView 的 背景，setImageResource()、setImageDrawable() 是  ImageView 的 content 内容。
	 
	 这两者是可以同时存在的！！！，所以如果你对 ImageView 设置了 background, 又设置了 setImage, 就会有两层！！！
	 
	 > 同时一个需要注意的点， Glide 默认对 imageView  的设置是 setImage...();

4. RecyclerView 的设置问题，

	尽量不要设置 `RecyclerView` 的 高为 `wrap_content`,
	
	若`RecyclerView ` 高度设置为`wrap_content`,子item有收缩、展开动画，在点击收缩时，可能会出现奇怪的界面刷新方式，原因可能是由于收缩时item已经没有空间位置了，但是需要做动画去完成，没有足够的空间位置，导致动画特别的奇怪，不协调。
	
5. 接口的实现

	接口的出现很大部分是为了降低一些耦合性；
	
	我觉着它的方便在于： 可以很方便的在一些合适的时机去执行对应的逻辑。
	
	监听事件 onClickListener() 是一个很好的接口. 
	
6. Flexibleadapter 确实挺好用的……

	这个是一个库，抽象化了 recyclerView 的 adapter， 并且在 adapter 里面 很好的区分了 headItem, subItem, 
	
	尤其是在 在某个时机（回调回来的时机），想要新增一个 view （嘘：例如广告位啊），新增的这部分View 的空间可以跟着 recyclerView 滑动而滑动， 那么这时， *利用 scrollView  去嵌套 recyclerView  会有事件的拦截问题，而且更重要的是往往会带来性能上的问题。*所以把这部分新增的 view 作为 一个 item 插入 recyclerView 的 头部是一个比较好的选择！！！ 
	
	这时在 recyclerItem 的头部加入一个新的 item，会显得很方便，直接 flexibleadapter.addScrollableHeader(...) 就可以达到要求。
	
7. SharedPreference 的问题

	要考虑到该部分 sp 是否需要跨进程传输数据，如果是跨进程就需要另外一套封装的方法；
	
	因为 原生的 sp 在跨进程传输时 可能会变得不稳定，出现错误，所以可以在跨进程时对原生的 sp 进行封装一下， 这里的封装，我公司这边使用的 ContentProvider 对它封装了一下，利用 CP 的 Bundle call(...) 方法实现数据的跨进程传递， 这个部分到后面在说心得吧。
	
8. 要考虑耗时操作

	对数据库文件的处理，对批量文件的读取，对复杂动画的开启，都需要放在子线程里面去实现，我们要考虑这些耗时的操作。
	
	当然最好是利用统一的一个 ThreadPool 去处理，如果我们各自在自己需要的地方 new Thread 去处理我们的耗时操作， 就会导致我们的程序里面出现大量的子线程，会出现大量的资源浪费问题，所以最好是用 线程池 去操作。

9. Collections.unmodifiableList(list) 的使用

	目的是返回一个不可修改的 list， 主要目的是为里面维护该list的私有性，对外只可读，不可修改，当要去修改时，会抛出`java.lang.UnsupportedOperationException`异常。
	
	从这里还可以印出来一些其他的东西， 像 `CopyOnWriteArrayList`, `CopyOnWriteArraySet `, `ConcurrentHashMap `, 这些我一下子解释不上来，还得一段时间去消化和理解。

10. 注解的使用

	注解好呀，注解可以很大的提高代码的可读性。
	
	注解有很多类，其中一类可以对方法的参数做一些限制，对返回值做一些限制，举个简单的例子： 
	
	```
	@StringDef({WHATS_APP_JUNK_IMAGE, WHATS_APP_JUNK_VIDEO...})
	@Retention(RetentionPolicy.SOURCE)
	public @interface JunkType {
	}
	
	// 简单使用
	@WhatsAppFileUtils.JunkType
    private String itemJunkType;
   
	```
	
	感觉注解还有好多好多的用处，我对注解的理解，一是它让代码的可读性更强了，二是它让代码写起来更舒服些，更便捷。
	
	还有很多很多的注解，像 `ButterKnife` 就是一个很好的注解库，从大学就开始用这个库.

11. 数组在方法中作为参数的使用，减少成员变量的个数

	有时候需要一个常量，在每次操作一个方法时可能需要对它进行一些操作，这个常量再某个地方会再次被使用。
	
	**现在有这样一种思路：**， *利用一个大小为1的数组实现该功能。*

	```
	int[] totalTemp = new int[1];
	updateFirstCard(totalTemp);
	updateSecondCard(totalTemp);
	updateThirdCard(totalTemp);
	
	int total = totalTemp[0];
	//total里为每次更新后的值
	
	....
	private void updateFirstCard(int[] totalTemp) {
		...
		totalTemp[0] += 100;
		...
	}
	...
	```
	
	其原理是引用，数组的引用在传递参数的过程中，它的指向一直没有变化，所发生的操作均是指向上的操作，所以最后我们拿到的值（这个指向所存储的值）是每次更新后的正确的值。
	
	这样的操作的话，就不需要有一个成员变量去记录每次变化了， 是一个比较小的点，但有时会思考不到这些。

#### 3.2 Clean WhatsApp 总结

做 Clean WhatsApp 时学了挺多的，对数据的操作，及时更新UI 等，也学习了一些关于分包的知识，感觉有些代码是仁者见仁智者见智，有些代码的写法也是这样，而且，我想，每一个 dev 都会有自己的一个代码的编写习惯，也有自己的一些思考方式，我觉着这个很重要，这个是在语言之上的，对代码的理解，对它的掌控，跟语言没很大关系。在这个过程中，我学到了很多代码编写上的一些小点，太多的小点了，有用的小点，慢慢的开始有了自己的一套代码风格，我想逐渐完善这个风格，让这个风格更通俗，更好。在这个过程中，难免要多次去思考，有时你面对一个问题，今天的想法和昨天的想法是不同的，很多思考的角度也在变化，且走且行吧，让自己有个更好的代码编写风格，加油加油。

### 4. 难度升级的模块， Safe Browsing

Safe Browsing 是一个更大的模块，感谢 leader 优先把这些学习的机会留给我， 很赞很赞。 Safe Browsing 的主要难点是对 WebView，，，我有些浅陋无知，对这个了解太少，组内人员也对这个没有很多的了解，mentor 前期调研了一些浏览器的内核相关，最后决定里是利用 Webview 原生控件 Chrome 的内核去实现。（其实更挫一点，就是给 Webview 加了一个壳子而已）

在这一部分，印象深刻的就是 WebView 本身。它有太多的未知问题了，有太多的不可预测的，太多的特殊的情况。

我之前曾经 把在 WebView 实践过程中遇到的问题，都记了下来，整理了两篇文档：

- 一篇是 WebView  的主要使用方法：

	链接：[WebView 的反思和记录 ---定制设置和常见问题](https://www.jianshu.com/p/45d771100c72)
	
	这里主要记录了 WebView 的常见设置和回调， 
	
- 另外一篇是 在使用 WebView 的过程中遇到的问题：

	链接：[WebView 遇到的问题](https://www.jianshu.com/p/ae6b92fbabcd)
	
	里面详细记录了，我在实际的开发中遇到的问题，并提供了一些简单的解决方案。
	
	但其实这也是我没有完善的一点，因为我依赖着 webview 的各种回调，但是这些回调往往不那么靠谱，我就在想，能不能把这些回调，再封装一下，对外面提供一个相对好些的 WebView，但还没时间去做，我觉着这是一个可以改进的地方，同时也会有难度。

我有时会自己做些总结，其实很多都是瞎写着玩，有很多都没有好好整理，因为自己看，所以很随意，也没有特别的上心。做完 Safe Browsing 后，可能是因为被 WebView 坑的比较有心得，可能也有一些成就感，所以好好的整理了上面的两篇总结，放在了简书上。 我想每一个写过博客的人，都会为自己写的文章得到肯定而高兴， 我没有想到的是，在简书上收到了鸿洋的消息，想要转载我的这两篇文章，在征询我的同意。我看到消息时，蛮惊喜的，也很开心，感觉自己写了一篇能够得到别人认可的文章，很有成就感。

### 5. 一个产生变化的过程，推荐机制

做 Safe Browsing 是一个很大的部分，需要处理很多很多逻辑上的情况，同时也需要考虑很多细小的细节，从开始做，到上线，差不多一个月的时间，20天左右。 再结束了这个部分后，我开始接触了我们组当时在准备的一个新的慨念的实现，名字叫 推荐机制。


我觉着做推荐机制这一套时，对我产生了很大的影响。让我开始从另外一个角度去思考代码的设计，整个 app 里面的一些功能的设计，让我开始站在一个更高的角度去思考和看待问题。

在推荐机制里面，有两种定义，一类是 `placement` , 称之为位置，一类是 `content`, 称之为 内容。它处理的一个事情大概可以概括为 在一个 `placement` 上是否要显示，以及要显示哪个 `content`。

一个 `placement` 对应多个 `content`, 当有机会，或者是可以在这个位置显示东西时，会根据 一个类似于权重的东西，依次去遍历 这个 `placement` 下面注册过的 `content`, 并且判断该 `content` 是否为 `Isvalid()`, 如果为有效，则交给该 `content` 去显示内容；如果无效，依次判断下一个 `content`, 直到所有的 `content` 都被遍历过。

这是我第一次参与这类功能的设计，并体会到在这个设计中的一些变化，最终选择了一个比较好的方式去处理该功能。 可能慢慢积累的量变，在某个点忽然变成质变一样，我开始会在做功能时去着眼更高的角度去思考一些问题（至少也是自以为更高的角度吧，希望不是瞎吹）。

#### 5.1 我和推荐机制

在后面，我做了一个小的部分 feature，这个 feature 是这个样子，在一个 界面上的一部分 新增一个 `ViewPager`，然后在这个里面去加载不同内容的卡片， 卡片的数量可能会变， 卡片的一些状态要传递给 外面。

#### 5.2 我实现的 “假” 推荐机制

**下面是我的一些实现想法：**

1. 我的这个 要显示的区域，可以看做是一个 `placement`, 它要显示的几个 card 都可以简单认为是 `content`;

2. 因为我要去显示的这些 card，它们之间是平等的，是否可以定义一个统一的接口，让它们都实现该 接口呢？

	我便写了一个 `IRecommendCard` 这个接口，在这个接口里面，我反复修改了挺多次它里面的方法， 我想要尽可能的把方法定义全面，同时也没有多余的感觉。
	
	```
	interface IRecommendCard {
		
		...
		/**
		* 判断该 card 是否有效，是否需要展示
		*/
		boolean isCardValid();
		
		/**
		* 初始化该 card 里的内容
		*/
		void inflateCardView();
		
		/**
		*  获取该 card 的需要显示的 view 
		*/
		View getCardView();
		
		/**
		*  标记该 card 的状态，是在显示状态 还是 不显示状态
		*/
		void markCardStatus(boolean isShowing);
		
		/**
		*  当该 card  可见时均会回调该方法， 它自身判断是否需要更新当前 view 显示的内容
		*/
		void updateCardOrNot();
		
		/**
		*  释放该 card 的资源，可能是未结束的动画之类的， 会在 销毁时，或用户关闭时调用
		*/
		void releaseCard()
		
		...
	}
	```
	
	其实还会有定义的一些接口 listener，把当前 card 的状态返回给它的老大（显示它的 host）
	
	> 可能在大家看来是比较简单的一种设计，可对当时的我来说，在做这个的过程中，感到很好玩，很有趣，很多内容有一种享受感，我比较菜，期待大家对我的有一些建议， 后面会有更多的可享受的地方，期待我会变得厉害。
	
3. 有了这些接口后，我就把我要有的 card 继承与该接口，复写接口的方法时，在各个方法里添加每个 card 自己独有的逻辑，布局，内容

	其实这一部分是相对自由的部分。每个 card ，他的实现以及逻辑都应由它自身控制，而与外部无关，它应该把自身的一些状态 反馈给外部，通过接口回调反馈。

	如果把接口写在 card 内部，card部分就不需要持有外部的一个引用， 但是外部会需要有每个  card  的监听； 
	
	如果把接口写在外面，card 部分实现了这个接口，就有了一个外部的引用，
	
	想起了  `Activity` 里面的 内部类 接口 `HostCallBack`, ， `Fragment` 里面持有这个接口的对象， 利用 `FragmentActivity` 里面封装好的一些方法，实现了  `Activity`  和 `Fragment` 之间相互通信，时机互调。
	
4. 在外部，一开始我在外部把这些所有的 card 都注册到该 `ViewPager` 下， card 的显示与否，跟外部完全没有关系。

#### 5.3 我对推荐机制的思考

我更多的觉着 推荐机制可以做很多东西，不只是这些方面的需求，有很多功能上，当我们换个思路去思考，是否会更加的合适呢？这是我在推荐机制里学到的东西，而且我有点形容不出来那种感觉，对外暴露的接口要准备的十分充分，对内的封装要尽可能详细。很棒的一种设计体验，一种思考体验。

### 6. 我对工具的思考

从开始进入到 IA 组，我的 leader 涛哥，就有介绍自己去写一些便捷的工具。慢慢的，我也了解到组内很多好用的工具，后来，我也跟着开始尝试着做一些有需求的工具。

既然是工具，必然是要达到某种功能的，我简单的介绍一下我们组的工具。我说的这些工具，是在项目编译时或者打包时起到一些作用， 我们利用 `build.gradle`  在里面设置了 task, 在特定的时候去做一些事情。例如，我们可以去跑一些脚本，去检查，确保一些代码里的内容， 如果发现有错误的地方，就打包不通过，修改之后，才能打包成功，我觉着这样的好处是可以防患于未然，很多线上的错误，可以通过工具在打包的时候去确保。

当然工具不仅仅只有这个用处，我们可以直接去写工具，可以把多语言的地方，删除一个语言的时候，其他所有的多语言也一起删除，会很大的节约时间；我们可以去检查一些自定义 view 里面构造函数的实现，看它是否符合要求（复写三个构造函数）; 我们可以去检查在 xml 写入的 自定义 view 的路径是否正确（现在可以通过 Lint 确保）；我们可以去检查是否有 scrollView 嵌套 recyclerView 的情况；我们可以去检查一些配置项是否正确；等等，利用工具，我们做很多事情，很多加快效率的事情。

我们大部分是利用 python 去写工具，我大二的时候曾经看过一段时间 Python，后来太长时间不去写它，差不多全忘了，我去写这个检查自定义 view 的脚本的时候，大部分的时间都在查 如何用一些 Python，但我觉着，一门语言，不影响你对这个工具的实现，重要的是你得知道该如何去实现这个工具，从哪个角度出发，通过什么样的方法，达到你的目的，而实现这些方法的语言代码，不会就去查嘛，总会解决。对代码的思考和理解，往往比代码语言要重要的多。

想记下来的几个问题：

1. build.gradle 

	 我对这个文件的理解程度还不够深， Groovy 是一个很强大的东西，我理解的点，不够啊。。。接下来要深入这部分去了解下，不过我对 build.gradle 这里面的东西还是有一定了解的。（主要是看过一篇超级好的博客，在里面对照着学了很多）。
	 

### 7. 对 ContentProvider 的思考

这个标题太大了，而我只能说出来一小部分。而且，很可能理解会出现偏差，但现在至少我是这么理解的，说出来嘛，有错的话，大家一起指出来嘛，一起讨论。

#### 7.1 先从 ContentObserver 说起

`ContentObserver` 是内容观察者，根据 `Uri` 观察 `ContentProvider` 中的数据变化，，在 `onChange()`方法中，回调通知。

使用步骤：

1. 我们可以自行定义一个类，继承于 `ContentObserver`， 重写它的 `onChange()` 方法；

	```
	private class MediaContentObserver extends ContentObserver {
	
		private Uri contentUri;
		....
		
		@Override
   		public void onChange(boolean selfChange) {
      		super.onChange(selfChange);
      
      		handlerMediaContentChange(contentUri);
   		}
		
	}
	```

2. 当然，我们首先要 注册这个监听:

	```
	ContentObserver externalObserver = new MediaContentObserver(handler, 	MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

    getContext().getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false, externalObserver);
      ```
	其中 我们创建 `ContentObserver` 时 传递的第二个参数 `uri`，就是 `media` 对应的 `ContentProvider` 的 `URI`！！！
	
	然后 getContentResolver().registerContentObserver(uri, boolean, ContentObserver...)
	
	- 第一个参数 uri:

		即需要监听的 uri, 当 这个 uri 对应的 ContentProvider 数据发生改变时，就会通知到 注册的第三个参数的 contentObserver 对象
		
	- 第二个参数： boolean 表示是否要精确匹配到对应的 uri

		当为 false 时，表示精确匹配，即只匹配该 uri 和它的祖先 ancestors（只有当该 uri 或者 URI 发生变化时，才会去通知注册的 contentObserver）;
		
		当为 true 时，表示可以匹配到该 uri 的后代
		
		我对 祖先和后代的理解为：
		
		`content://com.test.chen/room/bathroom` 祖先为 `content://com.test.chen/room/`, 后代为 `content://com.test.chen/room/bathroom/some`
		
	- 第三个参数，就是我们注册的 contentObserver 对象

3. 注册后，往往对应着反注册！！！

	在编程里面，有很多都是很有对称美的，例如 activity 的 onCreate() 与 onDestroy().
	
	我觉着这是一种编程中需要注意的思想，很多时候，我们都需要考虑到对称，感觉谷歌在设计代码时，也是加入了对称的思想在里面，所以在我们设计代码形式时，对称的代码更契合谷歌的思想，代码也更好维护。
	
	```
	getContentResolver().unregisterContentObserver(externalObserver);
	```
	
#### 7.2 ContentProvider 与 它的 URI

在一个 手机系统中，每一个 `ContentProvider` 和它所对应的 `URI` 都是唯一的，  `ContentProvider`  与 `URI` 也是一对一的！！！

因为 `ContentProvider` 可以跨进程使用，所以我们可以访问到其他 app 下的 `ContentProvider`。

上面我们注册时传入的 `MediaStore.Images.Media.EXTERNAL_CONTENT_URI`， 就是 系统掌管 `images` 的 `ContentProvider` 对应的 `URI` 。

当我们的 app 里面 添加了一个 `ContentProvider` 时，它的 `URI` 必须不能和 手机系统上 所有的 `URI`  相同，当有重复时（即系统上已经有一个 `ContentProvider` 的 `URI` 是当前我们添加的 `URI`）， 则会安装不到系统上。

#### 7.3 ContentResolver 的作用

它可以帮助我们去查询所有有关SD卡目录下的一些文件信息，例如 媒体文件， 通话记录，照片等。

同时我们注册 `ContentProvider` 和反注册时 都是通过 `getContentResolver()` 去获取到 `ContentResolver` 对象。

`ContentResolver` 提供了与 `ContentProvider` 相同名字的方法，用于数据的增, 删, 查, 改

它的 `query()` 方法用法示例：

```
//获取到媒体库中的照片文件，该文件满足，时间降序的第一个文件，且只包含两列数据
cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DATE_TAKEN
                    }, null, null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1");


//获取通话记录部分
// 去获取到系统所有的通话记录，并且只包含每个 callLog 的几列数据：
//CallLog.Calls.NUMBER， CallLog.Calls.DATE， CallLog.Calls.CACHED_NAME， CallLog.Calls.TYPE
cursor = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DATE,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

```

> 注：`MediaStore.Images.Media.EXTERNAL_CONTENT_URI` 是系统照片媒体库对应的 `ContentProvider` 的 `URI`
> 
> 而 `CallLog.Calls.CONTENT_URI` 是掌管系统通话记录 `ContentProvider` 的 `URI`；
> 

**对上述的理解和说明：**

1. 而上述两个过程，都是通过 `getContentResolver().query(...)` 去跨进程取得了里面的数据；

2. 这些数据 通过 `query`, `delete`, `add`, `update` 可以在多个进程操作数据， 本质上都是 `ContentProvider` 对应的 `query`, `delete`, `add`, `update`  方法。

3. 而 增删查改，这些对应的操作，为什么能够跨进程去做呢？？？ 

	其实本质上 上述过程 都是 `ContentProvider` 通过 匿名共享内存 在 app 之间进行数据共享的 ！！！当传输的数据量比较大时，使用匿名共享内存来传输数据是有很大好处的，可以减少数据的拷贝，提高传输效率.
	
	> 注： 我对 匿名共享内存 的理解还不够……
	
4. 但是呢？ 当传输的数据比较小，比较少时，使用匿名共享内存来作为媒介就有点浪费了，系统创建匿名共享内存也是有开销的。 那还有没有别的比较好的方式呢？？？ 有的 `Bundle` 要登场了~~~

	当我们在 `Activity` 之间跳转时，也许会从 A 传递一些简单数据到 B ， 这个过程我们就可以先把这些数据放在 `Bundle` 里面， 再通过 `intent`, 把 `bundle` 放在 intent 里面的 extra , 传递到 `Activity` B 中 。 这与 `ContentProvider` 有什么关系呢？ `ContentProvider` 里面有一个函数 `Bundle call(...)`, 这个函数，是我们下面记录的基础。
	
#### 7.4 我对 ContentProvider 的理解

上面我们说到，当有部分很小的数据需要在两个进程间进行传递时，也利用 `ContentProvider` 进行操作。

假设进程 A， 进程 B ，都需要访问一个数据 `TestData`, 那么我们可以利用 `ContentProvider` 中利用 `call()`函数， 利用 `Bundle` 去传值。

例如：

```
// 在一个自定义的 ContentProvider 中

public static int getTestData(){

	// 实质是 会去寻找对应 uri 的 ContentProvider ，然后调用它 里面的 call() 方法
	Bundle bundle = getContext().getContentResolver().call(uri, METHOD_GET_TEST_DATA, null, null);
	return null == bundle ? 0 : bundle.getInt(EXTRA_KEY_TEST_DATA, 0); 
}

public static void setTestData(int test) {
	Bundle bundle = new Bundle();
	bundle.putInt(EXTRA_KEY_TEST_DATA, test);
	getContext().getContentResolver().call(uri, METHOD_SET_TEST_DATA, null , bundle);
}

//复写 的  call() 方法
@Nullable
@Override
public Bundle call(String method, String arg, Bundle extras) {
	Bundle bundle = new Bundle();
	switch(method) {
		case METHOD_GET_TEST_DATA:
			bundle.putInt(EXTRA_KEY_TEST_DATA, PreferenceHelper.getInt(PREF_KEY_TEST_DATA, 0)(此处为 value))；
			break;
			
		case METHOD_SET_TEST_DATA:
			PreferenceHelper.putInt(PREF_KEY_TEST_DATA, extras.getInt(EXTRA_KEY_TEST_DATA));
			break;
			
		case ...
		
		default:
			break;
	}
	
	...
	
	return bundle;
}
```

在同一个 app 内，主进程 main process, 工作进程 work process,  如果我们想要实现 主进程和 工作进程之间的数据共享，那我们通过自定义一个 `ContentProvider`, 并且像上述实现了 `call()` 方法，就可以成功的实现 简单数据跨进程共享了。

> 注： `SharedPreference` 本身在跨进程传输数据时，可能会出现数据不稳定的情况，所以，原生的 `SharedPreference` 本身最好不要跨进程共享数据，如果需要跨进程共享数据，那就采用 `ContentProvider` 包装一层 `SharedPreference`， 在进行数据共享。

#### 7.5 对 ContentProvider 的总结

对 `ContentProvider` , 我个人现在会很享受利用 `Bundle` 在进程中传递数据这一块，觉着很赞！

现在我的理解可能还会有局限或者不对的地方，慢慢补充，慢慢增强。

### 8. Theme 主题，动态加载 与 ClassLoader

这是我目前做过的所有 feature 里最让我眼睛发光的一个 feature。

主要做的内容是为 applock 添加主题，添加主题的方式是 动态主题，通过下载 apk 去切换不同的主题样式。

在这个过程中，前期调研，后期实现，都遇到了很多有意思特别好玩的事情，所以，印象深刻。

#### 8.1 首先说一下 ClassLoader 

它本身就有说不完的东西，捡一些我想说的说一下：

在 `Android` 中 的 `ClassLoader` 分为两种，分别是系统 `ClassLoader` 和 自定义 `ClassLoader`. 其中系统 `ClassLoader` 包含三种，分别是: `BootClassLoader`, `PathClassLoader`, `DexClassLoader`.

##### 8.1.1 在 app 启动后，有哪些 ClassLoader

在 android 运行一个 app 时，其实不止一个 `ClassLoader` 在运行，而是两个以上。
 
用代码看一下：

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

打印结果为:

```
ThemeLayoutContainer: classLoader is dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.example.chenzhao.thememaintest-1/base.apk"],nativeLibraryDirectories=[/data/app/com.example.chenzhao.thememaintest-1/lib/arm, /data/app/com.example.chenzhao.thememaintest-1/base.apk!/lib/armeabi-v7a, /vendor/lib, /system/lib]]] --->from Log
ThemeLayoutContainer: classLoader is java.lang.BootClassLoader@f42ce93 --->from Log  in while
```

从代码中可以看到：

- 在我们的代码中，获取的 `getClassLoader()` 是 `PathClassLoader`

	`PathClassLoader` 里有一个对应的 `DexPathList`, 里面是它加载 class 的路径，也就是说，它可以加载的类，都在这个路径里面，超过这个路径的，就不是它可以加载的了，超出了它的权限。
	
- 它的 `parent` 是 `BootClassLoader`， 且该对象的 id 为 `@f42ce93`， 

	从打印的结果可以看到， `bootClassLoader`,为一个 具体的对象，且有id， 当我们去打印手机上其他 app 的 `parentClassLoader`, 发现 别的 app 里面也是 `java.lang.BootClassLoader@f42ce93`！！！
	
	是相同的一个对象，又经历过测试，发现，这个 `bootClassLoader` 是同一个 对象，是在手机系统刚开始初始化时加载进来的，并且当每个 app 启动时，会把该 `bootClassLoader` 作为 `parent`，传入给要启动的 app！！
	
	`BootClassLoader` 加载一些系统 `Framework` 层级需要的类，以后任何地方用到都不需要重新加载, 有共享作用。
	
- 提到这些就不得不说，**双亲委托模型**

	在 `ClassLoader` 机制中，采用了**双亲委托模型**。
	
	即 当新建一个 `ClassLoader` 时，会如下：

	```
	//构造
	ClassLoader(ClassLoader parentLoader, boolean nullAllowed) {
		if(parentLoader == null && ! nullAllowed) {
			throw new NullPointerException("parentLoader == null && !nullAllowed");
		}
	
		parent = parentLoader;
	}
	```
	
	可以看到， 创建一个 `ClassLoader` 时，需要使用一个 现有的 `ClassLoader` 实例作为 `parent`，这样一来，所有的 `ClassLoader` 都可以利用一颗树联系起来，这是 `ClassLoader` 的双亲委托模型。

	双亲委托模型加载类时，是通过 `loadClass()` 方法，它的主要逻辑如下：
	
	1. 会先查询当前 `ClassLoader` 是否**加载过**此类，加载过就返回；

	2. 如果没有，查询 它的 `parent` 是否已经加载过此类，如果有，就直接返回 `parent` 加载过的类；

	3. 如果继承路线上的 `ClassLoader` 都没有加载，则会有它去加载该类工作；

	当一个类被位于树根 的 `ClassLoader` 加载过，那么， 在以后整个系统的生命周期内，这个类永远不会被重新加载。

	所以 `BootClassLoader` 用于加载系统 `Framework` 层的类，是所有 app 内其他 `Classloader` 的 `parent`。

##### 8.1.2  PathClassLoader  是什么时候创建出来的？？？

当 app 启动时，它里面所有的类，应该都是 经过 `ClassLoader` 加载 进来的， 那么做为主要加载 app 内的 类的 `PathClassLoader` 是如何被创建出来的呢？

**这关系到 app 启动时的一系列操作**

1. app 启动时，`AMS(ActivityManagerService)` 会首先 启动我们 app 的进程，通过 `ActivityManagerService.startProcessLocked()` 启动进程

2. 在 `Process.start()` 会真正启动的代码，在 `start()`  方法会通过 `socket`  通信 向 `Zygote` 进程发起进程启动请求，进程起来后，再通过反射 调到 `ActivityThread.main()`方法；

3. 在 `ActivityThread.main()` 方法里面，会有一个主要的操作， `new` 一个 `ActivityThread` 对象，然后调用了 `thread.attach(false)`;

4. 其实 在 `thread.attach()` 里发生了很多很多操作！！！

	`thread.attach()` ---> `attachApplication()` ---> `thread.bindApplication()`
	
	---> `handleBindApplication()` 
	
	在 `handleBindApplication()`  里面 ：
	
	```
	Application app = data.info.makeApplication(data.restrictedBackupMode, null);
	mInitialApplication = app;
	```
	
	这里 `data.info` 得到的是一个 `LoadedApk` 对象； 也是在这里，创建出了 `application`,
	
	那么 `data.info.makeApplication()` 里面的细节是怎样的呢？
	
5. 调用 `LoadedApk.java`, `ApplicationLoaders.java` 部分

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

	所以，我们可以看到，`ClassLoader` 要 先与 `application` 创建出来。
	
	
6. 去看 `ClassLoader` 具体的得到逻辑， `java.lang.ClassLoader cl = getClassLoader()`

	具体的代码实现：
	
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

	在看一下 `createOrUpdateClassLoaderLocked()` 的实现：
	 
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
	 
	 对 `mClassLoader` 进行了赋值，那在`ApplicationLoaders.getDefault().getClassLoader()` 是如何操作的呢？
	
	对于 `ApplicationLoaders` 这个类，它里面维护了一个 `mLoaders`， 它是一个 `map`，`key` 为 `string` （可以看做是 包名）， `value` 为 `ClassLoader`(类加载器)，
	
	看一下 `getClassLoader()` 的实现：
	
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

	可以看到， 我们确实是创建了一个 `PathClassLoader` 对象，并且是与 apk的 zip 路径相关的（其实这里就是 在前面我们打印出的 `data/app/...` 里面的 `dexPathList`！！！）

	每个 app 进程都有唯一的 `ApplicationLoaders` 实例， 后续则通过 apk 的路径 （zip 参数）查询返回 `ClassLoader`, 因为 `ApplicationLoaders` 里 维护了  `ClassLoader` 的一个 `map` 常量 `mLoaders`, 所以 一个进程可以对应多个 apk ！！！

      > 其实在这个 任务中 我就是利用了主 app 去加载了 theme apk 中的资源，那么其实本质上就是 我的当前进程中持有着 theme  apk 对应的 `ClassLoader`	

**8.1.2 总结：**

这一节中，我们发现了在 `thread.attach()` 时 去创建了 `PathClassLoader`, 同时我们也发现， `PathClassLoader` 是对应具体的 apk 的 zip ， 也就是说 `PathClassLoader` 只可以读取当前 apk 里面的类。再次 `PathClassLoader` 创建的时机要早于 `Application`  的创建。

#### 8.2 我对 appLockTheme 的具体实现

在后期的讨论中，mentor 和 leader 觉着，我们只是从另外一个 apk 里去拿取资源文件，这样比较简单，并且可以快速的完成 theme apk 的开发。 而且，theme 这个本身也是一个大量的个数，如何短时间内可以上线大量的 theme 是一个比较重要的问题，只有当 theme。尽可能简单，尽可能不涉及到 theme apk 后期的升级问题，那么我们就要保证，每一个 theme 里面都不存在 bug， 那最简单的做法就是 我们只去 theme 里面去取 资源，不涉及或者少涉及 Java 代码。

##### 8.2.1 如何从主 `app` 中拿到 `theme app` 中的资源？ 即资源访问
	
这是实现过程中最重要的一点，如何能拿到 theme apk 下的资源。

其实在 android 中，我们通常去拿一些资源，总是通过 `getContext().getResources()`去获取到我们想要使用的资源。所以，当我们可以获取到 theme app 中的 `context`，按照道理来讲，我们便可以访问 theme app 中的任何资源了。

> 注： 如果深入代码去找，会发现 最后是通过 `AssetManager.addAssetPath()` 去加载资源的， 

**获取 Context**

```
Context context = null;
try {
        context = createPackageContext(packageName,
               Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
   } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
}
```

android  给我们提供了 `createPackageContext(String packageName, @CreatePackageOptions int flags)` 这个方法，可以获取到一个 theme app 的 `context` ， 该 `context` 和该 theme app 正常  `launched` 启动时的 context 是同样的。
	
获取到该 `themeContext` 后，我们便可以去拿到我们想要的资源了，代码如下：

```
	// 这是 Resources 里的一个方法，返回该资源对应的 ID
	public int getIdentifier(String name, String defType, String defPackage) {
		...
	}
	
	// 第一个参数 name : 我们想要的资源的名字, 即在theme app 中 该资源的ID
	// 第二个参数 defType:  想要资源的类型， drawable, dimen, color ...
	// 第三个参数 defPackage: 当 context 找不到时，默认去寻找的包名，可以为null
	
```

它的具体使用如下：

```
// themeContext 为上面我们获取到的 有关 theme apk 的 context
Resources themeResource = themeContext.getResources();
themeResource.getDrawable(themeResource.getIdentifier(drawableName, "drawable", packageName))));

// 即可得到该 drawable

```

##### 8.2.2 我对 theme 的前期调研部分 -- 从 theme apk 中取 Java 代码

在前期的调研中，我们的目标是 完全把整个锁屏界面给出去，全部由 theme apk 实现，这样的话，我们就可以更自由的，随心所欲的去实现不同的 样式，而我们只需要从 theme 中获取到 一整个 `View`， 然后把它塞进我们的主 app 里面的具体地方。

获取到 整个布局代码如下：

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

实际操作过，确实可以获取到该 `View`，并把它塞到我们想要显示的地方。

然后我们可以通过反射去调用 该 `View` 里面的方法，`invoke()` 实现：

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

**但是！！！当我们需要传递操作，或者在 theme 的 view 里面需要获取一个时机去反馈到主 app 内，该如何实现呢？？？**

一般我们的思路都是 回调！！！ 接口回调！！！ 但是 在上面 `PathClassLoader` 我们已经知道，加载 我们 主 app 的 `ClassLoader` 和 加载 theme apk 的 `ClassLoader` 不是同一个 `ClassLoader`，那么 同一个接口 即是包名，接口名完全相同，在这里的识别也是不同的类，那么怎么操作呢？

这里提供两种实际测试实现的方法： 

1. 参数为 `Handler`, 在 theme app 类 B 中通过 `Handler` 发送消息，在 main app 类 A 中去接受这些消息;

	是可行的，但也有一些问题，`Handler` 处理是异步的；

2. 在 在 theme app 类 B  中，在时机到了的地方，再通过反射去回调 main app 类A 中的方法；

	但是这种方法在实际代码中肯定是不太可行的，因为我们的主 app 一般都是混淆后才打包的，所以对于混淆后的代码，在通过反射是找不到的。

注：网上很多动态加载，动态打补丁的做法是如何实现的呢？

在这里我只有一个比较浅的认识，等待后续学习。其实在上面的分析中，我们可以发现要实现跨 apk 去利用同一个 `ClassLoader` 是 加载不同的类，利用 `PathClassLoader` 是完成不了的，需要用到的是 `DexClassLoader`.

##### 8.2.3 补充部分：`ClassLoader` 识别同一个类

1. 同一个 `class` =  相同的 `ClassName` + 相同的 `PackageName` + 相同的 `ClassLoader`

2. 由一个 `ClassLoader` 加载，并且该 `ClassLoader` 里没有和它一样的目录下的文件名，那么它就是唯一的

3. 当由同一个 `ClassLoader` 加载，并且是在相同的目录下，相同的文件名， 如果有两个时，加载时便会出错

#### 8.3 Theme 部分总结

做 theme 这部分时，其实整个人是极其兴奋的，当时还发了个朋友圈，说磨刀霍霍向猪羊，很好玩。做这部分时，从前期的调研，到后期的实现，经历了很多，经历过很多修改和调整。

1. leader 和 mentor  对我这部分做了很严苛的要求，对一些实现方式不好的地方都换了，所以，在这个过程中我学到了很多，学到了超多的东西；

2. 同时，因为这个涉及到加载大量图片，因此 leader 对性能做了很高的要求，力争少一点，再删少一点内存开销，让不需要这些图片自由时手动释放；

3. 为了后面一个小时便可以上线一款新的 theme apk, 我们做了很多设想，考虑了很多情况力争打造得足够强大和完善，这也是一个十分锻炼人的过程；

4. 也是通过这个 feature 我对很多东西的理解都加深了，例如 `ClassLoader`，`ContentProvider`。

### 9 总结

不知不觉，毕业已经快要一年了，真的是一个忙碌的一年。

上面是一个极长的记叙文……，还是不成熟的记叙文，能看完看到这里的都是真爱啊。。。

写着写着就发现这个过程被拉长了，被细节化了，还有很多知识点没有能够写进去，因为文章的长度已经被拉伸得很极限了。

下面可能是一部分的（假）议论文了……

从华科毕业，到如今2018年的6月，真的是一转眼的事情，生活方式变了，生活习惯也变了，爱好变成了工作，还好，我还有很多的兴趣，总觉着做着喜欢的工作，还有人给钱，是一件十分不错的事情。在北京见识了很多有趣的人，见识了很多未接触过的事物，在公司里面，有一个遗憾，就是没有很好的公司朋友，工作快要一年了，最熟悉的还是 mentor，我们两经常会一起吃饭，一起看代码，一起讨论问题，一起聊天，但其实在公司熟悉的人特别少，可能公司尽可能的减少纽带吧，同时我本身的性格就是不会主动会去结识人，不是因为社交能力不够，而是觉着没必要，我在这方面可能比较佛系，觉着有些重要的人，你迟早会认识，，有些人，即使你认识了，也……没什么用处啊，我不是一个和领导什么要搞好关系的人，我更觉着人与人相处是要自然的，所以我喜欢互联网，所以，我喜欢北京，在互联网公司，更受尊重的是你的技术能力和解决问题的能力，是一个相对公平的地方，是一个相对会肯定你能力的地方。其实我也渴望能有一些像在华科在联创的一些朋友，那些你一眼看去便觉着是开心，在我们组我们几个人经常会一块吃饭，有时觉着这样很赞，吃饭的时候偶尔说些其他的，技术上的，游戏上的，体育上的，一队人一起吃饭，总会让我想起在华科联创的日子，那些日子，很美好啊。

同时在北京生活，也是压力很大的生活，与学校不同的地方是你要自行去处理生活上的所有事情，衣食住行。记得刚来北京那会，我妈对我……可能也是真的放手吧，让我开始不依靠家里，不从父母那边拿钱去生活。拿着很少的钱来北京，果不其然，第一个月就艰难了，后来慢慢发了工资，最起码开始养活自己了。那种感觉，与其说开心，倒不如说释然。释然自己能够让人放心，让这个社会放心。

跌跌荡荡，起起伏伏，有很多事情会造成你成长路线上的偏折，那些事情，说不好是好的，还是坏的事情，影响着你，影响着你未来的走向，但其实更多的是自主的意志，你决定了要怎么走呢？我很开心，在我不太好的时候，能有一些朋友真心帮助我，还有我的 mentor ，在我不好的时候拉了我一把，在我迷茫的时候能够给我支持……到了这个年纪，再次提起迷茫这两个字，有种无法放下的感触，可能当我们前行不知所措时，当我们感到遇到瓶颈时，当我们感到周围的一切忽然陌生时，当我们感觉不到自己的位置时，迷茫，这种感觉总会随之而来。未来应该也会有迷茫再次来找我，当下次它要来的时候，我希望我能比这次感觉要浅一些，慢慢的稀释它。

总之，这一年过去了，我呢？成长的不够的话，下一年就补回来，成长足够的话，那就贼好，再接再厉！

和代码打交道，是一件简单幸福的事～

下个一年，有水水在身边～

下个一年，是否还会继续沿着我预想的方向发展呢～

下个一年，会碰到哪些好玩的事情呢～

下个一年，会碰到哪些困难的事情呢～

下个一年，会上升到一个什么高度呢～

下个一年，是否会更强更靠谱呢～

下个一年，我决定了，那就努力去做～

下个一年，我在期待和不安中等着它的到来～

下个一年，倒不如说，我想要主掌着它的走来～

下个一年，多多努力～～～

我很喜欢利文斯顿，他的转身后仰跳投简直美如画～

我一直努力奔跑，只为追上那个曾被寄予厚望的自己～











		
	


 