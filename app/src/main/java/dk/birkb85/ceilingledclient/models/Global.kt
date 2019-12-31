package dk.birkb85.ceilingledclient.models

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Button
import android.widget.TextView
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.interfaces.AlertDialogButtonOnClickListener
import dk.birkb85.ceilingledclient.models.Global.Companion.tcpClient


/**
 * Global methods.
 */
class Global : Application() {
    companion object {
        /**
         * Preference file key for saving to local preferences.
         */
        fun getPreferenceFileKey(): String = "SharedPreferences"

        var tcpClient: TcpClient? = null
        val connectTask = ConnectTask()
    }

    /**
     * Global class initialised when application is launched.
     * Initialise global accessible variables here.
     */
    override fun onCreate() {
        super.onCreate()

        // Test connection to TCP server.
//        connectTask.execute("")

        // Send the message to the server
//        tcpClient?.sendMessage("testing")

        // Stop connection.
//        tcpClient?.stopClient()
    }

    /**
     * Default dialog showing an message.
     * @param context context of activity.
     * @param message message to display.
     * @param onClickListener callback when clicking on button.
     * @return alert dialog.
     */
    fun getMessageAlertDialog(
        context: Context,
        message: String,
        onClickListener: AlertDialogButtonOnClickListener?
    ): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(context)

        val inflater = (context as Activity).layoutInflater
        val inflaterView = inflater.inflate(R.layout.dialog_text, null)
        alertDialogBuilder.setView(inflaterView)
        alertDialogBuilder.setCancelable(false)

        val alertDialog = alertDialogBuilder.create()

        val textView = inflaterView.findViewById(R.id.textView) as TextView?
        val okButton = inflaterView.findViewById(R.id.okButton) as Button?

        textView?.text = message

        okButton?.setOnClickListener {
            alertDialog.dismiss()
            onClickListener?.onClick()
        }

        return alertDialog
    }

    /**
     * Default dialog showing an message, asking for confirmation.
     * @param context context of activity.
     * @param message message to display.
     * @param okOnClickListener callback when confirming action.
     * @param cancelOnClickListener callback when canceling action.
     * @return alert dialog.
     */
    fun getConfirmAlertDialog(
        context: Context,
        message: String,
        okOnClickListener: AlertDialogButtonOnClickListener?,
        cancelOnClickListener: AlertDialogButtonOnClickListener?
    ): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(context)

        val inflater = (context as Activity).layoutInflater
        val inflaterView = inflater.inflate(R.layout.dialog_confirm, null)
        alertDialogBuilder.setView(inflaterView)
        alertDialogBuilder.setCancelable(false)

        val alertDialog = alertDialogBuilder.create()

        val textView = inflaterView.findViewById(R.id.textView) as TextView?
        val cancelButton = inflaterView.findViewById(R.id.cancelButton) as Button?
        val okButton = inflaterView.findViewById(R.id.okButton) as Button?

        textView?.text = message

        cancelButton?.setOnClickListener {
            alertDialog.dismiss()
            cancelOnClickListener?.onClick()
        }

        okButton?.setOnClickListener {
            alertDialog.dismiss()
            okOnClickListener?.onClick()
        }

        return alertDialog
    }
}