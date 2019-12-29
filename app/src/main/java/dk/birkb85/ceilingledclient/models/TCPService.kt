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
    private val binder = LocalBinder() // Binder given to clients

    private var mServerMessage: String? = null // message to send to the server
    private var mMessageListener: OnMessageReceived? = null // sends message received notifications
    private var mClientRun = false // Run client
    private var mRun = false // while this is true, the server will continue running (reading)
    private var mBufferOut: PrintWriter? = null // used to send messages
    private var mBufferIn: BufferedReader? = null // used to read messages from the server

    private var socketAddress: InetSocketAddress? = null
    private var socket: Socket? = null
    private var connected = false

    private val generator = Random() // Random number generator

    /** method for clients  */
    val randomNumber: Int
        get() = generator.nextInt(100)

    val isConnected: Boolean
        get() = connected

    // TODO BB 2019-12-29. Run in background. Decide on how to provide host and port. Restart connection if it disconnects.
    fun startClient(ip: String, port: Int) {
        mClientRun = true
        mRun = true
        socketAddress = InetSocketAddress(ip, port)
        clientThread()
    }

    private fun clientThread() {
        try {
            Thread(Runnable {
                while (mClientRun) {
                    try {
                        //create a socket to make the connection with the server
                        socket = Socket()
                        Log.d("DEBUG", "TCP Client: Connecting...")
                        socket?.connect(socketAddress, 10000)
                        Log.d("DEBUG", "TCP Client: Connected")
                        if (mMessageListener != null) {
                            connected = true
                            mMessageListener?.connected()
                        }
                        try {
                            //sends the message to the server
                            mBufferOut = PrintWriter(
                                BufferedWriter(OutputStreamWriter(socket?.getOutputStream())),
                                true
                            )
                            //receives the message which the server sends back
                            mBufferIn = BufferedReader(InputStreamReader(socket?.getInputStream()))
                            //in this while the client listens for the messages sent by the server
                            while (mRun) {
                                Log.d("DEBUG", "Reading line start.")
                                mServerMessage = mBufferIn?.readLine()
                                Log.d("DEBUG", "Reading line end.")
                                if (mServerMessage != null && mMessageListener != null) { //call the method messageReceived from MyActivity class
                                    mMessageListener?.messageReceived(mServerMessage)
                                }
                            }
                            Log.d(
                                "DEBUG",
                                "RESPONSE FROM SERVER: S: Received Message: '$mServerMessage'"
                            )
                        } catch (e: Exception) {
                            Log.e("DEBUG", "TCPService: ClientThread: Socket connected: error", e)
                        } finally {
                            //the socket must be closed. It is not possible to reconnect to this socket
                            // after it is closed, which means a new socket instance has to be created.
                            socket?.close()
                            if (mMessageListener != null) {
                                connected = false
                                mMessageListener?.disconnected()
                            }
                        }
                    }
                    catch (e: Exception) {
                        Log.e("DEBUG", "TCPService: ClientThread: Socket cannot connect: error", e)
                    }

                    Thread.sleep(5000)
                }
            }).start()
        } catch (e: Exception) {
            Log.e("DEBUG", "TCPService: ClientThread: error", e)
        }
    }

    /**
     * Close the connection and release the members
     */
    fun stopClient() {
        mClientRun = false
        mRun = false
        if (socket != null) {
            socket?.close()
            socket = null
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
        mMessageListener = null
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

    fun setOnMessageReceivedListener(listener: TCPService.OnMessageReceived)
    {
        mMessageListener = listener
    }

    interface OnMessageReceived {
        fun connected()
        fun disconnected()
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
        return binder
    }
}