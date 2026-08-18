package com.spyrent.child

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.spyrent.child.databinding.ActivityLockBinding

/** Full-screen, friendly stop sign. Shown when a locked app is opened. */
class LockActivity : Activity() {

    private lateinit var binding: ActivityLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val label = intent.getStringExtra(EXTRA_LABEL) ?: "That app"

        if (intent.getStringExtra(EXTRA_KIND) == KIND_SITE) {
            binding.title.text = "$label is blocked"
            binding.subtitle.text = "This site is not allowed on this device."
        } else {
            binding.title.text = "$label is done for today"
            binding.subtitle.text = "Time is up for now. Ask at home if you need more."
        }

        binding.home.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
            finish()
        }
    }

    /** Back button must not reveal the app underneath. */
    override fun onBackPressed() {
        binding.home.performClick()
    }

    companion object {
        private const val EXTRA_LABEL = "label"
        private const val EXTRA_KIND = "kind"
        const val KIND_BUDGET = "budget"
        const val KIND_SITE = "site"

        fun show(context: Context, appLabel: String, kind: String = KIND_BUDGET) {
            context.startActivity(
                Intent(context, LockActivity::class.java).apply {
                    putExtra(EXTRA_LABEL, appLabel)
                    putExtra(EXTRA_KIND, kind)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
                },
            )
        }
    }
}
