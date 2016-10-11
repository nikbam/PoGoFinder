package org.paidaki.pogofinder.api;

import org.paidaki.pogofinder.api.account.LoginManager;
import org.paidaki.pogofinder.api.account.PoGoAccount;
import org.paidaki.pogofinder.scanner.PokeScan;
import org.paidaki.pogofinder.scanner.ScanData;
import org.paidaki.pogofinder.scanner.ScanService;
import org.paidaki.pogofinder.web.Bridge;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class PoGoApi implements Observer {

    private Bridge bridge;
    private boolean ready;
    private ScanService scanService;
    private LoginManager loginManager;
    private ArrayList<PoGoAccount> accounts;

    public PoGoApi(Bridge bridge) {
        this.bridge = bridge;
        scanService = new ScanService(this);
        loginManager = new LoginManager();
        accounts = new ArrayList<>();
        ready = false;

        loginManager.addObserver(this);
        loginManager.startLogins();
    }

    public void scanNearbyPokemon(PokeScan pokeScan) {
        scanService.startScan(pokeScan);
    }

    public synchronized void scanComplete(ScanData scanData) {
        bridge.scanComplete(scanData);
    }

    public synchronized void scanFailed(PokeScan pokeScan) {
        bridge.scanFailed(pokeScan);
    }

    public boolean isReady() {
        return ready;
    }

    public synchronized void ready(PoGoAccount account) {
        account.setReady(true);
        if (account.getUseType() == PoGoAccount.UseType.USER) {
            bridge.readyToScan();
        }
    }

    public synchronized PoGoAccount getFirstAvailableAccount() {
        return getFirstAvailableAccount(PoGoAccount.UseType.ANY);
    }

    public synchronized PoGoAccount getFirstAvailableAccount(PoGoAccount.UseType useType) {
        for (PoGoAccount acc : accounts) {
            if (acc.isReady() && (useType == PoGoAccount.UseType.ANY || acc.getUseType() == useType)) {
                return acc;
            }
        }
        return null;
    }

    public synchronized int numOfAvailableAccounts() {
        return numOfAvailableAccounts(PoGoAccount.UseType.ANY);
    }

    public synchronized int numOfAvailableAccounts(PoGoAccount.UseType useType) {
        int count = 0;

        for (PoGoAccount acc : accounts) {
            if (acc.isReady() && (useType == PoGoAccount.UseType.ANY || acc.getUseType() == useType)) {
                count++;
            }
        }
        return count;
    }

    public ArrayList<PoGoAccount> getAccounts() {
        return accounts;
    }

    public ScanService getScanService() {
        return scanService;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof LoginManager.LoginFinalReport) {

            LoginManager.LoginFinalReport loginFinalReport = (LoginManager.LoginFinalReport) arg;

            if (loginFinalReport.successfulLogins > 0) {
                ready = loginFinalReport.done;
                scanService.setThreadPoolSize(loginFinalReport.successfulLogins);
                bridge.apiReady();
            }
            return;
        }

        if (arg instanceof LoginManager.LoginReport) {

            LoginManager.LoginReport loginReport = (LoginManager.LoginReport) arg;

            if (loginReport.success) {
                accounts.add(loginReport.acc);
            }
        }
    }
}
