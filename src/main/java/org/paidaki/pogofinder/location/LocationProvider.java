package org.paidaki.pogofinder.location;

import org.paidaki.pogofinder.web.Bridge;

import java.util.Random;

public class LocationProvider {

    private static final Random RAND = new Random();

    private static final double LOC_FLUCTUATION = 0.00005;

    private Bridge bridge;

    public LocationProvider(Bridge bridge) {
        this.bridge = bridge;
    }

    protected Location getCurrentLocation() {
        return bridge.getPlayerLocation();
    }

    public Location fluctuateLocation(Location location) {
        location.setLat(location.getLat() + (RAND.nextDouble() * 2 * LOC_FLUCTUATION) - LOC_FLUCTUATION);
        location.setLng(location.getLng() + (RAND.nextDouble() * 2 * LOC_FLUCTUATION) - LOC_FLUCTUATION);

        return location;
    }
}
