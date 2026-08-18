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

            val policy = api.fetchPolicy()
            store.childName = policy.childName
            store.saveLockedPackages(policy.apps.filter { it.locked }.map { it.packageName }.toSet())
            store.saveBlockedDomains(policy.sites.filter { it.blocked }.map { it.domain }.toSet())

            api.heartbeat()
            Result.success()
        } catch (e: ApiException) {
            Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val NAME = "spyrent-sync"

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
