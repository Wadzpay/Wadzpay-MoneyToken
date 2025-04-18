package com.wadzpay.ddflibrary.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.wadzpay.ddflibrary.R

class DynamicGeneratorActivity : AppCompatActivity() {

    val QRcodeWidth = 500
    var TOTAL_AMOUNT = 1
    lateinit var iv_qr_code:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_linear)
        initViews()
        initToolbar()
    }

    private fun initToolbar() {
//        txnToolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        val url = "upi://pay?pa=" +   // payment method.
                "9885111437@icici" +         // VPA number.
                "&am="+ TOTAL_AMOUNT +       // this param is for fixed amount (non editable).
                "&pn=Suresh%20G"+      // to showing your name in app.
                "&cu=INR" +                  // Currency code.
                "&mode=02" +                 // mode O2 for Secure QR Code.
                "&orgid=189999" +            //If the transaction is initiated by any PSP app then the respective orgID needs to be passed.
                "&sign=MEYCIQC8bLDdRbDhpsPAt9wR1a0pcEssDaV" +   // Base 64 encoded Digital signature needs to be passed in this tag
                "Q7lugo8mfJhDk6wIhANZkbXOWWR2lhJOH2Qs/OQRaRFD2oBuPCGtrMaVFR23t"

        try {
            val bitmap = textToImageEncode(url)
            iv_qr_code = findViewById(R.id.iv_qr_code);
            iv_qr_code.setImageBitmap(bitmap)
            iv_qr_code.invalidate()
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun textToImageEncode(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(
                Value,
                BarcodeFormat.QR_CODE,
                QRcodeWidth, QRcodeWidth, null
            )
        } catch (Illegalargumentexception: IllegalArgumentException) {
            return null
        }
        val bitMatrixWidth = bitMatrix.getWidth()
        val bitMatrixHeight = bitMatrix.getHeight()
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)
        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y))
                    Color.parseColor("#000000")
                else
                    Color.parseColor("#ffffff")
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)
        return bitmap
    }

}