package dk.birkb85.ceilingledclient.ui.main

import android.app.AlertDialog
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var mMainModesDialog: AlertDialog? = null
    var mMainModesDialogIsShowing: Boolean = false

    var mSelectedMode = ""

    var mSystemTimeLast: Long = 0
    var mSystemTimeInterval = 25
}
