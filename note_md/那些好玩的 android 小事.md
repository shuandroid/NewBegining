## 那些好玩的 android 小事

本文记录的是一些在开发时遇到的好玩的东西，一些容易出错的地方，一些迷惑的地方， 虽然记录的东西很简单，但是又特别的细节。

1. **View 的 setOnclickListener(...) 与 setClickable**

	view.setClickable(false);  
	view.setOnclickListener(...);
	
	则 该 view 仍然为可点击状态，因为
	在`setOnClickListener()`里面会把 view 设置为 clickable , 可点击状态：
	
	```
	// view 的 源码  
	public void setOnClickListener(@Nullable OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        getListenerInfo().mOnClickListener = l;
    }
	```
	
	会首先检查该 view 是否可点击，如果不可点击，则会把它设置为可点击状态。
	
2. **ImageView.setAlpha(int) 与 ImageView.setAlpha(float) 的区别**

	这是一个文档里，很容易踩进去的坑。
		
	setAlpha(int) 是对 image 进行 alpha 进行变化， 范围是 0 ~ 255;
		
	setAlpha(float)  是对 view  进行 alpha 进行变化，范围是 0f ~ 1f;
		
	两个方法 做 透明度变化的对象不同，要千万注意，注意！！！， 如果混用，就会出现问题的。
		
	例如，如果是想利用 float 进行设置，刚开始设置了 setAlpha(0), 后面都是 setAlpha(float), 则会出现 这个 view 永远展示不出来的情况, 刚开始设置 setAlpha(0f) 则是正确的。
		
	注意：官方已经抛弃了  setAlpha(float) 这个方法，推荐使用 setImageAlpha(int) （API 16 以上才会生效） 这个方法. 
	
3. **在 自定义 view 中， paint 的 setColor() 与 setAlpha() 的关系**

	paint.setColor(#12ffffff)； 
	如果设置的颜色里面包含了 透明度， 则 该画笔 的透明度 不一定就是 `12`;
	
	```
	/**
	* ... ts alpha can be any value, regardless of the values of r,g,b
	*/
	public void setColor(@ColorInt int color) {
        nSetColor(mNativePaint, color);
    }
	``` 
	它的 alpha 可能会变化，因为受 setAlpha 的影响
	
	```
	 /**
     * Helper to setColor(), that only assigns the color's alpha value,
     * leaving its r,g,b values unchanged. Results are undefined if the alpha
     * value is outside of the range [0..255]
     *
     * @param a set the alpha component [0..255] of the paint's color.
     */
    
    public void setAlpha(int a) {
        nSetAlpha(mNativePaint, a);
    }
    
	```

	setAlpha() 会覆盖 setColor 中的 透明度， 所以 当你做自定义 view 的变换时，如果同时设置了 setColor 和 setAlpha 则 透明度会有后者决定。


4. **图片的时间戳问题**

	在 媒体库里面 `MediaStore.Images` 里面， `ImageColumns.DATE_TAKEN`, 这项属性，描述的是该图片的时间戳。但是当你修改该图片后，该时间 `DATE_TAKEN`, 会怎么变化呢？假设原图片（A）的时间为 `2018.01.02 13:00`, 那么修改改图片后，一般会保留原图片，生成一个新的图片（B），那么这个新的图片的时间为多少呢？首先猜一猜，要么和原图一样，要么是现在修改的时间。 
	
	但是！！！事实上，当我去打印这个时间的时候，竟然发现，修改后的 B 的时间戳，竟然小于 原图片 A 的时间！！！，也就是说，这个时间 会早于 `2018.01.02 13:00` 可能是 `2018.01.02 12:59`.
	
	这个不是特别会影响功能的地方，我也是偶然发现的，在这里记录一下，也说不定是错的，我只测试了我手上的几款机型，可能根据不同的机型会有所不同吧。


5. **padding 与 margin 对同一个控件的影响**

	例如：父控件为LinearLayout, 子控件为button，下面两种设置的方式效果是一样的：

		1. 当父控件设置了`padding="8dp"`
			padding 是内边框， 使得该父控件里的子view的空间都会减少8dp
		2. 子控件设置了`layout_margin="8dp"`
			margin 是相对button而言的，使得自身距离父控件各个方向有8dp的距离；
	
	上面两种，实际button的点击区域，view的绘制区域都是相同的，但是padding后使得linearlayout里content会缩小8dp的距离，而margin则不会影响content的区域大小，从而造成一些特定情况下的问题。在margin下，button可以显示设置的阴影，而padding则没有足够的content去显示阴影。
	
	说的有点不清楚，可能需要特定的情况下，才可以发现区别，但是， padding 会导致里面 的 子 view  button 的大小不能超过 padding 的限制， 是父 view 对 子view 的限制，是被动的； 而 margin 是一个主动的行为，是子 view 对 自身的一个限制；


6. **数据库的操作条数限制 ： 1000 条**

	```
	Crash : SQLiteException: Expression tree is too large (maximum depth 1000)
	```
	
	原因： 在 sqlite 语句中 的 筛选条件里面 包含了太多的内容项，超过了1000 个，就会 crash;
	
	通常会出现在 删除记录时，例如， 筛选条件 `whereCause.append(ID + "=" + list.get(i).id)`, 然后循环添加 要删除的对象，如果 list 内容太多，就会造成 whereCause 十分的庞大，当超过 1000 深度时，便会 发生 这个 crash .
	
	解决方案：
	
	1. 手动对 whereCause.append 进行限制，如果超过 1000 , 则执行 sqlite 删除语句，然后接着 另外一个 whereCause 去 添加条件, 再次去执行 删除语句。

	2. 如果条件可以替换为 IN 处理 

		```
		whereCause = "Table.column._ID IN (ids[1], ids[2], ids[3], ...);
			
		whereCause = "Table.column._ID = ids[1] OR Table.column._ID = ids[2] OR Table.column._ID = ids[3] ...";
			
		上面两者是等价的， 但是 第一种不会造成 上述 exception， in 只代表一条语句，但是 or 却有多个 or 出现
		```
	
	代码二三事.md
    Floating Window.md
7. To be continued ...



上面是个人在开发过程中遇到的一些比较好玩的事情，有时候敲代码蛮枯燥，发现的一些小的惊奇的点，会让自己很开心，就像在沙滩上拾贝一样，偶尔发现几个特别奇特的贝壳， 会特别开心，这也算代码的魅力吧。 会持续更新， 发现一些好的点都会补充上去~


大家有什么感觉好的点，也可以指出来，如有哪里不对的地方， 水平有限，也请指出来，谢谢~~~







