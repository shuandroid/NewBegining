package chendroid.com.myapplication.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import chendroid.com.myapplication.R
import chendroid.com.myapplication.dip2px

@Suppress("IMPLICIT_CAST_TO_ANY")

class CircleImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {


    companion object {
        /**
         * android.widget.ImageView
         */
        val TYPE_NONE = 0
        /**
         * 圆形
         */
        val TYPE_CIRCLE = 1
        /**
         * 圆角矩形
         */
        val TYPE_ROUNDED_RECT = 2

        private val DEFAULT_TYPE = TYPE_NONE
        private val DEFAULT_BORDER_COLOR = Color.TRANSPARENT
        private val DEFAULT_BORDER_WIDTH = 0f
        private val DEFAULT_RECT_ROUND_RADIUS = 0f
    }

    private val mType: Int
    private val mBorderColor: Int
    private val mBorderWidth: Int
    private val mRectRoundRadius: Int

    private val mPaintBitmap = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaintBorder = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mRectBorder = RectF()
    private val mRectBitmap = RectF()

    private var mRawBitmap: Bitmap? = null
    private var mShader: BitmapShader? = null
    private val mMatrix = Matrix()

    private var viewHeight = 0
    private var viewWidth = 0

    private var viewMinSize = 0

//    private var rawBitmap


    init {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView)

        mType = ta.getInt(R.styleable.CircleImageView_type, DEFAULT_TYPE)
        mBorderColor = ta.getColor(R.styleable.CircleImageView_borderColor, DEFAULT_BORDER_COLOR)
        mBorderWidth = ta.getInt(R.styleable.CircleImageView_borderWidth, dip2px(DEFAULT_BORDER_WIDTH))

        mRectRoundRadius = ta.getDimensionPixelSize(R.styleable.CircleImageView_rectRoundRadius, dip2px(DEFAULT_RECT_ROUND_RADIUS))

        ta.recycle()


    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewHeight = height
        viewWidth = width

        viewMinSize = Math.min(viewHeight, viewWidth)

        initData()

    }

    override fun onDraw(canvas: Canvas) {
        val rawBitmap = getBitmap(drawable)

        if (rawBitmap == null || mType == TYPE_NONE) {
            super.onDraw(canvas)
            return
        }

        if (mType == TYPE_CIRCLE) {
            val radius = viewMinSize / 2.0f

            canvas.drawCircle(radius, radius, radius - mBorderWidth / 2f, mPaintBorder)
            canvas.translate(mBorderWidth.toFloat(), mBorderWidth.toFloat())
            canvas.drawCircle(radius - mBorderWidth, radius - mBorderWidth, radius - mBorderWidth, mPaintBitmap)

        } else if (mType == TYPE_ROUNDED_RECT) {

            val borderRadius = if (mRectRoundRadius - mBorderWidth / 2f > 0.0f) mRectRoundRadius - mBorderWidth / 2f else 0.0f
            val bitmapRadius = if (mRectRoundRadius - mBorderWidth > 0.0f) mRectRoundRadius - mBorderWidth else 0.0f

            canvas.drawRoundRect(mRectBorder, borderRadius, borderRadius, mPaintBorder)
            canvas.translate(mBorderWidth.toFloat(), mBorderWidth.toFloat())
            canvas.drawRoundRect(mRectBitmap, bitmapRadius as Float, bitmapRadius, mPaintBitmap)
        }

    }

    private fun initData() {
        val rawBitmap = getBitmap(drawable)

        rawBitmap ?: let { return }


        val dstWidth = (if (mType == TYPE_CIRCLE) viewMinSize else viewWidth).toFloat()
        val dstHeight = (if (mType == TYPE_CIRCLE) viewMinSize else viewHeight).toFloat()

        val halfBorderWidth = mBorderWidth / 2.0f
        val doubleBorderWidth = (mBorderWidth * 2).toFloat()

        if (mShader == null || rawBitmap != mRawBitmap) {
            mRawBitmap = rawBitmap
            mShader = BitmapShader(mRawBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        if (mShader != null) {
            mMatrix.setScale((dstWidth - doubleBorderWidth) / rawBitmap.width, (dstHeight - doubleBorderWidth) / rawBitmap.height)
            mShader!!.setLocalMatrix(mMatrix)
        }

        mPaintBitmap.shader = mShader
        mPaintBorder.style = Paint.Style.STROKE
        mPaintBorder.strokeWidth = mBorderWidth.toFloat()
        mPaintBorder.color = if (mBorderWidth > 0) mBorderColor else Color.TRANSPARENT


        mRectBorder.set(halfBorderWidth, halfBorderWidth, dstWidth - halfBorderWidth, dstHeight - halfBorderWidth)
        mRectBitmap.set(0.0f, 0.0f, dstWidth - doubleBorderWidth, dstHeight - doubleBorderWidth)


    }

    private fun getBitmap(drawable: Drawable): Bitmap? {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is ColorDrawable) {
            val rect = drawable.getBounds()
            val width = rect.right - rect.left
            val height = rect.bottom - rect.top
            val color = drawable.color
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawARGB(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
            return bitmap
        } else {
            return null
        }
    }
}