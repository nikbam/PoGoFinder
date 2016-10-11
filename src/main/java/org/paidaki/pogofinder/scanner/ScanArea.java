package org.paidaki.pogofinder.scanner;

import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.util.javascript.JSOStringify;

public class ScanArea implements JSOStringify {

    private Location northWest = null;
    private Location southEast = null;
    private Integer mapId = null;

    public ScanArea() {
        super();
    }

    private ScanArea(Location northWest, Location southEast) {
        this.northWest = northWest;
        this.southEast = southEast;
    }

    public boolean contains(Location loc) {
        return contains(loc.getLat(), loc.getLng());
    }

    public boolean contains(double lat, double lng) {
        return lat <= northWest.getLat() &&
                lat >= southEast.getLat() &&
                lng >= northWest.getLng() &&
                lng <= southEast.getLng();
    }

    public boolean equals(ScanArea scanArea) {
        return equals(scanArea.northWest.getLat(), scanArea.northWest.getLng(),
                scanArea.southEast.getLat(), scanArea.southEast.getLng());
    }

    public boolean equals(double nwLat, double nwLng, double seLat, double seLng) {
        return northWest.getLat() == nwLat &&
                northWest.getLng() == nwLng &&
                southEast.getLat() == seLat &&
                southEast.getLng() == seLng;
    }

    public Location getNorthWest() {
        return northWest;
    }

    public void setNorthWest(Location northWest) {
        this.northWest = northWest;
    }

    public Location getSouthEast() {
        return southEast;
    }

    public void setSouthEast(Location southEast) {
        this.southEast = southEast;
    }

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }

    @Override
    public String getJSObjectString() {
        return "{ " +
                "mapId: " + ((getMapId() == null) ? JSOStringify.UNDEFINED : String.valueOf(getMapId())) + ", " +
                "northWest: " + ((northWest == null) ? JSOStringify.UNDEFINED : String.valueOf(northWest)) + ", " +
                "southEast: " + ((southEast == null) ? JSOStringify.UNDEFINED : String.valueOf(southEast)) +
                " }";
    }

    @Override
    public String toString() {
        return getJSObjectString();
    }
}
