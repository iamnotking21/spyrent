package com.spyrent.child

import android.content.Context
import android.content.SharedPreferences

/** Small wrapper over SharedPreferences so the rest of the app never sees string keys. */
class Store(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("spyrent", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, BuildConfig.DEFAULT_BASE_URL)!!
        set(value) = prefs.edit().putString(KEY_BASE_URL, value.trimEnd('/')).apply()

    var deviceToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var childName: String?
        get() = prefs.getString(KEY_CHILD, null)
        set(value) = prefs.edit().putString(KEY_CHILD, value).apply()

    /** Millisecond timestamp of the last usage window we reported. */
    var lastReportedAt: Long
        get() = prefs.getLong(KEY_LAST_REPORT, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_REPORT, value).apply()

    val isPaired: Boolean
        get() = !deviceToken.isNullOrBlank()

    fun clear() = prefs.edit().clear().apply()

    /** Cached policy so blocking keeps working while offline. */
    fun saveLockedPackages(packages: Set<String>) =
        prefs.edit().putStringSet(KEY_LOCKED, packages).apply()

    fun lockedPackages(): Set<String> = prefs.getStringSet(KEY_LOCKED, emptySet()) ?: emptySet()

    fun saveBlockedDomains(domains: Set<String>) =
        prefs.edit().putStringSet(KEY_DOMAINS, domains).apply()

    fun blockedDomains(): Set<String> = prefs.getStringSet(KEY_DOMAINS, emptySet()) ?: emptySet()

    /**
     * Seconds remaining today for domains that carry a time budget rather than
     * an outright block. Reset from the server on every policy sync; ticked
     * down locally in between by whatever is watching the browser.
     */
    fun saveSiteBudgets(remainingSeconds: Map<String, Int>) {
        val encoded = remainingSeconds.map { "${it.key}|${it.value}" }.toSet()
        prefs.edit().putStringSet(KEY_SITE_BUDGETS, encoded).apply()
    }

    fun siteBudgets(): Map<String, Int> {
        val raw = prefs.getStringSet(KEY_SITE_BUDGETS, emptySet()) ?: emptySet()
        return raw.mapNotNull { entry ->
            val i = entry.lastIndexOf('|')
            if (i < 0) return@mapNotNull null
            val seconds = entry.substring(i + 1).toIntOrNull() ?: return@mapNotNull null
            entry.substring(0, i) to seconds
        }.toMap()
    }

    /** Ticks one domain's local budget down; returns the new remaining seconds, or null if untracked. */
    fun decrementSiteBudget(domain: String, bySeconds: Int): Int? {
        val current = siteBudgets().toMutableMap()
        val remaining = current[domain] ?: return null
        val next = (remaining - bySeconds).coerceAtLeast(0)
        current[domain] = next
        saveSiteBudgets(current)
        return next
    }

    fun api() = Api(baseUrl, deviceToken)

    private companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_TOKEN = "device_token"
        const val KEY_CHILD = "child_name"
        const val KEY_LAST_REPORT = "last_reported_at"
        const val KEY_LOCKED = "locked_packages"
        const val KEY_DOMAINS = "blocked_domains"
        const val KEY_SITE_BUDGETS = "site_budgets"
    }
}
