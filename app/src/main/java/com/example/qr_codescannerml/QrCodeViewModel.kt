package com.example.qr_codescannerml


import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode

class QrCodeViewModel(barcode: Barcode){
    var url: String = ""
    var boundingRect: Rect = barcode.boundingBox!!
    init {
        when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                url = barcode.url?.url.toString()
            }
        }
    }
}