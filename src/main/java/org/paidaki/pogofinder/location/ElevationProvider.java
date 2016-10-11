package org.paidaki.pogofinder.location;

import org.json.JSONArray;
import org.json.JSONObject;
import org.paidaki.pogofinder.util.threading.MyRunnable;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.web.Bridge;
import org.paidaki.pogofinder.web.HttpHandler;
import org.paidaki.pogofinder.web.HttpResponse;
import org.paidaki.pogofinder.web.enums.RequestMethod;
import org.paidaki.pogofinder.web.enums.ResponseCode;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ElevationProvider implements Stoppable {

    private static final Random RAND = new Random();

    private static final String API_KEY = "&key=AIzaSyBC-riloLrm9Em6PrGwkxl-TDBHLBYlxC0";
    private static final String API_URL = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
    private static final int CONN_TIMEOUT = 2000;       // 2 Second

    private static final long UPDATE_INTERVAL = 30000;      // 30 Seconds
    private static final double DEFAULT_ALT = 1.0;
    private static final double ALT_FLUCTUATION = 0.5;

    private ScheduledExecutorService elevationExecutor;
    private MyRunnable elevationTask;
    private Bridge bridge;

    private double altitude = DEFAULT_ALT;
    private double scanAltitude = DEFAULT_ALT;

    private class ElevationTask extends MyRunnable {

        private boolean isForced;

        public ElevationTask(boolean isForced) {
            super();
            this.isForced = isForced;
        }

        public ElevationTask(boolean isForced, Runnable callback) {
            super(callback);
            this.isForced = isForced;
        }

        @Override
        public void runTask() {
            try {
                Location loc = bridge.getPlayerLocation();
                HttpHandler httpHandler = new HttpHandler();
                URL url = new URL(API_URL + loc.getLat() + "," + loc.getLng() + API_KEY);

                HttpResponse httpResponse = httpHandler.sendRequest(url, RequestMethod.GET, CONN_TIMEOUT);

                if (httpResponse.getResponseCode() == ResponseCode.STATUS_OK) {
                    if (httpResponse.getContentType().toLowerCase().contains("json")) {
                        JSONObject jsonResponse = new JSONObject(httpResponse.getResponseBody());

                        if (isForced) scanAltitude = getAltitudeFromJSON(jsonResponse);
                        else altitude = getAltitudeFromJSON(jsonResponse);
                    }
                } else {
                    if (isForced) scanAltitude = DEFAULT_ALT;
                    else altitude = DEFAULT_ALT;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ElevationProvider(Bridge bridge) {
        this.bridge = bridge;
        elevationExecutor = Executors.newScheduledThreadPool(2);
        elevationTask = new ElevationTask(false);

        ThreadManager.addThread(this);
    }

    public void updateAltitude() {
        elevationExecutor.execute(new ElevationTask(false));
    }

    public void forceAltitudeUpdate(Runnable callback) {
        elevationExecutor.execute(new ElevationTask(true, callback));
    }

    public double getAltitude(boolean fluctuate) {
        if (!fluctuate) {
            return altitude;
        }
        return altitude + (RAND.nextDouble() * 2 * ALT_FLUCTUATION) - ALT_FLUCTUATION;
    }

    public double getScanAltitude(boolean fluctuate) {
        if (!fluctuate) {
            return scanAltitude;
        }
        return scanAltitude + (RAND.nextDouble() * 2 * ALT_FLUCTUATION) - ALT_FLUCTUATION;
    }

    private double getAltitudeFromJSON(JSONObject jsonResponse) {
        double altitude = DEFAULT_ALT;
        String status = jsonResponse.getString("status");

        if (status.equals("OK")) {
            JSONArray array = jsonResponse.getJSONArray("results");

            altitude = array.getJSONObject(0).getDouble("elevation");
        }

        return altitude;
    }

    public void start() {
        if (!elevationTask.isRunning()) {
            elevationExecutor.scheduleAtFixedRate(elevationTask, 0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void forceStop() {
        elevationExecutor.shutdownNow();
    }

    @Override
    public void stop() {
        elevationExecutor.shutdown();
    }
}
