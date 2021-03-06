package dk.birkb85.ceilingledclient.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dk.birkb85.ceilingledclient.R
import dk.birkb85.ceilingledclient.models.Compass
import dk.birkb85.ceilingledclient.models.Global
import dk.birkb85.ceilingledclient.models.TCPConnection

class MainFragment : Fragment() {
    private var mLoopIntervalSeekBar: SeekBar? = null
    private var mStripClearOffButton: Button? = null
    private var mStripClearOnButton: Button? = null
    private var mModeButton: Button? = null
    private var mColorPickerCardView: CardView? = null
    private var mColorPickerView: ColorPickerView? = null

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var mCompass: Compass

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
        mLoopIntervalSeekBar = activity?.findViewById(R.id.loopIntervalSeekBar)
        mStripClearOffButton = activity?.findViewById(R.id.stripClearOffButton)
        mStripClearOnButton = activity?.findViewById(R.id.stripClearOnButton)
        mModeButton = activity?.findViewById(R.id.modeButton)
        mColorPickerCardView = activity?.findViewById(R.id.colorPickerCardView)
        mColorPickerView = activity?.findViewById(R.id.colorPickerView)

        mLoopIntervalSeekBar?.max = 179
        mLoopIntervalSeekBar?.setOnSeekBarChangeListener(loopIntervalSeekBarOnSeekBarChangeListener)

        mStripClearOffButton?.setOnClickListener(stripClearOffButtonOnClickListener)
        mStripClearOnButton?.setOnClickListener(stripClearOnButtonOnClickListener)

        mainModesDialogInit()
        mModeButton?.setOnClickListener(modeButtonOnClickListener)

        mColorPickerView?.colorListener = colorEnvelopeListener

        mCompass = Compass(context)

        selectMode()
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
        viewModel.mMainModesDialog?.let {
            if (it.isShowing) {
                viewModel.mMainModesDialogIsShowing = true
                viewModel.mMainModesDialog?.dismiss()
            }
        }

        mCompass.onPause()
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
        val inflaterView =
            activity?.layoutInflater?.inflate(R.layout.dialog_main_modes, null, false)
        alertDialogBuilder.setView(inflaterView)
        //alertDialogBuilder.setCancelable(false)
        viewModel.mMainModesDialog = alertDialogBuilder.create()

        val blinkButton: Button? = inflaterView?.findViewById(R.id.blinkButton)
        val wipeButton: Button? = inflaterView?.findViewById(R.id.wipeButton)
        val theaterChaseButton: Button? = inflaterView?.findViewById(R.id.theaterChaseButton)
        val rainbowButton: Button? = inflaterView?.findViewById(R.id.rainbowButton)
        val theaterChaseRainbowButton: Button? =
            inflaterView?.findViewById(R.id.theaterChaseRainbowButton)
        val compassButton: Button? = inflaterView?.findViewById(R.id.compassButton)

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

