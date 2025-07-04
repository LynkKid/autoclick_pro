package com.auto.click.modules.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.auto.click.R

class PopupRename(context: Context, name: String) : Dialog(context) {

    private var _onDoneAction: ((name: String) -> Unit)? = null
    var onDoneAction: ((name: String) -> Unit)?
        get() = _onDoneAction
        set(value) {
            _onDoneAction = value
        }

    init {
        setContentView(R.layout.custom_rename_center_popup)
        window?.apply {
            setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        initializeViews(name)
    }

    private fun initializeViews(name: String) {
        val editConfigName = findViewById<EditText>(R.id.editConfigName)
        val ivClear = findViewById<ImageView>(R.id.ivClear)
        val btCancel = findViewById<TextView>(R.id.tvCancel)
        val btSave = findViewById<TextView>(R.id.tvSave)

        editConfigName.setText(name)

        editConfigName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val configName = s.toString()
                if (configName.isNotEmpty()) {
                    ivClear.visibility = View.VISIBLE
                } else {
                    ivClear.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Xử lý nếu cần trước khi văn bản thay đổi
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Xử lý nếu cần khi văn bản đang thay đổi
            }
        })

        ivClear.setOnClickListener {
            editConfigName.setText("")
        }

        btCancel.setOnClickListener {
            dismiss()
        }

        btSave.setOnClickListener {
            _onDoneAction?.invoke(editConfigName.text.toString())
            dismiss()
        }
    }

    fun showPopup() {
        show()
    }
}
