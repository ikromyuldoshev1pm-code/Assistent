package com.example.assistent

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale
import kotlin.math.*

class MainActivity : Activity() {

    private var voiceEnabled = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    private lateinit var voiceSwitch: TextView
    private lateinit var statusText: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(5, 9, 24)
        window.navigationBarColor = Color.rgb(5, 9, 24)

        tts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                val uz = tts?.setLanguage(Locale("uz", "UZ"))

                if (uz == TextToSpeech.LANG_NOT_SUPPORTED ||
                    uz == TextToSpeech.LANG_MISSING_DATA) {
                    tts?.language = Locale.getDefault()
                }

                tts?.setSpeechRate(1.0f)
            }
        }

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

        root.addView(
            SphereView(),
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
                55
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
                Color.rgb(17, 25, 48), 28
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
                Color.rgb(35, 85, 190), 50
            )

            setOnClickListener {
                val command = commandInput.text.toString().trim()

                if (command.isNotEmpty()) {
                    statusText.text = "Buyruq qabul qilindi"
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

        if (voiceEnabled) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    permissionCode
                )
            } else {
                startListening()
            }
        } else {
            stopListening()
            statusText.text = "Ovozli boshqaruv o‘chirildi"
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "Nutqni tanish xizmati mavjud emas"
            return
        }

        try {
            speechRecognizer?.destroy()

            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {
                        statusText.text = "Sizni tinglayapman..."
                    }

                    override fun onBeginningOfSpeech() {
                        statusText.text = "Ovoz aniqlandi..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        statusText.text = "Ovoz tahlil qilinmoqda..."
                    }

                    override fun onError(error: Int) {
                        statusText.text =
                            "Ovoz aniqlanmadi. Qayta yoqib ko‘ring."
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                        val spokenText = matches?.firstOrNull()

                        if (!spokenText.isNullOrBlank()) {
                            statusText.text = spokenText

                            tts?.speak(
                                "Siz aytdingiz: $spokenText",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "assistant_reply"
                            )
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )

            val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "uz-UZ"
                )
                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )
            }

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {
            statusText.text = "Mikrofonni ishga tushirib bo‘lmadi"
        }
    }

    private fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) {}

        statusText.text = "Ovozli boshqaruv o‘chirildi"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                voiceEnabled) {
                startListening()
            } else {
                voiceEnabled = false
                voiceSwitch.background = switchBackground(false)
                statusText.text = "Mikrofon ruxsati berilmadi"
            }
        }
    }

    private fun switchBackground(
        enabled: Boolean
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 40f
            setColor(
                if (enabled) Color.rgb(25, 110, 220)
                else Color.rgb(35, 43, 65)
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

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    inner class SphereView : View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var angle = 0f

        private val animationHandler =
            Handler(Looper.getMainLooper())

        private val animation = object : Runnable {
            override fun run() {
                angle += 0.012f
                invalidate()
                animationHandler.postDelayed(this, 30)
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            animationHandler.post(animation)
        }

        override fun onDetachedFromWindow() {
            animationHandler.removeCallbacks(animation)
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

                    val mix = ((x + y + 2) / 4)
                        .coerceIn(0.0, 1.0).toFloat()

                    paint.color = blendColors(
                        Color.rgb(255, 45, 75),
                        Color.rgb(35, 125, 255),
                        mix
                    )

                    paint.alpha = (100 + depth * 155).toInt()

                    canvas.drawCircle(
                        px, py, dotSize, paint
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
