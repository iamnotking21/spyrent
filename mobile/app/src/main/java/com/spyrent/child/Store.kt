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

    fun api() = Api(baseUrl, deviceToken)

    private companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_TOKEN = "device_token"
        const val KEY_CHILD = "child_name"
        const val KEY_LAST_REPORT = "last_reported_at"
        const val KEY_LOCKED = "locked_packages"
    }
}
