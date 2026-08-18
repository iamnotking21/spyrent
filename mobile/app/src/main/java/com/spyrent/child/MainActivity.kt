package com.spyrent.child

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spyrent.child.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * The child-facing screen. Shows exactly what is happening and why —
 * pairing, the two permissions we need, and today's remaining time.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        store = Store(this)

        binding.serverUrl.setText(store.baseUrl)

        binding.pairButton.setOnClickListener { pair() }
        binding.usageButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.sitesButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.unpairButton.setOnClickListener { unpair() }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val paired = store.isPaired

        binding.pairingGroup.visibility = if (paired) android.view.View.GONE else android.view.View.VISIBLE
        binding.pairedGroup.visibility = if (paired) android.view.View.VISIBLE else android.view.View.GONE

        binding.status.text = when {
            !paired -> "Not connected yet"
            !Usage.hasPermission(this) -> "Connected as ${store.childName ?: "this device"} — one step left"
            else -> "Connected as ${store.childName ?: "this device"}"
        }

        val needsSites = paired && store.blockedDomains().isNotEmpty() && !siteBlockingOn()
        binding.sitesButton.visibility = if (needsSites) android.view.View.VISIBLE else android.view.View.GONE
        binding.sitesHint.visibility = if (needsSites) android.view.View.VISIBLE else android.view.View.GONE

        val needsUsage = !Usage.hasPermission(this)
        binding.usageButton.visibility = if (needsUsage) android.view.View.VISIBLE else android.view.View.GONE
        binding.usageHint.visibility = if (needsUsage) android.view.View.VISIBLE else android.view.View.GONE

        if (paired) {
            // the service dies with the app on a force-stop; bring it back whenever
            // the child opens the screen, not only at pairing time
            if (Usage.hasPermission(this)) {
                BlockerService.start(this)
                SyncWorker.schedule(this)
                SyncWorker.runOnce(this)
            }
            refreshPolicy()
        }
    }

    /** Accessibility services are enabled in Settings; this reads that list. */
    private fun siteBlockingOn(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()

        // must match our own service, not merely any accessibility service
        val ours = "$packageName/${SiteBlockerService::class.java.name}"
        val oursShort = "$packageName/.${SiteBlockerService::class.java.simpleName}"
        return enabled.split(':').any { it == ours || it == oursShort }
    }

    private fun pair() {
        val token = binding.tokenInput.text.toString().trim()
        if (token.isEmpty()) {
            binding.status.text = "Type the code from your parent's dashboard"
            return
        }

        store.baseUrl = binding.serverUrl.text.toString().trim()
        binding.pairButton.isEnabled = false
        binding.status.text = "Connecting…"

        lifecycleScope.launch {
            runCatching {
                val name = Api(store.baseUrl, null).pair(token, android.os.Build.MODEL)
                store.deviceToken = token
                store.childName = name
                name
            }.onSuccess { name ->
                binding.status.text = "Connected as $name"
                uploadInventory()
                BlockerService.start(this@MainActivity)
                SyncWorker.schedule(this@MainActivity)
                render()
            }.onFailure { error ->
                binding.status.text = error.message ?: "That code did not work"
            }
            binding.pairButton.isEnabled = true
        }
    }

    private fun unpair() {
        BlockerService.stop(this)
        SyncWorker.cancel(this)
        store.clear()
        render()
    }

    /** A first pass at pairing time; SyncWorker keeps it current after that. */
    private fun uploadInventory() {
        lifecycleScope.launch {
            runCatching {
                val installed = Usage.installedApps(this@MainActivity)
                val needIcons = store.api().uploadApps(installed)

                val withIcons = installed
                    .filter { needIcons.contains(it.packageName) }
                    .map { it.copy(icon = Icons.dataUri(this@MainActivity, it.packageName)) }
                    .filter { it.icon != null }

                if (withIcons.isNotEmpty()) store.api().uploadApps(withIcons, complete = false)
            }
        }
    }

    private fun refreshPolicy() {
        lifecycleScope.launch {
            runCatching { store.api().fetchPolicy() }
                .onSuccess { policy ->
                    store.saveLockedPackages(
                        policy.apps.filter { it.locked }.map { it.packageName }.toSet(),
                    )
                    store.saveBlockedDomains(
                        policy.sites.filter { it.blocked }.map { it.domain }.toSet(),
                    )
                    binding.rules.text = summarise(policy)
                }
                .onFailure { binding.rules.text = "Showing the last rules we saw — no connection right now." }
        }
    }

    private fun summarise(policy: Policy): String {
        if (policy.apps.isEmpty() && policy.sites.isEmpty()) {
            return "No limits set yet. Enjoy your day."
        }

        val lines = mutableListOf<String>()
        policy.apps.forEach { rule ->
            // prefer the name the device knows; fall back to what the parent named it
            val installed = Usage.label(this, rule.packageName)
            val label = if (installed == rule.packageName) rule.label else installed
            lines += when {
                rule.blocked -> "$label — not allowed"
                rule.dailyMinutes == null -> "$label — no limit"
                rule.locked -> "$label — done for today"
                else -> "$label — ${rule.remainingMinutes} min left"
            }
        }
        policy.sites.filter { it.blocked }.forEach { lines += "${it.domain} — blocked" }
        return lines.joinToString("\n")
    }
}
