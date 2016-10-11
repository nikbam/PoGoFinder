package org.paidaki.pogofinder.map;

import org.paidaki.pogofinder.api.model.Gym;
import org.paidaki.pogofinder.api.model.PokeStop;
import org.paidaki.pogofinder.api.model.SpawnPoint;
import org.paidaki.pogofinder.scanner.ScanArea;

import java.util.ArrayList;

public class MapData {

    private ScanArea scanArea;
    private ArrayList<Gym> gyms;
    private ArrayList<PokeStop> pokestops;
    private ArrayList<SpawnPoint> spawnPoints;

    public MapData() {
        this(new ScanArea());
    }

    public MapData(ScanArea scanArea) {
        this.scanArea = scanArea;
        gyms = new ArrayList<>();
        pokestops = new ArrayList<>();
        spawnPoints = new ArrayList<>();
    }

    public ScanArea getScanArea() {
        return scanArea;
    }

    public void setScanArea(ScanArea scanArea) {
        this.scanArea = scanArea;
    }

    public ArrayList<Gym> getGyms() {
        return gyms;
    }

    public void setGyms(ArrayList<Gym> gyms) {
        this.gyms = gyms;
    }

    public ArrayList<PokeStop> getPokestops() {
        return pokestops;
    }

    public void setPokestops(ArrayList<PokeStop> pokestops) {
        this.pokestops = pokestops;
    }

    public ArrayList<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    public void setSpawnPoints(ArrayList<SpawnPoint> spawnPoints) {
        this.spawnPoints = spawnPoints;
    }
}
