package dk.birkb85.ceilingledclient.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dk.birkb85.ceilingledclient.MainActivity
import dk.birkb85.ceilingledclient.PongActivity
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global

class HomeFragment : Fragment() {
    private var mBrightnessSeekBar: SeekBar? = null
    private var mMainButton: Button? = null
    private var mPongButton: Button? = null

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    /**
//     * Method called when option is selection in option menu.
//     * @param item the item selected.
//     * @return return true if 'item selected' event is handled here, else return the event.
//     */
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_settings -> {
//                val intent = Intent(context, ConnectionSetupActivity::class.java)
//                startActivity(intent)
//                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                return true
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Set views
        mBrightnessSeekBar = activity?.findViewById(R.id.brightnessSeekBar)
        mMainButton = activity?.findViewById(R.id.mainButton)
        mPongButton = activity?.findViewById(R.id.pongButton)

        mBrightnessSeekBar?.max = 129
        mBrightnessSeekBar?.setOnSeekBarChangeListener(brightnessSeekBarOnSeekBarChangeListener)

        pongDialogInit()
        mMainButton?.setOnClickListener(mainButtonOnClickListener)
        mPongButton?.setOnClickListener(pongButtonOnClickListener)
//        mPongButton?.setBackgroundResource(R.drawable.blink_animation)
//        (mPongButton?.background as AnimationDrawable).start()

//        lateinit var rocketAnimation: AnimationDrawable
//        val rocketImage = activity?.findViewById<ImageView>(R.id.rocket_image)?.apply {
//            setBackgroundResource(R.drawable.blink_animation)
//            rocketAnimation = background as AnimationDrawable
//        }
//        rocketImage?.setOnClickListener { rocketAnimation.start() }
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.mPongDialogIsShowing) viewModel.mPongDialog?.show()
    }

    override fun onPause() {
        super.onPause()

        viewModel.mPongDialogIsShowing = false
        viewModel.mPongDialog?.let {
            if (it.isShowing) {
                viewModel.mPongDialogIsShowing = true
                viewModel.mPongDialog?.dismiss()
            }
        }
    }

    private fun pongDialogInit() {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val inflaterView = activity?.layoutInflater?.inflate(R.layout.dialog_pong, null, false)
        alertDialogBuilder.setView(inflaterView)
        //alertDialogBuilder.setCancelable(false)
        viewModel.mPongDialog = alertDialogBuilder.create()

        val player1Button: Button? = inflaterView?.findViewById(R.id.player1Button)
        val player2Button: Button? = inflaterView?.findViewById(R.id.player2Button)
        val player12Button: Button? = inflaterView?.findViewById(R.id.player12Button)

        player1Button?.setOnClickListener {
            viewModel.mPongDialog?.dismiss()

            val intent = Intent(context, PongActivity::class.java)
            intent.putExtra("Player1Active", true)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        player2Button?.setOnClickListener {
            viewModel.mPongDialog?.dismiss()

            val intent = Intent(context, PongActivity::class.java)
            intent.putExtra("Player2Active", true)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        player12Button?.setOnClickListener {
            viewModel.mPongDialog?.dismiss()

            val intent = Intent(context, PongActivity::class.java)
            intent.putExtra("Player1Active", true)
            intent.putExtra("Player2Active", true)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private val brightnessSeekBarOnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val systemTimeCurrent = System.currentTimeMillis()
                if (systemTimeCurrent - viewModel.mSystemTimeInterval > viewModel.mSystemTimeLast) {
                    viewModel.mSystemTimeLast = systemTimeCurrent
                    Global.tcpConnection.sendMessage(
                        Global.DATA_SET_BRIGHTNESS + ":" +
                                p1.toString() + ";"
                    )
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.progress?.let {
                    Global.tcpConnection.sendMessage(
                        Global.DATA_SET_BRIGHTNESS + ":" +
                                it.toString() + ";"
                    )
                }
            }
        }

    private val mainButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_SET_MODE + ":" + Global.MODE_MAIN + ";")
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private val pongButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_SET_MODE + ":" + Global.MODE_PONG + ";")
        viewModel.mPongDialog?.show()
    }
}
