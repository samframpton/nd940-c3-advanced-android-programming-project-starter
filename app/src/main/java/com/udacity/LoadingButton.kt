package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator: ValueAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                duration = 1000
                start()
            }
    private var animationProgress = 0f

    private lateinit var backgroundPaint: Paint
    private lateinit var loadingPaint: Paint
    private lateinit var circlePaint: Paint
    private lateinit var textPaint: Paint

    init {
        valueAnimator.addUpdateListener {
            animationProgress = it.animatedValue as Float
            invalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                isEnabled = true
            }
        })
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            }
            loadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = getColor(R.styleable.LoadingButton_loadingColor, 0)
            }
            circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = getColor(R.styleable.LoadingButton_circleColor, 0)
            }
            textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                textAlign = Paint.Align.CENTER
                color = getColor(R.styleable.LoadingButton_textColor, 0)
                textSize = 60.0f
            }
        }
        isClickable = true
    }

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Idle) { _, _, new ->
        when (new) {
            ButtonState.Idle -> {
                valueAnimator.cancel()
                animationProgress = 0f
                buttonText = resources.getString(R.string.button_download)
                invalidate()
            }

            ButtonState.Loading -> {
                valueAnimator.start()
                buttonText = resources.getString(R.string.button_loading)
                invalidate()
            }
        }
    }

    private var buttonText = resources.getString(R.string.button_download)
    private val textBounds = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), backgroundPaint)

        textPaint.getTextBounds(buttonText, 0, buttonText.length, textBounds)
        val centerX = widthSize.toFloat() / 2
        val centerY = heightSize.toFloat() / 2 - textBounds.centerY()

        if (buttonState == ButtonState.Loading) {
            val progress = animationProgress * widthSize.toFloat()
            canvas.drawRect(0f, 0f, progress, heightSize.toFloat(), loadingPaint)

            val diameter = 60f
            val textWidth = textPaint.measureText(buttonText)
            val left = centerX + (textWidth / 2)
            val top = (heightSize / 2) - (diameter / 2)
            val right = left + diameter
            val bottom = top + diameter
            canvas.drawArc(
                    left,
                    top,
                    right,
                    bottom,
                    0f,
                    animationProgress * 360f,
                    true,
                    circlePaint)
        }

        canvas.drawText(buttonText, centerX, centerY, textPaint)
    }

    fun setState(state: ButtonState) {
        buttonState = state
    }

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
        setMeasuredDimension(w, h)
    }

}
