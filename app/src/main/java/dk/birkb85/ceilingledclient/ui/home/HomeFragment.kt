package dk.birkb85.ceilingledclient.ui.home

import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import dk.birkb85.ceilingledclient.PongActivity
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global

class HomeFragment : Fragment() {
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
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        // Set views
        mMainButton = activity?.findViewById(R.id.mainButton)
        mPongButton = activity?.findViewById(R.id.pongButton)

        pongDialogInit()
        mMainButton?.setOnClickListener(mainButtonOnClickListener)
        mPongButton?.setOnClickListener(pongButtonOnClickListener)
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.mPongDialogIsShowing) viewModel.mPongDialog?.show()
    }

    override fun onPause() {
        super.onPause()

        viewModel.mPongDialogIsShowing = false
        viewModel.mPongDialog.let {
            if (it != null && it.isShowing) {
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

    private val mainButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_SET_MODE + ":" + Global.MODE_MAIN + ";")
    }

    private val pongButtonOnClickListener = View.OnClickListener {
        Global.tcpConnection.sendMessage(Global.DATA_SET_MODE + ":" + Global.MODE_PONG + ";")
        viewModel.mPongDialog?.show()
    }
}
