package com.zen.api.event;

public class FactoryEvent {
    int type;
    public FactoryEvent(int type) {
       this.type = type;
    }

    public int getType() {
        return type;
    }
}
