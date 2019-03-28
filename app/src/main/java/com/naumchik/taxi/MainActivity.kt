package com.naumchik.taxi

import android.animation.Animator
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

    private val onTouchListener = View.OnTouchListener { _, event ->
        destX = event.x
        destY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startRotationAnimation()
        }
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
        calculateScreenOffside()
        viewCar.animate()
            .rotation(calculateRotationAngle())
            .setDuration(DURATION_ROTATION)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    enableOnTouchListener(false)
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
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
            .setDuration(DURATION_MOVING)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    // set new start coordinates
                    startX = destX
                    startY = destY

                    enableOnTouchListener(true)
                }
            })
            .start()
    }

    private fun calculateRotationAngle(): Float {
        val atanX = (startX - destX).toDouble()
        val atanY = (startY - destY).toDouble()
        if (atanX > 0 && atanY > 0) {
            val atan = Math.atan(-atanX / -atanY)
            angle = if (startY < destY) 180 - atan.toDegrees() else (180 - atan.toDegrees()) + 180
        }

        return angle
    }

    private fun enableOnTouchListener(enable: Boolean) {
        if (enable) root.setOnTouchListener(onTouchListener)
        else root.setOnTouchListener(null)
    }

    companion object {
        const val CAR_WIDTH = 180
        const val CAR_HEIGHT = 360

        const val DURATION_ROTATION = 1000L
        const val DURATION_MOVING = 2000L
    }

    private fun Double.toDegrees(): Float = Math.toDegrees(this).toFloat()
}
