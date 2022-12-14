package xyz.uchiha.remotto.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import xyz.uchiha.remotto.background.MessageCodes
import xyz.uchiha.remotto.background.Utils
import xyz.uchiha.remotto.databinding.ActivityMKBinding
import xyz.uchiha.remotto.viewModel.MainViewModel
import xyz.uchiha.remotto.viewModel.MainViewModelFactory
import kotlin.math.abs
import kotlin.math.absoluteValue

class MKActivity : AppCompatActivity() {
    private val TAG: String = this.javaClass.simpleName
    private val REQUEST_CODE = 3651
    private lateinit var binding: ActivityMKBinding
    private lateinit var model: MainViewModel

    private var currentTouchPosX: Int = 0
    private var currentTouchPosY: Int = 0
    private var isTouchDoubleFingers = false
    private var isTouchMove = false
    private var isVerticalScroll = false
    private var isHold = false
    private val VERTICAL_SCROLL_OFFSET: Float = 5f
    private var moveSequence = 0
    private var clickEventCounter = 0
    private var verticalScrollMean = 0
    private var isDelayedHandlerActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMKBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setupComponents()
        initViewModel()
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel: ")
        // get user data
        val clientName = Utils.getString(this, Utils.DEVICE_KEY)
        val ip: String = Utils.getString(this, Utils.IP_KEY)
        val mac: String = Utils.getString(this, Utils.MAC_KEY)

        val model: MainViewModel by viewModels { MainViewModelFactory(clientName, ip, mac) }
        this.model = model

        model.ping()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupComponents() {
        Log.d(TAG, "setupComponents: ")

        // force android keyboard to use action done instead of new line
        binding.keyboardText.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.keyboardText.setRawInputType(InputType.TYPE_CLASS_TEXT)

        // set button listeners
        binding.btnSend.setOnClickListener { onBtnEnterClick() }
        binding.btnCopy.setOnClickListener { onButtonClick(MessageCodes.COPY) }
        binding.btnPaste.setOnClickListener { onButtonClick(MessageCodes.PASTE) }
        //binding.btnZoomIn.setOnClickListener { onButtonClick(MessageCodes.ZOOM_IN) }
        //binding.btnZoomOut.setOnClickListener { onButtonClick(MessageCodes.ZOOM_OUT) } binding.btnBackspace.setOnClickListener { onButtonClick(MessageCodes.BACKSPACE) }
        binding.btnTouchLeft.setOnClickListener { onButtonClick(MessageCodes.LEFT_CLICK) }
        binding.btnTouchRight.setOnClickListener { onButtonClick(MessageCodes.RIGHT_CLICK) }

        // ----------------------------------- set keyboard listener ----------------------------------- //
        binding.keyboardText.setOnEditorActionListener{ _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                onKeyboardSubmit()
                true
            }
            else false
        }

        // ----------------------------------- set touchpad listener ----------------------------------- //
        var isDoubleFingers = false
        var isMove = false

        binding.touchpad.setOnTouchListener{ v, event ->
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN -> onTouchStartAction(event.x, event.y)
                MotionEvent.ACTION_POINTER_DOWN -> onTouchDoubleFingers()
                MotionEvent.ACTION_MOVE -> onTouchMoveAction(event.x, event.y)
                MotionEvent.ACTION_UP -> onTouchFinishAction(v, event.x,event.y)
            }

