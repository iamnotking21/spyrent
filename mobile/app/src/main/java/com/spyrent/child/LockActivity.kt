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

        val appLabel = intent.getStringExtra(EXTRA_LABEL) ?: "That app"
        binding.title.text = "$appLabel is done for today"

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

        fun show(context: Context, appLabel: String) {
            context.startActivity(
                Intent(context, LockActivity::class.java).apply {
                    putExtra(EXTRA_LABEL, appLabel)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
                },
            )
        }
    }
}
