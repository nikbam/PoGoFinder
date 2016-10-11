package org.paidaki.pogofinder.map;

import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.paidaki.pogofinder.api.model.Gym;
import org.paidaki.pogofinder.api.model.PokeStop;
import org.paidaki.pogofinder.api.model.SpawnPoint;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.scanner.ScanArea;
import org.paidaki.pogofinder.scanner.ScanData;
import org.paidaki.pogofinder.util.Util;
import org.paidaki.pogofinder.util.fileio.FileIO;
import org.paidaki.pogofinder.util.threading.MyRunnable;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.web.Bridge;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.paidaki.pogofinder.util.fileio.FileIO.DATA_JSON;

public class MapDataManager implements Stoppable {

    private Bridge bridge;
    private ArrayList<MapData> mapData;
    private JSONObject dataObject;
    private ScheduledExecutorService executor;
    private LoadTask loadTask;
    private UpdateTask updateTask;

    private class LoadTask extends MyRunnable {

        private String jsonData;

        @Override
        public void runTask() throws InterruptedException {
            if (jsonData == null) {
                throw new NullPointerException("Invalid JSON Data");
            }
            try {
                String file = FileIO.readFile(jsonData);
                dataObject = new JSONObject(file);
                JSONArray dataArray = dataObject.getJSONArray("data");

                for (Object m : dataArray) {
                    MapData data = new MapData();
                    JSONObject md = (JSONObject) m;

                    JSONObject scanArea = md.getJSONObject("scan_area");
                    data.setScanArea(loadScanAreas(scanArea));

                    JSONArray gymsArray = md.getJSONArray("gyms");
                    data.setGyms(loadGyms(gymsArray));

                    JSONArray pokestopsArray = md.getJSONArray("pokestops");
                    data.setPokestops(loadPokestops(pokestopsArray));

                    JSONArray spawnPointsArray = md.getJSONArray("spawn_points");
                    data.setSpawnPoints(loadSpawnPoints(spawnPointsArray));

                    mapData.add(data);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        private void setData(String jsonData) {
            this.jsonData = jsonData;
        }
    }

    private class UpdateTask extends MyRunnable {

        private ScanData scanData;

        @Override
        public void runTask() throws InterruptedException {
            if (scanData == null) {
                throw new NullPointerException("Invalid Scan Data");
            }
            UpdatedMapData updatedMapData = new UpdatedMapData();
            boolean changed = false;
            boolean newData = false;

            for (MapData data : mapData) {
                ScanArea scanArea = data.getScanArea();
                MapData changedMapData = new MapData(scanArea);
                MapData newMapData = new MapData(scanArea);

                ArrayList<Gym> gyms = data.getGyms();

                for (com.pokegoapi.api.gym.Gym g : scanData.getGyms()) {
                    Gym gym = (Gym) MapObject.getMapObject(gyms, g.getId());
                    Gym.Team team = Gym.Team.getTeam(g.getOwnedByTeam().getNumber());

                    if (gym != null) {
                        if (gym.getTeam() != team) {
                            gym.setTeam(team);
                            changedMapData.getGyms().add(gym);
                            changed = true;
                        }
                    } else {
                        if (scanArea.contains(g.getLatitude(), g.getLongitude())) {
                            Gym newGym = new Gym(g.getId(), g.getLatitude(), g.getLongitude(), team);

                            gyms.add(newGym);
                            newMapData.getGyms().add(newGym);
                            newData = true;
                        }
                    }
                }
                ArrayList<PokeStop> pokestops = data.getPokestops();

                for (Pokestop p : scanData.getPokestops()) {
                    PokeStop pokestop = (PokeStop) MapObject.getMapObject(pokestops, p.getId());
                    boolean hasLure = p.hasLure();

                    if (pokestop != null) {
                        if (pokestop.getHasLure() != hasLure) {
                            pokestop.setHasLure(hasLure);
                            changedMapData.getPokestops().add(pokestop);
                            changed = true;
                        }
                    } else {
                        if (scanArea.contains(p.getLatitude(), p.getLongitude())) {
                            PokeStop newPokestop = new PokeStop(p.getId(), p.getLatitude(), p.getLongitude(), hasLure);

                            pokestops.add(newPokestop);
                            newMapData.getPokestops().add(newPokestop);
                            newData = true;
                        }
                    }
                }
                ArrayList<SpawnPoint> spawns = data.getSpawnPoints();
                LinkedHashMap<SpawnPoint, Integer> indexes = new LinkedHashMap<>();

                for (CatchablePokemon c : scanData.getPokemon()) {
                    if (c.getExpirationTimestampMs() < 0 || c.getSpawnPointId().length() != 11) {
                        continue;
                    }
                    int time = (Util.getSeconds(c.getExpirationTimestampMs()) -
                            SpawnPoint.SPAWN_DURATION_SECS + 3600) % 3600;
                    int pos = Util.binarySearch(spawns, time);
                    SpawnPoint newSpawn = null;

                    for (int i = pos; i < spawns.size(); i++) {
                        SpawnPoint spawn = spawns.get(i);

                        if (spawn.getSpawnTime() == time) {
                            if (!spawn.getId().equals(c.getSpawnPointId()) ||
                                    spawn.getLocation().getLat() != c.getLatitude() ||
                                    spawn.getLocation().getLng() != c.getLongitude()) {
                                continue;
                            } else {
                                break;
                            }
                        }
                        if (scanArea.contains(c.getLatitude(), c.getLongitude())) {
                            newSpawn = new SpawnPoint(c.getSpawnPointId(), c.getLatitude(),
                                    c.getLongitude(), time);

                            newMapData.getSpawnPoints().add(newSpawn);
                            indexes.put(newSpawn, pos);
                            newData = true;
                            break;
                        }
                    }
                    if (newSpawn != null) spawns.add(pos, newSpawn);
                }
                updatedMapData.getChangedData().add(changedMapData);
                updatedMapData.getNewData().add(newMapData);
                updatedMapData.getSpawnIndexes().put(newMapData, indexes);
            }
            if (changed) {
                updatedMapData.getChangedData().forEach(bridge::populateMap);
            }
            if (newData) {
                updatedMapData.getNewData().forEach(bridge::populateMap);

                try {
                    saveData(updatedMapData);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        public void setData(ScanData scanData) {
            this.scanData = scanData;
        }
    }

    public MapDataManager(Bridge bridge) {
        this.bridge = bridge;
        mapData = new ArrayList<>();
        executor = Executors.newSingleThreadScheduledExecutor();
        loadTask = new LoadTask();
        updateTask = new UpdateTask();

        ThreadManager.addThread(this);
    }

    public void loadData(String jsonData) {
        if (!loadTask.isRunning()) {
            loadTask.setData(jsonData);
            executor.execute(loadTask);
        }
    }

    public void updateMapData(ScanData scanData) {
        if (!updateTask.isRunning()) {
            updateTask.setData(scanData);
            executor.execute(updateTask);
        }
    }

    public ArrayList<MapData> getMapData() {
        return mapData;
    }

    public JSONObject getDataObject() {
        return dataObject;
    }

    private void saveData(UpdatedMapData updatedData) throws IOException, JSONException {
        ArrayList<MapData> newData = updatedData.getNewData();
        JSONArray dataArray = dataObject.getJSONArray("data");

        for (Object d : dataArray) {
            JSONObject data = (JSONObject) d;
            JSONObject scanArea = data.getJSONObject("scan_area");
            double nwLat = scanArea.getJSONObject("NW").getDouble("lat");
            double nwLng = scanArea.getJSONObject("NW").getDouble("lng");
            double seLat = scanArea.getJSONObject("SE").getDouble("lat");
            double seLng = scanArea.getJSONObject("SE").getDouble("lng");

            for (MapData md : newData) {
                if (md.getScanArea().equals(nwLat, nwLng, seLat, seLng)) {
                    JSONArray gymsArray = data.getJSONArray("gyms");
                    JSONArray pokestopsArray = data.getJSONArray("pokestops");
                    JSONArray spawnsArray = data.getJSONArray("spawn_points");

                    saveForts(gymsArray, md.getGyms());
                    saveForts(pokestopsArray, md.getPokestops());
                    saveSpawnPoints(spawnsArray, updatedData.getSpawnIndexes().get(md));

                    try (FileWriter writer = new FileWriter(DATA_JSON)) {
                        writer.write(String.valueOf(dataObject));

                        System.err.println("INFO: New data has been successfully saved.");
                    }
                    break;
                }
            }
        }
    }

    private ScanArea loadScanAreas(JSONObject ScanAreaObj) throws JSONException {
        ScanArea scanArea = new ScanArea();

        scanArea.setNorthWest(new Location(ScanAreaObj.getJSONObject("NW").getDouble("lat"),
                ScanAreaObj.getJSONObject("NW").getDouble("lng")));
        scanArea.setSouthEast(new Location(ScanAreaObj.getJSONObject("SE").getDouble("lat"),
                ScanAreaObj.getJSONObject("SE").getDouble("lng")));

        return scanArea;
    }

    private ArrayList<Gym> loadGyms(JSONArray gymsArray) throws JSONException {
        ArrayList<Gym> gyms = new ArrayList<>();

        for (Object g : gymsArray) {
            JSONObject gymObject = (JSONObject) g;
            Gym gym = new Gym();

            gym.setId(gymObject.getString("id"));
            gym.setLocation(new Location(gymObject.getDouble("lat"), gymObject.getDouble("lng")));
            if (gymObject.has("team")) gym.setTeam(Gym.Team.getTeam(gymObject.getInt("team")));

            gyms.add(gym);
        }
        return gyms;
    }

    private ArrayList<PokeStop> loadPokestops(JSONArray pokestopsArray) throws JSONException {
        ArrayList<PokeStop> pokestops = new ArrayList<>();

        for (Object ps : pokestopsArray) {
            JSONObject pokestopObject = (JSONObject) ps;
            PokeStop pokestop = new PokeStop();

            pokestop.setId(pokestopObject.getString("id"));
            pokestop.setLocation(new Location(pokestopObject.getDouble("lat"), pokestopObject.getDouble("lng")));
            if (pokestopObject.has("lure")) pokestop.setHasLure(pokestopObject.getInt("lure") == 1);

            pokestops.add(pokestop);
        }
        return pokestops;
    }

    private void saveForts(JSONArray fortsArray, List<? extends MapObject> forts) throws JSONException {
        for (MapObject fort : forts) {
            JSONObject fortObj = new JSONObject();

            fortObj.put("id", fort.getId());
            fortObj.put("lat", fort.getLocation().getLat());
            fortObj.put("lng", fort.getLocation().getLng());

            fortsArray.put(fortObj);

            System.err.println("INFO: Found new " + ((fort instanceof Gym) ? "gym" : "pokestop") + " - " + fort);
        }
    }

    private ArrayList<SpawnPoint> loadSpawnPoints(JSONArray spawnPointsArray) throws JSONException {
        ArrayList<SpawnPoint> spawns = new ArrayList<>();

        for (Object sp : spawnPointsArray) {
            JSONObject spawnPointObject = (JSONObject) sp;
            SpawnPoint spawnPoint = new SpawnPoint();

            spawnPoint.setId(spawnPointObject.getString("sid"));
            spawnPoint.setLocation(new Location(spawnPointObject.getDouble("lat"), spawnPointObject.getDouble("lng")));
            spawnPoint.setSpawnTime(spawnPointObject.getInt("time"));

            spawns.add(spawnPoint);
        }
        return spawns;
    }

    private void saveSpawnPoints(JSONArray spawnPointsArray, LinkedHashMap<SpawnPoint, Integer> spawns) throws JSONException {
        for (Map.Entry<SpawnPoint, Integer> entry : spawns.entrySet()) {
            SpawnPoint spawn = entry.getKey();
            int index = entry.getValue();

            JSONObject spawnObj = new JSONObject();

            spawnObj.put("time", spawn.getSpawnTime());
            spawnObj.put("lat", spawn.getLocation().getLat());
            spawnObj.put("lng", spawn.getLocation().getLng());
            spawnObj.put("sid", spawn.getId());

            for (int i = spawnPointsArray.length() - 1; i >= index; i--) {
                JSONObject obj = (JSONObject) spawnPointsArray.get(i);
                spawnPointsArray.put(i + 1, obj);
            }
            spawnPointsArray.put(index, spawnObj);

            System.err.println("INFO: Found new spawn - " + spawn);
        }
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

    @Override
    public void forceStop() {
        executor.shutdownNow();
    }
}
