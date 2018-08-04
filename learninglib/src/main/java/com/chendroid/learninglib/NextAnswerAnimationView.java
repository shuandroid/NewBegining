package com.chendroid.learninglib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zhaochen @ Zhihu Inc.
 * @intro
 * @since 2018/8/4
 */
public class NextAnswerAnimationView extends LinearLayout {


    public interface StatusChangedListener {
        void onDrag();
    }

    @IntDef({PlaceType.TYPE_WHEN_CLICK, PlaceType.TYPE_WHEN_SCROLLED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlaceType {
        int TYPE_WHEN_SCROLLED = 1;
        int TYPE_WHEN_CLICK = 2;
    }

    private static final String TAG = "NextAnswerAnimationView";

    private static final int ANIM_DURATION = 200;

    //下面的具体数值是按照设计图中的边距设定的，因为该按钮在 xml 里面不会有具体布局的参数
    private static final int MARGIN_TOP = 10;
    private static final int MARGIN_BOTTOM = 59;
    private static final int MARGIN_HORIZONTAL = 10;
    private static final int MARGIN_PARENT = 6;
    private static final int PADDING_PARENT = 10;

    private static final int DELAY_TIME_WHEN_SCROLLED = 600;
    private static final int DELAY_TIME_SHOW_TIPS = 2000;
    private static final int DELAY_TIME_FOLD_ANIMATION = 200;
    private static final int DURATION_SHOW_TIPS = 5000;
    private static final int DURATION_FOLD_ANIMATION = 500;
    private static final int DURATION_ALPHA_ANIMATION = 300;

    private ImageView mArrowView;
    private TextView mNextTextView;

//    private Tooltips tooltips;

    private float mPrevX, mPrevY, mCurX, mCurY;
    private float mTouchDownX, mTouchDownY, mFirstX, mFirstY;

    // 是否在滑动的标识位
    private boolean mScrolling = false;
    // 对多个手指做处理的标识位
    private boolean mIsCanMove = false;
    private boolean isFirstMove = true;
    private boolean isHasFold = false;

    //该值是指是否支持拖拽，默认为 true， 依赖实验的配置
    private boolean isSupportDrag = true;

    private boolean isFirstLayout = true;

    private int mScreenWidth, mMarginHorizontal;
    private int mScreenForAnswerHeight;
    private int mScreenForAnswerMarginTop;

    private Fragment hostFragment;

    private StatusChangedListener statusChangedListener;

    public NextAnswerAnimationView(Context pContext) {
        super(pContext);
        init();
    }

    public NextAnswerAnimationView(Context pContext, AttributeSet pAttributeSet) {
        super(pContext, pAttributeSet);
        init();
    }

    public NextAnswerAnimationView(Context pContext, AttributeSet pAttributeSet, int pDefaultStyle) {
        super(pContext, pAttributeSet, pDefaultStyle);
        init();
    }

    private void init() {

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mMarginHorizontal = DisplayUtils.dpToPixel(getContext(), MARGIN_HORIZONTAL);
        mScreenForAnswerMarginTop = DisplayUtils.dpToPixel(getContext(), MARGIN_TOP);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mArrowView = findViewById(R.id.next_arrow_view);
        mNextTextView = findViewById(R.id.next_text_view);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        //如果是第一次初始化该 view 需要查询是否存储过该 view 的位置
        if (isFirstLayout) {
            Log.i(TAG, "onLayout() isFirstLayout is true ");
            isFirstLayout = false;
            mScreenForAnswerHeight = ((ViewGroup) getParent()).getHeight() - DisplayUtils.dpToPixel(getContext(), MARGIN_BOTTOM);

            //不支持拖拽，则用默认的值
            if (!isSupportDrag) {
                setX(mScreenWidth - getWidth() - mMarginHorizontal);
                setY(mScreenForAnswerHeight - getHeight());
                return;
            }

//            float xValue = CommunityPreferenceHelper2.getNextAnswerXValue(getContext());

            // 0 为默认的值，表明此时没有在 sp 存储过坐标值， 使用默认的位置
//            if (xValue == 0) {
            setX(mScreenWidth - getWidth() - mMarginHorizontal);
            setY(mScreenForAnswerHeight - getHeight());
//            } else {
            //如果存储的 x 坐标值 不足以显示该 view 整体（在折叠情况下记录的 x 的值，会使在未折叠状态下不能完全显示该 view），则把 x 设置在屏幕的边缘
//                if (xValue > mScreenWidth - getWidth() - mMarginHorizontal) {
//                    setX(mScreenWidth - getWidth() - mMarginHorizontal);
//                    CommunityPreferenceHelper2.setNextAnswerXValue(getContext(), mScreenWidth - getWidth() - mMarginHorizontal);
//                } else {
//                    setX(xValue);
//                }

//                setY(CommunityPreferenceHelper2.getNextAnswerYValue(getContext()));
//            }
        }
    }

    // 如果 NextAnswerAnimationView 的父 view 没有对 Touch 事件做处理，那么该方法是可以不用复写的；
    // 在 lite 极速版里，父 view 没有拦截事件，所以可以不需要复写该方法
    // 在 主工程 里面，父 view 对事件进行了拦截，需要复写该方法
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        //如果不支持拖拽，则不拦截
        if (!isSupportDrag) {
            return super.onInterceptTouchEvent(event);
        }

        int delta;
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                mScrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                delta = (int) Math.sqrt(Math.pow(mTouchDownX - event.getX(), 2) + Math.pow(mTouchDownY - event.getY(), 2));
                mScrolling = delta >= ViewConfiguration.get(getContext()).getScaledTouchSlop();
                break;

            case MotionEvent.ACTION_UP:
                mScrolling = false;
                break;
        }

        return mScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isSupportDrag) {
            return super.onTouchEvent(event);
        }

