package dk.birkb85.ceilingledclient.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection


class MainFragment : Fragment() {
    private var mModeButton: Button? = null
    private var mColorPickerCardView: CardView? = null
    private var mColorPickerView: ColorPickerView? = null

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
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Set views
        mModeButton = activity?.findViewById(R.id.modeButton)
        mColorPickerCardView = activity?.findViewById(R.id.colorPickerCardView)
        mColorPickerView = activity?.findViewById(R.id.colorPickerView)

        selectMode()

        mColorPickerView?.colorListener = colorEnvelopeListener

        mainModesDialogInit()
        mModeButton?.setOnClickListener(modeButtonOnClickListener)
    }

    override fun onResume() {
        super.onResume()
        Global.tcpConnection.bindMessageReceivedListener(messageReceivedListener)

        if (viewModel.mMainModesDialogIsShowing) viewModel.mMainModesDialog?.show()
    }

    override fun onPause() {
        super.onPause()
        Global.tcpConnection.unbindMessageReceivedListener()

        viewModel.mMainModesDialogIsShowing = false
        viewModel.mMainModesDialog.let {
            if (it != null && it.isShowing) {
                viewModel.mMainModesDialogIsShowing = true
                viewModel.mMainModesDialog?.dismiss()
            }
        }
    }

    private val messageReceivedListener: TCPConnection.MessageReceivedListener =
        object : TCPConnection.MessageReceivedListener {
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

    private fun mainModesDialogInit() {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val inflaterView = activity?.layoutInflater?.inflate(R.layout.dialog_main_modes, null, false)
        alertDialogBuilder.setView(inflaterView)
        //alertDialogBuilder.setCancelable(false)
        viewModel.mMainModesDialog = alertDialogBuilder.create()

        val blinkButton: Button? = inflaterView?.findViewById(R.id.blinkButton)
        val wipeButton: Button? = inflaterView?.findViewById(R.id.wipeButton)
        val theaterChaseButton: Button? = inflaterView?.findViewById(R.id.theaterChaseButton)
        val rainbowButton: Button? = inflaterView?.findViewById(R.id.rainbowButton)
        val theaterChaseRainbowButton: Button? = inflaterView?.findViewById(R.id.theaterChaseRainbowButton)

        blinkButton?.setOnClickListener {
            viewModel.mMainModesDialog?.dismiss()

            Global.tcpConnection.sendMessage(
                Global.DATA_MAIN + ":" +
                        Global.DATA_MAIN_SET_MODE + ":" +
                        Global.MODE_MAIN_BLINK + ";"
            )

            viewModel.mSelectedMode = Global.MODE_MAIN_BLINK
            selectMode()
        }

        wipeButton?.setOnClickListener {
            viewModel.mMainModesDialog?.dismiss()

            Global.tcpConnection.sendMessage(
                Global.DATA_MAIN + ":" +
                        Global.DATA_MAIN_SET_MODE + ":" +
                        Global.MODE_MAIN_WIPE + ";"
            )

            viewModel.mSelectedMode = Global.MODE_MAIN_WIPE
            selectMode()
        }

        theaterChaseButton?.setOnClickListener {
            viewModel.mMainModesDialog?.dismiss()

            Global.tcpConnection.sendMessage(
                Global.DATA_MAIN + ":" +
                        Global.DATA_MAIN_SET_MODE + ":" +
                        Global.MODE_MAIN_THEATER_CHASE + ";"
            )

            viewModel.mSelectedMode = Global.MODE_MAIN_THEATER_CHASE
            selectMode()
        }

        rainbowButton?.setOnClickListener {
            viewModel.mMainModesDialog?.dismiss()

            Global.tcpConnection.sendMessage(
                Global.DATA_MAIN + ":" +
                        Global.DATA_MAIN_SET_MODE + ":" +
                        Global.MODE_MAIN_RAINBOW + ";"
            )

            viewModel.mSelectedMode = Global.MODE_MAIN_RAINBOW
            selectMode()
        }

        theaterChaseRainbowButton?.setOnClickListener {
            viewModel.mMainModesDialog?.dismiss()

            Global.tcpConnection.sendMessage(
                Global.DATA_MAIN + ":" +
                        Global.DATA_MAIN_SET_MODE + ":" +
                        Global.MODE_MAIN_THEATER_CHASE_RAINBOW + ";"
            )

            viewModel.mSelectedMode = Global.MODE_MAIN_THEATER_CHASE_RAINBOW
            selectMode()
        }
    }

    private fun selectMode() {
        when(viewModel.mSelectedMode) {
            "" -> {
                mModeButton?.text = getString(R.string.main_chooseMode)
                mColorPickerCardView?.visibility = View.GONE
            }
            Global.MODE_MAIN_BLINK -> {
                mModeButton?.text = getString(R.string.dialogMainModes_blink)
                mColorPickerCardView?.visibility = View.VISIBLE
            }
            Global.MODE_MAIN_WIPE -> {
                mModeButton?.text = getString(R.string.dialogMainModes_wipe)
                mColorPickerCardView?.visibility = View.GONE
            }
            Global.MODE_MAIN_THEATER_CHASE -> {
                mModeButton?.text = getString(R.string.dialogMainModes_theaterChase)
                mColorPickerCardView?.visibility = View.GONE
            }
            Global.MODE_MAIN_RAINBOW -> {
                mModeButton?.text = getString(R.string.dialogMainModes_rainbow)
                mColorPickerCardView?.visibility = View.GONE
            }
            Global.MODE_MAIN_THEATER_CHASE_RAINBOW -> {
                mModeButton?.text = getString(R.string.dialogMainModes_theaterChaseRainbow)
                mColorPickerCardView?.visibility = View.GONE
            }
        }
    }

    private val modeButtonOnClickListener = View.OnClickListener {
        viewModel.mMainModesDialog?.show()
    }

    private val colorEnvelopeListener = ColorEnvelopeListener { envelope, fromUser ->
        if (envelope.argb.size == 4) {
            val systemTimeCurrent = System.currentTimeMillis()
            if (systemTimeCurrent - viewModel.mSystemTimeInterval > viewModel.mSystemTimeLast) {
                viewModel.mSystemTimeLast = systemTimeCurrent
                val color =
                    (envelope.argb[1].shl(16) + envelope.argb[2].shl(8) + envelope.argb[3]).toString()
                Global.tcpConnection.sendMessage(
                    Global.DATA_MAIN + ":" +
                            Global.DATA_MAIN_COLOR + ":" +
                            color + ";"
                )

//                var text = "color argb: "
//                for (i in envelope.argb) {
//                    text += "$i, "
//                }
//                Log.d("DEBUG", text)
//                mColorPickerView?.setBackgroundColor(envelope.color)
            }
        }
    }
}
