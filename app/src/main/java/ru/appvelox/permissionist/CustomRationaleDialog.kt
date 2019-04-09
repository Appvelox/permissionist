package ru.appvelox.permissionist

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.custom_rationale_dialog.view.*

class CustomRationaleDialog: DialogFragment(), Permissionist.CustomRationale {

    private var onProceedListener: Permissionist.CustomRationale.OnProceedListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.custom_rationale_dialog, null)

        builder.setView(view)

        view.buttonOk.setOnClickListener {
            onProceedListener?.onProceed()
            dismiss()
        }

        view.buttonCancel.setOnClickListener {
            dismiss()
        }

        val dialog = builder.create()

        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    override fun setOnProceedListener(onProceedListener: Permissionist.CustomRationale.OnProceedListener) {
        this.onProceedListener = onProceedListener
    }

    override fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, "")
    }
}