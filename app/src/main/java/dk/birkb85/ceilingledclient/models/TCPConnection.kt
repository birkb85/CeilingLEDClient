package dk.birkb85.ceilingledclient.models

import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

class TCPConnection {
    private var mServerMessage: String? = null // message to send to the server

    private var mOnMessageReceivedListener: OnMessageReceivedListener? = null // sends message received notifications

    private var mIsRunning = false // while this is true, the server will continue running (reading)

    private var mPauseTimer: Timer? = null

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

    fun startClient(ip: String, port: Int) {
        if (!mIsRunning && mStatus == Status.DISCONNECTED) {
            mIsRunning = true
            mRetryCount = 0
            mSocketAddress = InetSocketAddress(ip, port)
            clientThread()
        }
    }

    fun bindTCPConnection(onMessageReceivedListener: OnMessageReceivedListener?) {
        mOnMessageReceivedListener = onMessageReceivedListener
        cancelPauseTimer()
    }

    fun unbindTCPConnection() {
        mOnMessageReceivedListener = null
        startPauseTimer()
    }

    private fun clientThread() {
        Thread(Runnable {
            while (mIsRunning) {
                try {
                    //create a socket to make the connection with the server
                    setStatus(Status.CONNECTING)
                    mSocket = Socket()
                    mSocket?.connect(mSocketAddress, 10000)
                    setStatus(Status.CONNECTED)
                    mRetryCount = 0
                    try {
                        //sends the message to the server
                        mBufferOut = PrintWriter(
                            BufferedWriter(OutputStreamWriter(mSocket?.getOutputStream())),
                            true
                        )

                        //receives the message which the server sends back
                        mBufferIn = BufferedReader(InputStreamReader(mSocket?.getInputStream()))

                        // Start heart beat.
                        heartBeatThread()

                        //in this while the client listens for the messages sent by the server
                        while (mIsRunning) {
                            mServerMessage = mBufferIn?.readLine()
                            if (mServerMessage != "#[HB]") {
                                //call the method messageReceived from MyActivity class
                                Log.d("DEBUG", "TcpClient: Receiving: $mServerMessage")
                                mOnMessageReceivedListener?.messageReceived(mServerMessage)
                            } else {
                                Log.d("DEBUG", "TcpClient: Receiving: Heart Beat")
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

                if (mIsRunning && mRetryCount < mRetryCountMax) {
                    mRetryCount ++
                    setStatus(Status.RECONNECTING)
                    Thread.sleep(5000)
                } else {
                    mIsRunning = false
                }
            }

            setStatus(Status.DISCONNECTED)
        }).start()
    }

    /**
     * Sends out a heart beat signal every 5 seconds, while connected to server, to keep connection alive.
     */
    private fun heartBeatThread() {
        Thread(Runnable {
            while (mIsRunning && mStatus == Status.CONNECTED) {
                try {
                    if (mBufferOut != null) {
                        Log.d("DEBUG", "TcpClient: Sending: Heart Beat")
                        mBufferOut?.println("[HB]")
                        mBufferOut?.flush()
                    }
                    Thread.sleep(5000)
                } catch (e: Exception) {
                    Log.e("DEBUG", "TCPConnection heartBeatThread error: ", e)
                }
            }
        }).start()
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    fun sendMessage(message: String) {
        if (mIsRunning && mStatus == Status.CONNECTED) {
            Thread(Runnable {
                try {
                    if (mBufferOut != null) {
                        Log.d("DEBUG", "TcpClient: Sending: $message")
                        mBufferOut?.println(message)
                        mBufferOut?.flush()
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG", "TCPConnection sendMessage error: ", e)
                }
            }).start()
        }
    }

    /**
     * Starts timer that stops client if it runs in background for too long.
     */
    private fun startPauseTimer() {
        if (mIsRunning) {
            mPauseTimer?.cancel()
            mPauseTimer = Timer()
            mPauseTimer?.schedule(object : TimerTask() {
                override fun run() {
                    stopClient()
                }
            }, 10000)
        }
    }

    /**
     * Cancels timer that stops client if it runs in background for too long.
     */
    private fun cancelPauseTimer() {
        mPauseTimer?.cancel()
    }

    /**
     * Close the connection and release the members
     */
    fun stopClient() {
        if (mIsRunning) {
            setStatus(Status.DISCONNECTING)
            mIsRunning = false
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
    }

    /**
     * Status of connection.
     */
    enum class Status {
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTING,
        DISCONNECTED
    }

    /**
     * Listen for connection updates.
     */
    interface OnMessageReceivedListener {
        fun statusChanged(status: Status)
        fun messageReceived(message: String?)
    }
}