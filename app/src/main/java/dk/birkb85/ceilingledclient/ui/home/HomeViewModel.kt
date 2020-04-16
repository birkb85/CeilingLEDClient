package dk.birkb85.ceilingledclient.ui.home

import android.app.AlertDialog
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var mPongDialog: AlertDialog? = null
    var mPongDialogIsShowing: Boolean = false

    var mSystemTimeLast: Long = 0
    var mSystemTimeInterval = 25
}
