package com.example.pixelmeasure

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.setBackgroundColor(0xFFFFFFFF.toInt())

        // The measurement canvas
        val measureView = MeasureView(this)
        root.addView(measureView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Top bar with screen info and clear button
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(32, 16, 32, 16)
            setBackgroundColor(0xEEF5F5F5.toInt())
        }

        val display = resources.displayMetrics
        val infoText = TextView(this).apply {
            text = "Screen: ${display.widthPixels} x ${display.heightPixels} px  |  ${display.densityDpi} dpi"
            textSize = 14f
            setTextColor(0xFF555555.toInt())
        }

        val clearBtn = Button(this).apply {
            text = "Clear"
            textSize = 14f
            setOnClickListener { measureView.clearMeasurements() }
        }

        topBar.addView(infoText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        topBar.addView(clearBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        root.addView(topBar, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ))

        setContentView(root)

        // Hide system UI for full-screen measuring
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }
}
