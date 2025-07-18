package com.manager.cli_password_manager.core.entity.enums;

public enum MessageColor {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    BRIGHT(8);

    private final int value;

    MessageColor(int value) {
        this.value = value;
    }

    public int getColorValue() {
        return this.value;
    }
}
