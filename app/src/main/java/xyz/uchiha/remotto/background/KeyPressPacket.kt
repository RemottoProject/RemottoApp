package xyz.uchiha.remotto.background

class KeyPressPacket(private val code: MessageCodes, private val clientName: String) : IPacket {
    override fun toString(): String {
        return "$code$clientName"
    }
}