package xyz.uchiha.remotto.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import xyz.uchiha.remotto.R
import xyz.uchiha.remotto.background.Utils

class LaunchActivity : AppCompatActivity() {
    companion object{
        const val LAUNCH_MAIN_EXTRA = "LAUNCH_MAIN_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // set dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        // get user data
        val ip: String = Utils.getString(this, Utils.IP_KEY)
        val mac: String = Utils.getString(this, Utils.MAC_KEY)

        if (ip.isNotEmpty() && mac.isNotEmpty())
            startActivity(Intent(this, MainActivity::class.java))
        else {
            val intent = Intent(this, PairingActivity::class.java)
            intent.putExtra(LAUNCH_MAIN_EXTRA, true)
            startActivity(intent)
        }

        finish()
    }
}