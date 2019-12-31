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
import dk.birkb85.ceilingledclient.models.TCPService

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
    private lateinit var tcpService: TCPService
    private var serviceBound: Boolean = false

    companion object {
        fun newInstance() = ConnectionFragment()
    }

    private lateinit var viewModel: ConnectionViewModel

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TCPService.TCPBinder
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

        tcpService.setOnMessageReceivedListener(object : TCPService.OnMessageReceived {
            override fun statusChanged(status: TCPService.Status) {
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

    private fun onStatusChanged(status: TCPService.Status) {
        when(status) {
            TCPService.Status.CONNECTING -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_connecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPService.Status.CONNECTED -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_connected)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = true
                messageButton?.isEnabled = true
            }
            TCPService.Status.RECONNECTING -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_reconnecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPService.Status.DISCONNECTING -> {
                val statusText = getString(R.string.connection_status) + " " + getText(R.string.status_disconnecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = false
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPService.Status.DISCONNECTED -> {
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
        // TODO: Use the ViewModel

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
//        Intent(activity, TCPService::class.java).also { intent ->
//            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        }
//    }

    override fun onResume() {
        super.onResume()
        // Bind to LocalService
        Intent(activity, TCPService::class.java).also { intent ->
            // Seems like a new intance is created every time when using BIND_AUTO_CREATE.. Try the following:
            // https://stackoverflow.com/questions/14746245/use-0-or-bind-auto-create-for-bindservices-flag
//            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            activity?.bindService(intent, connection, 0)
            activity?.startService(intent) // TODO BB 2019-12-31. Maybe only start service once. Make global function for starting service and save if service is started in a Global companion object.
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
