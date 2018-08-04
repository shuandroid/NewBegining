## android Test 动画小测试
 对于一些自定义的动画实现， 实现 扫雷似的 扇形扫描效果。
 
 单独的 通过 view 的 ValueAnimator 和 ObjectAnimator 是有些难实现的，就往 自定义 view 通过 canvas 去 draw 实现。
 
 ### 分析动画：
  这个动画的基本实现是， 刚开始 从正中间（12点） 开始扫描， 并且 扫描的扇形角度逐渐变大，然后扇形的角度 逐渐缩写为0， 然后 扫描的角度逐渐增大； 在整个过程中 开始扫描的角度 startAngle 一直在变大, sweepAngle 扫描过得角度（即 扇形角度） 先变大，后变小。
 
 
 * 动画的难点在于 控制 startAngle 和 sweepAngle 的变化幅度，两者如何结合起来，实现一个比较好的动画形式？

 
 * 主要的函数 是利用 canvas.drawArc(...);

 	canvas.drawArc(arcRect, sectorScanStartAngle, sectorScanSweepAngle, true, sectorScanPaint) 说明： 
 	
 	arcRect  是绘画的矩形空间， 在该 绘画中，为 该 view 的 区域大小;
 	
 	sectorScanStartAngle： 扇形弧度开始绘画的角度
 	
 	sectorScanSweepAngle： 扇形弧度扫过的角度
 	
 	true : 这里的参数为 userCenter, 当为 true 时，会从 该 arcRect 的中心点开始为一个散发 的 扇形弧度，如果未 false，则只会绘画出一个 圆圈上的弧度（圆形进度条的实现）;
 	
 	sectorScanPaint  绘画 该弧形时的 画笔，可定义一些 颜色等参数
 
 
```
 // 圆弧扫描类
 public class ArcScanView extends View {
 
 	// 开始扫描的角度， 这里是因为 12点方向 在 canvas 画布里面是 -90度
 	private static final float START_ANGLE = -90f;
 
 	// 扇形的绘画区域
 	private RectF arcRect = new RectF();

	// 扇形的画笔
   private Paint sectorScanPaint;
   
   // 弧形 开始的角度 和 扫过的角度
   private float sectorScanStartAngle, sectorScanSweepAngle;
 	
 	private PaintFlagsDrawFilter paintFlagsDrawFilter;
 
    public ArcScanView(Context context) {
        super(context);
        init();
    }

    public ArcScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcScanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
 
 	private void init() {
 		...
 		
 	}
 
 	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        arcRect = new RectF(0, 0, w - offset, h - offset);
    }
 
 	@Override
    protected void onDraw(Canvas canvas) {
    	...
    }
 
 }
```
 
 上述代码，实现构造函数，并且添加基本的常量.
 
 PaintFlagsDrawFilter 是 canvas 在绘制中 抗锯齿使用。
 
 canvas.setDrawFilter(paintFlagsDrawFilter); 目的是为了 抗锯齿
 
 在 初始化 函数 init() 中应该做些什么呢？
 
 ```
 sectorScanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 sectorScanPaint.setStyle(Paint.Style.FILL);
 sectorScanPaint.setColor(JunkUtil.PRIMARY_WHITE);
 sectorScanPaint.setAlpha(JunkUtil.TRANSPARENT_NORMAL);
 sectorScanPaint.setAntiAlias(true);

 paintFlagsDrawFilter = new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
 ```
 

onDraw() 里面的写法：

```
@Override
protected void onDraw(Canvas canvas) {
   
   canvas.setDrawFilter(paintFlagsDrawFilter);
   
   canvas.drawArc(arcRect, sectorScanStartAngle, sectorScanSweepAngle, true, sectorScanPaint);
}
```


至此为止，前提工作已经完成的差不多了，下面就是需要动态的变化 sectorScanStartAngle 和 sectorScanSweepAngle 的大小， 刷新 onDraw()

1. 首先让 sectorScanStartAngle 从 -90 到 270 之间变化, 动画时间为 1000 ms

	插值器变化， 自定义的数值变化 PathInterpolatorCompat.create(...) 是指值的变化曲线；
	
2. sectorSweepAngleIncreaseAnimator 是为了先逐渐把 sectorScanSweepAngle 变大，动画时间 差不多为 sectorStartAngleAnimator 的 一半

3. sectorSweepAngleDecreaseAnimator 是为了把 sectorScanSweepAngle 变小，动画时间同样为 sectorStartAngleAnimator 的 一半

4. 看情况，是否需要重新 开启新的一轮动画

```
public void startSectorScanAnim() {
	
	ValueAnimator sectorStartAngleAnimator = ValueAnimator.ofFloat(0, 1);
    sectorStartAngleAnimator.setDuration(1000);
    sectorStartAngleAnimator.setInterpolator(PathInterpolatorCompat.create(0.33f, 0.0f, 0.67f, 1.00f));
    sectorStartAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
           float percent = (float) valueAnimator.getAnimatedValue();
           sectorScanStartAngle = 360 * percent + START_ANGLE;
           invalidate();
        }
    });
    
    ValueAnimator sectorSweepAngleIncreaseAnimator = ValueAnimator.ofFloat(0, 0.35f);
    sectorSweepAngleIncreaseAnimator.setDuration((long) (1000 * 5 / 11f));
	sectorSweepAngleIncreaseAnimator.setInterpolator(PathInterpolatorCompat.create(0.33f, 0.0f, 0.67f, 1.00f));
	sectorSweepAngleIncreaseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    	@Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float value = (float) valueAnimator.getAnimatedValue();
            sectorScanSweepAngle = 360 * value;
        }
    }); 
   
    
    ValueAnimator sectorSweepAngleDecreaseAnimator = ValueAnimator.ofFloat(0.35f, 0f);
    sectorSweepAngleDecreaseAnimator.setStartDelay((long) (1000 * 5 / 11f));
    sectorSweepAngleDecreaseAnimator.setDuration((long) (1000 * 6 / 11f));    
    sectorSweepAngleDecreaseAnimator.setInterpolator(PathInterpolatorCompat.create(0.33f, 0.0f, 0.67f, 1.00f));
    sectorSweepAngleDecreaseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
             float value = (float) valueAnimator.getAnimatedValue();
             sectorScanSweepAngle = 360 * value;
        }
    });
   
  	sectorScanAnimatorSet = new AnimatorSet();
	sectorScanAnimatorSet.play(sectorStartAngleAnimator).with(sectorSweepAngleIncreaseAnimator)
                .with(sectorSweepAngleDecreaseAnimator);
	sectorScanAnimatorSet.addListener(new AnimatorListenerAdapter() {   
   
   		 
   		@Override
        public void onAnimationEnd(Animator animation) {
             if (isScanFinish) {
                sectorScanStartAngle = START_ANGLE;
                sectorScanSweepAngle = 0f;
                sectorScanAnimatorSet.removeAllListeners();
                sectorScanAnimatorSet.cancel();
              } else {
                if (!isFinishing) {
                    sectorScanAnimatorSet.setStartDelay(50L);
                    // 如果仍需要继续扫描，在这里开始新的一轮 start
                    sectorScanAnimatorSet.start();
                }
              }
         }
   });
   
   sectorScanAnimatorSet.start();
}
```


主要的难点是如何控制 startAngle 和 sweepAngle 的变化



 