package dk.birkb85.ceilingledclient.models

import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

class TCPConnection {
    private var mStatusListener: StatusListener? = null
    private var mMessageReceivedListener: MessageReceivedListener? = null

    private var mIsRunning = false
    private var mRetryCount = 0
    private val mRetryCountMax = 5

    private var mPauseTimer: Timer? = null

    private var mSocketAddress: InetSocketAddress? = null
    private var mSocket: Socket? = null
    private var mStatus: Status = Status.DISCONNECTED

    private var mBufferOut: PrintWriter? = null
    private var mBufferIn: BufferedReader? = null

    private var mServerMessage: String? = null

    /**
     * Get current status of connection.
     */
    fun getStatus(): Status {
        return mStatus
    }

    /**
     * Get current retry number.
     */
    fun getRetryCount(): Int {
        return mRetryCount
    }

    /**
     * Get number of max retries.
     */
    fun getRetryCountMax(): Int {
        return mRetryCountMax
    }

    /**
     * Set status.
     */
    private fun setStatus(status: Status) {
        Log.d("DEBUG", "TCPConnection setStatus: $status")
        mStatus = status
        mStatusListener?.statusChanged(status)
        mMessageReceivedListener?.statusChanged(status)
    }

    /**
     * Start TCP Client.
     */
    fun startClient(ip: String, port: Int) {
        if (!mIsRunning && mStatus == Status.DISCONNECTED) {
            mIsRunning = true
            mRetryCount = 0
            mSocketAddress = InetSocketAddress(ip, port)
            clientThread()
        }
    }

    /**
     * Bind status.
     */
    fun bindStatusListener(statusListener: StatusListener?) {
        Log.d("DEBUG", "TCPConnection bindStatusListener")
        mStatusListener = statusListener
        cancelPauseTimer()
    }

    /**
     * Unbind status.
     */
    fun unbindStatusListener() {
        Log.d("DEBUG", "TCPConnection unbindStatusListener")
        mStatusListener = null
        startPauseTimer()
    }

    /**
     * Bind activity.
     */
    fun bindMessageReceivedListener(messageReceivedListener: MessageReceivedListener?) {
        Log.d("DEBUG", "TCPConnection bindMessageReceivedListener")
        mMessageReceivedListener = messageReceivedListener
    }

    /**
     * Unbind activity.
     */
    fun unbindMessageReceivedListener() {
        Log.d("DEBUG", "TCPConnection unbindMessageReceivedListener")
        mMessageReceivedListener = null
    }

    /**
     * Connection thread. Connect to server and listen for messages.
     */
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
                            if (mServerMessage != "HB") {
                                //call the method messageReceived from MyActivity class
                                Log.d("DEBUG", "TcpClient: Receiving: $mServerMessage")
                                mStatusListener?.messageReceived(mServerMessage)
                                mMessageReceivedListener?.messageReceived(mServerMessage)
                            } else {
                                Log.d("DEBUG", "TcpClient: Receiving: Heart Beat")
                            }
                        }
                    } catch (e: Exception) {
//                        Log.e("DEBUG", "TCPConnection error: ", e)
                    } finally {
                        //the socket must be closed. It is not possible to reconnect to this socket
                        // after it is closed, which means a new socket instance has to be created.
                        resetConnection()
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
                    Log.d("DEBUG", "TcpClient: Sending: Heart Beat")
                    mBufferOut?.println("HB")
                    mBufferOut?.flush()
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
                    Log.d("DEBUG", "TcpClient: Sending: $message")
                    mBufferOut?.println(message)
                    mBufferOut?.flush()
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
            resetConnection()
        }
    }

    /**
     * Reset variables for connection.
     */
    private fun resetConnection() {
        mSocket?.close()
        mSocket = null
        mBufferOut?.flush()
        mBufferOut?.close()
        mBufferOut = null
        mBufferIn?.close()
        mBufferIn = null
        mServerMessage = null
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
     * Listen for status updates.
     */
    interface StatusListener {
        fun statusChanged(status: Status)
        fun messageReceived(message: String?)
    }

    /**
     * Listen for connection updates.
     */
    interface MessageReceivedListener {
        fun statusChanged(status: Status)
        fun messageReceived(message: String?)
    }
}