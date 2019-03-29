package com.naumchik.taxi

import android.animation.Animator
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewCar: ImageView
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var destX: Float = 0f
    private var destY: Float = 0f
    private var angle = 180f
    private var isNeedToBeMoved = true

    private val onTouchListener = View.OnTouchListener { _, event ->
        destX = event.x
        destY = event.y
        log("x:$destX, y:$destY")
        if (event.action == MotionEvent.ACTION_DOWN) startRotationAnimation()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initLocationAndAngle()
        enableOnTouchListener(true)
    }

    private fun initView() {
        viewCar = ImageView(baseContext)
        viewCar.id = R.id.viewCar
        viewCar.setImageResource(R.drawable.ic_taxi)
        viewCar.layoutParams = RelativeLayout.LayoutParams(CAR_WIDTH, CAR_HEIGHT)
        root.addView(viewCar)
    }

    private fun initLocationAndAngle() {
        val startLocation = IntArray(2)
        viewCar.getLocationOnScreen(startLocation)
        startX = startLocation[0].toFloat()
        startY = startLocation[1].toFloat()

        viewCar.animate().rotation(angle).start()
    }

    private fun startRotationAnimation() {
        pbMoving.visibility = View.VISIBLE

        destX -= (CAR_WIDTH / 2)
        destY -= (CAR_HEIGHT / 2)

        angle = calculateRotationAngle()
        log(" angle: $angle")
        calculateScreenOffside()
        viewCar.animate()
            .rotation(angle)
            .setDuration(DURATION_ROTATION)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    log("startRotatingAnimation()")
                    enableOnTouchListener(false)
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    log("startMovingAnimation()")
                    startMovingAnimation()
                }
            })
            .start()
    }

    private fun calculateScreenOffside() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        if (destX > (width - CAR_WIDTH)) destX -= CAR_WIDTH
        if (destY > (height - CAR_HEIGHT)) destY -= CAR_HEIGHT
    }

    private fun startMovingAnimation() {
        viewCar.animate()
            .translationX(destX)
            .translationY(destY)
            .setDuration(if (isNeedToBeMoved) DURATION_MOVING else 0)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    // set new start coordinates
                    log("endAnimation(). startX:$startX, startY:$startY, destX:$destX, destY:$destY")
                    startX = destX
                    startY = destY
                    if (!isNeedToBeMoved) isNeedToBeMoved = true
                    enableOnTouchListener(true)
                    pbMoving.visibility = View.GONE
                }
            })
            .start()
    }

    private fun calculateRotationAngle(): Float {
        val atanX = (startX - destX).toDouble()
        val atanY = (startY - destY).toDouble()
        val atan = if (atanY == 0.0) {
            Math.atan(atanX).toDegrees()
        } else {
            Math.atan(atanX / atanY).toDegrees()
        }

        if (atan == 0f) isNeedToBeMoved = false
        log("atan: $atan")

        return if (startY < destY) 180 - atan else (360 - atan)
    }

    private fun enableOnTouchListener(enable: Boolean) {
        if (enable) root.setOnTouchListener(onTouchListener)
        else root.setOnTouchListener(null)
    }

    companion object {
        const val CAR_WIDTH = 180
        const val CAR_HEIGHT = 360

        const val DURATION_ROTATION = 750L
        const val DURATION_MOVING = 1500L
    }

    private fun Double.toDegrees(): Float = Math.toDegrees(this).toFloat()

    private fun Context.log(message: String) = Log.d("TAG.D", message)
}
