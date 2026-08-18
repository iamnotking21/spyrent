package com.spyrent.child

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

/**
 * The lock screen, drawn as a system overlay rather than started as an Activity.
 *
 * This exists because starting an Activity from a background service does not
 * work on every device: MIUI in particular refuses it silently unless the user
 * has granted an extra "pop-up while running in the background" permission that
 * is buried and easy to miss. An overlay window only needs SYSTEM_ALERT_WINDOW
 * ("Display over other apps"), which is a single visible toggle, and it is what
 * app-lockers on the platform actually use.
 *
 * LockActivity is kept for the case where no overlay permission has been
 * granted, so the feature degrades rather than disappearing.
 */
object LockOverlay {

    private var view: View? = null
    private val main = Handler(Looper.getMainLooper())

    fun canShow(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    /** Opens the system screen where "Display over other apps" is granted. */
    fun permissionIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}"),
        )

    /**
     * Show the lock. Returns false when there is no overlay permission, so the
     * caller can fall back to launching LockActivity instead.
     */
    fun show(
        context: Context,
        label: String,
        kind: String,
        target: String?,
        onAsk: ((String, String) -> Unit)? = null,
    ): Boolean {
        if (!canShow(context)) return false

        main.post {
            if (view != null) return@post

            val windows = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val root = LayoutInflater.from(context).inflate(R.layout.activity_lock, null)

            val isSite = kind == LockActivity.KIND_SITE
            root.findViewById<TextView>(R.id.title).text =
                if (isSite) "$label is blocked" else "$label is done for today"
            root.findViewById<TextView>(R.id.subtitle).text =
                if (isSite) {
                    "This site is not allowed on this device."
                } else {
                    "Time is up for now."
                }

            val ask = root.findViewById<Button>(R.id.askButton)
            val askResult = root.findViewById<TextView>(R.id.askResult)

            val canAsk = !isSite && target != null && onAsk != null
            ask.visibility = if (canAsk) View.VISIBLE else View.GONE
            ask.setOnClickListener {
                ask.isEnabled = false
                askResult.visibility = View.VISIBLE
                askResult.text = "Asked. You will see the extra time once it is approved."
                onAsk?.invoke(target!!, label)
            }

            root.findViewById<Button>(R.id.home).setOnClickListener {
                hide(context)
                context.startActivity(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )
            }

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                // focusable so the buttons work, but it must not become the
                // target for the hardware back key dismissing it accidentally
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT,
            ).apply { gravity = Gravity.CENTER }

            runCatching {
                windows.addView(root, params)
                view = root
            }
        }

        return true
    }

    fun hide(context: Context) {
        main.post {
            val current = view ?: return@post
            val windows = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            runCatching { windows.removeView(current) }
            view = null
        }
    }

    val isShowing: Boolean
        get() = view != null
}
