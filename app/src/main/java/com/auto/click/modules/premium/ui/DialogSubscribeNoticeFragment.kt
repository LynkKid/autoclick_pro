package com.auto.click.modules.premium.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.auto.click.R

class DialogSubscribeNoticeFragment : DialogFragment() {

    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.let { dialog ->
            dialog.window?.let { window ->
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            dialog.setOnKeyListener { _, keyCode, event ->
                false
            }
        }

        return inflater.inflate(R.layout.dialog_sub_notice_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvOk)?.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, theme)
    }

    companion object {
        private const val TAG = "dialog_sub_notice"

        fun show(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager

            if (fragmentManager.findFragmentByTag(TAG) == null) {
                val dialog = DialogSubscribeNoticeFragment()
                dialog.show(fragmentManager, TAG)
            }
        }

        fun show(
            activity: FragmentActivity,
            onDismiss: (() -> Unit)? = null
        ) {
            val fragmentManager = activity.supportFragmentManager

            if (fragmentManager.findFragmentByTag(TAG) == null) {
                val dialog = DialogSubscribeNoticeFragment()
                onDismiss?.let { callback ->
                    dialog.onDismissCallback = callback
                }

                dialog.show(fragmentManager, TAG)
            }
        }
    }
} 