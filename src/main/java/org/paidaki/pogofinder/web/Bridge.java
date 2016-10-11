package org.paidaki.pogofinder.web;

import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import netscape.javascript.JSObject;
import org.paidaki.pogofinder.api.PoGoApi;
import org.paidaki.pogofinder.api.account.PoGoAccount;
import org.paidaki.pogofinder.api.model.Gym;
import org.paidaki.pogofinder.api.model.PokeStop;
import org.paidaki.pogofinder.api.model.Pokemon;
import org.paidaki.pogofinder.exceptions.ControllerException;
import org.paidaki.pogofinder.exceptions.NotEnoughAccountsException;
import org.paidaki.pogofinder.gamepad.GamepadListener;
import org.paidaki.pogofinder.location.ElevationProvider;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.location.LocationProvider;
import org.paidaki.pogofinder.map.MapData;
import org.paidaki.pogofinder.map.MapDataManager;
import org.paidaki.pogofinder.scanner.PokeScan;
import org.paidaki.pogofinder.scanner.ScanData;
import org.paidaki.pogofinder.scanner.SpawnManager;
import org.paidaki.pogofinder.util.javascript.JavascriptEngine;

import java.util.List;

import static org.paidaki.pogofinder.api.model.SpawnPoint.BUGGED_TIME_MS;
import static org.paidaki.pogofinder.util.fileio.FileIO.DATA_JSON;

public class Bridge {

    static final String JS_BRIDGE_NAME = "java";

    private JavascriptEngine jsEngine;
    private GamepadListener gamepadListener;
    private WebListener webListener;
    private ElevationProvider elevationProvider;
    private LocationProvider locationProvider;
    private PoGoApi poGo;
    private MapDataManager mapDataManager;
    private SpawnManager spawnManager;
    private boolean documentReady;

    protected Bridge(Browser browser) {
        documentReady = false;
        jsEngine = new JavascriptEngine(browser);
        gamepadListener = new GamepadListener(this);
        webListener = new WebListener(this);
        locationProvider = new LocationProvider(this);
        elevationProvider = new ElevationProvider(this);
        spawnManager = new SpawnManager(this);
        mapDataManager = new MapDataManager(this);

        mapDataManager.loadData(DATA_JSON);
    }

