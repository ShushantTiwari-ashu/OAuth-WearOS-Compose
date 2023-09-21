package dev.shushant.deviceauthenticationsample.qrcode

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.EnumMap

class QRGEncoder(data: String?, private var dimension: Int) {
    private var colorWhite = -0x1
    private var colorBlack = -0x1000000
    private var contents: String? = null
    private var displayContents: String? = null
    private var title: String? = null
    private var format: BarcodeFormat? = null
    private var encoded = false

    init {
        encoded = encodeContents(data)
    }

    private fun encodeContents(data: String?): Boolean {
        format = BarcodeFormat.QR_CODE
        encodeQRCodeContents(data)
        return contents != null && contents!!.isNotEmpty()
    }

    private fun encodeQRCodeContents(data: String?) {
        if (!data.isNullOrEmpty()) {
            contents = data
            displayContents = data
            title = "Text"
        }
    }

    val bitmap: Bitmap?
        get() = if (!encoded) null else try {
            var hints: MutableMap<EncodeHintType?, Any?>? = null
            val encoding = guessAppropriateEncoding(contents)
            if (encoding != null) {
                hints = EnumMap(EncodeHintType::class.java)
                hints[EncodeHintType.CHARACTER_SET] = encoding
            }
            val writer = MultiFormatWriter()
            val result = writer.encode(contents, format, dimension, dimension, hints)
            val width = result.width
            val height = result.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (result[x, y]) colorWhite else colorBlack
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (ex: Exception) {
            null
        }

    private fun guessAppropriateEncoding(contents: CharSequence?): String? {
        // Very crude at the moment
        for (i in 0 until contents!!.length) {
            if (contents[i].code > 0xFF) {
                return "UTF-8"
            }
        }
        return null
    }

}