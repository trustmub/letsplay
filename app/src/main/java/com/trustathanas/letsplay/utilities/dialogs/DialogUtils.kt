package com.trustathanas.letsplay.utilities.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    /**
     * Dialog with two buttons a title and a message
     *
     * @param context            current context
     * @param title              dialog title
     * @param message            dialog message
     * @param positiveButtonText positive button text
     * @param positiveListener   positive button listener
     * @param negativeButtonText negative button text
     * @param negativeListener   negative button listener
     * @return the AlertDialog
     */
    fun createAlertDialogWithTwoButtons(
        context: Context, title: String, message: String,
        positiveButtonText: String,
        positiveListener: DialogInterface.OnClickListener,
        negativeButtonText: String,
        negativeListener: DialogInterface.OnClickListener
    ): AlertDialog {

        // Theme_Holo_Light_Dialog

        return AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog) //AppTheme_Dialog
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, positiveListener)
            .setNegativeButton(negativeButtonText, negativeListener)
            .create()
    }
}