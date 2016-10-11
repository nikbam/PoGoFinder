package org.paidaki.pogofinder.map;

import org.paidaki.pogofinder.util.javascript.JSOStringify;
import org.paidaki.pogofinder.location.Location;

import java.util.List;
import java.util.Optional;

public abstract class MapObject implements JSOStringify {

    private String id = null;
    private Integer mapId = null;
    private Location location = null;

    public MapObject() {
        super();
    }

    public MapObject(String id, Location location) {
        this.id = id;
        this.location = location;
    }

    public MapObject(String id, double lat, double lng) {
        this.id = id;
        location = new Location(lat, lng);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static MapObject getMapObject(List<? extends MapObject> list, String id) {
        Optional<? extends MapObject> opt = list.stream().filter(mapObject -> mapObject.getId().equals(id)).findFirst();

        return opt.isPresent() ? opt.get() : null;
    }
}