        int delta;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:

                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                mScrolling = false;
                mIsCanMove = isFirstFingerTouch(event);
                Log.i(TAG, "onTouchEvent() mIsCanMove is " + mIsCanMove);

                break;

            case MotionEvent.ACTION_MOVE:

                if (!mIsCanMove) {
                    break;
                }

                delta = (int) Math.sqrt(Math.pow(mTouchDownX - event.getX(), 2) + Math.pow(mTouchDownY - event.getY(), 2));

                if (!(delta >= ViewConfiguration.get(getContext()).getScaledTouchSlop())) {
                    //不满足对滑动距离的判断，则直接返回
                    break;
                }

                mScrolling = true;

                if (isNeedShowTips()) {
                    setIsNeedShowTips(false);
                }

                dismissTips();

                if (isFirstMove) {
                    Log.i(TAG, "onTouchEvent() isFirstMove is true ");
                    mFirstX = getX();
                    mFirstY = getY();
                    mPrevX = event.getRawX();
                    mPrevY = event.getRawY();
                    isFirstMove = false;
                }

                float deltaX = event.getRawX() - mPrevX;
                float deltaY = event.getRawY() - mPrevY;
                mCurX = mFirstX + deltaX;
                mCurY = mFirstY + deltaY;

                handleViewWhenOverScreen();

                setX(mCurX);
                setY(mCurY);

                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:

                if (isFirstFingerTouch(event)) {
                    moveViewToEdge();
                    isFirstMove = true;
                    mIsCanMove = false;

                    if (mScrolling) {
                        Log.i("zc_test", "mScrolling is true");
                        mScrolling = false;
                        if (statusChangedListener != null) {
                            statusChangedListener.onDrag();
                        }
                        return true;
                    }
                }

                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 调整 NextAnswerAnimationView mCurY, mCurX， 避免当前 NextAnswerAnimationView 拖动超出可移动范围
     */
    private void handleViewWhenOverScreen() {

        if (mCurY <= mScreenForAnswerMarginTop) {
            mCurY = mScreenForAnswerMarginTop;
        }

        if (mCurY > mScreenForAnswerHeight - getHeight()) {
            mCurY = mScreenForAnswerHeight - getHeight();
        }

        if (mCurX <= mMarginHorizontal) {
            mCurX = mMarginHorizontal;
        }

        if (mCurX > mScreenWidth - mMarginHorizontal - getWidth()) {
            mCurX = mScreenWidth - mMarginHorizontal - getWidth();
        }
    }

