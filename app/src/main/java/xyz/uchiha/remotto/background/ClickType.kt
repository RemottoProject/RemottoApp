package xyz.uchiha.remotto.background

enum class ClickType(val value: Int) {
    CLICK(2), HOLD(3), DOUBLE_CLICK(4);

    companion object {
        fun fromInt(value: Int) = ClickType.values().first { it.value == value }
    }
}