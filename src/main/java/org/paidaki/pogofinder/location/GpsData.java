package org.paidaki.pogofinder.location;

import java.util.Random;

public class GpsData {

    private static final Random RAND = new Random();

    private static final float DEFAULT_SPEED = 1.0f;
    private static final float DEFAULT_BEARING = 4.0f;
    private static final float DEFAULT_ACC = 32.0f;

    private static final double ABC_CHANGE_POSSIBILITY = 0.5;
    private static final float ACC_STEP = 0.5f;
    private static final int ACC_FLUCTUATION = 8;

    private Location location;
    private float acc;
    private float speed;
    private float bearing;

    public GpsData(Location location) {
        this.location = location;
        acc = DEFAULT_ACC;
        speed = DEFAULT_SPEED;
        bearing = DEFAULT_BEARING;
    }

    public GpsData(Location location, float acc, float speed, float bearing) {
        this.location = location;
        this.acc = acc;
        this.speed = speed;
        this.bearing = bearing;
    }

    public void fluctuateAccuracy() {
        boolean change = (RAND.nextDouble() > ABC_CHANGE_POSSIBILITY);

        if (change) acc = DEFAULT_ACC + (RAND.nextInt(2 * ACC_FLUCTUATION) - ACC_FLUCTUATION) * ACC_STEP;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getAcc() {
        return acc;
    }

    public void setAcc(float acc) {
        this.acc = acc;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }
}
