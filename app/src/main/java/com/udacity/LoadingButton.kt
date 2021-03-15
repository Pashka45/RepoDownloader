package com.udacity

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates
import kotlin.random.Random


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var currentPercentage = 0

    private var drawType = DrawType.DEFAULT
    private val circle = RectF()

    private var backColor = 0
    private var frontColor = 0
    private var circleColor = 0

    private var isTouchable = true

    private var text = ""
    private val rect = Rect(0, 0, 0, 0)
    private val rectFront = Rect(0, 0, 0, 0)
    private var loadingWidth = 0f

    private fun Rect.setRightNBottom(right: Int, bottom: Int) {
        this.right = right
        this.bottom = bottom
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                animateDownloading()
            }
            ButtonState.Completed -> {
                drawType = DrawType.DEFAULT
                text = context.getString(R.string.download)
                isTouchable = true
                invalidate()
            }
        }
    }

    private fun setCircleDimensions() {
        val horizontalCenter = (width / 1.25).toFloat()
        val verticalCenter = (height / 2).toFloat()
        val circleSize = (heightSize / 3.5).toFloat()
        circle.set(
            horizontalCenter - circleSize,
            verticalCenter - circleSize,
            horizontalCenter + circleSize,
            verticalCenter + circleSize
        )
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            text = getString(R.styleable.LoadingButton_text).toString()
            backColor = getColor(R.styleable.LoadingButton_backColor, Color.YELLOW)
            frontColor = getColor(R.styleable.LoadingButton_frontColor, Color.RED)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, Color.GRAY)
        }
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        color = Color.BLACK
        measureText(text)
        style = Paint.Style.FILL_AND_STROKE
    }

    private val paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        color = backColor
    }

    private val paintFront = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        color = frontColor
    }

    private val fillArcPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = circleColor
        strokeWidth = 40f
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            if (drawType == DrawType.ANIMATED) {

                rect.setRightNBottom(widthSize, heightSize)
                drawRect(rect, paint)
                rectFront.setRightNBottom(loadingWidth.toInt(), heightSize)
                drawRect(rectFront, paintFront)
                setCircleDimensions()
                drawInnerArc(this)
            } else {

                rect.setRightNBottom(widthSize, heightSize)
                drawRect(rect, paint)
            }

            drawText(text, widthSize / 2f, heightSize / 1.5f, paintText)
        }
    }

    private fun drawInnerArc(canvas: Canvas) {
        val percentageToFill = getCurrentPercentageToFill()
        canvas.drawArc(circle, 0f, percentageToFill, true, fillArcPaint)
    }

    private fun getCurrentPercentageToFill() =
        (ARC_FULL_ROTATION_DEGREE * (currentPercentage / PERCENTAGE_DIVIDER)).toFloat()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        rect.setRightNBottom(w, h)
        setMeasuredDimension(w, h)
    }

    private enum class DrawType {
        DEFAULT,
        ANIMATED
    }

    private fun animateDownloading() {
        if (!isTouchable) return
        isTouchable = false
        drawType = DrawType.ANIMATED
        text = context.getString(R.string.downloading)
        val valueAnimator = ValueAnimator.ofFloat(0f, measuredWidth.toFloat())
        val interpolator = getInterpolator()
        val animSet = AnimatorSet()

        valueAnimator.duration = 2000
        valueAnimator.interpolator = interpolator
        valueAnimator.addUpdateListener {
            loadingWidth = valueAnimator.animatedValue as Float
            invalidate()
        }

        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                buttonState = ButtonState.Completed
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })

        val valuesHolder = PropertyValuesHolder.ofFloat("percentage", 0f, 100f)
        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = 2000
            addUpdateListener {
                val percentage = it.getAnimatedValue(PERCENTAGE_VALUE_HOLDER) as Float
                currentPercentage = percentage.toInt()
                invalidate()
            }
        }
        animator.interpolator = interpolator

        animSet.playTogether(animator, valueAnimator)
        animSet.start()
    }

    private fun getInterpolator(): TimeInterpolator {

        return when(Random.nextInt(0, 3)) {
            1 -> AccelerateDecelerateInterpolator()
            0 -> AccelerateInterpolator(2f)
            else -> DecelerateInterpolator(2f)
        }
    }

    fun start() {
        buttonState = ButtonState.Loading
        invalidate()
    }

    companion object {
        const val ARC_FULL_ROTATION_DEGREE = 360
        const val PERCENTAGE_DIVIDER = 100.0
        const val PERCENTAGE_VALUE_HOLDER = "percentage"
    }
}