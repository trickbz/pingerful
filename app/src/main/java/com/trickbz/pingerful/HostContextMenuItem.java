package com.trickbz.pingerful;

public enum  HostContextMenuItem {

    UPDATE(0),
    DELETE(1),
    FORCE_PING(2);

    private int value;

    private HostContextMenuItem(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }


}
