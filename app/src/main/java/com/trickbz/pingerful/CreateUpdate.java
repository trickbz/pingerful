package com.trickbz.pingerful;

public enum CreateUpdate {

    CREATE(0),
    UPDATE(1);

    private int value;

    private CreateUpdate(int value) {
        this.value = value;
    }
    public int value() {
        return this.value;
    }

}