    /**
     * 移动 NextAnswerAnimationView 到可显示区域的边缘
     */
    private void moveViewToEdge() {

        Log.i(TAG, "moveViewToEdge()");
        int destX;
        if (getX() + getWidth() / 2 > mScreenWidth / 2) {
            destX = mScreenWidth - getWidth() - mMarginHorizontal;
        } else {
            destX = mMarginHorizontal;
        }

        animate().translationXBy(destX - getX()).setDuration(ANIM_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        if (getContext() == null) {
                            return;
                        }

                        saveSelfLocation();
                    }
                });
    }

    /**
     * 只处理第一个手指的 touch event，防止多点触控引起的问题
     *
     * @return true if the
     */
    private boolean isFirstFingerTouch(MotionEvent event) {
        return event.getPointerId(event.getActionIndex()) == 0;
    }

    /**
     * 判断该 NextAnswerAnimationView 是否折叠
     *
     * @return 折叠则返回 true， 未折叠返回 false
     */
    public boolean isHasFold() {
        return isHasFold;
    }

    /**
     * 记录 NextAnswerAnimationView 的位置信息
     */
    private void saveSelfLocation() {

//        CommunityPreferenceHelper2.setNextAnswerXValue(getContext(), getX());
//        CommunityPreferenceHelper2.setNextAnswerYValue(getContext(), getY());
    }

    /**
     * 销毁 NextAnswerAnimationView
     */
    public void destroyView() {

        hostFragment = null;
        dismissTips();
        new Handler().removeCallbacksAndMessages(null);
    }

    public void setStatusChangedListener(StatusChangedListener listener) {
        statusChangedListener = listener;
    }

    public void setIsSupportDrag(boolean isSupportDragInConfig) {
        isSupportDrag = isSupportDragInConfig;
    }

    /**
     * 需要设置一个宿主 Fragment， 用在显示气泡提示上，若未设置，则不会显示气泡
     *
     * @param fragment host fragment 宿主 fragment
     */
    public void setAttachFragment(Fragment fragment) {
        hostFragment = fragment;
    }

    /**
     * 检查是否需要折叠当「下一个回答」，需要的话，便折叠
     *
     * @param placeType 为调用该方法的位置，目前支持 NextAnswerAnimationView 自身被点击调用，和 appView 发生滚动时调用
     */
    public void startNextAnswerFoldAnim(@PlaceType int placeType) {

        if (isHasFold) {
            return;
        }

        //立马把 isHasFold 设置为 true, 避免多次开启折叠动画
        isHasFold = true;

        switch (placeType) {
            case PlaceType.TYPE_WHEN_SCROLLED:
                startFoldAnimByDelay();
                break;
            case PlaceType.TYPE_WHEN_CLICK:
                startFoldAnim();
                break;
        }
    }

    private void startFoldAnimByDelay() {
        new Handler().postDelayed(() -> {
            startFoldAnim();
        }, DELAY_TIME_WHEN_SCROLLED);
    }

    /**
     * 最终开启动画的入口
     */
    private void startFoldAnim() {
        Log.i(TAG, "startNextAnswerFoldAnim()");
        if (getX() + getWidth() / 2 > mScreenWidth / 2) {
            startRightFoldAnim();
        } else {
            startLeftFoldAnim();
        }
    }

    /**
     * 向左折叠动画， 先 text 逐渐 alpha 变化，再整个 view 的布局参数向左移动伴随着 text 的向左移动
     */
    private void startLeftFoldAnim() {

        Log.i(TAG, "startLeftFoldAnim()");

        int oldWidth = getWidth();
        int newWidth = DisplayUtils.dpToPixel(getContext(), 2 * (MARGIN_PARENT + PADDING_PARENT)) + mArrowView.getWidth();

        LinearLayout.LayoutParams textViewLayoutParams = (LinearLayout.LayoutParams) mNextTextView.getLayoutParams();
        int textLeftMargin = textViewLayoutParams.leftMargin;
        int textRightMargin = textViewLayoutParams.rightMargin;

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();

        ValueAnimator textDisappearAnim = ValueAnimator.ofFloat(0, 1);
        textDisappearAnim.setDuration(DURATION_FOLD_ANIMATION);
        textDisappearAnim.setStartDelay(DELAY_TIME_FOLD_ANIMATION);
        textDisappearAnim.setInterpolator(new FastOutSlowInInterpolator());
        textDisappearAnim.addUpdateListener(animation -> {

            float percent = animation.getAnimatedFraction();

            layoutParams.width = (int) (oldWidth - (oldWidth - newWidth) * percent);
            setLayoutParams(layoutParams);

            //去除 mNextTextView 的 margin_left 和 margin_right
            textViewLayoutParams.setMargins(Math.round((1f - percent) * textLeftMargin), textViewLayoutParams.topMargin,
                    Math.round((1 - percent) * textRightMargin), textViewLayoutParams.bottomMargin);
            mNextTextView.setLayoutParams(textViewLayoutParams);
        });

        ValueAnimator alphaAnim = ValueAnimator.ofFloat(0, 1f);
        alphaAnim.setDuration(DURATION_ALPHA_ANIMATION);
        alphaAnim.setInterpolator(new FastOutSlowInInterpolator());
        alphaAnim.addUpdateListener(animation -> {
            float percent = animation.getAnimatedFraction();

            mNextTextView.setAlpha(1f - percent);
        });

        textDisappearAnim.start();
        alphaAnim.start();
    }

    /**
     * 向右折叠动画， 先 text 逐渐 alpha 变化，再整个 view 的布局参数向右移动伴随着 text 的 向右移动
     */
    private void startRightFoldAnim() {

        int oldWidth = getWidth();
        int newWidth = DisplayUtils.dpToPixel(getContext(), 2 * (MARGIN_PARENT + PADDING_PARENT)) + mArrowView.getWidth();

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        LinearLayout.LayoutParams textViewLayoutParams = (LinearLayout.LayoutParams) mNextTextView.getLayoutParams();
        int textLeftMargin = textViewLayoutParams.leftMargin;
        int textRightMargin = textViewLayoutParams.rightMargin;

        ValueAnimator textDisappearAnim = ValueAnimator.ofFloat(getX(), (mScreenWidth - newWidth - mMarginHorizontal));
        textDisappearAnim.setDuration(DURATION_FOLD_ANIMATION);
        textDisappearAnim.setStartDelay(200);
        textDisappearAnim.setInterpolator(new FastOutSlowInInterpolator());
        textDisappearAnim.addUpdateListener(animation -> {

            float percent = animation.getAnimatedFraction();
            float value = (float) animation.getAnimatedValue();

            layoutParams.width = (int) (oldWidth - (oldWidth - newWidth) * percent);
            setLayoutParams(layoutParams);

            //这一句至关重要，手动改变父 view 的 x 坐标，实现从左向右移动的效果
            setX(value);

            //去除 mNextTextView 的 margin_left 和 margin_right
            textViewLayoutParams.setMargins(Math.round((1f - percent) * textLeftMargin), textViewLayoutParams.topMargin,
                    Math.round((1 - percent) * textRightMargin), textViewLayoutParams.bottomMargin);
            mNextTextView.setLayoutParams(textViewLayoutParams);
        });

        textDisappearAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                if (getContext() == null) {
                    return;
                }

                if (!isSupportDrag) {
                    return;
                }

                if (!isNeedShowTips()) {
                    return;
                }

                Log.i(TAG, "textDisappearAnim.addListener() onAnimationEnd  delay show tips");
                delayToShowTips();
            }
        });

        ValueAnimator alphaAnim = ValueAnimator.ofFloat(0, 1f);
        alphaAnim.setDuration(DURATION_ALPHA_ANIMATION);
        alphaAnim.setInterpolator(new FastOutSlowInInterpolator());
        alphaAnim.addUpdateListener(animation -> {

            float percent = animation.getAnimatedFraction();
            mNextTextView.setAlpha(1f - percent);
        });

        textDisappearAnim.start();
        alphaAnim.start();
    }

    /**
     * 数据存在 sp 中
     *
     * @return 是否需要显示 tips， 是的话，返回 true，反之返回 false
     */
    private boolean isNeedShowTips() {
//        return CommunityPreferenceHelper2.getNextAnswerIsNeedShowTips(getContext());
        //暂时不显示 气泡提示
        return false;
    }

    private void setIsNeedShowTips(boolean isNeedShowTips) {
//        CommunityPreferenceHelper2.setNextAnswerIsNeedShowTips(getContext(), isNeedShowTips);
    }

    /**
     * 开始 2 秒延迟，并判断 2 秒后是否有机会显示该 tips
     */
    private void delayToShowTips() {

        new Handler().postDelayed(() -> {

            if (getContext() == null) {
                return;
            }

            if (hostFragment == null) {
                return;
            }

            if (!isNeedShowTips()) {
                return;
            }

            showTips();
        }, DELAY_TIME_SHOW_TIPS);
    }

    /**
     * 真正显示 tips 的地方
     */
    private void showTips() {
//        preTips();
//        if (tooltips != null) {
//            setIsNeedShowTips(false);
//            tooltips.show();
//        }
    }

    /**
     * 做一些准备工作，把 tooltips 对象建立完整
     */
