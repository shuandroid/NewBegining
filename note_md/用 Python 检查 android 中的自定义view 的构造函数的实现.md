WebView  的反思和记录 （处理很多特殊的情况）.md
WebView 遇到的问题.md## 用 Python 检查 android 中的自定义view 的构造函数的实现

一般来说，自定义的 view 一共有四个构造函数， 例如：

```
    public LinearLayout(Context context) {
        this(context, null);
    }

    public LinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public LinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
     	...
     	    
    }
```

四个构造函数的应用场景：

1. 在 java 代码中 直接 new 一个 view 对象， 会调用第一个构造函数， 即 View(Context)

	
2. 在 xml 中 inflate view 时，会调用 两个参数的构造函数，即 View(Context， AttributeSet)

3. 	当在 xml 中用到了 一些 在自定义 view 里面声明的 属性，类似 app:lineColor 之类的， 这里调用的还是 View(Context， AttributeSet)， 不过我们要再 view 的Java代码中 调用 三个参数的构造函数，例如在 View(Context， AttributeSet) 中调用

4. 四个参数 通常被主动调用，传过来 style 去实现

	这里 ， 网上看到 资源运用的顺序是  xml 定义 > style > theme


#### 为什么需要去检查自定义构造函数的实现呢？

在某些低版本手机上面，发现，如果不复写 一个参数的构造函数和三个参数的构造函数会发生 crash，为了减少 crash，在打包前，通过 一些工具去检查出这些可能会出现问题的点，防患于未然。

#### 如何实现呢？

我对 python 的使用不多，基本算是门外汉。

##### 实现的基本思路：

大概分为几步：

1. 首先要能找到要查询的 Java 代码的目录

	通常是一个路径，通过 path, 逐层遍历到下面的每一个 Java 文件
	
2. 要能够 递归的遍历 目录下的 Java 文件

3. 具体到每一个 Java 文件，分辨出 是否为自定义 view

4. 检查到是 自定义 view 后，需要能够查询到它的代码里面 构造函数的个数（或构造函数的具体实现）

	通过何种方式去检测 是否是构造函数，及里面具体的实现呢？这是一个不太好实现的点， 可能各有自己的想法吧， 如果有好的检查方式 可以说明一下。
	
利用 Python 写一个脚本，在 build.gradle 里 当是 release 时 执行该脚本，可以通过这种方式在打包上线前确保一些意外不发生。这也是我理解的工具的作用。

#### 具体实现

1. 得到要遍历的目录：

	获取到当前 脚本文件 check.py 的路径：
	
	```
	def get_head_path():
	
		temp_head_path = os.path.realpath(__file__)
		return temp_head_path.split("libs")[0]
		
	```
	
	`__file__` 是内部变量, temp_head_path 是 当前 check.py 的绝对路径，是个 string , 对 string 进行处理，获取到 对应的 Java 目录前面的部分， 在通过：
	
	```
	head_path = get_head_path()
	all_path = head_path + "app/src/main/java"
	```
	得到完整的路径。
	
2. 递归遍历 目录下的所有：

	
	```
	def recursive_file(path):
    # print "start travelFile"

    fs = os.listdir(path)

    for file in fs:
        temp_path = os.path.join(path, file)
        if not os.path.isdir(temp_path):
        
        	   // 是 具体的Java 代码， 做其他的步骤
            if check_is_custom_view(temp_path):
            
            		//  是 自定义 view， 检查 它 的构造函数
                print_custom_view_constructor(temp_path)

        else:
            recursive_file(temp_path)
	```

	首先取出该目录下的所有 file， 看它是否为 文件夹； 是的话，接着递归 `recursive_file(temp_path)`，；当不是文件夹，还是文件时，检查是否为自定义 view `if check_is_custom_view(temp_path):`； 当不是自定义 view 时 ，不需要处理，当是自定义 view 时 检查它 的构造函数是否满足要求 `print_custom_view_constructor(temp_path)`.
	
	
3. 分辨是否为 自定义 view :

	这一部分是比较关键的一部分，怎么区分是自定义 view 呢？ 一个android 项目中，那么多文件，如何判断呢？下面是我的想法，可能不是最好的，也只是一个大概率能分别它为自定义 view 的方式。
	
	我们读取到一个 目录下的文件时，通常是 `file.read()` 可以得到这个 文件里面所有的内容，以 string 的形式保存，在 file_content 变量中，我们要去 对这个字符串进行处理， `file_content .split("{")[0]`， 拿到 这个字符串第一个 `{` 前的字符， 保存在 target_content 里面，如果 target_content 包含 `extends xxxView` 等， 我们差不多就可以说 这个是一个自定义 view. 
	
	没有想到特别好的方法， 匹配时 如何匹配到众多 的 view类型？ 第一想法就是利用 正则表达式。
	
	```
	target_string = re.compile(
    r'extends(\s+)(PercentRelativeLayout|RelativeLayout|LinearLayout|FrameLayout|Button|ImageView'
    r'|AppCompatImageView|AppCompatTextView|AppCompatEditText|AppCompatButton|ProgressBar|ScrollView|View'
    r'|ListView|RecyclerView)(\s+)')
	```
	
	`(s+)`: 表示至少一个 空格(多个空格)
	
	`RelativeLayout|LinearLayout`： 表示两者之一满足，都算是匹配上。
	
	正则表达式比较强大，同时也比较深，没有想到特别合适的一个方法去匹配所有的 继承与 view 的表达式，最后只好利用 这样 有限穷举的方式
	
	
	
	
	
	
	
	
	
