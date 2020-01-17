package dk.birkb85.ceilingledclient.ui.connectionSetup

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class ConnectionSetupFragment : Fragment() {
    private var mStatusTextView: TextView? = null
    private var mIPEditText: EditText? = null
    private var mPortEditText: EditText? = null
    private var mConnectButton: Button? = null
    private var mDisconnectButton: Button? = null
    private var mMessageEditText: EditText? = null
    private var mMessageButton: Button? = null
    private var mTimeTextView: TextView? = null
    private var mMessageTextView: TextView? = null

    private var mTimestamp: Long = 0

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

        // Set views
        mStatusTextView = activity?.findViewById(R.id.statusTextView)
        mIPEditText = activity?.findViewById(R.id.ipEditText)
        mPortEditText = activity?.findViewById(R.id.portEditText)
        mConnectButton = activity?.findViewById(R.id.connectButton)
        mDisconnectButton = activity?.findViewById(R.id.disconnectButton)
        mMessageEditText = activity?.findViewById(R.id.messageEditText)
        mMessageButton = activity?.findViewById(R.id.messageButton)
        mTimeTextView = activity?.findViewById(R.id.timeTextView)
        mMessageTextView = activity?.findViewById(R.id.messageTextView)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val sharedPref = activity?.getSharedPreferences(Global.preferenceFileKey, Context.MODE_PRIVATE)

        val connectionIP = sharedPref?.getString("connection_ip", "")
        if (connectionIP != null && connectionIP != "")
            mIPEditText?.setText(connectionIP)
        else
            mIPEditText?.setText(getString(R.string.connection_defaultIP))
        mIPEditText?.setSelection(mIPEditText?.text.toString().length)

        val connectionPort = sharedPref?.getInt("connection_port", 0)
        if (connectionPort != null && connectionPort != 0)
            mPortEditText?.setText(connectionPort.toString())
        else
            mPortEditText?.setText(getString(R.string.connection_defaultPort))

        mMessageEditText?.setText(getString(R.string.connection_defaultText))

        mIPEditText?.isEnabled = false
        mPortEditText?.isEnabled = false
        mConnectButton?.isEnabled = false
        mDisconnectButton?.isEnabled = false
        mMessageEditText?.isEnabled = false
        mMessageButton?.isEnabled = false
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

        mConnectButton?.setOnClickListener {
            val sharedPref = activity?.getSharedPreferences(Global.preferenceFileKey, Context.MODE_PRIVATE)
            val ip: String = mIPEditText?.text.toString()
            val port: Int = mPortEditText?.text.toString().toInt()
            val sharedPrefEdit = sharedPref?.edit()
            sharedPrefEdit?.putString("connection_ip", ip)
            sharedPrefEdit?.putInt("connection_port", port)
            sharedPrefEdit?.apply()
            if (ip != "" && port != 0)
                Global.tcpConnection.startClient(ip, port)
        }

        mDisconnectButton?.setOnClickListener {
            Global.tcpConnection.stopClient()
        }

        mMessageButton?.setOnClickListener {
            mTimestamp = System.currentTimeMillis()
            mTimeTextView?.text = getText(R.string.connectionSetup_time)
            mMessageTextView?.text = ""

            Global.tcpConnection.sendMessage(mMessageEditText?.text.toString())
        }

//        Global.tcpConnection.setOnMessageReceivedListener(onMessageReceivedListener)
        Global.tcpConnection.bindTCPConnection(onMessageReceivedListener)
    }

    private fun unbindTCP() {
        Log.d("DEBUG", "ConnectionSetup unbindTCP")
        mConnectButton?.setOnClickListener(null)
        mDisconnectButton?.setOnClickListener(null)
        mMessageButton?.setOnClickListener(null)
//        Global.tcpConnection.setOnMessageReceivedListener(null)
        Global.tcpConnection.unbindTCPConnection()
    }

    private val onMessageReceivedListener: TCPConnection.OnMessageReceivedListener = object: TCPConnection.OnMessageReceivedListener {
        override fun statusChanged(status: TCPConnection.Status) {
            activity?.runOnUiThread(Runnable {
                onStatusChanged(status)
            })
        }

        override fun messageReceived(message: String?) {
            val time = System.currentTimeMillis() - mTimestamp
            activity?.runOnUiThread(Runnable {
                val timeText = getText(R.string.connectionSetup_time).toString() + " " + time + " ms"
                mTimeTextView?.text = timeText
                val messageText = mMessageTextView?.text.toString() + message
                mMessageTextView?.text = messageText
            })
        }
    }

    private fun onStatusChanged(status: TCPConnection.Status) {
        when(status) {
            TCPConnection.Status.CONNECTING -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_connecting)
                mStatusTextView?.text =  statusText
                mIPEditText?.isEnabled = false
                mPortEditText?.isEnabled = false
                mConnectButton?.isEnabled = false
                mDisconnectButton?.isEnabled = true
                mMessageEditText?.isEnabled = false
                mMessageButton?.isEnabled = false
            }
            TCPConnection.Status.CONNECTED -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_connected)
                mStatusTextView?.text =  statusText
                mIPEditText?.isEnabled = false
                mPortEditText?.isEnabled = false
                mConnectButton?.isEnabled = false
                mDisconnectButton?.isEnabled = true
                mMessageEditText?.isEnabled = true
                mMessageButton?.isEnabled = true
            }
            TCPConnection.Status.RECONNECTING -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_reconnecting) + " (" + Global.tcpConnection.getRetryCount() + "/" + Global.tcpConnection.getRetryCountMax() + ")"
                mStatusTextView?.text =  statusText
                mIPEditText?.isEnabled = false
                mPortEditText?.isEnabled = false
                mConnectButton?.isEnabled = false
                mDisconnectButton?.isEnabled = true
                mMessageEditText?.isEnabled = false
                mMessageButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTING -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_disconnecting)
                mStatusTextView?.text =  statusText
                mIPEditText?.isEnabled = false
                mPortEditText?.isEnabled = false
                mConnectButton?.isEnabled = false
                mDisconnectButton?.isEnabled = false
                mMessageEditText?.isEnabled = false
                mMessageButton?.isEnabled = false
            }
            TCPConnection.Status.DISCONNECTED -> {
                val statusText = getString(R.string.connectionSetup_status) + " " + getText(R.string.status_disconnected)
                mStatusTextView?.text =  statusText
                mIPEditText?.isEnabled = true
                mPortEditText?.isEnabled = true
                mConnectButton?.isEnabled = true
                mDisconnectButton?.isEnabled = false
                mMessageEditText?.isEnabled = false
                mMessageButton?.isEnabled = false
            }
        }
    }
}
