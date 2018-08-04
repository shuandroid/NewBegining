## WebView  的反思和记录 ---定制设置和常见问题

**一些基本的内容就不提及了，下面主要记录在开发中尤其需要注意的内容**

### webview 自带接口的基本使用

要完成一定的自定义功能的webview，肯定就需要涉及到以下几个内容，**`WebSetting`**, **`WebViewClient`**, **`WebChromeClient`**,  它们可以让我们去定制一些内容.

####  WebSetting

初始化

```
    private void initWebSetting() {
        WebSetting webSettings = webView.getSettings();

        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

		 // 启用应用缓存
        webSettings.setAppCacheEnabled(true);
        //缓存路径
        webSettings.setAppCachePath(getContext().getCacheDir().getAbsolutePath());
        // 比较重要，一共有四种模式，在下面说明
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        //开启数据库缓存和 DOM 缓存
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
        webSettings.setGeolocationDatabasePath(getContext().getFilesDir().toString());
        
        //支持缩放, 
        webSettings.setSupportZoom(true);
        //显示缩放按钮
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        // 是否主动加载图片。其实是对4.4 已上做兼容，使图片加载占用的内存更少
        if (android.os.Build.VERSION.SDK_INT < 19) {
            webSettings.setLoadsImagesAutomatically(false);
        } else {
            webSettings.setLoadsImagesAutomatically(true);
        }

        webSettings.setBlockNetworkImage(false);
        
        // 支持运行 JS
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setSaveFormData(false);
        webSettings.setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm.NORMAL);
    }
```

其中比较重要的有一下：

* **`setCacheMode()`**

	它有四种缓存模式：
	
	1. **`LOAD_DEFAULT`**:

		默认的缓存模式，在页面进行前进或后退时，如果有缓存可用并未过期， 就会优先加载缓存，否则，从网络上获取数据。
		
	2. **`LOAD_CACHE_ELSE_NETWORK`**:
		
		只要有缓存便会使用，哪怕它已经过期，如果缓存不可用，会从网络上获取数据。
		
		> 注：缓存的失效： 可能数据发生变化，还有可能是缓存的时间到期了，在浏览器中的`header`中的`expires`存储着数据的过期时间;

	3. **`LOAD_NO_CACHE`**: 不加载缓存，只从网络获取数据。
	4. **`LOAD_CACHE_ONLY`**: 只有缓存加载获取数据

	当然有时也可根据网络情况去设置：
	
	```
	    ConnectivityManager connectivityManager = (ConnectivityManager) context().
            getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo.isAvailable()) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        }
	```
	

* **`setBlockNetworkImage(boolean flag)`**

	是否禁止从网络上加载图片， false 表示可以从网络上加载图片；
	
	> 注： 如果设置是从禁止到允许转变的话，图片数据并不会在设置改变后立即去获取， 而是在reload 时才会生效； 默认 `flag = false`.
	
	
#### WebViewClient

帮助处理webView的各种通知，事件；

设置如下：

```
webView.setWebViewClient(new WebViewClient() {
     @Override
     public void onPageStarted(WebView view, String url, Bitmap favicon) {
           Log.e(TAG, "onPageStarted url = " + url);
     }

     @Override
     public void onPageFinished(WebView view, String url) {
           Log.e(TAG, "onPageFinished");
           //恢复图片的主动加载，若不设置，会出现在4.4以下图片加载不出来的情况
           if(!webView.getSettings().getLoadImagesAutomatically()) {
           	webView.getSettings().setLoadImagesAutomatically(true);
           }
     }

     @Override
     public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
           return false;
     }

     @Override
     public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
           super.onReceivedHttpError(view, request, errorResponse);
           Log.e(TAG, "onReceivedHttpError");
     }

     @Override
     public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
           super.onReceivedError(view, request, error);
           Log.e(TAG, "onReceivedError");         
     }
});
```

**onPageStarted()**

* 会在网络加载时去调用，在这里可以做一些逻辑上的处理，例如，进度条的展示，以及进度数的设置：

```
progressBar.setVisibility(VISIBLE);
progressBar.setProgress(0);
```

 很多时候，这个方法都会被调用**不只一次**，因为网址存在着重定向问题，所以会存在`onpageStarted()`不只被调用一次的情况，里面的逻辑处理也会被调用多次，编写时要注意争取保证里面的逻辑虽被调多次，但最好是只执行一次；加入一些防范机制。

* 同时也可以在`onpageStarted()`方法里面进行一些判断，例如，当前webview是否可以回退、前进：

```
boolean canGoBack = webView.canGoBack();
boolean canGoForward = webView.canGoForward();
```

