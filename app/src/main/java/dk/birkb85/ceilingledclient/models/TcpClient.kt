package dk.birkb85.ceilingledclient.models

import android.util.Log
import java.io.*
import java.net.InetAddress
import java.net.Socket

/**
 * TCP Client.
 * https://stackoverflow.com/questions/38162775/really-simple-tcp-client
 */
class TcpClient(listener: OnMessageReceived?) {
    // message to send to the server
    private var mServerMessage: String? = null
    // sends message received notifications
    private var mMessageListener: OnMessageReceived? = null
    // while this is true, the server will continue running
    private var mRun = false
    // used to send messages
    private var mBufferOut: PrintWriter? = null
    // used to read messages from the server
    private var mBufferIn: BufferedReader? = null

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    fun sendMessage(message: String) {
        val runnable = Runnable {
            if (mBufferOut != null) {
                Log.d("DEBUG", "TcpClient: Sending: $message")
                mBufferOut?.println(message)
                mBufferOut?.flush()
            }
        }
        val thread = Thread(runnable)
        thread.start()
    }

    /**
     * Close the connection and release the members
     */
    fun stopClient() {
        mRun = false
        if (mBufferOut != null) {
            mBufferOut?.flush()
            mBufferOut?.close()
        }
        mMessageListener = null
        mBufferIn = null
        mBufferOut = null
        mServerMessage = null
    }

    fun run() {
        mRun = true
        try { //here you must put your computer's IP address.
            val serverAddr = InetAddress.getByName(SERVER_IP)
            Log.d("DEBUG", "TCP Client: C: Connecting...")
            //create a socket to make the connection with the server
            val socket = Socket(serverAddr, SERVER_PORT)
            try { //sends the message to the server
                mBufferOut = PrintWriter(
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                    true
                )
                //receives the message which the server sends back
                mBufferIn = BufferedReader(InputStreamReader(socket.getInputStream()))
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    mServerMessage = mBufferIn!!.readLine()
                    if (mServerMessage != null && mMessageListener != null) { //call the method messageReceived from MyActivity class
                        mMessageListener!!.messageReceived(mServerMessage)
                    }
                }
                Log.d(
                    "DEBUG",
                    "RESPONSE FROM SERVER: S: Received Message: '$mServerMessage'"
                )
            } catch (e: Exception) {
                Log.e("DEBUG", "TCP: S: Error", e)
            } finally { //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close()
            }
        } catch (e: Exception) {
            Log.e("DEBUG", "TCP: C: Error", e)
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    interface OnMessageReceived {
        fun messageReceived(message: String?)
    }

    companion object {
        const val SERVER_IP = "192.168.4.1" //server IP address
        const val SERVER_PORT = 333
    }

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    init {
        mMessageListener = listener
    }
}