## WebView 遇到的问题

这篇文章主要记录一些WebView 出现的奇怪问题。

1. onPageStarted() 被调用多次问题：

	因为网址的加载过程中存在重定向问题，所以会被调用多次。 这个是不可避免的，重定向后都是一个新的网址，肯定会重新再调用`onPageStarted()`.
	
	如果在这个方法里面涉及到一些逻辑，尽可能在这个逻辑里面加入一些判断，保证想要执行的逻辑只执行一次，这也是有益于程序的。
	
	```
	@Override
   public void onPageStarted(WebView view, String url, Bitmap favicon) {
   		progressBar.setVisibility(VISIBLE);
   		progressBar.setProgress(0);
   		
   		if(！isLoading) {
   			//isLoading 默认为false
   			isLoading = true;
   			...
   			...
   			//执行只需要执行一次的逻辑
   		}
   		
   }
   
   
   // 然后在`onPageFinished()` 里
   
   isLoading = false;
	```

2. 在onPageStarted() 调用webView.canGoBack()  返回 false 问题

	有时在加载网址的时候，需要对当前webview的状态进行判断，例如，自定义了一些前进、后退按钮，便需要实时的刷新按钮，需要在网址发生变化时监听webView；
	
	在onPageStarted()里设置监听，去获取webView 的状态，但是有时会出现上述问题，明明webview是可以返回的，在onPageStarted()里获取的值为false。
	
	**解决方案：** 经过测试onProgressChanged()方法的调用情况，发现，在网络的加载进度在30%左右以后，canGoBack() 才返回 true；所以可以在onProgressChanged() 里加入监听，或是在onPageFinished()里去监听 webView的状态。
	
3. 在onPageFinished() 里去设置progressBar 不可见，仍然看到进度条显示出来：

	问题出现的原因，有些不清楚为什么，但是在设置的时候更改为以下便可以：
	
	```
	progressBar.setVisibility(View.GONE);
	```
	
	所以在自行设置的时候，可以设置progressBar 为GONE 而不是INVISIBLE;
	
4. 在一些低版本上，会出现webview 销毁时 程序发生crash的情况：

	此类情况的出现，往往是webview销毁时出现了问题，可以仔细检查下自己代码的编写；
	
	webView销毁代码示例：
	
	```
	if (webView != null) {
       // 要首先移除
       removeView(webView);
            
       // 清理缓存
       webView.stopLoading();
       webView.onPause();
       webView.clearHistory();
       webView.clearCache(true);
       webView.clearFormData();
       webView.clearSslPreferences();
       WebStorage.getInstance().deleteAllData();
       webView.destroyDrawingCache();
       webView.removeAllViews();
            
       // 最后再去webView.destroy();
       webView.destroy();
   }
	```	

	若是  webView.destroy() 放在了前面，则就会出现错误，需要把 webView.destroy()放在最后面。


5. 	上面的销毁 webview时，在低版本4.4以下，这样会出现错误，包 空指针，

	具体的原因还没有查明，有说是系统bug的，尚且原因不清楚，可做两种处理，第一种：
	
	```
	if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
		//4.4 以下
		webView = null;
	} else {
		webView.destroy();
	}
	```
	
	第二种：
	
	```
	webView.destroy();
	webView = null;
	
	```
	第二种这样不需要判断一些版本，是否效果会更好些？
	
6. webview 快速销毁后， 可能会出现在onPageFinished() 等地方发生crash：

	出现的情况可能是加载了很多网页，然后快速点击back键， 退出程序，这时，有可能在部分地方webview 的回调仍在进行，但是webView 可能已经是空了的， 就是发生空指针的crash， 解决方法：在webView的回调函数中 如果要对webview进行操作，先进行一次判空处理：
	
	```
	onPageFinished() {
		//
		...
		if(webView != null) {
			...
			//执行要做的有关webview操作的逻辑
			boolean canGoForward = webView.canGoForward();
		}
	}
	```
	
7. webView 加载视频时出现的问题： 全屏问题

	要想实现webview 全屏加载视频，需要在`webView.setWebChromeClient()`里重写`onShowCustomView()`和`onHideCustomView()`，这两个方法。
	
	可能出现的问题：在切换过程中，背景有白色的底色或是黑色的其他背景闪动一下；
	
	原因： 可能是有根布局的背景色引起的，可自行检查。
	
	
8. webView 加载视频时出现的问题： 按back键 视频仍在播放：

	应该是返回到:

	```
	if(webView.canGoBack()) {
		webView.goBack();
	}  else {
		...
	}
	
	```
	
	webview可以canGoBack()， 成功goback（）后 但是视频仍在播放
	
	webview不知道对视频的处理调用了那些接口，解决方法：
	
	在 webView.goBack() 前调用 webView.reload(); webView.stopLoading()
	
	```
   webView.reload();
   webView.stopLoading();
   webView.goBack();
	```
	 这样可以让视频暂停。
	 
	 **注：**很多地方都说应该调用webView 的onPause()和onResume() 方法去暂停h和控制播放视频，这种方式下面介绍。
	 
	 
9. 	 webView 加载视频时出现的问题： 按home键和多任务键 视频仍在播放：

	此时根据用户习惯应该暂停视频，但是webview本身没有处理这些事件，需要咱们自己去处理。
	
	首先去通过一个广播监听用户点击home键和多任务键， 在监听中，点击后调用webView.onPause();
	
	在用户重新进入到程序中时，在onresume() 中调用webView.onResume();
	
	**注：**有些地方是利用反射的机制实现的：
	
	```
	webView.getClass().getMethod("onPause").invoke(webView, (Object[]) null);
	```
	
	其实这是为了兼容低版本3.0以下，所以现在基本上不需要再去兼容3.0，不需要利用反射的模式去写。
	 
	
10. 在加载视频的过程中，点击某个视频，会出现webview不去触发任何方法的情况：

	在某些视频网站，点击某个具体的视频，不会去调用webView 的onPageStarted(), onPageFinished();
	
	原因还不清楚， 有待追究；
	
	这个问题可能会导致某些需求去刷新当前webview是否可前进和后退，可能导致后退的情况得不到及时的反馈（例如在onPageStarted()里去刷新状态，但是点击视频没有调用这个方法）。
	
	
11. 关于webview 图片加载的问题：

	在一般的设置里，都会设置图片的加载模式为：
	
	```
	//webSetting设置
	if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
		webSettings.setLoadsImagesAutomatically(false);
	} else {
		webSettings.setLoadsImagesAutomatically(true);
	}
	```
	
	然后在onPageFinished()里设置：
	
	```
	if(!webView.getSettings().getLoadsImagesAutomatically()) {
		webView.getSetting.setLoadsImagesAutomatically(true);
	}
	```
	如果不设置的话，会出现在android 4.4以下 图片加载不出来的问题，因为手动设置为false了。
	
	上面的代码，其目的是对API 在19 以上的版本做了兼容，因为4.4以上系统在onPageFinished时再恢复图片加载时,如果存在多张图片引用的是相同的src时，会只有一个image标签得到加载，这样会造成界面不对的情况，所以4.4以上要在加载网页的时候就去主动加载图片，4.4以下可以等到onPageFinished()后再去加载图片（这样会首先去加载网页的界面，省去用户等待的时间）
	


6. To be continued


 
