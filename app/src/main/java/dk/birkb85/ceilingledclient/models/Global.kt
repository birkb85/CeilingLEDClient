package dk.birkb85.ceilingledclient.models

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
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

        /**
         * Data values to be sent to server.
         */
        // LEDServer.ino
        const val MODE_BOOTING = "0"
        const val MODE_MAIN = "1"
        const val MODE_PONG = "2"

        const val DATA_UNDEFINED = "0"
        const val DATA_HEART_BEAT = "1"
        const val DATA_MESSAGE = "2"
        const val DATA_SET_BRIGHTNESS = "3"
        const val DATA_SET_LOOP_INTERVAL = "4"
        const val DATA_SET_STRIP_CLEAR = "5"
        const val DATA_SET_MODE = "6"
        const val DATA_MAIN = "7"
        const val DATA_PONG = "8"

        // Main.h
        const val MODE_MAIN_BLINK = "0"
        const val MODE_MAIN_WIPE = "1"
        const val MODE_MAIN_THEATER_CHASE = "2"
        const val MODE_MAIN_RAINBOW = "3"
        const val MODE_MAIN_THEATER_CHASE_RAINBOW = "4"
        const val MODE_MAIN_COMPASS = "5"

        const val DATA_MAIN_UNDEFINED = "0"
        const val DATA_MAIN_SET_MODE = "1"
        const val DATA_MAIN_COLOR = "2"
        const val DATA_MAIN_X = "3"

        // Pong.h
        const val DATA_PONG_P1_PRESSED = "0"
        const val DATA_PONG_P1_RELEASED = "1"
        const val DATA_PONG_P2_PRESSED = "2"
        const val DATA_PONG_P2_RELEASED = "3"
    }

    /**
     * Global class initialised when application is launched.
     * Initialise global accessible variables here.
     */
    override fun onCreate() {
        super.onCreate()

//        val sharedPref = getSharedPreferences(preferenceFileKey, Context.MODE_PRIVATE)
//        val connectionIP = sharedPref.getString("connection_ip", "")
//        val connectionPort = sharedPref.getInt("connection_port", 0)
//        if (connectionIP != null && connectionIP != "" && connectionPort != 0)
//            tcpConnection.startClient(connectionIP, connectionPort)
    }

    /**
     * Default dialog showing an message.
     * @param activity activity.
     * @param message message to display.
     * @param onClickListener callback when clicking on button.
     * @return alert dialog.
     */
    fun getMessageAlertDialog(
        activity: Activity?,
        message: String,
        onClickListener: AlertDialogButtonOnClickListener?
    ): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val inflaterView = activity?.layoutInflater?.inflate(R.layout.dialog_text, null, false)
        alertDialogBuilder.setView(inflaterView)
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        val textView: TextView? = inflaterView?.findViewById(R.id.textView)
        val okButton: Button? = inflaterView?.findViewById(R.id.okButton)

        textView?.text = message

        okButton?.setOnClickListener {
            alertDialog.dismiss()
            onClickListener?.onClick()
        }

        return alertDialog
    }

    /**
     * Default dialog showing an message, asking for confirmation.
     * @param activity activity.
     * @param message message to display.
     * @param okOnClickListener callback when confirming action.
     * @param cancelOnClickListener callback when canceling action.
     * @return alert dialog.
     */
    fun getConfirmAlertDialog(
        activity: Activity?,
        message: String,
        okOnClickListener: AlertDialogButtonOnClickListener?,
        cancelOnClickListener: AlertDialogButtonOnClickListener?
    ): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val inflaterView = activity?.layoutInflater?.inflate(R.layout.dialog_confirm, null, false)
        alertDialogBuilder.setView(inflaterView)
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        val textView: TextView? = inflaterView?.findViewById(R.id.textView)
        val cancelButton: Button? = inflaterView?.findViewById(R.id.cancelButton)
        val okButton: Button? = inflaterView?.findViewById(R.id.okButton)

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