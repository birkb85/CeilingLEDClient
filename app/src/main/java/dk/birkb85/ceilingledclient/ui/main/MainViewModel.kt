package dk.birkb85.ceilingledclient.ui.main

import android.app.AlertDialog
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var mPongDialog: AlertDialog? = null
    var mPongDialogIsShowing: Boolean = false
}
