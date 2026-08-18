package com.spyrent.child

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.spyrent.child.databinding.ActivityLockBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Full-screen, friendly stop sign. Shown when a locked app or site is opened. */
class LockActivity : Activity() {

    private lateinit var binding: ActivityLockBinding
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val label = intent.getStringExtra(EXTRA_LABEL) ?: "That app"
        val target = intent.getStringExtra(EXTRA_TARGET)
        val isSite = intent.getStringExtra(EXTRA_KIND) == KIND_SITE

        if (isSite) {
            binding.title.text = "$label is blocked"
            binding.subtitle.text = "This site is not allowed on this device."
        } else {
            binding.title.text = "$label is done for today"
            binding.subtitle.text = "Time is up for now."
        }

        // asking only makes sense for a spent budget — a blocked site is a
        // standing decision, not a countdown
        val store = Store(this)
        val canAsk = !isSite && target != null && store.isPaired
        binding.askButton.visibility = if (canAsk) View.VISIBLE else View.GONE

        binding.askButton.setOnClickListener {
            binding.askButton.isEnabled = false
            askForMore(target!!, label)
        }

        binding.home.setOnClickListener { goHome() }
    }

    private fun askForMore(target: String, label: String) {
        val store = Store(this)
        scope.launch {
            val sent = withContext(Dispatchers.IO) {
                runCatching { store.api().requestMoreTime(target, label, ASK_MINUTES) }.isSuccess
            }

            binding.askResult.visibility = View.VISIBLE
            binding.askResult.text = if (sent) {
                binding.askButton.visibility = View.GONE
                "Asked. You will see the extra time once it is approved."
            } else {
                binding.askButton.isEnabled = true
                "Could not send that just now. Try again in a moment."
            }
        }
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
        finish()
    }

    /** Back button must not reveal the app underneath. */
    override fun onBackPressed() {
        goHome()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_LABEL = "label"
        private const val EXTRA_KIND = "kind"
        private const val EXTRA_TARGET = "target"
        private const val ASK_MINUTES = 15

        const val KIND_BUDGET = "budget"
        const val KIND_SITE = "site"

        fun show(
            context: Context,
            label: String,
            kind: String = KIND_BUDGET,
            target: String? = null,
        ) {
            context.startActivity(
                Intent(context, LockActivity::class.java).apply {
                    putExtra(EXTRA_LABEL, label)
                    putExtra(EXTRA_KIND, kind)
                    putExtra(EXTRA_TARGET, target)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
                },
            )
        }
    }
}
