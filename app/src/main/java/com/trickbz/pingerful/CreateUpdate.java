package com.trickbz.pingerful;

import java.io.Serializable;

public enum CreateUpdate implements Serializable {

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
