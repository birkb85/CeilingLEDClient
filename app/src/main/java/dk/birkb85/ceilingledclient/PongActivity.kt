package dk.birkb85.ceilingledclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import dk.birkb85.ceilingledclient.models.SystemUI
import dk.birkb85.ceilingledclient.ui.connectionStatus.ConnectionStatusFragment
import dk.birkb85.ceilingledclient.ui.main.MainFragment
import dk.birkb85.ceilingledclient.ui.pong.PongFragment

class PongActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pong_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_connectionStatus, ConnectionStatusFragment.newInstance())
                .commitNow()

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PongFragment.newInstance())
                .commitNow()
        }

        SystemUI.hideImmersiveSticky(window)
    }

    /**
     * Method called when key is pressed on device.
     * @param keyCode the key code of the key.
     * @param event the event of the key.
     * @return returns true if key is handled in method, else event is returned.
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
