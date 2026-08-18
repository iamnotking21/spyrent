package com.spyrent.child

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Shows the lock by whichever route the device actually permits.
 *
 * An overlay window is tried first: starting an Activity from a background
 * service is silently refused on some devices (MIUI most notably), which made
 * every block fail there while working fine on an emulator. The Activity is
 * kept as the fallback for devices without overlay permission granted yet.
 */
object Lock {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun show(context: Context, label: String, kind: String, target: String? = null) {
        val shown = LockOverlay.show(context, label, kind, target) { pkg, name ->
            askForMoreTime(context, pkg, name)
        }

        if (!shown) LockActivity.show(context, label, kind, target)
    }

    private fun askForMoreTime(context: Context, packageName: String, label: String) {
        val store = Store(context)
        scope.launch {
            runCatching { store.api().requestMoreTime(packageName, label, ASK_MINUTES) }
        }
    }

    private const val ASK_MINUTES = 15
}
