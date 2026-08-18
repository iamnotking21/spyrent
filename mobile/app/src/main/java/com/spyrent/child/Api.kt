package com.spyrent.child

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/** One app rule as the server sees it. */
data class AppRule(
    val packageName: String,
    val label: String,
    val dailyMinutes: Int?,
    val usedMinutes: Int,
    val blocked: Boolean,
) {
    /** True when the child may not open this app right now. */
    val locked: Boolean
        get() = blocked || (dailyMinutes != null && usedMinutes >= dailyMinutes)

    val remainingMinutes: Int?
        get() = dailyMinutes?.let { (it - usedMinutes).coerceAtLeast(0) }
}

data class SiteRule(val domain: String, val label: String, val dailyMinutes: Int?, val blocked: Boolean)

data class Policy(val childName: String, val apps: List<AppRule>, val sites: List<SiteRule>)

class ApiException(message: String) : Exception(message)

/**
 * Talks to the Spyrent web app. Replaces the old PHP endpoints under res_api:
 * one bearer token, one JSON envelope, no per-screen PHP file.
 */
class Api(private val baseUrl: String, private val deviceToken: String?) {

    private suspend fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        authed: Boolean = true,
    ): JSONObject = withContext(Dispatchers.IO) {
        val conn = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            if (authed) {
                val token = deviceToken ?: throw ApiException("device is not paired")
                setRequestProperty("Authorization", "Bearer $token")
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        try {
            body?.let { conn.outputStream.use { out -> out.write(it.toString().toByteArray()) } }

            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()

            if (text.isBlank()) throw ApiException("empty response from $path")

            val json = JSONObject(text)
            if (!json.optBoolean("ok", false)) {
                throw ApiException(json.optString("error", "request failed (${conn.responseCode})"))
            }
            json
        } finally {
            conn.disconnect()
        }
    }

    /** Swap the pairing token typed by the parent for a child profile. */
    suspend fun pair(token: String, deviceModel: String): String {
        val body = JSONObject()
            .put("token", token)
            .put("deviceModel", deviceModel)
            // so the server rolls budgets over at midnight here, not in UTC
            .put("timezone", java.util.TimeZone.getDefault().id)
        val res = request("/api/v1/pair", "POST", body, authed = false)
        return res.getJSONObject("child").getString("name")
    }

    suspend fun heartbeat() {
        request("/api/v1/heartbeat", "POST", JSONObject())
    }

    suspend fun fetchPolicy(): Policy {
        val res = request("/api/v1/policy")

        val apps = res.getJSONArray("apps").mapObjects {
            AppRule(
                packageName = it.getString("packageName"),
                label = it.optString("label").ifBlank { it.getString("packageName") },
                dailyMinutes = if (it.isNull("dailyMinutes")) null else it.getInt("dailyMinutes"),
                usedMinutes = it.optInt("usedMinutes", 0),
                blocked = it.optBoolean("blocked", false),
            )
        }

        val sites = res.getJSONArray("sites").mapObjects {
            SiteRule(
                domain = it.getString("domain"),
                label = it.optString("label").ifBlank { it.getString("domain") },
                dailyMinutes = if (it.isNull("dailyMinutes")) null else it.getInt("dailyMinutes"),
                blocked = it.optBoolean("blocked", false),
            )
        }

        return Policy(res.getJSONObject("child").getString("name"), apps, sites)
    }

    /** Upload the installed app inventory so the parent can pick from real names. */
    suspend fun uploadApps(apps: List<InstalledApp>) {
        if (apps.isEmpty()) return
        val array = JSONArray()
        apps.forEach {
            array.put(
                JSONObject()
                    .put("packageName", it.packageName)
                    .put("label", it.label)
                    .put("sizeBytes", it.sizeBytes),
            )
        }
        request("/api/v1/apps", "POST", JSONObject().put("apps", array))
    }

    /** Report usage. The server adds these minutes to the matching rule budget. */
    suspend fun reportUsage(events: List<UsageEvent>) {
        if (events.isEmpty()) return
        val array = JSONArray()
        events.forEach {
            array.put(
                JSONObject()
                    .put("kind", "app")
                    .put("target", it.packageName)
                    .put("label", it.label)
                    .put("minutes", it.minutes)
                    .put("blocked", it.blocked),
            )
        }
        request("/api/v1/events", "POST", JSONObject().put("events", array))
    }
}

data class InstalledApp(val packageName: String, val label: String, val sizeBytes: Long)
data class UsageEvent(val packageName: String, val label: String, val minutes: Int, val blocked: Boolean)

private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { transform(getJSONObject(it)) }
