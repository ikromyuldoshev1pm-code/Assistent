package com.example.assistent

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import kotlin.math.*

class MainActivity : Activity() {

    private var voiceEnabled = false
    private lateinit var voiceSwitch: TextView
    private lateinit var statusText: TextView
    private lateinit var sphereView: SphereView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(5, 9, 24)
        window.navigationBarColor = Color.rgb(5, 9, 24)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 20, 24, 24)
            setBackgroundColor(Color.rgb(5, 9, 24))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val voiceTitle = TextView(this).apply {
            text = "Ovoz orqali chaqirish"
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        header.addView(
            voiceTitle,
            LinearLayout.LayoutParams(0, 55, 1f)
        )

        voiceSwitch = TextView(this).apply {
            text = "●"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = switchBackground(false)
            setOnClickListener {
                toggleVoice()
            }
        }

        header.addView(
            voiceSwitch,
            LinearLayout.LayoutParams(76, 38)
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            )
        )

        root.addView(
            View(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        sphereView = SphereView()

        root.addView(
            sphereView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                310
            )
        )

        statusText = TextView(this).apply {
            text = "Assistent tayyor"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(145, 160, 190))
        }

        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        root.addView(
            View(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedBackground(
                Color.rgb(17, 25, 48),
                28
            )
            setPadding(16, 5, 8, 5)
        }

        val commandInput = EditText(this).apply {
            hint = "Buyruqni yozing..."
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(125, 140, 170))
            background = null
            setSingleLine(true)
        }

        inputRow.addView(
            commandInput,
            LinearLayout.LayoutParams(0, 52, 1f)
        )

        val sendButton = TextView(this).apply {
            text = "➤"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedBackground(
                Color.rgb(35, 85, 190),
                50
            )

            setOnClickListener {
                val command = commandInput.text.toString().trim()

                if (command.isNotEmpty()) {
                    statusText.text = "Buyruq qabul qilindi"

                    Toast.makeText(
                        this@MainActivity,
                        "Buyruq: $command",
                        Toast.LENGTH_SHORT
                    ).show()

                    commandInput.text.clear()
                }
            }
        }

        inputRow.addView(
            sendButton,
            LinearLayout.LayoutParams(48, 48)
        )

        root.addView(
            inputRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                62
            )
        )

        setContentView(root)
    }

    private fun toggleVoice() {
        voiceEnabled = !voiceEnabled

        voiceSwitch.background = switchBackground(voiceEnabled)

        statusText.text = if (voiceEnabled) {
            "Ovozli boshqaruv yoqildi"
        } else {
            "Ovozli boshqaruv o‘chirildi"
        }

        voiceSwitch.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(100)
            .withEndAction {
                voiceSwitch.scaleX = 1f
                voiceSwitch.scaleY = 1f
            }
    }

    private fun switchBackground(
        enabled: Boolean
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 40f

            setColor(
                if (enabled) {
                    Color.rgb(25, 110, 220)
                } else {
                    Color.rgb(35, 43, 65)
                }
            )
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }

    inner class SphereView : View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var angle = 0f

        private val handler = android.os.Handler(
            android.os.Looper.getMainLooper()
        )

        private val animation = object : Runnable {
            override fun run() {
                angle += 0.012f
                invalidate()
                handler.postDelayed(this, 30)
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            handler.post(animation)
        }

        override fun onDetachedFromWindow() {
            handler.removeCallbacks(animation)
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val cx = width / 2f
            val cy = height / 2f
            val radius = min(width, height) * 0.37f

            val rows = 19
            val columns = 42

            for (i in 0..rows) {
                val lat = -PI / 2 + PI * i / rows

                for (j in 0 until columns) {
                    val lon = 2 * PI * j / columns + angle

                    val x = cos(lat) * cos(lon)
                    val y = sin(lat)
                    val z = cos(lat) * sin(lon)

                    if (z < -0.15) continue

                    val px = cx + (x * radius).toFloat()
                    val py = cy + (y * radius).toFloat()

                    val depth = ((z + 1) / 2).toFloat()
                    val dotSize = 1.5f + depth * 2.1f

                    val red = Color.rgb(255, 45, 75)
                    val blue = Color.rgb(35, 125, 255)

                    val mix = ((x + y + 2) / 4)
                        .coerceIn(0.0, 1.0)
                        .toFloat()

                    paint.color = blendColors(
                        red,
                        blue,
                        mix
                    )

                    paint.alpha = (100 + depth * 155).toInt()

                    canvas.drawCircle(
                        px,
                        py,
                        dotSize,
                        paint
                    )
                }
            }
        }

        private fun blendColors(
            start: Int,
            end: Int,
            ratio: Float
        ): Int {
            val inverse = 1f - ratio

            val r = Color.red(start) * inverse +
                    Color.red(end) * ratio

            val g = Color.green(start) * inverse +
                    Color.green(end) * ratio

            val b = Color.blue(start) * inverse +
                    Color.blue(end) * ratio

            return Color.rgb(
                r.toInt(),
                g.toInt(),
                b.toInt()
            )
        }
    }
}
