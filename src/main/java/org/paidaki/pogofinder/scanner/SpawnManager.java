package org.paidaki.pogofinder.scanner;

import org.paidaki.pogofinder.api.PoGoApi;
import org.paidaki.pogofinder.api.account.PoGoAccount;
import org.paidaki.pogofinder.api.model.SpawnPoint;
import org.paidaki.pogofinder.exceptions.NotEnoughAccountsException;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.map.MapData;
import org.paidaki.pogofinder.util.Util;
import org.paidaki.pogofinder.util.threading.MyRunnable;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.web.Bridge;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnManager implements Observer, Stoppable {

    private static final int SPAWNS_PER_SCANNER = 250;
    private static final int SCAN_DELAY_OFFSET = 10;        // 10 Seconds

    private PoGoApi poGo;
    private Bridge bridge;
    private ArrayList<MapData> mapData;
    private ArrayList<SpawnPoint> spawns;
    private Map<PoGoAccount, SpawnScanner> scanners;
    private ScheduledExecutorService scanExecutor;
    private AtomicInteger spawnIndex;
    private boolean running;

    private class SpawnScanner extends MyRunnable {

        private PoGoAccount account;
        private SpawnPoint spawn;
        private int delay;

        private SpawnScanner(PoGoAccount account) {
            this.account = account;
        }

        private void scan(SpawnPoint spawn, int delay) {
            this.spawn = spawn;
            this.delay = delay;

            scanExecutor.schedule(this, delay, TimeUnit.SECONDS);
        }

        private boolean isReady() {
            return account.isReady();
        }

        @Override
        public void runTask() {
            if (account == null || spawn == null || delay < 0) {
                return;
            }
            bridge.scanForPokemon(account, spawn.getLocation());
        }
    }

    public SpawnManager(Bridge bridge) {
        this.bridge = bridge;
        running = false;
        scanExecutor = Executors.newScheduledThreadPool(1);

        ThreadManager.addThread(this);
    }

    public void start(PoGoApi poGo, ArrayList<MapData> mapData) throws NotEnoughAccountsException {
        if (running) return;

        this.poGo = poGo;
        this.mapData = mapData;
        spawns = new ArrayList<>();
        scanners = new HashMap<>();
        spawnIndex = new AtomicInteger();
        ArrayList<PoGoAccount> accounts = poGo.getAccounts();

        if (accounts.isEmpty()) {
            throw new NotEnoughAccountsException("There are no logged in accounts.");
        }
        initialize();
        startScanning();
        running = true;
    }

    private void initialize() throws NotEnoughAccountsException {
        for (MapData md : mapData) {
            spawns.addAll(md.getSpawnPoints());
        }
        if (!Util.isSorted(spawns, SpawnPoint.ASCENDING_COMPARATOR)) {
            Collections.sort(spawns, SpawnPoint.ASCENDING_COMPARATOR);
        }
        int numOfScanners = ((spawns.size() - 1) / SPAWNS_PER_SCANNER) + 1;

        if (numOfScanners + 1 > poGo.numOfAvailableAccounts()) {
            throw new NotEnoughAccountsException("Not enough available accounts.");
        }
        Util.setThreadPoolSize(scanExecutor, numOfScanners);

        for (int i = 0; i < numOfScanners; i++) {
            PoGoAccount acc = poGo.getFirstAvailableAccount(PoGoAccount.UseType.USER);
            acc.setUseType(PoGoAccount.UseType.SPAWN);

            SpawnScanner scanner = new SpawnScanner(acc);
            scanners.put(acc, scanner);
        }
        poGo.getScanService().addObserver(this);
    }

    private void startScanning() {
        int currSecs = Util.getCurrentSeconds();

        spawnIndex.set(Util.binarySearch(spawns, currSecs));
        scanners.values().forEach(this::startScanner);
    }

    private synchronized void startScanner(SpawnScanner scanner) {
        int index = spawnIndex.getAndIncrement() % spawns.size();
        SpawnPoint spawn = spawns.get(index);
        int delay = spawn.getSpawnTime() - Util.getCurrentSeconds() + SCAN_DELAY_OFFSET;
        delay = (delay < 0) ? 0 : delay;

        scanner.scan(spawn, delay);
    }

    private ArrayList<SpawnPoint> getFilteredSpawns(ArrayList<SpawnPoint> spawns, double scanRadius) {
        ArrayList<SpawnPoint> filteredSpawns = new ArrayList<>(spawns);

        for (int i = 0; i < spawns.size(); i++) {
            Location spawnLoc1 = filteredSpawns.get(i).getLocation();

            for (int j = i + 1; j < spawns.size(); j++) {
                Location spawnLoc2 = filteredSpawns.get(j).getLocation();
                double distance = Util.haversine(spawnLoc1.getLat(), spawnLoc1.getLng(),
                        spawnLoc2.getLat(), spawnLoc2.getLng());

                if (distance < scanRadius) {
                    filteredSpawns.remove(j);
                }
            }
        }
        return filteredSpawns;
    }

    private SpawnScanner getAvailableScanner() {
        for (SpawnScanner scanner : scanners.values()) {
            if (scanner.isReady()) {
                return scanner;
            }
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof ScanService.ReadyReport) {

            ScanService.ReadyReport readyReport = (ScanService.ReadyReport) arg;
            PoGoAccount readyAcc = readyReport.account;

            if (readyAcc.getUseType() == PoGoAccount.UseType.SPAWN) {
                SpawnScanner scanner = scanners.get(readyAcc);

                startScanner(scanner);
            }
        }
    }

    @Override
    public void stop() {
        scanExecutor.shutdown();
        running = false;
        for (SpawnScanner scanner : scanners.values()) {
            scanner.account.setReady(true);
            scanner.account.setUseType(PoGoAccount.UseType.USER);
        }
    }

    @Override
    public void forceStop() {
        scanExecutor.shutdownNow();
    }
}
