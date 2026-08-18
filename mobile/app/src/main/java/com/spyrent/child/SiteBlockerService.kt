package com.spyrent.child

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Blocks browsing to domains the parent has ruled out — outright blocks and
 * time budgets alike.
 *
 * Android gives an app no way to see another app's network traffic without a
 * VPN, so the workable route is the address bar itself: browsers publish it to
 * accessibility services. That means this only covers browsers we know the id
 * of, and only what is actually shown in the bar — it is a house rule, not a
 * content filter, and the README says so.
 *
 * Two mechanisms run side by side:
 *  - onAccessibilityEvent fires on every window change, so an outright block
 *    is instant.
 *  - a coroutine ticks every few seconds to time how long a budgeted domain
 *    has been open, since accessibility events do not fire on a timer and a
 *    parent who set "2 minutes on youtube.com" needs that actually counted.
 */
class SiteBlockerService : AccessibilityService() {

    private lateinit var store: Store
    private var lastBlockedAt = 0L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Seconds spent on each budgeted domain since the last report to the server. */
    private val unreportedSeconds = mutableMapOf<String, Int>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = Store(this)
        watchBudgets()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!::store.isInitialized) store = Store(this)

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in SUPPORTED_BROWSERS) return

        val blocked = store.blockedDomains()
        if (blocked.isEmpty()) return

        val host = currentHost(packageName) ?: return
        val match = blocked.firstOrNull { host == it || host.endsWith(".$it") } ?: return

        block(match)
    }

    override fun onInterrupt() = Unit

    /**
     * Runs independently of accessibility events: those fire on content
     * change, which tells us nothing about how long a page has stayed open.
     */
    private fun watchBudgets() {
        scope.launch {
            var ticksSinceReport = 0

            while (isActive) {
                runCatching { tick() }

                ticksSinceReport++
                if (ticksSinceReport >= REPORT_EVERY_N_TICKS) {
                    ticksSinceReport = 0
                    runCatching { flushUsage() }
                }

                delay(TICK_MS)
            }
        }
    }

    private fun tick() {
        val budgets = store.siteBudgets()
        if (budgets.isEmpty()) return

        val packageName = rootInActiveWindow?.packageName?.toString()
        if (packageName == null || packageName !in SUPPORTED_BROWSERS) return

        val host = currentHost(packageName) ?: return
        val match = budgets.keys.firstOrNull { host == it || host.endsWith(".$it") } ?: return

        unreportedSeconds[match] = (unreportedSeconds[match] ?: 0) + TICK_SECONDS

        val remaining = store.decrementSiteBudget(match, TICK_SECONDS) ?: return
        if (remaining <= 0) block(match)
    }

    /** Send accumulated seconds to the server as whole minutes, keeping the remainder. */
    private suspend fun flushUsage() {
        if (unreportedSeconds.isEmpty()) return

        val events = unreportedSeconds.mapNotNull { (domain, seconds) ->
            val minutes = seconds / 60
            if (minutes <= 0) return@mapNotNull null
            unreportedSeconds[domain] = seconds % 60
            SiteUsageEvent(domain, minutes)
        }

        if (events.isNotEmpty()) store.api().reportSiteUsage(events)
    }

    private fun currentHost(browserPackage: String): String? {
        val url = readAddressBar(browserPackage) ?: return null
        return hostOf(url)
    }

    private fun block(domain: String) {
        // one lock screen per few seconds, not one per keystroke or per tick
        val now = System.currentTimeMillis()
        if (now - lastBlockedAt < BLOCK_COOLDOWN_MS) return
        lastBlockedAt = now

        performGlobalAction(GLOBAL_ACTION_BACK)
        LockActivity.show(this, domain, LockActivity.KIND_SITE)
    }

    private fun readAddressBar(browser: String): String? {
        val root = rootInActiveWindow ?: return null
        return try {
            ADDRESS_BAR_IDS
                .map { "$browser:id/$it" }
                .asSequence()
                .mapNotNull { id ->
                    root.findAccessibilityNodeInfosByViewId(id)
                        ?.firstOrNull()
                        ?.text
                        ?.toString()
                        ?.takeIf { it.isNotBlank() }
                }
                .firstOrNull()
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    /** The bar shows "example.com/page" as often as a full URL, so parse both. */
    private fun hostOf(raw: String): String? {
        val trimmed = raw.trim().lowercase()
        if (trimmed.isEmpty() || trimmed.contains(' ')) return null

        val withScheme = if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
        return runCatching {
            java.net.URI(withScheme).host?.removePrefix("www.")
        }.getOrNull()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val BLOCK_COOLDOWN_MS = 3_000L
        const val TICK_MS = 5_000L
        const val TICK_SECONDS = (TICK_MS / 1000).toInt()

        /** Report accumulated site time roughly once a minute. */
        const val REPORT_EVERY_N_TICKS = 12

        val SUPPORTED_BROWSERS = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.brave.browser",
            "com.sec.android.app.sbrowser",
            "com.duckduckgo.mobile.android",
        )

        val ADDRESS_BAR_IDS = listOf("url_bar", "mozac_browser_toolbar_url_view", "url_field")
    }
}
