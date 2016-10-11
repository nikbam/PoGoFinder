package org.paidaki.pogofinder.map;

import org.paidaki.pogofinder.api.model.SpawnPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class UpdatedMapData {

    private ArrayList<MapData> changedData;
    private ArrayList<MapData> newData;
    private HashMap<MapData, LinkedHashMap<SpawnPoint, Integer>> spawnIndexes;

    public UpdatedMapData() {
        changedData = new ArrayList<>();
        newData = new ArrayList<>();
        spawnIndexes = new HashMap<>();
    }

    public ArrayList<MapData> getChangedData() {
        return changedData;
    }

    public ArrayList<MapData> getNewData() {
        return newData;
    }

    public HashMap<MapData, LinkedHashMap<SpawnPoint, Integer>> getSpawnIndexes() {
        return spawnIndexes;
    }
}
