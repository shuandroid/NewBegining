package com.chendroid.learninglib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

import static com.chendroid.learninglib.DisplayUtils.dpToPixel;

/**
 * @author zhaochen @ Zhihu Inc.
 * 竖直方向上的 TextView
 * @since 2018/8/4
 */
public class VerticalTextView extends AppCompatTextView {

    private static final float OFFSET_MARGIN = 1.5f;

    private int width, height;

    private Rect boundRect = new Rect();

    private String text;

    private TextPaint textPaint;

    public VerticalTextView(Context pContext) {
        super(pContext);
    }

    public VerticalTextView(Context pContext, AttributeSet pAttributeSet) {
        super(pContext, pAttributeSet);
    }

    public VerticalTextView(Context pContext, AttributeSet pAttributeSet, int pDefaultStyle) {
        super(pContext, pAttributeSet, pDefaultStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 宽和高交换
        height = getMeasuredWidth();
        width = getMeasuredHeight();
        setMeasuredDimension(width, height);

        text = text();
        textPaint = getPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.save();

        canvas.translate(width, height);
        canvas.rotate(-90);

        textPaint.getTextBounds(text, 0, text.length(), boundRect);

        canvas.drawText(text, getCompoundPaddingLeft(), (boundRect.height() - width) / 2 - dpToPixel(getContext(), OFFSET_MARGIN), textPaint);

        canvas.restore();

    }

    private String text() {
        return super.getText().toString();
    }

}
