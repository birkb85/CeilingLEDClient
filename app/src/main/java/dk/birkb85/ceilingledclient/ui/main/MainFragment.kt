package dk.birkb85.ceilingledclient.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class MainFragment : Fragment() {
    private var mBlinkButton: Button? = null
    private var mWipeButton: Button? = null
    private var mTheaterChaseButton: Button? = null
    private var mRainbowButton: Button? = null
    private var mTheaterChaseRainbowButton: Button? = null

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        // Set views
        mBlinkButton = activity?.findViewById(R.id.blinkButton)
        mWipeButton = activity?.findViewById(R.id.wipeButton)
        mTheaterChaseButton = activity?.findViewById(R.id.theaterChaseButton)
        mRainbowButton = activity?.findViewById(R.id.rainbowButton)
        mTheaterChaseRainbowButton = activity?.findViewById(R.id.theaterChaseRainbowButton)

        mBlinkButton?.setOnClickListener(blinkButtonOnClickListener)
        mWipeButton?.setOnClickListener(wipeButtonOnClickListener)
        mTheaterChaseButton?.setOnClickListener(theaterChaseButtonOnClickListener)
        mRainbowButton?.setOnClickListener(rainbowButtonOnClickListener)
        mTheaterChaseRainbowButton?.setOnClickListener(theaterChaseRainbowButtonOnClickListener)
    }

    override fun onResume() {
        super.onResume()
        Global.tcpConnection.bindMessageReceivedListener(messageReceivedListener)
    }

    override fun onPause() {
        super.onPause()
        Global.tcpConnection.unbindMessageReceivedListener()
    }

    private val messageReceivedListener: TCPConnection.MessageReceivedListener = object: TCPConnection.MessageReceivedListener {
        override fun statusChanged(status: TCPConnection.Status) {
//            activity?.runOnUiThread {
//                //Log.d("DEBUG", "Main Status: $status")
//            }
        }

        override fun messageReceived(message: String) {
//            activity?.runOnUiThread {
//                //Log.d("DEBUG", "Main Message: $message")
//            }
        }
    }

    private val blinkButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_MAIN + ":" +
                Global.DATA_MAIN_SET_MODE + ":" +
                Global.MODE_MAIN_BLINK + ";")
    }

    private val wipeButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_MAIN + ":" +
                Global.DATA_MAIN_SET_MODE + ":" +
                Global.MODE_MAIN_WIPE + ";")
    }

    private val theaterChaseButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_MAIN + ":" +
                Global.DATA_MAIN_SET_MODE + ":" +
                Global.MODE_MAIN_THEATER_CHASE + ";")
        Log.d("DEBUG", "theaterChaseButtonOnClickListener")
    }

    private val rainbowButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_MAIN + ":" +
                Global.DATA_MAIN_SET_MODE + ":" +
                Global.MODE_MAIN_RAINBOW + ";")
    }

    private val theaterChaseRainbowButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_MAIN + ":" +
                Global.DATA_MAIN_SET_MODE + ":" +
                Global.MODE_MAIN_THEATER_CHASE_RAINBOW + ";")
    }
}
