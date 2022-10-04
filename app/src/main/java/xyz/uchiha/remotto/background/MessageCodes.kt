package xyz.uchiha.remotto.background

enum class MessageCodes(val code: String) {
    // Text write
    TEXT_WRITE("T00"),
    // Complex command
    ZOOM_IN("C01"), ZOOM_OUT("C02"), COPY("C03"), PASTE("C04"),
    // Mouse
    LEFT_CLICK("M01"), RIGHT_CLICK("M02"), LEFT_DOUBLE_CLICK("M03"), MOVE("M04"),
    SCROLL_VER("M05"), SCROLL_HOR("M06"), LEFT_HOLD_START("M07"), LEFT_HOLD_END("M08"),
    // Special
    SLEEP("S01"), SHUTDOWN("S02"),
    // Key press actions
    PLAY("K01"), STOP("K02"), PREVIOUS("K03"), NEXT("K04"), VOL_DOWN("K05"),
    VOL_UP("K06"), MUTE("K07"), ENTER("K08"), UP("K09"), DOWN("K10"), LEFT("K11"),
    RIGHT("K12"), BACKSPACE("K13")
}