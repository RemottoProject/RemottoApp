package xyz.uchiha.remotto.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import xyz.uchiha.remotto.R

class QrCodeScanActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scan)

        val scannerView = findViewById<CodeScannerView>(R.id.qrScanner)

        codeScanner = CodeScanner(this, scannerView)

        configureScanner()
        registerCallbacks()

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun configureScanner() {
        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK        // or CAMERA_FRONT or specific camera id
        codeScanner.formats = listOf(BarcodeFormat.QR_CODE) // list of type BarcodeFormat

        codeScanner.autoFocusMode = AutoFocusMode.SAFE  // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE          // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true   // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false      // Whether to enable flash or not
    }

    private fun registerCallbacks() {
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val res = validateText(it.text)
                val intent = Intent()

                intent.putExtra(IP_EXTRA, res[0])
                intent.putExtra(MAC_EXTRA, res[1])

                setResult(RESULT_OK, intent)
                finish()
            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                val intent = Intent()

                intent.putExtra(ERROR_EXTRA, true)
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun validateText(text: String): List<String> {
        val res = text.split("-")

        if (res.size != 2)
            return listOf("#", "#")

        return res
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    companion object {
        const val IP_EXTRA = "IP_EXTRA"
        const val MAC_EXTRA = "MAC_EXTRA"
        const val ERROR_EXTRA = "ERROR_EXTRA"
    }
}