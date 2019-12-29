package dk.birkb85.ceilingledclient.models

import android.os.AsyncTask
import android.util.Log
import dk.birkb85.ceilingledclient.models.Global.Companion.tcpClient

class ConnectTask : AsyncTask<String, String, TcpClient>() {
    private var mMessageListener: TcpClient.OnMessageReceived? = null

    override fun doInBackground(vararg message: String): TcpClient? { //we create a TCPClient object
        Log.d("DEBUG", "doInBackground start.")
        if (message.size != 2) return null
        val ip: String = message[0]
        val port: Int = message[1].toInt()

        Log.d("DEBUG", "doInBackground 1.")

        tcpClient = TcpClient(object : TcpClient.OnMessageReceived {
            //here the messageReceived method is implemented
            override fun messageReceived(message: String?) { //this method calls the onProgressUpdate
                publishProgress(message)
            }
        })

        Log.d("DEBUG", "doInBackground 2.")

        tcpClient?.run(ip, port)

        Log.d("DEBUG", "doInBackground end.")
        return null
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        //response received from server
        Log.d("DEBUG", "onProgressUpdate: response: " + values[0])
        //process server response here....
        mMessageListener?.messageReceived(values[0])
    }

    fun setOnMessageReceivedListener(listener: TcpClient.OnMessageReceived)
    {
        mMessageListener = listener
    }
}