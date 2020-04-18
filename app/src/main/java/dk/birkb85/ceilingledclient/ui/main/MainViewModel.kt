package dk.birkb85.ceilingledclient.ui.main

import android.app.AlertDialog
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var mMainModesDialog: AlertDialog? = null
    var mMainModesDialogIsShowing: Boolean = false

    var mLoopIntervalTimeLast: Long = 0
    var mLoopIntervalTimeInterval = 25

    var mSelectedMode = ""

    var mColorTimeLast: Long = 0
    var mColorTimeInterval = 25
}
