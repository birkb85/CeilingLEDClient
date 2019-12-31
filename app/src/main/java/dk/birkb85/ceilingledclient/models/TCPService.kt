package dk.birkb85.ceilingledclient.models

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.*
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

// Brug en service, eller bare en normal baggrundstråd, da en AsyncTask kun kan køres en gang og ikke er lavet til at køre i lang tid.
// Dette vil også være med til at tråden kan lukkes ordentligt.
// Læs om services her:
// https://stackoverflow.com/questions/15671207/tcp-socket-connection-as-a-service
// https://developer.android.com/guide/components/bound-services.html#Binder
// https://web.archive.org/web/20121103125621/http://myandroidsolutions.blogspot.com/2012/07/android-tcp-connection-tutorial.html
class TCPService : Service() {
    private val mBinder = TCPBinder() // Binder given to clients

    private var mServerMessage: String? = null // message to send to the server
    private var mOnMessageReceived: OnMessageReceived? = null // sends message received notifications
    private var mRun = false // while this is true, the server will continue running (reading)
    private var mBufferOut: PrintWriter? = null // used to send messages
    private var mBufferIn: BufferedReader? = null // used to read messages from the server

    private var mSocketAddress: InetSocketAddress? = null
    private var mSocket: Socket? = null
    private var mStatus: Status = Status.DISCONNECTED

//    val status: Status
//        get() = mStatus

    fun getStatus(): Status = mStatus

    fun getTCPService(): TCPService = this@TCPService // TODO Testing

    fun setOnMessageReceivedListener(onMessageReceived: OnMessageReceived) {
        mOnMessageReceived = onMessageReceived
    }

    private fun setStatus(status: Status) {
        Log.d("DEBUG", "TCPService setStatus: $status")
        mStatus = status
        if (mOnMessageReceived != null)
            mOnMessageReceived?.statusChanged(status)
    }

    fun startClient(ip: String, port: Int) {
        mRun = true
        mSocketAddress = InetSocketAddress(ip, port)
        clientThread()
    }

    private fun clientThread() {
        try { // TODO BB 2019-12-31. Maybe remove this try...
            Thread(Runnable {
                while (mRun) {
                    try {
                        //create a socket to make the connection with the server
                        setStatus(Status.CONNECTING)
                        mSocket = Socket()
                        mSocket?.connect(mSocketAddress, 10000)
                        setStatus(Status.CONNECTED)
                        try {
                            //sends the message to the server
                            mBufferOut = PrintWriter(
                                BufferedWriter(OutputStreamWriter(mSocket?.getOutputStream())),
                                true
                            )
                            //receives the message which the server sends back
                            mBufferIn = BufferedReader(InputStreamReader(mSocket?.getInputStream()))
                            //in this while the client listens for the messages sent by the server
                            while (mRun) {
                                mServerMessage = mBufferIn?.readLine()
                                if (mServerMessage != null && mOnMessageReceived != null) { //call the method messageReceived from MyActivity class
                                    mOnMessageReceived?.messageReceived(mServerMessage)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DEBUG", "TCPService error: ", e)
                        } finally {
                            //the socket must be closed. It is not possible to reconnect to this socket
                            // after it is closed, which means a new socket instance has to be created.
                            mSocket?.close()
                        }
                    }
                    catch (e: Exception) {
                        Log.e("DEBUG", "TCPService error: ", e)
                    }

                    if (mRun) {
                        setStatus(Status.RECONNECTING)
                        Thread.sleep(5000)
                    }
                }
                setStatus(Status.DISCONNECTED)
            }).start()
        } catch (e: Exception) {
            Log.e("DEBUG", "TCPService error: ", e)
        }
    }

    /**
     * Close the connection and release the members
     */
    fun stopClient() {
        setStatus(Status.DISCONNECTING)
        mRun = false
        if (mSocket != null) {
            mSocket?.close()
            mSocket = null
        }
        if (mBufferOut != null) {
            mBufferOut?.flush()
            mBufferOut?.close()
            mBufferOut = null
        }
        if (mBufferIn != null) {
            mBufferIn?.close()
            mBufferIn = null
        }
        mServerMessage = null
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    fun sendMessage(message: String) {
        Log.d("DEBUG", "Send message start.")
        val runnable = Runnable {
            if (mBufferOut != null) {
                Log.d("DEBUG", "TcpClient: Sending: $message")
                mBufferOut?.println(message)
                mBufferOut?.flush()
            }
            Log.d("DEBUG", "Send message end.")
        }
        val thread = Thread(runnable)
        thread.start()
    }

    /**
     * Status of service.
     */
    enum class Status {
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTING,
        DISCONNECTED
    }

    /**
     * Listen for service updates.
     */
    interface OnMessageReceived {
        fun statusChanged(status: Status)
        fun messageReceived(message: String?)
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class TCPBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): TCPService = this@TCPService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}