package org.paidaki.pogofinder.scanner;

import com.pokegoapi.api.map.Map;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import org.paidaki.pogofinder.api.PoGoApi;
import org.paidaki.pogofinder.api.account.PoGoAccount;
import org.paidaki.pogofinder.location.Location;
import org.paidaki.pogofinder.util.threading.MyRunnable;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.util.Util;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScanService extends Observable implements Stoppable {

    private static final long READY_TIMEOUT = 10000;        // 10 Seconds

    private PoGoApi poGo;
    private ScheduledExecutorService scanExecutor;

    protected class ReadyReport {

        protected PoGoAccount account;

        protected ReadyReport(PoGoAccount account) {
            this.account = account;
        }
    }

    private class ScanTask extends MyRunnable {

        private PoGoAccount account;
        private PokeScan pokeScan;
        private Location location;

        protected ScanTask(PokeScan pokeScan) {
            this.pokeScan = pokeScan;
            location = pokeScan.getLocation();
            account = pokeScan.getAccount();
        }

        @Override
        public void runTask() {
            if (pokeScan == null || location == null || account == null) {
                return;
            }
            try {
                ScanData scanData = new ScanData();
                Map map = account.go.getMap();

                account.go.setLocation(location.getLat(), location.getLng(), location.getAlt());

                scanData.setPokeScan(pokeScan);
                scanData.addPokemon(map.getCatchablePokemon());
                scanData.addGyms(map.getGyms());
                scanData.addPokestops(map.getMapObjects().getPokestops());

                poGo.scanComplete(scanData);
            } catch (LoginFailedException | RemoteServerException e) {
                e.printStackTrace();

                poGo.scanFailed(pokeScan);
            }
            scanExecutor.schedule(new ReadyTask(account), READY_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    private class ReadyTask extends MyRunnable {

        private PoGoAccount account;

        protected ReadyTask(PoGoAccount account) {
            this.account = account;
        }

        @Override
        public void runTask() {
            poGo.ready(account);
            setChanged();
            notifyObservers(new ReadyReport(account));
        }
    }

    public ScanService(PoGoApi poGo) {
        this.poGo = poGo;
        scanExecutor = Executors.newScheduledThreadPool(1);

        ThreadManager.addThread(this);
    }

    public void setThreadPoolSize(int threads) {
        Util.setThreadPoolSize(scanExecutor, threads);
    }

    public void startScan(PokeScan pokeScan) {
        scanExecutor.execute(new ScanTask(pokeScan));
    }

    @Override
    public void stop() {
        scanExecutor.shutdown();
    }

    @Override
    public void forceStop() {
        scanExecutor.shutdownNow();
    }
}
