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
import java.util.*

class TCPService : Service() {
    private val mBinder = LocalBinder() // Binder given to clients

    private var mServerMessage: String? = null // message to send to the server
    private var mOnMessageReceived: OnMessageReceived? = null // sends message received notifications
    private var mRun = false // while this is true, the server will continue running (reading)
    private var mBufferOut: PrintWriter? = null // used to send messages
    private var mBufferIn: BufferedReader? = null // used to read messages from the server

    private var mSocketAddress: InetSocketAddress? = null
    private var mSocket: Socket? = null
    private var mStatus: Status = Status.DISCONNECTED

    val status: Status
        get() = mStatus

    fun setOnMessageReceivedListener(onMessageReceived: OnMessageReceived) {
        mOnMessageReceived = onMessageReceived
    }

    private fun setStatus(status: Status) {
        Log.d("DEBUG", "TCPService setStatus: $status")
        mStatus = status
        if (mOnMessageReceived != null)
            mOnMessageReceived?.statusChanged(status)
    }

    // TODO BB 2019-12-29. Run in background. Decide on how to provide host and port. Restart connection if it disconnects.
    fun startClient(ip: String, port: Int) {
        mRun = true
        mSocketAddress = InetSocketAddress(ip, port)
        clientThread()
    }

    private fun clientThread() {
        try {
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

//    fun backgroundThread() {
//        // Testing background thread.
//        try {
//            val thread = Thread(Runnable {
//                while (true) {
//                    Log.d("DEBUG", "Running in background")
//                    Thread.sleep(1000)
//                }
//            }).start()
//        }
//        catch (e: Exception) {
//            Log.e("DEBUG", "LocalService: thread error", e)
//        }
//    }

//    init {
//        backgroundThread()
//    }

    enum class Status {
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTING,
        DISCONNECTED
    }

    interface OnMessageReceived {
        fun statusChanged(status: Status)
        fun messageReceived(message: String?)
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): TCPService = this@TCPService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}