package dk.birkb85.ceilingledclient.models

import android.util.Log
import java.io.*
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

class TCPConnection {
    private var mServerMessage: String? = null // message to send to the server
    private var mOnMessageReceivedListener: OnMessageReceivedListener? = null // sends message received notifications
    private var mRun = false // while this is true, the server will continue running (reading)
    private var mRetryCount = 0
    private val mRetryCountMax = 5
    private var mBufferOut: PrintWriter? = null // used to send messages
    private var mBufferIn: BufferedReader? = null // used to read messages from the server

    private var mSocketAddress: InetSocketAddress? = null
    private var mSocket: Socket? = null
    private var mStatus: Status = Status.DISCONNECTED

    fun getStatus(): Status {
        return mStatus
    }

    fun getRetryCount(): Int {
        return mRetryCount
    }

    fun getRetryCountMax(): Int {
        return mRetryCountMax
    }

    private fun setStatus(status: Status) {
        Log.d("DEBUG", "TCPConnection setStatus: $status")
        mStatus = status
        if (mOnMessageReceivedListener != null)
            mOnMessageReceivedListener?.statusChanged(status)
    }

    fun setOnMessageReceivedListener(onMessageReceivedListener: OnMessageReceivedListener?) {
        mOnMessageReceivedListener = onMessageReceivedListener
    }

    fun startClient(ip: String, port: Int) {
        mRun = true
        mRetryCount = 0
        mSocketAddress = InetSocketAddress(ip, port)
        clientThread()
    }

    private fun clientThread() {
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
                            if (mServerMessage != null && mOnMessageReceivedListener != null) { //call the method messageReceived from MyActivity class
                                mOnMessageReceivedListener?.messageReceived(mServerMessage)
                            }
                        }
                    } catch (e: Exception) {
//                        Log.e("DEBUG", "TCPConnection error: ", e)
                    } finally {
                        //the socket must be closed. It is not possible to reconnect to this socket
                        // after it is closed, which means a new socket instance has to be created.
                        mSocket?.close()
                    }
                }
                catch (e: Exception) {
//                    Log.e("DEBUG", "TCPConnection error: ", e)
                }

                if (mRun && mRetryCount < mRetryCountMax) {
                    mRetryCount ++
                    setStatus(Status.RECONNECTING)
                    Thread.sleep(5000)
                } else {
                    mRun = false
                }
            }

            setStatus(Status.DISCONNECTED)
        }).start()
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
    interface OnMessageReceivedListener {
        fun statusChanged(status: Status)
        fun messageReceived(message: String?)
    }
}