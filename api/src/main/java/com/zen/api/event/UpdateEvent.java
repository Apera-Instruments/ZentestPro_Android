package com.zen.api.event;

import com.zen.api.protocol.Convent;

public class UpdateEvent {
    int code=0;
    int dataCode;
    Object data;
    public UpdateEvent() {

    }
    public UpdateEvent(Convent object) {
        data = object;
        dataCode = object.getCode();
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return super.toString()+", "+code+", "+dataCode+" < "+data + " > ";
    }
}
