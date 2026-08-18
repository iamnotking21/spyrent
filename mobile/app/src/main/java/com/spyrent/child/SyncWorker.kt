package com.spyrent.child

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Reports usage, refreshes the policy, and caches which packages are locked so
 * the blocker keeps working without a network.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val store = Store(applicationContext)
        if (!store.isPaired) return Result.success()

        val api = store.api()

        return try {
            if (Usage.hasPermission(applicationContext)) {
                val now = System.currentTimeMillis()
                val since = store.lastReportedAt.takeIf { it > 0 } ?: (now - TimeUnit.MINUTES.toMillis(20))

                val events = Usage.minutesSince(applicationContext, since, now)
                    // our own screen time is not the child's screen time
                    .filterKeys { it != applicationContext.packageName }
                    .map { (pkg, minutes) ->
                        UsageEvent(pkg, Usage.label(applicationContext, pkg), minutes, blocked = false)
                    }

                api.reportUsage(events)
                store.lastReportedAt = now
            }

            syncInventory(api)
            PolicySync.refresh(applicationContext, api, store)
            api.heartbeat()
            Result.success()
        } catch (e: ApiException) {
            Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /**
     * Keep the parent's app list current.
     *
     * Without this the inventory was only uploaded at pairing, so anything
     * installed afterwards never reached the portal unless the child happened
     * to open Spyrent — which is exactly when a parent most wants to see it.
     */
    private suspend fun syncInventory(api: Api) {
        val installed = Usage.installedApps(applicationContext)
        if (installed.isEmpty()) return

        val needIcons = api.uploadApps(installed, complete = true)
        if (needIcons.isEmpty()) return

        // second pass carrying only the icons the server asked for, a slice at
        // a time so a fresh device does not push fifty of them at once
        val withIcons = installed
            .filter { needIcons.contains(it.packageName) }
            .take(MAX_ICONS_PER_SYNC)
            .map { it.copy(icon = Icons.dataUri(applicationContext, it.packageName)) }
            .filter { it.icon != null }

        if (withIcons.isNotEmpty()) api.uploadApps(withIcons, complete = false)
    }

    companion object {
        private const val NAME = "spyrent-sync"

        /** Icons are a few kB each; spread a big first sync over a few rounds. */
        private const val MAX_ICONS_PER_SYNC = 15

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        /** Run one sync straight away, e.g. when the child opens the app. */
        fun runOnce(context: Context) {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NAME)
        }
    }
}
