package com.zen.ui.event;

public class UpdateAssetEvent {
    private int postion;
    private String noteName;

    public int getPostion() {
        return postion;
    }

    public void setPostion(int postion) {
        this.postion = postion;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public UpdateAssetEvent(int postion) {
        this.postion = postion;
    }
}
