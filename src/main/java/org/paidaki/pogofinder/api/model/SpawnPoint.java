package org.paidaki.pogofinder.api.model;

import org.paidaki.pogofinder.util.javascript.JSOStringify;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.map.MapObject;

import java.util.Comparator;

public class SpawnPoint extends MapObject implements Comparable<Number> {

    public static final int BUGGED_TIME_MS = 900000;        // 15 Minutes
    public static final int SPAWN_DURATION_SECS = 900;        // 15 Minutes

    public static final Comparator<SpawnPoint> ASCENDING_COMPARATOR = (o1, o2) -> o1.spawnTime.compareTo(o2.spawnTime);
    public static final Comparator<SpawnPoint> DESCENDING_COMPARATOR = (o1, o2) -> o2.spawnTime.compareTo(o1.spawnTime);

    private Integer spawnTime = null;
    private Integer despawnTime = null;

    public SpawnPoint() {
        super();
    }

    public SpawnPoint(String id, double lat, double lng, int spawnTime) {
        super(id, lat, lng);
        setSpawnTime(spawnTime);
    }

    public SpawnPoint(String id, Location loc, int spawnTime) {
        this(id, loc.getLat(), loc.getLng(), spawnTime);
    }

    public Integer getSpawnTime() {
        return spawnTime;
    }

    public void setSpawnTime(Integer spawnTime) {
        this.spawnTime = spawnTime;
        despawnTime = (spawnTime + SPAWN_DURATION_SECS) % 3600;
    }

    public Integer getDespawnTime() {
        return despawnTime;
    }

    @Override
    public String getJSObjectString() {
        return "{ " +
                "id: " + ((getId() == null) ? JSOStringify.UNDEFINED : "\"" + getId() + "\"") + ", " +
                "mapId: " + ((getMapId() == null) ? JSOStringify.UNDEFINED : String.valueOf(getMapId())) + ", " +
                "lat: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLat())) + ", " +
                "lng: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLng())) + ", " +
                "spawnTime: " + ((spawnTime == null) ? JSOStringify.UNDEFINED : String.valueOf(spawnTime)) + ", " +
                "despawnTime: " + ((despawnTime == null) ? JSOStringify.UNDEFINED : String.valueOf(despawnTime)) +
                " }";
    }

    @Override
    public String toString() {
        return getJSObjectString();
    }

    @Override
    public int compareTo(Number o) {
        return this.spawnTime.compareTo(o.intValue());
    }
}
