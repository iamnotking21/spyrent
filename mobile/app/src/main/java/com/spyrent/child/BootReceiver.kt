package com.spyrent.child

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Rules that stop at reboot are not rules. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!Store(context).isPaired) return
        BlockerService.start(context)
        SyncWorker.schedule(context)
    }
}
