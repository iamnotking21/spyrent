package com.spyrent.child

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Watches the foreground app and shows the lock screen when a locked app opens.
 *
 * Also polls the policy on its own short cycle. WorkManager's fifteen-minute
 * sync is too slow for a rule to feel real-time — a parent blocking an app
 * should not have their child playing it for another quarter of an hour. This
 * service is already alive and battery-exempt (it has to be, to watch the
 * foreground app at all), so it is the cheapest place to also refresh the
 * cached rules on a much shorter interval.
 *
 * Deliberately a visible foreground service: the child is meant to know this
 * is running. Spyrent is a house rule, not a hidden tracker.
 */
class BlockerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        watch()
    }

    private fun watch() {
        val store = Store(this)
        var ticksSincePolicySync = 0

        scope.launch {
            while (isActive) {
                runCatching {
                    if (Usage.hasPermission(this@BlockerService)) {
                        val current = Usage.foregroundPackage(this@BlockerService)
                        if (current.isNotBlank() &&
                            current != packageName &&
                            store.lockedPackages().contains(current)
                        ) {
                            LockActivity.show(
                                this@BlockerService,
                                Usage.label(this@BlockerService, current),
                                LockActivity.KIND_BUDGET,
                                current,
                            )
                        }
                    }
                }

                ticksSincePolicySync++
                if (ticksSincePolicySync >= POLICY_SYNC_EVERY_N_TICKS) {
                    ticksSincePolicySync = 0
                    runCatching { PolicySync.refresh(this@BlockerService, store.api(), store) }
                }

                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Spyrent", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Shows that screen time rules are active"
                },
            )
        }

        val store = Store(this)
        val who = store.childName?.let { "Screen time rules are on for $it" } ?: "Screen time rules are on"

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Spyrent")
            .setContentText(who)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "spyrent-blocker"
        private const val NOTIFICATION_ID = 4711
        private const val CHECK_INTERVAL_MS = 2_000L

        /** 2s checks × 10 = a rule reaches the device within ~20s of being saved. */
        private const val POLICY_SYNC_EVERY_N_TICKS = 10

        fun start(context: Context) {
            val intent = Intent(context, BlockerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BlockerService::class.java))
        }
    }
}