            true
        }
    }

    private fun onKeyboardSubmit() {
        Log.d(TAG, "onKeyboardSubmit: ")
        val text = binding.keyboardText.text.toString()
        model.sendPkg(MessageCodes.TEXT_WRITE, text)
        binding.keyboardText.setText("")
    }

    private fun onBtnEnterClick(){
        if(binding.keyboardText.length() > 0)
            onKeyboardSubmit()
        else
            onButtonClick(MessageCodes.ENTER)
    }

    private fun onButtonClick(code: MessageCodes){
        Log.d(TAG, "onButtonClick: ")
        model.sendPkg(code)
    }

    // ------------------------ Touchpad events control ------------------------ //

    private fun onTouchStartAction(x: Float, y: Float){
        Log.d(TAG, "onTouchStartAction: ")
        currentTouchPosX = x.toInt()
        currentTouchPosY = y.toInt()
        clickEventCounter++

        if(clickEventCounter == 3) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (clickEventCounter == 3) {
                    Log.d(TAG, "onTouchFinishAction: hold")
                    model.sendPkg(MessageCodes.LEFT_HOLD_START)
                    clickEventCounter = 0
                    isHold = true
                }
            }, 100)
        }
    }

    private fun onTouchDoubleFingers(){
        Log.d(TAG, "onTouchDoubleFingers: ")
        isTouchDoubleFingers = true
    }

    private fun onTouchMoveAction(x: Float, y: Float){
        Log.d(TAG, "onTouchMoveAction: ")

        // caso seja movimento com dois dedos
        if(isTouchDoubleFingers) {
            val minx = currentTouchPosX - VERTICAL_SCROLL_OFFSET
            val maxx = currentTouchPosX + VERTICAL_SCROLL_OFFSET
            val miny = currentTouchPosY - VERTICAL_SCROLL_OFFSET
            val maxy = currentTouchPosY + VERTICAL_SCROLL_OFFSET

            // se estiver fora da tolerancia, inicia o scroll vertical
            // do contrario, ignora o movimento. Caso o movimento esteja
            // dentro da tolerancia at?? o fim, sera tratado como a????o
            // de clique no bot??o direito no metodo onTouchFinishAction().
            // (minx < x < maxx) and (miny < y < maxy)

            if (! (x > minx && x < maxx && y > miny && y < maxy))
                onVerticalScroll(x, y)
        }
        // caso seja movimento com um dedo
        else onSimpleMove(x, y)
    }

    private fun onSimpleMove(x: Float, y: Float){
        Log.d(TAG, "onSimpleMove: ")

        // calc offset
        val moveX = Utils.normalizeMove(x.toInt() - currentTouchPosX)
        val moveY = Utils.normalizeMove(y.toInt() - currentTouchPosY)

        val xstring = padToPattern(moveX, 4)
        val ystring = padToPattern(moveY, 4)
        val sequenceString = padToPattern(moveSequence++, 12)

        model.sendPkg(MessageCodes.MOVE, "$xstring$ystring$sequenceString") // send packet

        // update pos
        currentTouchPosX = x.toInt()
        currentTouchPosY = y.toInt()
        isTouchMove = true
    }

    private fun onVerticalScroll(x: Float, y: Float){
        Log.d(TAG, "onVerticalScroll: ")

        var moveY = Utils.normalizeScroll(y.toInt() - currentTouchPosY)    // calc offset
        moveY = Utils.normalizeToMean(moveY, verticalScrollMean, 7)

        val ystring = padToPattern(moveY, 4)
        val sequenceString = padToPattern(moveSequence++, 12)

        // send packet
        model.sendPkg(MessageCodes.SCROLL_VER, "$ystring$sequenceString")

        // update control data
        currentTouchPosX = x.toInt()
        currentTouchPosY = y.toInt()
        isVerticalScroll = true
        verticalScrollMean += (moveY.absoluteValue - verticalScrollMean) / moveSequence
    }

    private fun onTouchFinishAction(view: View, x: Float, y: Float) {
        Log.d(TAG, "onTouchFinishAction: ")

        when {
            isTouchDoubleFingers and isVerticalScroll -> {
                Log.d(TAG, "onTouchFinishAction: case -> vertical scroll")

                // disable the flags
                isTouchDoubleFingers = false
                isVerticalScroll = false
                moveSequence = 0
                clickEventCounter = 0   // restart counter
                verticalScrollMean = 0
            }
            isTouchDoubleFingers -> {
                Log.d(TAG, "onTouchFinishAction: case -> Double fingers")
                isTouchDoubleFingers = false    // disable the flag
                clickEventCounter = 0   // restart counter

                // send packet
                model.sendPkg(MessageCodes.RIGHT_CLICK)    // double fingers is equivalent to right click

                view.performClick()
            }
            isHold and isTouchMove -> {
                Log.d(TAG, "onTouchFinishAction: case -> hold and move")

                // send packet free hold
                model.sendPkg(MessageCodes.LEFT_HOLD_END)

                moveSequence = 0
                isTouchMove = false
                isHold = false
                clickEventCounter = 0   // restart counter
            }
            isHold -> {
                Log.d(TAG, "onTouchFinishAction: case -> hold")

                // send packet free hold
                model.sendPkg(MessageCodes.LEFT_HOLD_END)

                isHold = false
                clickEventCounter = 0   // restart counter
            }
            isTouchMove -> {
                Log.d(TAG, "onTouchFinishAction: case -> move")
                moveSequence = 0
                isTouchMove = false
                clickEventCounter = 0   // restart counter
            }
            else -> {
                clickEventCounter++

                if(clickEventCounter == 4) {
                    Log.d(TAG, "onTouchFinishAction: click duplo")
                    model.sendPkg(MessageCodes.LEFT_DOUBLE_CLICK)    // send packet
                    clickEventCounter = 0
                }
                else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (clickEventCounter == 2) {
                            Log.d(TAG, "onTouchFinishAction: click comum")
                            model.sendPkg(MessageCodes.LEFT_CLICK)    // send packet
                            clickEventCounter = 0
                        }
                    }, 100)
                }
            }
        }
    }

    /**
     * Transform a Int value into a String with a sequence of 0 before the value until it match length of the pattern.
     * Example: value: 5, length: 4 -> Result: "0005"
     * @param value Value to be transformed.
     * @param length Length of the pattern.
     * @return String of the value matching the pattern.
     */
    private fun padToPattern(value: Int, length: Int) : String {
        if (value < 0)
            return abs(value).toString().padStart(length, '0').replaceFirst('0', '-')

        return value.toString().padStart(length, '0')
    }
}