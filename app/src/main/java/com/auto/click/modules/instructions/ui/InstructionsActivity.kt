package com.auto.click.modules.instructions.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import com.auto.click.R
import com.auto.click.databinding.ActivityInstructionsBinding

class InstructionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInstructionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.inToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = getString(R.string.text_novice_gude)
        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(
            StyleSpan(Typeface.BOLD),  // Kiểu chữ đậm
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        supportActionBar?.title = spannableTitle
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}