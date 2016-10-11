package org.paidaki.pogofinder.api.account;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

public class PoGoAccount {

    public enum AccType {
        PTC,
        GOOGLE
    }

    public enum UseType {
        ANY,
        USER,
        SPAWN,
        RESERVED
    }

    public PokemonGo go;
    private OkHttpClient httpClient;
    private UseType useType;
    private AccType accType;
    private boolean ready;

    public PoGoAccount(OkHttpClient httpClient, UseType useType, AccType accType) {
        this.httpClient = httpClient;
        this.useType = useType;
        this.accType = accType;
        ready = false;
        go = new PokemonGo(httpClient);
    }

    public PoGoAccount(OkHttpClient httpClient, AccType accType) {
        this(httpClient, UseType.USER, accType);
    }

    public void login(String username, String password) throws LoginFailedException, RemoteServerException {
        switch (accType) {
            case PTC:
                go.login(new PtcCredentialProvider(httpClient, username, password));
                break;
            case GOOGLE:
                //TODO Implement Google Login
                throw new LoginFailedException("Currently not supporting Google login.");
            default:
                throw new LoginFailedException("Unknown account type.");
        }
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public UseType getUseType() {
        return useType;
    }

    public void setUseType(UseType useType) {
        this.useType = useType;
    }
}
