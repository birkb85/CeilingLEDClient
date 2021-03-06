package dk.birkb85.ceilingledclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import dk.birkb85.ceilingledclient.ui.connectionStatus.ConnectionStatusFragment
import dk.birkb85.ceilingledclient.ui.home.HomeFragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_connectionStatus, ConnectionStatusFragment.newInstance())
                .commitNow()

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commitNow()
        }
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

//    /**
//     * Method called when options menu is created.
//     * @param menu the menu instance object created.
//     * @return true if menu is created
//     */
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_home, menu)
//        return true
//    }
}