但是经实际的测试，发现有时在点击链接后，其实webview是可以返回的，但是`canGoBack`是`false`, 后来经过不断测试，发现了有些网址的加载在进度为30% 左右以后时，webView.canGoBack(), 才会返回true，。

> 注： 具体可以在WebChromeClient 里的 `onProgressChanged()` 方法里测试。

**onPageFinished()**

会在网站加载结束后调用，在里面同样可以处理一些逻辑， 例如进度条逻辑 ：

```
progressBar.setVisibility(GONE);

```

这里要注意，一定要把visibility 设置为 `GONE`, 若是设置为`INVISIBILITY`,则可能仍然会出现进度条加载到100% 后不消失的情况，要把其设置为`GONE`.

-------
注意： 在一些低版本的某些手机上面，这个方法也会被调用多次，


**shouldOverrideUrlLoading()**

返回false为最好，慎重返回为true；

> 网络上一大堆说 ，返回true后才会使得网络的链接跳转由webview 处理, 返回false会调用系统浏览器去处理, 这种说法是错误的。


在官网上的说法是这样的：

1. 如果没有提供 WebViewClient 对象， 则WebView 会请求 AM 选择系统的浏览器去加载；
2. 提供了 WebViewClient 对象, 且shouldOverrideUrlLoading() 返回true , 则android 系统处理 URL；
3. 提供了 WebViewClient 对象, 且shouldOverrideUrlLoading() 返回 false, 则当前 webview 处理URL；

并且，这个方法默认是返回 false, 因此我们不需要去重写这个方法， 只需要：

```
webView.setWebViewClient(new WebViewClient()...); 
```

便可实现利用webview去加载链接。


*其他两个方法看意思便可知道如何使用。*


#### WebChromeClient

`WebChromeClient`是辅助webView 处理javaScript 的对话框，网站图标， 网站title， 加载进度等 事件；

设置如下：

```
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //网站标题的处理
                webTitle = title.split(" ")[0];
            }
            
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
            		super.onReceivedIcon(view, icon);
            		
            }
        });
```

**onProgressChanged()**

处理进度， 并且更新progressBar的进度；在这里设置去获取 webView.canGoBack(); 会发现在刚开始时，返回值为false, 当进度达到30左右时才开始返回true；


### 不同时机 回调方法执行的先后顺序

**页面加载按照下列顺序执行：**

```
shouldOverrideUrlLoading();
onProgressChanged(10);
shouldInterceptRequest();
onProgressChanged(...);
onPageStarted();
onProgressChanged(...);
onLoadResource();
onProgressChanged(...);
onReceivedTitle()/ onPageCommitVisible()
onPageFinished(100);
onReceivedIcon();
```

> 注：上述 只是理论上的执行顺序，实际的顺序，不一定是这个样子的

**资源加载回调：**

```
shouldInterceptRequest() -> onLoadResource();
```
 
**发生重定向时回调**

```
onPageStarted() --> shouldOverrideUrlLoading()
```

**直接loadUrl() 的回调**

```
// 无重定向
onPageStarted() -> onPageFinished();

//有重定向时，shouldOverideUrlLoading 返回 true ， onPageFinished 仍会执行
onPageStarted() ->  .... -> onPageFinished()
```

**用户点击链接的回调**

```
onPageStarted()  --> onPageFinished()
```
**后退、前进、刷新 时的回调**

```
onPageStarted()  --> onPageFinished()
```



### WebView 和ProgressBar 连用

很多时候，都需要一边展示加载，一边显示加载的进度，这时，我们可以把WebView 和ProgressBar 放在一个`LinearLayout`里面，这样更加方便去操作这两者。

例如：

```
public class ProgressWebView extends LinearLayout {
	//自定义ProgressWebView 继承与LinearLayout 
	//在里面实现对这两个view的绘制和加载；
	public ProgressWebView(Context context, @Nullable AttributeSet attrs) {
		//通过addView() 分别把二者加进去
		...
		...
		
		init();
		initWebSettings();
		initWebListener();
	}
	
	//初始化一些参数
	private void init() {
		...
	}
	
	//初始化webSettings 
	private void initWebSettings() {
		...
	}
	
	//初始化webListener
	private void initWebListener() {
		...
		webView.setWebViewClient(...);
		webView.setWebChromeClient(...);
	}
}
```

注意：加载完成时，要将 progressBar 设置为 setVisibility(View.GONE); 


### webView 内存泄漏问题 以及 销毁 

