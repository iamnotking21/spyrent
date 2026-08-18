package com.spyrent.child

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
import java.io.File

/** Reads what the child actually used, via the platform's own usage stats. */
object Usage {

    /** The permission is granted in Settings, not through a runtime dialog. */
    fun hasPermission(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Foreground time per package between two timestamps, in whole minutes. */
    fun minutesSince(context: Context, since: Long, until: Long): Map<String, Int> {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, since, until)
            ?: return emptyMap()

        return stats
            .filter { it.totalTimeInForeground > 0 }
            .groupBy { it.packageName }
            .mapValues { (_, entries) ->
                (entries.sumOf { it.totalTimeInForeground } / 60_000L).toInt()
            }
            .filterValues { it > 0 }
    }

    /**
     * Which app is in front right now.
     *
     * queryUsageStats buckets by day and its lastTimeUsed lags by minutes, which
     * is useless for blocking. The event stream reports each MOVE_TO_FOREGROUND
     * as it happens, so walk that instead and take the most recent one.
     */
    fun foregroundPackage(context: Context): String {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = manager.queryEvents(now - 10_000, now)
        val event = android.app.usage.UsageEvents.Event()

        var latestPackage = ""
        var latestAt = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val isForeground = event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
            if (isForeground && event.timeStamp >= latestAt) {
                latestAt = event.timeStamp
                latestPackage = event.packageName
            }
        }
        return latestPackage
    }

    /** Launchable apps only — the parent does not want to scroll through system services. */
    fun installedApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.packageName == "com.android.chrome" }
            .map {
                InstalledApp(
                    packageName = it.packageName,
                    label = pm.getApplicationLabel(it).toString(),
                    sizeBytes = runCatching { File(it.sourceDir).length() }.getOrDefault(0L),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun label(context: Context, packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)
}
