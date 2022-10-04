package xyz.uchiha.remotto.background

import android.content.Context
import java.lang.NumberFormatException
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class Utils {

    companion object{
        private const val APP_KEY: String = "xyz.uchiha.remotto.APP_KEY"
        const val DEVICE_KEY: String = "$APP_KEY.DEVICE"
        const val IP_KEY: String = "$APP_KEY.IP"
        const val MAC_KEY: String = "$APP_KEY.MAC"

        fun saveString(context: Context, key: String, value: String){
            val sharedPref = context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString(key, value)
                commit()
            }
        }

        fun getString(context: Context, key: String): String{
            val sharedPref = context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE) ?: return ""
            return sharedPref.getString(key, "").toString()
        }

        fun deleteString(context: Context, key: String){
            saveString(context, key, "")
        }

        fun validateDeviceName(name: String): Boolean {
            if (name.isEmpty())
                return false

            if (name.length > 9)
                return false

            return true
        }

        fun validateIP(ip: String): Boolean{
            val array = ip.split(".")

            if (array.size != 4)
                return false
            else{
                for (v in array) {
                    try {
                        val value = v.toInt()

                        if (value > 255 || value < 0)
                            return false
                    }
                    catch (e: NumberFormatException){
                        return false
                    }
                }

                return true
            }
        }

        fun validateMAC(mac: String): Boolean{
            val array = mac.split(":")

            if (array.size != 6)
                return false
            else{
                for (v in array) {
                    if (v.length != 2)
                        return false
                }

                // TODO: fazer a validação de forma mais precisa
                return true
            }
        }

        fun broadcastFromIP(ip: String): String{
            val array: MutableList<String> = ip.split(".").toMutableList()
            array[3] = "255"
            return array.joinToString(".")
        }

        fun normalizeScroll(v: Int): Int{
            return (v * 0.2f).toInt()
        }

        fun normalizeMove(v: Int): Int{
            var res = abs(v)

            res = when {
                (res < 5) -> (res * 0.5f).roundToInt()
                (res < 10) -> (res * 0.75f).roundToInt()
                (res < 15) -> res
                (res < 20) -> (res * 1.25f).roundToInt()
                (res < 25) -> (res * 1.5f).roundToInt()
                (res < 30) -> (res * 1.75f).roundToInt()
                (res < 40) -> (res * 2.25f).roundToInt()
                else -> (res * 2.5f).roundToInt()
            }

            if (v < 0) res *= -1

            return res
        }

        /**
         * Removes values that are too far from the mean.
         * @param value Value to be checked.
         * @param mean Average of values.
         * @param tolerance The maximum allowable deviation from the mean.
         * @return The original value if it is within tolerance.
         * Otherwise, return the average. And if that happens, if the original value is negative,
         * it will return the negative mean value as well.
         */
        fun normalizeToMean(value: Int, mean: Int, tolerance: Int): Int {
            val absValue = value.absoluteValue

            if (absValue > mean + tolerance)
                return if (value < 0) -mean else mean

            return value
        }
    }
}