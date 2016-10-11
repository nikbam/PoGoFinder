package org.paidaki.pogofinder.web;

import org.json.JSONArray;
import org.json.JSONObject;
import org.paidaki.pogofinder.location.GpsData;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.web.WebServer.ResponseHandler;

import java.util.Observable;
import java.util.Observer;

public class WebListener implements Observer, Stoppable {

    private Bridge bridge;
    private WebServer webServer;

    protected WebListener(Bridge bridge) {
        this.bridge = bridge;
        webServer = new WebServer();
        webServer.addObserver(this);

        ThreadManager.addThread(this);
    }

    protected void start() {
        webServer.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        ResponseHandler rh = (ResponseHandler) arg;
        GpsData gpsData = new GpsData(bridge.getFluctuatedLocation());

        gpsData.fluctuateAccuracy();

        JSONObject json = new JSONObject();
        JSONArray results = new JSONArray();
        JSONObject firstResult = new JSONObject();
        JSONObject location = new JSONObject();

        location.put("latitude", gpsData.getLocation().getLat());
        location.put("longitude", gpsData.getLocation().getLng());

        firstResult.put("altitude", gpsData.getLocation().getAlt());
        firstResult.put("location", location);
        firstResult.put("accuracy", gpsData.getAcc());
        firstResult.put("speed", gpsData.getSpeed());
        firstResult.put("bearing", gpsData.getBearing());

        results.put(firstResult);

        json.put("results", results);
        json.put("status", "OK");

        webServer.sendResponse(rh, json);
    }

    @Override
    public void forceStop() {
        webServer.stop();
    }
}