    public void documentReady() {
        jsEngine.jsStringFunctionFX("loading", true);
        poGo = new PoGoApi(this);

        try {
            gamepadListener.start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        elevationProvider.start();
        webListener.start();

        populateMap(mapDataManager);

        if (poGo.isReady()) jsEngine.jsStringFunctionFX("loading", false);
        documentReady = true;
    }

    public void log(String text) {
        System.out.println(text);
    }

    public void offsetMarker(float modLat, float modLng) {
        jsEngine.jsStringFunctionFX("offsetMarker", modLat, modLng);
    }

    public void offsetMap(float modLat, float modLng) {
        jsEngine.jsStringFunctionFX("offsetMap", modLat, modLng);
    }

    public void requestScanForPokemon() {
        jsEngine.jsStringFunctionFX("scanForPokemonOnMarker");
    }

    public void mapZoomIn() {
        jsEngine.jsStringFunctionFX("map.zoomIn");
    }

    public void mapZoomOut() {
        jsEngine.jsStringFunctionFX("map.zoomOut");
    }

    public void toggleFollowMarker() {
        jsEngine.jsStringFunctionFX("toggleFollow");
    }

    public void toggleShowScanCircles() {
        jsEngine.jsStringFunctionFX("toggleScanCircles");
    }

    public void toggleScanOnly() {
        jsEngine.jsStringFunctionFX("toggleScanOnly");
    }

    public void toggleSpeedBoost() {
        jsEngine.jsStringFunctionFX("toggleSpeedBoost");
    }

    public void toggleForts() {
        jsEngine.jsStringFunctionFX("toggleForts");
    }

    public void changeSpeed(float modSpeed) {
        jsEngine.jsStringFunctionFX("changeSpeed", modSpeed);
    }

    public void reconnectController() {
        try {
            gamepadListener.connectController();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    public void controllerDisconnected() {
        jsEngine.jsStringFunctionFX("controllerDisconnected");
    }

    public void controllerConnected() {
        jsEngine.jsStringFunctionFX("controllerConnected");
    }

    public Location getPlayerLocation() {
        Location location = new Location();
        JSObject loc = (JSObject) jsEngine.jsStringFunctionFX("getCurrentLocation");

        location.setLat((Double) jsEngine.getJsMember(loc, "lat"));
        location.setLng((Double) jsEngine.getJsMember(loc, "lng"));
        location.setAlt(elevationProvider.getAltitude(false));

        return location;
    }

    public Location getFluctuatedLocation() {
        Location loc = locationProvider.fluctuateLocation(getPlayerLocation());
        loc.setAlt(elevationProvider.getAltitude(true));

        return loc;
    }

    public void userPokemonScan(double lat, double lng) {
        elevationProvider.forceAltitudeUpdate(() -> {
            Location loc = new Location(lat, lng, elevationProvider.getScanAltitude(false));
            scanForPokemon(loc);
        });
    }

    public synchronized void scanForPokemon(double lat, double lng) {
        Location loc = new Location(lat, lng, elevationProvider.getAltitude(true));

        scanForPokemon(loc);
    }

    public synchronized void scanForPokemon(Location loc) {
        if (loc.getAlt() == null) {
            scanForPokemon(loc.getLat(), loc.getLng());
            return;
        }
        if (!poGo.isReady()) {
            return;
        }
        int count = poGo.numOfAvailableAccounts(PoGoAccount.UseType.USER);

        if (count <= 0) {
            boolean scanOnly = (boolean) jsEngine.jsStringFunctionFX("getScanOnly");
            if (!scanOnly) elevationProvider.updateAltitude();
            return;
        }
        PoGoAccount account = poGo.getFirstAvailableAccount(PoGoAccount.UseType.USER);

        jsEngine.jsStringFunctionFX("scanStarted", count == 1);
        scanForPokemon(account, loc);
    }

    public synchronized void scanForPokemon(PoGoAccount account, double lat, double lng) {
        Location loc = new Location(lat, lng, elevationProvider.getAltitude(true));

        scanForPokemon(account, loc);
    }

    public synchronized void scanForPokemon(PoGoAccount account, Location loc) {
        if (loc.getAlt() == null) {
            scanForPokemon(account, loc.getLat(), loc.getLng());
            return;
        }
        if (!account.isReady()) {
            return;
        }
        account.setReady(false);

        JSObject scanCircle = (JSObject) jsEngine.jsStringFunctionFX("addScanCircle", loc.getLat(), loc.getLng());
        PokeScan pokeScan = new PokeScan(account, loc, scanCircle);

        poGo.scanNearbyPokemon(pokeScan);
    }

    public void scanComplete(ScanData scanData) {
        long maxExpire = spawnPokemon(scanData.getPokemon());
        long expire = maxExpire - System.currentTimeMillis();
        JSObject scanCircle = scanData.getPokeScan().getScanCircle();

        mapDataManager.updateMapData(scanData);

        if (maxExpire != -1) jsEngine.setJsMember(scanCircle, "expireMs", expire);
        jsEngine.jsObjectFunctionFX("doneScanning");
        jsEngine.jsObjectFunctionFX("expireScanCircle", scanCircle);
    }

    public void scanFailed(PokeScan pokeScan) {
        jsEngine.jsObjectFunctionFX("doneScanning");
        jsEngine.jsObjectFunctionFX("expireScanCircle", pokeScan.getScanCircle());
    }

    public void readyToScan() {
        jsEngine.jsStringFunctionFX("readyToScan");
    }

    public void apiReady() {
        try {
            spawnManager.start(poGo, mapDataManager.getMapData());
        } catch (NotEnoughAccountsException e) {
            e.printStackTrace();
        }
        readyToScan();

        if (documentReady) jsEngine.jsStringFunctionFX("loading", false);
    }

    public boolean isDocumentReady() {
        return documentReady;
    }

    private long spawnPokemon(List<CatchablePokemon> pokemonList) {
        long maxExpire = -1;

        for (CatchablePokemon pokemon : pokemonList) {
            long expire = pokemon.getExpirationTimestampMs();
            boolean isBugged = (expire < 1);
            expire = isBugged ? System.currentTimeMillis() + BUGGED_TIME_MS : expire;

            Pokemon p = new Pokemon(
                    pokemon.getPokemonId().name(),
                    pokemon.getLatitude(),
                    pokemon.getLongitude(),
                    pokemon.getPokemonId().getNumber(),
                    pokemon.getSpawnPointId(),
                    pokemon.getEncounterId(),
                    expire,
                    isBugged
            );
            boolean successSpawn = (boolean) jsEngine.jsStringFunctionFX("spawnPokemon", p.getJSObjectString());

            if (pokemon.getExpirationTimestampMs() > maxExpire && successSpawn) {
                maxExpire = pokemon.getExpirationTimestampMs();
            }
        }
        return maxExpire;
    }

    public void populateMap(MapData data) {
        int scanAreaMapId = (int) jsEngine.jsStringFunctionFX("addScanArea", data.getScanArea());

        if (scanAreaMapId != -1) {
            data.getScanArea().setMapId(scanAreaMapId);
        }
        for (Gym gym : data.getGyms()) {
            int gymMapId = (int) jsEngine.jsStringFunctionFX("addGymMarker", gym);

            if (gymMapId != -1) {
                gym.setMapId(gymMapId);
            }
        }
        for (PokeStop pokestop : data.getPokestops()) {
            int pokestopMapId = (int) jsEngine.jsStringFunctionFX("addPokestopMarker", pokestop);

            if (pokestopMapId != -1) {
                pokestop.setMapId(pokestopMapId);
            }
        }
    }

    private void populateMap(MapDataManager loader) {
        loader.getMapData().forEach(this::populateMap);
    }
}
