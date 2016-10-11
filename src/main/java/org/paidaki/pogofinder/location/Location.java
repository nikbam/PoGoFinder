package org.paidaki.pogofinder.location;

import org.paidaki.pogofinder.util.javascript.JSOStringify;

public class Location implements JSOStringify {

    private static final double MIN_LAT = -90.0;
    private static final double MAX_LAT = 90.0;
    private static final double MIN_LNG = -180.0;
    private static final double MAX_LNG = 180.0;
    private static final double MIN_ALT = -10000.0;
    private static final double MAX_ALT = 10000.0;

    private Double lat = null;
    private Double lng = null;
    private Double alt = null;

    public Location() {
        super();
    }

    public Location(double lat, double lng) {
        setLat(lat);
        setLng(lng);
    }

    public Location(double lat, double lng, double alt) {
        setLat(lat);
        setLng(lng);
        setAlt(alt);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        if (lat < MIN_LAT) {
            this.lat = MIN_LAT;
        } else if (lat > MAX_LAT) {
            this.lat = MAX_LAT;
        } else {
            this.lat = lat;
        }
    }

    public double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        if (lng < MIN_LNG) {
            this.lng = MIN_LNG;
        } else if (lng > MAX_LNG) {
            this.lng = MAX_LNG;
        } else {
            this.lng = lng;
        }
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        if (alt < MIN_ALT) {
            this.alt = MIN_ALT;
        } else if (alt > MAX_ALT) {
            this.alt = MAX_ALT;
        } else {
            this.alt = alt;
        }
    }

    @Override
    public String getJSObjectString() {
        return "{ " +
                "lat: " + ((lat == null) ? JSOStringify.UNDEFINED : String.valueOf(lat)) + ", " +
                "lng: " + ((lng == null) ? JSOStringify.UNDEFINED : String.valueOf(lng)) + ", " +
                "alt: " + ((alt == null) ? JSOStringify.UNDEFINED : String.valueOf(alt)) +
                " }";
    }

    @Override
    public String toString() {
        return getJSObjectString();
    }
}
