package org.paidaki.pogofinder.scanner;

import netscape.javascript.JSObject;
import org.paidaki.pogofinder.api.account.PoGoAccount;
import org.paidaki.pogofinder.location.Location;

public class PokeScan {

    private PoGoAccount account;
    private Location location;
    private JSObject scanCircle;

    public PokeScan(PoGoAccount account, Location location, JSObject scanCircle) {
        this.account = account;
        this.location = location;
        this.scanCircle = scanCircle;
    }

    public PoGoAccount getAccount() {
        return account;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public JSObject getScanCircle() {
        return scanCircle;
    }

    public void setScanCircle(JSObject scanCircle) {
        this.scanCircle = scanCircle;
    }
}
