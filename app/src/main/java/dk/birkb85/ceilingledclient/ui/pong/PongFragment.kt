package dk.birkb85.ceilingledclient.ui.pong

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import android.widget.Space
import androidx.fragment.app.Fragment
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class PongFragment : Fragment() {
    private var mPlayer1Button: View? = null
    private var mPlayer2Button: View? = null
    private var mButtonSpace: Space? = null

    private var mPlayer1Active = false
    private var mPlayer2Active = false

    companion object {
        fun newInstance() = PongFragment()
    }

    private lateinit var viewModel: PongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.intent?.let {
            mPlayer1Active = it.getBooleanExtra("Player1Active", false)
            mPlayer2Active = it.getBooleanExtra("Player2Active", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.pong_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PongViewModel::class.java)

        // Set views
        mPlayer1Button = activity?.findViewById(R.id.player1Button)
        mPlayer2Button = activity?.findViewById(R.id.player2Button)
        mButtonSpace = activity?.findViewById(R.id.buttonSpace)

        if (!mPlayer1Active) {
            mPlayer1Button?.visibility = View.GONE
            mButtonSpace?.visibility = View.GONE
        }
        if (!mPlayer2Active) {
            mPlayer2Button?.visibility = View.GONE
            mButtonSpace?.visibility = View.GONE
        }

        mPlayer1Button?.setOnTouchListener(player1ButtonOnTouchListener)
        mPlayer2Button?.setOnTouchListener(player2ButtonOnTouchListener)
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
            activity?.runOnUiThread {
                //Log.d("DEBUG", "Pong Status: $status")
            }
        }

        override fun messageReceived(message: String) {
            activity?.runOnUiThread {
                //Log.d("DEBUG", "Pong Message: $message")
            }
        }
    }

    private var player1ButtonOnTouchListener = View.OnTouchListener { view, motionEvent ->
        if(motionEvent.action == MotionEvent.ACTION_DOWN){
            //Log.d("DEBUG", "Player 1 ACTION_DOWN")
            mPlayer1Button?.setBackgroundColor(resources.getColor(R.color.colorP1ButtonDown))
//            Global.tcpConnection.sendMessage("PONG:P1=1;")
            Global.tcpConnection.sendMessage(Global.DATA_PONG + ":" + Global.DATA_PONG_P1_PRESSED + ";")
            return@OnTouchListener true
        }

        if(motionEvent.action == MotionEvent.ACTION_UP){
            //Log.d("DEBUG", "Player 1 ACTION_UP")
            mPlayer1Button?.setBackgroundColor(resources.getColor(R.color.colorP1ButtonUp))
//            Global.tcpConnection.sendMessage("PONG:P1=0;")
            Global.tcpConnection.sendMessage(Global.DATA_PONG + ":" + Global.DATA_PONG_P1_RELEASED + ";")
            return@OnTouchListener true
        }

        return@OnTouchListener true
    }

    private var player2ButtonOnTouchListener = View.OnTouchListener { view, motionEvent ->
        if(motionEvent.action == MotionEvent.ACTION_DOWN){
            //Log.d("DEBUG", "Player 2 ACTION_DOWN")
            mPlayer2Button?.setBackgroundColor(resources.getColor(R.color.colorP2ButtonDown))
//            Global.tcpConnection.sendMessage("PONG:P2=1;")
            Global.tcpConnection.sendMessage(Global.DATA_PONG + ":" + Global.DATA_PONG_P2_PRESSED + ";")
            return@OnTouchListener true
        }

        if(motionEvent.action == MotionEvent.ACTION_UP){
            //Log.d("DEBUG", "Player 2 ACTION_UP")
            mPlayer2Button?.setBackgroundColor(resources.getColor(R.color.colorP2ButtonUp))
//            Global.tcpConnection.sendMessage("PONG:P2=0;")
            Global.tcpConnection.sendMessage(Global.DATA_PONG + ":" + Global.DATA_PONG_P2_RELEASED + ";")
            return@OnTouchListener true
        }

        return@OnTouchListener true
    }
}
