package xyz.uchiha.remotto.background

import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Send packets and other network stuff.
 * @param host IP from target.
 * @param port Port from target.
 */
class Network(private val TAG: String, private val host: String, private val port: Int) {
    private val macBytesSize = 6

    fun sendUDPPacket(message: String){
        try {
            val addr = InetAddress.getByName(host)
            val msgByteArray = message.encodeToByteArray()

            // Create a new packet
            val pkg = DatagramPacket(msgByteArray, msgByteArray.size, addr, port)

            val ds = DatagramSocket()
            ds.send(pkg)
            ds.close()
        }
        catch (e: IOException){
            Log.d(TAG, "deu ruim aqui mano")
            Log.d(TAG, e.stackTraceToString())
        }
    }

    /**
     * -----------------------------------------------------------------------------------------------------
     * Code below is based in:
     * - https://gist.github.com/Shterneregen/14243dcecdad03c068176d9c24eafb59
     * -----------------------------------------------------------------------------------------------------
     */

    /**
     * @param ip Broadcast ip
     */
    fun wakeOnLan(mac: String, ip: String = "255.255.255.255") {
        try {
            val magicPacketBytes = getMagicPacketBytes(mac)
            val pkg = DatagramPacket(magicPacketBytes, magicPacketBytes.size, InetAddress.getByName(ip), port)
            val socket = DatagramSocket()
            socket.send(pkg)
            socket.close()
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to send Wake-on-LAN packet: {}" + e.message)
            Log.e(TAG, e.message, e)
        }
    }

    private fun getMagicPacketBytes(mac: String): ByteArray {
        val macBytes = getMacBytes(mac)
        val packetSize = macBytesSize + 16 * macBytes.size
        val magicPacketBytes = ByteArray(packetSize)
        for (i in 0 until macBytesSize) magicPacketBytes[i] = 0xff.toByte()
        for (item in macBytesSize.until(magicPacketBytes.size).step(macBytes.size))
            System.arraycopy(macBytes, 0, magicPacketBytes, item, macBytes.size)
        return magicPacketBytes
    }

    private fun getMacBytes(mac: String): ByteArray {
        val hex: List<String> = mac.split(":", "-")
        if (hex.size != macBytesSize) throw IllegalArgumentException("Invalid MAC address")

        val bytes = ByteArray(macBytesSize)
        for (i in 0 until macBytesSize) bytes[i] = hex[i].toInt(radix = 16).toByte()

        return bytes
    }
}