package org.paidaki.pogofinder.api.model;

import org.paidaki.pogofinder.util.javascript.JSOStringify;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.map.MapObject;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Gym extends MapObject {

    public enum Team {
        UNRECOGNIZED(-1),
        NEUTRAL(0),
        BLUE(1),
        RED(2),
        YELLOW(3);

        private final int value;
        private final static Map<Integer, Team> map =
                Arrays.stream(Team.values()).collect(Collectors.toMap(team -> team.value, team -> team));

        Team(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Team getTeam(int value) {
            Team team = map.get(value);
            return (team == null) ? UNRECOGNIZED : team;
        }
    }

    private Team team = Team.NEUTRAL;

    public Gym() {
        super();
    }

    public Gym(String id, Location location, Team team) {
        super(id, location);
        this.team = team;
    }

    public Gym(String id, double lat, double lng, Team team) {
        super(id, lat, lng);
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String getJSObjectString() {
        return "{ " +
                "id: " + ((getId() == null) ? JSOStringify.UNDEFINED : "\"" + getId() + "\"") + ", " +
                "mapId: " + ((getMapId() == null) ? JSOStringify.UNDEFINED : String.valueOf(getMapId())) + ", " +
                "lat: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLat())) + ", " +
                "lng: " + ((getLocation() == null) ? JSOStringify.UNDEFINED : String.valueOf(getLocation().getLng())) + ", " +
                "team: " + ((team == null) ? JSOStringify.UNDEFINED : "\"" + String.valueOf(team).toLowerCase() + "\"") +
                " }";
    }

    @Override
    public String toString() {
        return getJSObjectString();
    }
}
