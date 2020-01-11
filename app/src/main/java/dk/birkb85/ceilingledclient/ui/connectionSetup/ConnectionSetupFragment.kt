package dk.birkb85.ceilingledclient.ui.connectionSetup

import android.content.Context
import android.os.Bundle
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
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class ConnectionSetupFragment : Fragment() {
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

    companion object {
        fun newInstance() = ConnectionSetupFragment()
    }

    private lateinit var viewModel: ConnectionSetupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.connection_setup_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConnectionSetupViewModel::class.java)
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

        val sharedPref = activity?.getSharedPreferences(Global.preferenceFileKey, Context.MODE_PRIVATE)

        val connectionIP = sharedPref?.getString("connection_ip", "")
        if (connectionIP != null && connectionIP != "")
            ipEditText?.setText(connectionIP)
        else
            ipEditText?.setText(getString(R.string.connection_defaultIP))

        val connectionPort = sharedPref?.getInt("connection_port", 0)
        if (connectionPort != null && connectionPort != 0)
            portEditText?.setText(connectionPort.toString())
        else
            portEditText?.setText(getString(R.string.connection_defaultPort))

        messageEditText?.setText(getString(R.string.connection_defaultText))

        ipEditText?.isEnabled = false
        portEditText?.isEnabled = false
        connectButton?.isEnabled = false
        disconnectButton?.isEnabled = false
        messageEditText?.isEnabled = false
        messageButton?.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        bindTCP()
    }

    override fun onPause() {
        super.onPause()
        unbindTCP()
    }

    private fun bindTCP() {
        Log.d("DEBUG", "ConnectionSetup bindTCP")

        onStatusChanged(Global.tcpConnection.getStatus())

        connectButton?.setOnClickListener {
            val sharedPref = activity?.getSharedPreferences(Global.preferenceFileKey, Context.MODE_PRIVATE)
            val ip: String = ipEditText?.text.toString()
            val port: Int = portEditText?.text.toString().toInt()
            val sharedPrefEdit = sharedPref?.edit()
            sharedPrefEdit?.putString("connection_ip", ip)
            sharedPrefEdit?.putInt("connection_port", port)
            sharedPrefEdit?.apply()
            if (ip != "" && port != 0)
                Global.tcpConnection.startClient(ip, port)
        }

        disconnectButton?.setOnClickListener {
            Global.tcpConnection.stopClient()
        }

        messageButton?.setOnClickListener {
            timestamp = System.currentTimeMillis()
            timeTextView?.text = getText(R.string.connectionSetup_time)
            messageTextView?.text = ""

            Global.tcpConnection.sendMessage(messageEditText?.text.toString())
        }

        Global.tcpConnection.setOnMessageReceivedListener(onMessageReceivedListener)
    }

    private fun unbindTCP() {
        Log.d("DEBUG", "ConnectionSetup unbindTCP")
        connectButton?.setOnClickListener(null)
        disconnectButton?.setOnClickListener(null)
        messageButton?.setOnClickListener(null)
        Global.tcpConnection.setOnMessageReceivedListener(null)
    }

    private val onMessageReceivedListener: TCPConnection.OnMessageReceivedListener = object: TCPConnection.OnMessageReceivedListener {
        override fun statusChanged(status: TCPConnection.Status) {
            activity?.runOnUiThread(Runnable {
                onStatusChanged(status)
            })
        }

        override fun messageReceived(message: String?) {
            val time = System.currentTimeMillis() - timestamp
            activity?.runOnUiThread(Runnable {
                val timeText = getText(R.string.connectionSetup_time).toString() + " " + time + " ms"
                timeTextView?.text = timeText
                val messageText = messageTextView?.text.toString() + message
                messageTextView?.text = messageText
            })
        }
    }

    private fun onStatusChanged(status: TCPConnection.Status) {
        when(status) {
            TCPConnection.Status.CONNECTING -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_connecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPConnection.Status.CONNECTED -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_connected)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = true
                messageButton?.isEnabled = true
            }
            TCPConnection.Status.RECONNECTING -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_reconnecting) + " (" + Global.tcpConnection.getRetryCount() + "/" + Global.tcpConnection.getRetryCountMax() + ")"
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = true
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTING -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_disconnecting)
                statusTextView?.text =  statusText
                ipEditText?.isEnabled = false
                portEditText?.isEnabled = false
                connectButton?.isEnabled = false
                disconnectButton?.isEnabled = false
                messageEditText?.isEnabled = false
                messageButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTED -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_disconnected)
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
}
