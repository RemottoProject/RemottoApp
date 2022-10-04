package xyz.uchiha.remotto.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import xyz.uchiha.remotto.R
import xyz.uchiha.remotto.ui.LaunchActivity.Companion.LAUNCH_MAIN_EXTRA
import xyz.uchiha.remotto.background.Utils
import xyz.uchiha.remotto.databinding.ActivityPairingBinding

class PairingActivity : AppCompatActivity() {
    private val TAG: String = this.javaClass.simpleName
    private lateinit var binding: ActivityPairingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupElements()
        getData()
    }

    override fun onBackPressed() {
        if(safeExit())
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            safeExit()

        return super.onOptionsItemSelected(item)
    }

    private fun setupElements() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.btnScan.setOnClickListener { onBtnScanClick() }
        binding.btnSave.setOnClickListener {
            if (saveData())
                closeActivity()
        }
    }

    private fun onBtnScanClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        else
            getResult.launch(Intent(this, QrCodeScanActivity::class.java))
    }

    private fun safeExit(): Boolean{
        val device: String = Utils.getString(this, Utils.DEVICE_KEY)
        val ip: String = Utils.getString(this, Utils.IP_KEY)
        val mac: String = Utils.getString(this, Utils.MAC_KEY)

        if (device.isNotEmpty() && ip.isNotEmpty() && mac.isNotEmpty()) {
            finish()
            return true
        }
        else{
            saveData()
            return false
        }
    }

    private fun closeActivity(){
        val launchMain: Boolean = intent.getBooleanExtra(LAUNCH_MAIN_EXTRA, false)
        Toast.makeText(this, getString(R.string.preferences_saved), Toast.LENGTH_SHORT).show()

        // check if we need start the main activity
        if (launchMain)
            startActivity(Intent(this, MainActivity::class.java))
        else
            setResult(RESULT_OK)

        finish()
    }

    private fun saveData(): Boolean{
        val device: String = binding.deviceTextInput.text.toString()
        val ip: String = binding.ipTextInput.text.toString()
        val mac: String = binding.macTextInput.text.toString()

        // validate device
        val isValidDevice = Utils.validateDeviceName(device)

        if (device.isEmpty())
            binding.deviceInputLayout.error = getString(R.string.device_name_empty_error)
        else if (! isValidDevice)
            binding.deviceInputLayout.error = getString(R.string.device_name_invalid_error)
        else
            binding.deviceInputLayout.error = null

        // validate ip
        val isValidIp = Utils.validateIP(ip)

        if (ip.isEmpty())
            binding.ipInputLayout.error = getString(R.string.ip_empty_error)
        else if(! isValidIp)
            binding.ipInputLayout.error = getString(R.string.ip_invalid_error)
        else
            binding.ipInputLayout.error = null

        // validate mac
        val isValidMac = Utils.validateMAC(mac)

        if (mac.isEmpty())
            binding.macInputLayout.error = getString(R.string.mac_empty_error)
        else if (! isValidMac)
            binding.macInputLayout.error = getString(R.string.mac_invalid_error)
        else
            binding.macInputLayout.error = null

        if (device.isNotEmpty() && isValidDevice)
            Utils.saveString(this, Utils.DEVICE_KEY, normalizeDeviceName(device))

        if (ip.isNotEmpty() && isValidIp)
            Utils.saveString(this, Utils.IP_KEY, ip)

        if (mac.isNotEmpty() && isValidMac)
            Utils.saveString(this, Utils.MAC_KEY, mac)

        if (device.isNotEmpty() && isValidDevice && ip.isNotEmpty() && isValidIp && mac.isNotEmpty() && isValidMac)
            return true

        return false
    }

    private fun getData() {
        val device: String = Utils.getString(this, Utils.DEVICE_KEY)
        val ip: String = Utils.getString(this, Utils.IP_KEY)
        val mac: String = Utils.getString(this, Utils.MAC_KEY)

        binding.ipTextInput.setText(ip)
        binding.macTextInput.setText(mac)

        if (device.isNotEmpty())
            binding.deviceTextInput.setText(removeExtraChars(device))
        else {
            // if there is no name defined, we put the bluetooth name in the field
            var btName = Settings.Secure.getString(contentResolver, "bluetooth_name")
            btName = if (btName.length > 9) btName.substring(0, 10) else btName
            binding.deviceTextInput.setText(btName)
        }
    }

    private fun normalizeDeviceName(device: String): String {
        return device.padEnd(9, '&')
    }

    private fun removeExtraChars(device: String): String {
        return device.replace("&", "")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                onBtnScanClick()
            else
                Toast.makeText(this, getString(R.string.camera_permission_denied_error), Toast.LENGTH_SHORT).show()
        }
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK){
            val ip = it.data?.getStringExtra(QrCodeScanActivity.IP_EXTRA)
            val mac = it.data?.getStringExtra(QrCodeScanActivity.MAC_EXTRA)

            if (ip != "#" && mac != "#") {
                binding.ipTextInput.setText(ip)
                binding.macTextInput.setText(mac)
            }
            else
                Toast.makeText(this, getString(R.string.qr_format_error), Toast.LENGTH_SHORT).show()
        }
        else if (it.resultCode == Activity.RESULT_CANCELED) {
            val isError = it.data?.getBooleanExtra(QrCodeScanActivity.ERROR_EXTRA, false)

            if (isError == true)
                Toast.makeText(this, getString(R.string.qr_scan_generic_error), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 100
    }
}