1. 当webView加载大量的网络界面时，可能会产生大量的内存泄漏， 

	- 展示webview的activity可以另开一个进程，

		在`Androidmanifest.xml` 的activity标签里添加:
		
		```
		Android:process=": webBrowsing"
		
		```
		
		当结束这个进程时， 手动调用System.exit(0);

	- 可以在webView destroy 中调用下面的方法，在很大程度上可避免内存泄漏：

		```
		public void releaseAllWebViewCallback() {
         if (android.os.Build.VERSION.SDK_INT < 16) {
             try {
                 Field field = WebView.class.getDeclaredField("mWebViewCore");
                 field = field.getType().getDeclaredField("mBrowserFrame");
                 field = field.getType().getDeclaredField("sConfigCallback");
                 field.setAccessible(true);
                 field.set(null, null);
             } catch (NoSuchFieldException e) {
                 if (BuildConfig.DEBUG) {
                     e.printStackTrace();
                 }
             } catch (IllegalAccessException e) {
                 if (BuildConfig.DEBUG) {
                     e.printStackTrace();
                 }
             }
         } else {
             try {
                 Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
                 if (sConfigCallback != null) {
                     sConfigCallback.setAccessible(true);
                     sConfigCallback.set(null, null);
                 }
             } catch (NoSuchFieldException e) {
                 if (BuildConfig.DEBUG) {
                     e.printStackTrace();
                 }
             } catch (ClassNotFoundException e) {
                 if (BuildConfig.DEBUG) {
                     e.printStackTrace();
                 }
             } catch (IllegalAccessException e) {
                 if (BuildConfig.DEBUG) {
                     e.printStackTrace();
                 }
             }
         }
     }
		```
		
2. WebView 的销毁：

	一定要进行 WebView 的销毁!!!
	
	在activity 的onDestroy 中去调用：ProgressWebView  的onDestroy();
	
	在 ProgressWebView 中：
	
	```
	 public void onDestroy() {
        Log.e(TAG, "WebView onDestroy");

        if (webView != null) {
        	  // 要首先移除webview
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

		  // 清理cookie 
        CookieSyncManager.createInstance(HSApplication.getContext());
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().removeSessionCookie();

    }
	```
	步骤：
	1. 首先要removeView;
	2. 清理缓存；
	3. webView.removeAllViews();
	4. 最后 webView.destroy();
	
	
3. 	webSetting.setBuiltInZoomControls(true) 引发的crash；

	这个方法调用以后 如果触摸屏幕 弹出的提示框还没消失的时候 如果activity结束了 就会报错了。3.0以上 4.4以下很多手机会出现这种情况。
	
	**解决：**  
	
	在activity 的onDestroy() 中把 webview设置为 setVisibility(View.GONE);
		
		
4. 加载视频的问题：

	要想实现webview 全屏加载视频，需要在`webView.setWebChromeClient()`里重写`onShowCustomView()`和`onHideCustomView()`，这两个方法：
	
	```
	//onShowCustomView()方法里面的实现：
	//customView 为 一个新的全屏的view
	// 全屏
activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	
	//
	if(customView != null) {
		callBack.onCustomViewHidden();
		return;
	}
	
	customView = view;
   customViewCallback = callback;
   videoShowLayout.addView(customView);
   videoShowLayout.setVisibility(View.VISIBLE);
   //videoShowLayout 是视频全屏的加载的父布局
   webDetailLayout.setVisibility(View.INVISIBLE);
	
	```
	
	`onHideCustomView()`的实现如下:
	
	```
	if (customView == null) {
   		return;
   }
   //界面转回垂直
   activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
   webDetailLayout.setVisibility(View.VISIBLE);

   customView.setVisibility(View.GONE);
   videoShowLayout.removeView(customView);
   customView = null;
   videoShowLayout.setVisibility(View.GONE);
   customViewCallback.onCustomViewHidden();
   
	```


	**注意：** 注意根布局的背景，因为在全屏的切换中可能会出现一些白色、黑色的底色，一般是由根布局的背景色引起的。



> 注： 以上是初稿，可能会有错误，如发现错误，请不吝留言，谢谢；
> 
> 后面，会继续更新有关 WebView 的内容


### 一些不寻常的接口回调

1. webSettings.setJavaScriptEnabled()

	```
	webSettings.setJavaScriptEnabled(true);
	```
	
	这句代码在 `android 4.3` 上会调用一下 `reload()`, 同时会多次回调 `webChromeClient.onJsPrompt()`



参考链接：

1. [很详细的一篇介绍](https://jiandanxinli.github.io/2016-08-31.html)

2. [WebView 遇到的问题](http://blog.csdn.net/u012124438/article/details/53401663)

