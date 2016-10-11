package org.paidaki.pogofinder.api.account;

public class AccountData {

    private PoGoAccount.AccType type;
    private String username;
    private String password;
    private boolean active;

    protected AccountData() {
        super();
    }

    public PoGoAccount.AccType getType() {
        return type;
    }

    public void setType(PoGoAccount.AccType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
