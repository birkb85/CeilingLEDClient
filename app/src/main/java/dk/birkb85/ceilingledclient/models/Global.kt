package dk.birkb85.ceilingledclient.models

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.widget.Button
import android.widget.TextView
import dk.birkb85.ceilingledclient.R


/**
 * Global methods.
 */
class Global : Application() {
    companion object {
        /**
         * Preference file key for saving to local preferences.
         */
        const val preferenceFileKey: String = "SharedPreferences"

        /**
         * Global TCPConnection controlling TCP connection to Arduino.
         */
        val tcpConnection: TCPConnection = TCPConnection()
    }

    /**
     * Global class initialised when application is launched.
     * Initialise global accessible variables here.
     */
    override fun onCreate() {
        super.onCreate()

        val sharedPref = getSharedPreferences(preferenceFileKey, Context.MODE_PRIVATE)
        val connectionIP = sharedPref.getString("connection_ip", "")
        val connectionPort = sharedPref.getInt("connection_port", 0)
        if (connectionIP != null && connectionIP != "")
            tcpConnection.startClient(connectionIP, connectionPort)
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

    /**
     * On click listener callback.
     */
    interface AlertDialogButtonOnClickListener {
        /**
         * On click listener callback.
         */
        fun onClick()
    }
}