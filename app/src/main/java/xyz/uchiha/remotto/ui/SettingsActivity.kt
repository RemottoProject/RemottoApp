package xyz.uchiha.remotto.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import xyz.uchiha.remotto.background.Utils
import xyz.uchiha.remotto.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupElements()
        devSetupComponentsForTesting() // TODO: disable this on production builds
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    private fun setupElements() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.layoutPairing.setOnClickListener { getResult.launch(Intent(this, PairingActivity::class.java)) }
        binding.layoutVisuals.setOnClickListener { Toast.makeText(this, "Hoje não, hoje não...", Toast.LENGTH_SHORT).show() }
        binding.layoutAbout.setOnClickListener { Toast.makeText(this, "Nunca desista", Toast.LENGTH_SHORT).show() }
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK)
            setResult(RESULT_OK)    // passes the result code to the main activity when this activity is closed
    }

    // --------------------------- dev usage only --------------------------- //
    private var devUserDataClearCount = 0

    private fun devSetupComponentsForTesting() {
        binding.layoutVisuals.setOnLongClickListener {
            if (devUserDataClearCount >= 0) {
                devUserDataClearCount++

                if (devUserDataClearCount >= 5)
                    Toast.makeText(this, "Perform the final step to delete the data.", Toast.LENGTH_SHORT).show()
                else if (devUserDataClearCount > 3)
                    Toast.makeText(this, "You are about to delete data in ${6 - devUserDataClearCount}...", Toast.LENGTH_SHORT).show()
            }
            else
                Toast.makeText(this, "Data deleted. Restart the App.", Toast.LENGTH_SHORT).show()

            true
        }

        binding.layoutAbout.setOnLongClickListener {
            if (devUserDataClearCount >= 5) {
                Utils.deleteString(this, Utils.DEVICE_KEY)
                Utils.deleteString(this, Utils.IP_KEY)
                Utils.deleteString(this, Utils.MAC_KEY)
                devUserDataClearCount = -10
                Toast.makeText(this, "Data deleted. Restart the App.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }

            true
        }
    }
}