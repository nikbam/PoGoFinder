package org.paidaki.pogofinder.api.model;

import org.paidaki.pogofinder.util.javascript.JSOStringify;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.map.MapObject;

public class PokeStop extends MapObject {

    private Boolean hasLure = false;

    public PokeStop() {
        super();
    }

    public PokeStop(String id, Location location, boolean hasLure) {
        super(id, location);
        this.hasLure = hasLure;
    }

    public PokeStop(String id, double lat, double lng, boolean hasLure) {
        super(id, lat, lng);
        this.hasLure = hasLure;
    }

    public Boolean getHasLure() {
        return hasLure;
    }

    public void setHasLure(Boolean hasLure) {
        this.hasLure = hasLure;
    }

    @Override
    public String getJSObjectString() {
        return "{ " +
                "id: " + ((getId() == null) ? JSOStringify.UNDEFINED : "\"" + getId() + "\"") + ", " +
                "mapId: " + ((getMapId() == null) ? JSOStringify.UNDEFINED : String.valueOf(getMapId())) + ", " +
                "lat: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLat())) + ", " +
                "lng: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLng())) + ", " +
                "hasLure: " + ((hasLure == null) ? JSOStringify.UNDEFINED : String.valueOf(hasLure)) +
                " }";
    }

    @Override
    public String toString() {
        return getJSObjectString();
    }
}
