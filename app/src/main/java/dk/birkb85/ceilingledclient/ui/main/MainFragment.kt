package dk.birkb85.ceilingledclient.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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

class MainFragment : Fragment() {

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
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TCPService.LocalBinder
            tcpService = binder.getService()
            serviceBound = true
            onServiceBind()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }

    fun onServiceBind() {

        onStatusChanged(tcpService.status)

        // Brug en service, eller bare en normal baggrundstråd, da en AsyncTask kun kan køres en gang og ikke er lavet til at køre i lang tid.
        // Dette vil også være med til at tråden kan lukkes ordentligt.
        // Læs om services her:
        // https://stackoverflow.com/questions/15671207/tcp-socket-connection-as-a-service
        // https://developer.android.com/guide/components/bound-services.html#Binder
        // https://web.archive.org/web/20121103125621/http://myandroidsolutions.blogspot.com/2012/07/android-tcp-connection-tutorial.html
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
            timeTextView?.text = "Tid for send/modtag:"
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
                    timeTextView?.text = "Tid for send/modtag: $time millis"
                    messageTextView?.text = messageTextView?.text.toString() + message
                })
            }
        })
    }

    private fun onStatusChanged(status: TCPService.Status) {
        when(status) {
            TCPService.Status.CONNECTING -> {
                statusTextView?.text = "Status: Forbinder"
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPService.Status.CONNECTED -> {
                statusTextView?.text = "Status: Forbundet"
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = true
                messageButton?.isEnabled = true
            }
            TCPService.Status.RECONNECTING -> {
                statusTextView?.text = "Status: Forbinder igen"
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPService.Status.DISCONNECTING -> {
                statusTextView?.text = "Status: Lukker forbindelse"
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = false
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPService.Status.DISCONNECTED -> {
                statusTextView?.text = "Status: Ikke forbundet"
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
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
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

//        connectTask.setOnMessageReceivedListener(object : TcpClient.OnMessageReceived {
//            override fun messageReceived(message: String?) {
//                val time = System.currentTimeMillis() - timestamp
//                timeTextView?.text = "Tid for send/modtag: $time millis"
//                messageTextView?.text = messageTextView?.text.toString() + message
//            }
//        })
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(activity, TCPService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind from LocalService
        activity?.unbindService(connection)
        serviceBound = false
    }
}
