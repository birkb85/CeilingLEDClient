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
class TCPServiceExample : Service() {
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

    fun getTCPService(): TCPServiceExample = this@TCPServiceExample

    fun setOnMessageReceivedListener(onMessageReceived: OnMessageReceived) {
        mOnMessageReceived = onMessageReceived
    }

    private fun setStatus(status: Status) {
        Log.d("DEBUG", "TCPServiceExample setStatus: $status")
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
        try { // BB 2019-12-31. Maybe remove this try...
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
                            Log.e("DEBUG", "TCPServiceExample error: ", e)
                        } finally {
                            //the socket must be closed. It is not possible to reconnect to this socket
                            // after it is closed, which means a new socket instance has to be created.
                            mSocket?.close()
                        }
                    }
                    catch (e: Exception) {
                        Log.e("DEBUG", "TCPServiceExample error: ", e)
                    }

                    if (mRun) {
                        setStatus(Status.RECONNECTING)
                        Thread.sleep(5000)
                    }
                }
                setStatus(Status.DISCONNECTED)
            }).start()
        } catch (e: Exception) {
            Log.e("DEBUG", "TCPServiceExample error: ", e)
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
        fun getService(): TCPServiceExample = this@TCPServiceExample
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}

// Example on how to use!
/*
package dk.birkb85.ceilingledclient.ui.connection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.TCPServiceExample

class ConnectionFragment : Fragment() {
    private var statusTextView: TextView? = null
    private var ipEditText: EditText? = null
    private var portEditText: EditText? = null
    private var connectButton: Button? = null
    private var disconnectButton: Button? = null
    private var messageEditText: EditText? = null
    private var messageButton: Button? = null
    private var timeTextView: TextView? = null
    private var messageTextView: TextView? = null

    private var timestamp: Long = 0

    // Service
    private lateinit var tcpService: TCPServiceExample
    private var serviceBound: Boolean = false

    companion object {
        fun newInstance() = ConnectionFragment()
    }

    private lateinit var viewModel: ConnectionViewModel

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TCPServiceExample.TCPBinder
            tcpService = binder.getService()
            serviceBound = true
            onServiceBind()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }

    fun onServiceBind() {
        Log.d("DEBUG", "onServiceBind: ${tcpService.getTCPService()}")

        onStatusChanged(tcpService.getStatus())

        connectButton?.setOnClickListener {
            val ip: String = ipEditText?.text.toString()
            val port: Int = portEditText?.text.toString().toInt()
            tcpService.startClient(ip, port)

//            connectTask.execute(ipEditText?.text.toString(), portEditText?.text.toString())
        }

        disconnectButton?.setOnClickListener {
            tcpService.stopClient()

//            tcpClient?.stopClient()
        }

        messageButton?.setOnClickListener {
            timestamp = System.currentTimeMillis()
            timeTextView?.text = getText(R.string.connection_time)
            messageTextView?.text = ""

            tcpService.sendMessage(messageEditText?.text.toString())

//            tcpClient?.sendMessage(messageEditText?.text.toString())

//            // Testing service.
//            if (serviceBound) {
//                // Call a method from the LocalService.
//                // However, if this call were something that might hang, then this request should
//                // occur in a separate thread to avoid slowing down the activity performance.
//                val num: Int = tcpService.randomNumber
//                Toast.makeText(activity, "number: $num", Toast.LENGTH_SHORT).show()
//            }
        }

        tcpService.setOnMessageReceivedListener(object : TCPServiceExample.OnMessageReceivedListener {
            override fun statusChanged(status: TCPServiceExample.Status) {
                activity?.runOnUiThread(Runnable {
                    onStatusChanged(status)
                })
            }

            override fun messageReceived(message: String?) {
                val time = System.currentTimeMillis() - timestamp
                activity?.runOnUiThread(Runnable {
                    val timeText = getText(R.string.connection_time).toString() + " " + time + " ms"
                    timeTextView?.text = timeText
                    val messageText = messageTextView?.text.toString() + message
                    messageTextView?.text = messageText
                })
            }
        })
    }

    private fun onStatusChanged(status: TCPServiceExample.Status) {
        when(status) {
            TCPServiceExample.Status.CONNECTING -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_connecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPServiceExample.Status.CONNECTED -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_connected)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = true
                messageButton?.isEnabled = true
            }
            TCPServiceExample.Status.RECONNECTING -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_reconnecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPServiceExample.Status.DISCONNECTING -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_disconnecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = false
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPServiceExample.Status.DISCONNECTED -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_disconnected)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = true
                portEditText?.isEnabled = true
                connectButton?.isEnabled = true
                disconnectButton?.isEnabled = false
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.connection_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConnectionViewModel::class.java)

        // Set views
        statusTextView = activity?.findViewById(R.id.statusTextView)
        ipEditText = activity?.findViewById(R.id.ipEditText)
        portEditText = activity?.findViewById(R.id.portEditText)
        connectButton = activity?.findViewById(R.id.connectButton)
        disconnectButton = activity?.findViewById(R.id.disconnectButton)
        messageEditText = activity?.findViewById(R.id.messageEditText)
        messageButton = activity?.findViewById(R.id.messageButton)
        timeTextView = activity?.findViewById(R.id.timeTextView)
        messageTextView = activity?.findViewById(R.id.messageTextView)

        ipEditText?.setText("192.168.4.1")
        portEditText?.setText("333")
        messageEditText?.setText("Test")

        ipEditText?.isEnabled = false
        portEditText?.isEnabled = false
        connectButton?.isEnabled = false
        disconnectButton?.isEnabled = false
        messageEditText?.isEnabled = false
        messageButton?.isEnabled = false
    }

//    override fun onStart() {
//        super.onStart()
//        // Bind to LocalService
//        Intent(activity, TCPServiceExample::class.java).also { intent ->
//            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }
//    }

    override fun onResume() {
        super.onResume()
        // Bind to LocalService
        Intent(activity, TCPServiceExample::class.java).also { intent ->
            // Seems like a new intance is created every time when using BIND_AUTO_CREATE.. Try the following:
            // https://stackoverflow.com/questions/14746245/use-0-or-bind-auto-create-for-bindservices-flag
//            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            activity?.bindService(intent, connection, 0)
            activity?.startService(intent) // BB 2019-12-31. Maybe only start service once. Make global function for starting service and save if service is started in a Global companion object.
        }
    }

    override fun onPause() {
        super.onPause()
        // Unbind from LocalService
        activity?.unbindService(connection)
        serviceBound = false
    }

//    override fun onStop() {
//        super.onStop()
//        // Unbind from LocalService
//        activity?.unbindService(connection)
//        serviceBound = false
//    }
}
 */