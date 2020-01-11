package dk.birkb85.ceilingledclient.ui.pong

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import dk.birkb85.ceilingledclient.R

class PongFragment : Fragment() {

    private var mPlayer1Active = false // TODO BB 2020-01-11. Implement.
    private var mPlayer2Active = false // TODO BB 2020-01-11. Implement.

    companion object {
        fun newInstance() = PongFragment()
    }

    private lateinit var viewModel: PongViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.pong_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PongViewModel::class.java)


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}
