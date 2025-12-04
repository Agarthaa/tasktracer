package com.example.tasktracker.presentation.auth.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.tasktracker.R

class ResetPasswordDialog(
    private val onSubmit: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reset_password, null)

        val etEmail = view.findViewById<EditText>(R.id.et_reset_email)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        // Use Material 3 themed dialog
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_TaskTracker_Dialog)
            .setView(view)
            .create()

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                onSubmit(email)
                dialog.dismiss()
            } else {
                etEmail.error = "Email cannot be empty"
            }
        }

        return dialog
    }
}
