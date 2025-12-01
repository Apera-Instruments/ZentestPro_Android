package com.zen.ui.base;


public class TrackAgentManager {
    private static TrackAgentManager instance = new TrackAgentManager();
    private TrackAgent trackAgent = new TrackAgent();

    public static TrackAgentManager getInstance() {
        return instance;
    }


    public TrackAgent getTrackAgent() {
        return trackAgent;
    }

    public void setTrackAgent(TrackAgent trackAgent) {
        this.trackAgent = trackAgent;
    }
}
