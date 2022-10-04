package xyz.uchiha.remotto.ui

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.uchiha.remotto.R
import xyz.uchiha.remotto.background.MessageCodes
import xyz.uchiha.remotto.background.Utils
import xyz.uchiha.remotto.databinding.ActivityMainBinding
import xyz.uchiha.remotto.databinding.ModalBottomSheetContentBinding
import xyz.uchiha.remotto.viewModel.MainViewModel
import xyz.uchiha.remotto.viewModel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private val TAG: String = this.javaClass.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var model: MainViewModel
    private lateinit var vibrator: Vibrator
    private val modalBottomSheet = ModalBottomSheet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        setupComponents()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        model.ping()
    }

    private fun initViewModel(){
        // get user data
        val clientName = Utils.getString(this, Utils.DEVICE_KEY)
        val ip: String = Utils.getString(this, Utils.IP_KEY)
        val mac: String = Utils.getString(this, Utils.MAC_KEY)

        val model: MainViewModel by viewModels { MainViewModelFactory(clientName, ip, mac) }
        this.model = model

        model.getStateLiveData().observe(this) { state ->
            if (state)
                modalBottomSheet.serverStateText = getString(R.string.on)
            else
                modalBottomSheet.serverStateText = getString(R.string.off)
        }

        model.ping()
    }

    private fun setupComponents() {
        Log.d(TAG, "Setting listeners.")

        // set listeners
        binding.btnPowerOff.setOnClickListener { onButtonClick(MessageCodes.SHUTDOWN) }
        binding.btnTurnOn.setOnClickListener { vibrate(); model.sendWOLPkg() }

        binding.btnUp.setOnClickListener { onButtonClick(MessageCodes.UP) }
        binding.btnRight.setOnClickListener { onButtonClick(MessageCodes.RIGHT) }
        binding.btnDown.setOnClickListener { onButtonClick(MessageCodes.DOWN) }
        binding.btnLeft.setOnClickListener { onButtonClick(MessageCodes.LEFT) }
        binding.btnOk.setOnClickListener { onButtonClick(MessageCodes.ENTER) }

        binding.btnVolUp.setOnClickListener { onButtonClick(MessageCodes.VOL_UP) }
        binding.btnVolDown.setOnClickListener { onButtonClick(MessageCodes.VOL_DOWN) }
        binding.btnNext.setOnClickListener { onButtonClick(MessageCodes.NEXT) }
        binding.btnPrevious.setOnClickListener { onButtonClick(MessageCodes.PREVIOUS) }

        binding.btnMute.setOnClickListener { onButtonClick(MessageCodes.MUTE) }
        binding.btnPlay.setOnClickListener { onButtonClick(MessageCodes.PLAY) }
        binding.btnStop.setOnClickListener { onButtonClick(MessageCodes.STOP) }

        binding.btnSettings.setOnClickListener { startSettingsActivity() }
        binding.btnKeyboard.setOnClickListener { startMKActivity() }
        binding.btnMouse.setOnClickListener { startMouseActivity() }
        binding.btnMore.setOnClickListener { modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG) }
        modalBottomSheet.btnMoreZoomListener = View.OnClickListener { onButtonClick(MessageCodes.ZOOM_IN) }
        modalBottomSheet.btnMinusZoomListener = View.OnClickListener { onButtonClick(MessageCodes.ZOOM_OUT) }

        // set long click listeners
        binding.btnUp.setOnLongClickListener { onButtonLongClick(MessageCodes.UP); false }
        binding.btnRight.setOnLongClickListener { onButtonLongClick(MessageCodes.RIGHT); false }
        binding.btnDown.setOnLongClickListener { onButtonLongClick(MessageCodes.DOWN); false }
        binding.btnLeft.setOnLongClickListener { onButtonLongClick(MessageCodes.LEFT); false }

        binding.btnVolUp.setOnLongClickListener { onButtonLongClick(MessageCodes.VOL_UP); false }
        binding.btnVolDown.setOnLongClickListener { onButtonLongClick(MessageCodes.VOL_DOWN); false }
    }

    private fun onButtonClick(code: MessageCodes){
        // case false is returning from longClickListener, so should be ignored
        if(! model.cancelPkgLoop(code)) {
            vibrate()
            model.sendPkg(code)
        }
    }

    private fun onButtonLongClick(code: MessageCodes){
        //Utils.deleteString(this, Utils.IP_KEY)
        //Utils.deleteString(this, Utils.MAC_KEY)

        vibrate(20)
        model.sendPkgLoop(code)
    }

    private fun startSettingsActivity(){
        getResult.launch(Intent(this, SettingsActivity::class.java))
    }

    private fun startMKActivity(){
        startActivity(Intent(this, MKActivity::class.java))
    }

    private fun startMouseActivity(){
        startActivity(Intent(this, MouseActivity::class.java))
    }

    private fun vibrate(duration: Long = 10){
        vibrator.vibrate(duration)
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            Log.d(TAG, "getResult: pairing information updated.")
            val device = Utils.getString(this, Utils.DEVICE_KEY)
            val ip = Utils.getString(this, Utils.IP_KEY)
            val mac = Utils.getString(this, Utils.MAC_KEY)
            model.updateData(device, ip, mac)
        }
    }
}

class ModalBottomSheet : BottomSheetDialogFragment() {
    lateinit var binding: ModalBottomSheetContentBinding
    var btnMoreZoomListener: View.OnClickListener? = null
    var btnMinusZoomListener: View.OnClickListener? = null
    var serverStateText: String = "Unknown"
        set(value) {
            if (this::binding.isInitialized)
                binding.serverState.text = value

            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val view = inflater.inflate(R.layout.modal_bottom_sheet_content, container, false)

        binding = ModalBottomSheetContentBinding.inflate(inflater)
        setupElements()

        return binding.root
    }

    private fun setupElements() {
        binding.btnMoreZoom.setOnClickListener(btnMoreZoomListener)
        binding.btnMinusZoom.setOnClickListener(btnMinusZoomListener)
        binding.serverState.text = serverStateText
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}