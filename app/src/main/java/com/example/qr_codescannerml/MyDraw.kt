package com.example.qr_codescannerml

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable


class MyDraw(qrCodeViewModel: QrCodeViewModel) : Drawable() {

    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.YELLOW
        strokeWidth = 5F
        alpha = 200
    }

    private val contentRectPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
        alpha = 255
    }

    private val contentTextPaint = Paint().apply {
        color = Color.DKGRAY
        alpha = 255
        textSize = 42F
    }

    private val myMv = qrCodeViewModel
    private val contentPadding = 25
    private var textWidth = contentTextPaint.measureText(myMv.url).toInt()
    override fun draw(canvas: Canvas) {
        canvas.drawRect(myMv.boundingRect, boundingRectPaint)

        canvas.drawRect(
            Rect(
                myMv.boundingRect.left,
                myMv.boundingRect.bottom + contentPadding / 2,
                myMv.boundingRect.left + textWidth + contentPadding * 2,
                myMv.boundingRect.bottom + contentTextPaint.textSize.toInt() + contentPadding
            ),
            contentRectPaint
        )
        canvas.drawText(
            myMv.url,
            (myMv.boundingRect.left + contentPadding).toFloat(),
            (myMv.boundingRect.bottom + contentPadding * 2).toFloat(),
            contentTextPaint
        )
    }

    override fun setAlpha(alpha: Int) {
        TODO("Not yet implemented")
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        TODO("Not yet implemented")
    }
}