package com.spyrent.child

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Turns an app icon into something the portal can render.
 *
 * 96px is enough for a list row on any screen and keeps each icon around 5 kB,
 * which matters because they travel as data URIs inside the inventory upload.
 */
object Icons {

    private const val SIZE_PX = 96
    private const val QUALITY = 100

    fun dataUri(context: Context, packageName: String): String? = runCatching {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        val bitmap = toBitmap(drawable) ?: return null

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, out)
        bitmap.recycle()

        "data:image/png;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()

    private fun toBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return Bitmap.createScaledBitmap(drawable.bitmap, SIZE_PX, SIZE_PX, true)
        }

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: SIZE_PX
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: SIZE_PX

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        if (width == SIZE_PX && height == SIZE_PX) return bitmap

        val scaled = Bitmap.createScaledBitmap(bitmap, SIZE_PX, SIZE_PX, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }
}
