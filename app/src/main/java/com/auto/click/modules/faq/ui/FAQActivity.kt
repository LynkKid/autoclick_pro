package com.auto.click.modules.faq.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.R
import com.auto.click.databinding.ActivityFaqactivityBinding

class FAQActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaqactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = getString(R.string.text_troubleshooting)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableTitle

        binding.lineFQA1.setOnClickListener {
            binding.lineFQA11.visibility =
                if (binding.lineFQA11.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        binding.lineFQA2.setOnClickListener {
            binding.lineFQA21.visibility =
                if (binding.lineFQA21.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        binding.lineFQA3.setOnClickListener {
            binding.lineFQA31.visibility =
                if (binding.lineFQA31.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        binding.lineFQA4.setOnClickListener {
            binding.lineFQA41.visibility =
                if (binding.lineFQA41.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        binding.lineFQA5.setOnClickListener {
            binding.lineFQA51.visibility =
                if (binding.lineFQA51.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}