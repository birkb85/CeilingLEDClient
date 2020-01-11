package dk.birkb85.ceilingledclient.ui.connectionStatus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import dk.birkb85.ceilingledclient.ConnectionSetupActivity
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class ConnectionStatusFragment : Fragment() {
    private var statusTextView: TextView? = null
    private var actionsLinearLayout: LinearLayout? = null
    private var setupButton: Button? = null
    private var connectButton: Button? = null

    companion object {
        fun newInstance() = ConnectionStatusFragment()
    }

    private lateinit var viewModel: ConnectionStatusViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.connection_status_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConnectionStatusViewModel::class.java)
        // TODO: Use the ViewModel

        // Set views
        statusTextView = activity?.findViewById(R.id.statusTextView)
        actionsLinearLayout = activity?.findViewById(R.id.actionsLinearLayout)
        setupButton = activity?.findViewById(R.id.setupButton)
        connectButton = activity?.findViewById(R.id.connectButton)

        actionsLinearLayout?.visibility = View.GONE
        setupButton?.isEnabled = false
        connectButton?.isEnabled = false
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
        Log.d("DEBUG", "ConnectionStatus bindTCP")

        onStatusChanged(Global.tcpConnection.getStatus())

        setupButton?.setOnClickListener {
            connectionSetup()
        }

        connectButton?.setOnClickListener {
            val sharedPref = activity?.getSharedPreferences(Global.preferenceFileKey, Context.MODE_PRIVATE)
            val ip = sharedPref?.getString("connection_ip", "")
            val port = sharedPref?.getInt("connection_port", 0)
            if (ip != null && port != null && ip != "" && port != 0)
                Global.tcpConnection.startClient(ip, port)
            else
                connectionSetup()
        }

        Global.tcpConnection.setOnMessageReceivedListener(onMessageReceivedListener)
    }

    private fun unbindTCP() {
        Log.d("DEBUG", "ConnectionStatus unbindTCP")
        setupButton?.setOnClickListener(null)
        connectButton?.setOnClickListener(null)
        Global.tcpConnection.setOnMessageReceivedListener(null)
    }

    private fun connectionSetup() {
        val intent = Intent(context, ConnectionSetupActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private val onMessageReceivedListener: TCPConnection.OnMessageReceivedListener = object: TCPConnection.OnMessageReceivedListener {
        override fun statusChanged(status: TCPConnection.Status) {
            activity?.runOnUiThread(Runnable {
                onStatusChanged(status)
            })
        }

        override fun messageReceived(message: String?) {
        }
    }

    private fun onStatusChanged(status: TCPConnection.Status) {
        when(status) {
            TCPConnection.Status.CONNECTING -> {
                val statusText = getString(R.string.connectionStatus_status) + " " + getText(R.string.status_connecting)
                statusTextView?.text =  statusText
                actionsLinearLayout?.visibility = View.VISIBLE
                setupButton?.isEnabled = true
                connectButton?.isEnabled = false
            }
            TCPConnection.Status.CONNECTED -> {
                val statusText = getString(R.string.connectionStatus_status) + " " + getText(R.string.status_connected)
                statusTextView?.text =  statusText
                actionsLinearLayout?.visibility = View.GONE
                setupButton?.isEnabled = false
                connectButton?.isEnabled = false
            }
            TCPConnection.Status.RECONNECTING -> {
                val statusText = getString(R.string.connectionStatus_status) + " " + getText(R.string.status_reconnecting) + " (" + Global.tcpConnection.getRetryCount() + "/" + Global.tcpConnection.getRetryCountMax() + ")"
                statusTextView?.text =  statusText
                actionsLinearLayout?.visibility = View.VISIBLE
                setupButton?.isEnabled = true
                connectButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTING -> {
                val statusText = getString(R.string.connectionStatus_status) + " " + getText(R.string.status_disconnecting)
                statusTextView?.text =  statusText
                actionsLinearLayout?.visibility = View.VISIBLE
                setupButton?.isEnabled = true
                connectButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTED -> {
                val statusText = getString(R.string.connectionStatus_status) + " " + getText(R.string.status_disconnected)
                statusTextView?.text =  statusText
                actionsLinearLayout?.visibility = View.VISIBLE
                setupButton?.isEnabled = true
                connectButton?.isEnabled = true
            }
        }
    }
}