        compassButton?.setOnClickListener {
            viewModel.mMainModesDialog?.dismiss()

            Global.tcpConnection.sendMessage(
                Global.DATA_MAIN + ":" +
                        Global.DATA_MAIN_SET_MODE + ":" +
                        Global.MODE_MAIN_COMPASS + ";"
            )

            viewModel.mSelectedMode = Global.MODE_MAIN_COMPASS
            selectMode()
        }
    }

    private fun selectMode() {
        when (viewModel.mSelectedMode) {
            "" -> {
                mModeButton?.text = getString(R.string.main_chooseMode)
                mColorPickerCardView?.visibility = View.GONE
                mCompass.onPause()
            }
            Global.MODE_MAIN_BLINK -> {
                mModeButton?.text = getString(R.string.dialogMainModes_blink)
                mColorPickerCardView?.visibility = View.VISIBLE
                mCompass.onPause()
            }
            Global.MODE_MAIN_WIPE -> {
                mModeButton?.text = getString(R.string.dialogMainModes_wipe)
                mColorPickerCardView?.visibility = View.VISIBLE
                mCompass.onPause()
            }
            Global.MODE_MAIN_THEATER_CHASE -> {
                mModeButton?.text = getString(R.string.dialogMainModes_theaterChase)
                mColorPickerCardView?.visibility = View.VISIBLE
                mCompass.onPause()
            }
            Global.MODE_MAIN_RAINBOW -> {
                mModeButton?.text = getString(R.string.dialogMainModes_rainbow)
                mColorPickerCardView?.visibility = View.GONE
                mCompass.onPause()
            }
            Global.MODE_MAIN_THEATER_CHASE_RAINBOW -> {
                mModeButton?.text = getString(R.string.dialogMainModes_theaterChaseRainbow)
                mColorPickerCardView?.visibility = View.GONE
                mCompass.onPause()
            }
            Global.MODE_MAIN_COMPASS -> {
                mModeButton?.text = getString(R.string.dialogMainModes_compass)
                mColorPickerCardView?.visibility = View.VISIBLE
                mCompass.onResume(compassUpdateListener)
            }
        }
    }

    private val loopIntervalSeekBarOnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val systemTimeCurrent = System.currentTimeMillis()
                if (systemTimeCurrent - viewModel.mLoopIntervalTimeInterval > viewModel.mLoopIntervalTimeLast) {
                    viewModel.mLoopIntervalTimeLast = systemTimeCurrent
                    p0?.let {
                        Global.tcpConnection.sendMessage(
                            Global.DATA_SET_LOOP_INTERVAL + ":" +
                                    (it.max - it.progress).toString() + ";"
                        )
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    Global.tcpConnection.sendMessage(
                        Global.DATA_SET_LOOP_INTERVAL + ":" +
                                (it.max - it.progress).toString() + ";"
                    )
                }
            }
        }

    private val stripClearOffButtonOnClickListener = View.OnClickListener {
        mStripClearOffButton?.isEnabled = false
        mStripClearOnButton?.isEnabled = true

        Global.tcpConnection.sendMessage(Global.DATA_SET_STRIP_CLEAR + ":0;")
    }

    private val stripClearOnButtonOnClickListener = View.OnClickListener {
        mStripClearOffButton?.isEnabled = true
        mStripClearOnButton?.isEnabled = false

        Global.tcpConnection.sendMessage(Global.DATA_SET_STRIP_CLEAR + ":1;")
    }

    private val modeButtonOnClickListener = View.OnClickListener {
        viewModel.mMainModesDialog?.show()
    }

    private val colorEnvelopeListener = ColorEnvelopeListener { envelope, fromUser ->
        if (envelope.argb.size == 4) {
            val systemTimeCurrent = System.currentTimeMillis()
            if (systemTimeCurrent - viewModel.mColorTimeInterval > viewModel.mColorTimeLast) {
                viewModel.mColorTimeLast = systemTimeCurrent
                val color =
                    (envelope.argb[1].shl(16) + envelope.argb[2].shl(8) + envelope.argb[3]).toString()
                if (color != "0")
                    Global.tcpConnection.sendMessage(
                        Global.DATA_MAIN + ":" +
                                Global.DATA_MAIN_COLOR + ":" +
                                color + ";"
                    )

//                if (envelope.argb[0] != 255)
//                    Log.d("DEBUG", "envelope.argb[0] != 255: ${envelope.argb[0]}")

//                if (color == "0")
//                    Log.d("DEBUG", "color == \"0\": $color, argb[0]: ${envelope.argb[0]}")

//                var text = "color argb: "
//                for (i in envelope.argb) {
//                    text += "$i, "
//                }
//                Log.d("DEBUG", text)
            }
        }
    }

    private val compassUpdateListener: Compass.UpdateListener = object : Compass.UpdateListener {
        override fun onUpdate(azimuth: Float, pitch: Float, roll: Float) {
            val systemTimeCurrent = System.currentTimeMillis()
            if (systemTimeCurrent - viewModel.mCompassTimeInterval > viewModel.mCompassTimeLast) {
                viewModel.mCompassTimeLast = systemTimeCurrent

                val ledNumber =
                    (((2 * Math.PI) - (azimuth + Math.PI)) / ((2 * Math.PI) / 592)).toInt()
                        .toString()

                Global.tcpConnection.sendMessage(
                    Global.DATA_MAIN + ":" +
                            Global.DATA_MAIN_X + ":" +
                            ledNumber + ";"
                )
            }
        }
    }
}
