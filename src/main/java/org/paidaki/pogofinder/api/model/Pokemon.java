package org.paidaki.pogofinder.api.model;

import org.paidaki.pogofinder.util.javascript.JSOStringify;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.map.MapObject;

public class Pokemon extends MapObject implements JSOStringify {

    private String name = null;
    private String spawnPointId = null;
    private Long encounterId = null;
    private Long despawnTime = null;
    private Boolean isBugged = null;

    public Pokemon() {
        super();
    }

    public Pokemon(String name, Location location, String id,
                   String spawnPointId, Long encounterId, Long despawnTime, Boolean isBugged) {
        super(id, location);
        this.name = name;
        this.spawnPointId = spawnPointId;
        this.encounterId = encounterId;
        this.despawnTime = despawnTime;
        this.isBugged = isBugged;
    }

    public Pokemon(String name, double lat, double lng, String id,
                   String spawnPointId, Long encounterId, Long despawnTime, Boolean isBugged) {
        super(id, lat, lng);
        this.name = name;
        this.spawnPointId = spawnPointId;
        this.encounterId = encounterId;
        this.despawnTime = despawnTime;
        this.isBugged = isBugged;
    }

    public Pokemon(String name, Location location, int id,
                   String spawnPointId, Long encounterId, Long despawnTime, Boolean isBugged) {
        this(name, location, fixId(id), spawnPointId, encounterId, despawnTime, isBugged);
    }

    public Pokemon(String name, double lat, double lng, int id,
                   String spawnPointId, Long encounterId, Long despawnTime, Boolean isBugged) {
        this(name, lat, lng, fixId(id), spawnPointId, encounterId, despawnTime, isBugged);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        setId(fixId(id));
    }

    public String getSpawnPointId() {
        return spawnPointId;
    }

    public void setSpawnPointId(String spawnPointId) {
        this.spawnPointId = spawnPointId;
    }

    public Long getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Long encounterId) {
        this.encounterId = encounterId;
    }

    public Long getDespawnTime() {
        return despawnTime;
    }

    public void setDespawnTime(Long despawnTime) {
        this.despawnTime = despawnTime;
    }

    public Boolean getBugged() {
        return isBugged;
    }

    public void setBugged(Boolean bugged) {
        isBugged = bugged;
    }

    private static String fixId(int id) {
        return String.format("%03d", id);
    }

    @Override
    public String getJSObjectString() {
        return "{ " +
                "id: " + ((getId() == null) ? JSOStringify.UNDEFINED : "\"" + getId() + "\"") + ", " +
                "mapId: " + ((getMapId() == null) ? JSOStringify.UNDEFINED : String.valueOf(getMapId())) + ", " +
                "name: " + ((name == null) ? JSOStringify.UNDEFINED : "\"" + name + "\"") + ", " +
                "lat: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLat())) + ", " +
                "lng: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLng())) + ", " +
                "spawnPointId: " + ((spawnPointId == null) ? JSOStringify.UNDEFINED : "\"" + spawnPointId + "\"") + ", " +
                "encounterId: " + ((encounterId == null) ? JSOStringify.UNDEFINED : String.valueOf(encounterId)) + ", " +
                "despawnTime: " + ((despawnTime == null) ? JSOStringify.UNDEFINED : String.valueOf(despawnTime)) + ", " +
                "isBugged: " + ((isBugged == null) ? JSOStringify.UNDEFINED : String.valueOf(isBugged)) +
                " }";
    }

    @Override
    public String toString() {
        return getJSObjectString();
    }
}
