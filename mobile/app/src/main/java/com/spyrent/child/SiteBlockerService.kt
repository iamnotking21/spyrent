package com.spyrent.child

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Blocks browsing to domains the parent has ruled out.
 *
 * Android gives an app no way to see another app's network traffic without a
 * VPN, so the workable route is the address bar itself: browsers publish it to
 * accessibility services. That means this only covers browsers we know the id
 * of, and only what is actually shown in the bar — it is a house rule, not a
 * content filter, and the README says so.
 */
class SiteBlockerService : AccessibilityService() {

    private lateinit var store: Store
    private var lastBlockedAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = Store(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!::store.isInitialized) store = Store(this)

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in SUPPORTED_BROWSERS) return

        val blocked = store.blockedDomains()
        if (blocked.isEmpty()) return

        val url = readAddressBar(packageName) ?: return
        val host = hostOf(url) ?: return

        val match = blocked.firstOrNull { host == it || host.endsWith(".$it") } ?: return

        // one lock screen per few seconds, not one per keystroke
        val now = System.currentTimeMillis()
        if (now - lastBlockedAt < BLOCK_COOLDOWN_MS) return
        lastBlockedAt = now

        performGlobalAction(GLOBAL_ACTION_BACK)
        LockActivity.show(this, match, LockActivity.KIND_SITE)
    }

    override fun onInterrupt() = Unit

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

    private companion object {
        const val BLOCK_COOLDOWN_MS = 3_000L

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