//    private void preTips() {
//
//        if (getContext() == null) {
//            return;
//        }
//
//        if (hostFragment == null) {
//            return;
//        }
//
//        if (tooltips != null) {
//            return;
//        }
//
//        int[] location = new int[2];
//
//        getLocationOnScreen(location);
//
//        VerticalTextView tipTextView = new VerticalTextView(getContext());
//        tipTextView.setText(getResources().getString(R.string.answer_next_tips_text));
//        tipTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
//        tipTextView.setTextColor(getContext().getResources().getColor(R.color.GBK99B));
//        tipTextView.setGravity(Gravity.CENTER);
//        tipTextView.setRotation(180f);
//
//        tooltips = Tooltips.in(hostFragment)
//                .setArrowAtBottomCenter()
//                .setArrowLocation((location[0] - DisplayUtils.dpToPixel(getContext(), 8)), location[1] + getHeight() / 2)
//                .setAutoDismissWhenTouchOutside(false)
//                .setBackgroundColor(getContext().getResources().getColor(R.color.GBL03A))
//                .setContentView(tipTextView)
//                .setDuration(DURATION_SHOW_TIPS)
//                .setElevationDp(4.0f)
//                .rotate270()
//                .build();
//    }

    /**
     * 关闭该 tips
     */
    private void dismissTips() {

//        if (tooltips != null) {
//
//            if (tooltips.isShowing()) {
//                tooltips.dismiss();
//            }
//            tooltips = null;
//        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        new Handler().removeCallbacksAndMessages(null);
    }

}
