package dk.birkb85.ceilingledclient.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import dk.birkb85.ceilingledclient.ConnectionSetupActivity
import dk.birkb85.ceilingledclient.PongActivity
import dk.birkb85.ceilingledclient.R

class MainFragment : Fragment() {
    private var mPongButton: Button? = null

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

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
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        // Set views
        mPongButton = activity?.findViewById(R.id.pongButton)

        pongDialogInit()
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
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as Activity).layoutInflater
        val inflaterView = inflater.inflate(R.layout.dialog_pong, null)
        alertDialogBuilder.setView(inflaterView)
        //alertDialogBuilder.setCancelable(false)
        viewModel.mPongDialog = alertDialogBuilder.create()

        val player1Button = inflaterView.findViewById(R.id.player1Button) as Button?
        val player2Button = inflaterView.findViewById(R.id.player2Button) as Button?
        val player12Button = inflaterView.findViewById(R.id.player12Button) as Button?

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

    private val pongButtonOnClickListener = View.OnClickListener {
        viewModel.mPongDialog?.show()
    }
}
