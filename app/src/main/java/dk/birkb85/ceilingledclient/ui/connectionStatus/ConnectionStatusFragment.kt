package dk.birkb85.ceilingledclient.ui.connectionStatus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dk.birkb85.ceilingledclient.ConnectionSetupActivity
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class ConnectionStatusFragment : Fragment() {
    private var mStatusTextView: TextView? = null
    private var mMessageTextView: TextView? = null
    private var mActionsLinearLayout: LinearLayout? = null
    private var mSetupButton: Button? = null
    private var mConnectButton: Button? = null

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
        viewModel = ViewModelProvider(this).get(ConnectionStatusViewModel::class.java)

        // Set views
        mStatusTextView = activity?.findViewById(R.id.statusTextView)
        mMessageTextView = activity?.findViewById(R.id.messageTextView)
        mActionsLinearLayout = activity?.findViewById(R.id.actionsLinearLayout)
        mSetupButton = activity?.findViewById(R.id.setupButton)
        mConnectButton = activity?.findViewById(R.id.connectButton)

        mActionsLinearLayout?.visibility = View.GONE
        mSetupButton?.isEnabled = false
        mConnectButton?.isEnabled = false
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
        onStatusChanged(Global.tcpConnection.getStatus())

        mSetupButton?.setOnClickListener {
            connectionSetup()
        }

        mConnectButton?.setOnClickListener {
            val sharedPref = activity?.getSharedPreferences(Global.preferenceFileKey, Context.MODE_PRIVATE)
            val ip = sharedPref?.getString("connection_ip", "")
            val port = sharedPref?.getInt("connection_port", 0)
            if (ip != null && port != null && ip != "" && port != 0)
                Global.tcpConnection.startClient(ip, port)
            else
                connectionSetup()
        }

        Global.tcpConnection.bindStatusListener(statusListener)
    }

    private fun unbindTCP() {
        mSetupButton?.setOnClickListener(null)
        mConnectButton?.setOnClickListener(null)
        Global.tcpConnection.unbindStatusListener()
    }

    private fun connectionSetup() {
        val intent = Intent(context, ConnectionSetupActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private val statusListener: TCPConnection.StatusListener = object: TCPConnection.StatusListener {
        override fun statusChanged(status: TCPConnection.Status) {
            activity?.runOnUiThread {
                onStatusChanged(status)
            }
        }

        override fun messageReceived(message: String) {
            activity?.runOnUiThread {
                if (message == "") {
                    mMessageTextView?.text = ""
                } else {
                    var messageText = getString(R.string.connectionStatus_message)
                    messageText = messageText.replace("[MEMORY]", message)
                    mMessageTextView?.text = messageText
                }
            }
        }
    }

    private fun onStatusChanged(status: TCPConnection.Status) {
        when(status) {
            TCPConnection.Status.CONNECTING -> {
                var statusText = getString(R.string.connectionStatus_status)
                statusText = statusText.replace("[STATUS]", getString(R.string.status_connecting))
                mStatusTextView?.text =  statusText
                mActionsLinearLayout?.visibility = View.VISIBLE
                mSetupButton?.isEnabled = true
                mConnectButton?.isEnabled = false
            }
            TCPConnection.Status.CONNECTED -> {
                var statusText = getString(R.string.connectionStatus_status)
                statusText = statusText.replace("[STATUS]", getString(R.string.status_connected))
                mStatusTextView?.text =  statusText
                mActionsLinearLayout?.visibility = View.GONE
                mSetupButton?.isEnabled = false
                mConnectButton?.isEnabled = false
            }
            TCPConnection.Status.RECONNECTING -> {
                var statusText = getString(R.string.connectionStatus_status)
                statusText = statusText.replace("[STATUS]", getString(R.string.status_reconnecting) + " (" + Global.tcpConnection.getRetryCount() + "/" + Global.tcpConnection.getRetryCountMax() + ")")
                mStatusTextView?.text =  statusText
                mActionsLinearLayout?.visibility = View.VISIBLE
                mSetupButton?.isEnabled = true
                mConnectButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTING -> {
                var statusText = getString(R.string.connectionStatus_status)
                statusText = statusText.replace("[STATUS]", getString(R.string.status_disconnecting))
                mStatusTextView?.text =  statusText
                mActionsLinearLayout?.visibility = View.VISIBLE
                mSetupButton?.isEnabled = true
                mConnectButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTED -> {
                var statusText = getString(R.string.connectionStatus_status)
                statusText = statusText.replace("[STATUS]", getString(R.string.status_disconnected))
                mStatusTextView?.text =  statusText
                mActionsLinearLayout?.visibility = View.VISIBLE
                mSetupButton?.isEnabled = true
                mConnectButton?.isEnabled = true
            }
        }
    }
}
