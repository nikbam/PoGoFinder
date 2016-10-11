package org.paidaki.pogofinder.api.account;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.paidaki.pogofinder.util.fileio.FileIO;
import org.paidaki.pogofinder.util.threading.MyRunnable;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.paidaki.pogofinder.util.fileio.FileIO.ACCOUNTS_JSON;

public class LoginManager extends Observable implements Stoppable {

    private static int CONN_TIMEOUT = 30;       // Seconds
    private static int MAX_LOGIN_TRIES = 5;

    private OkHttpClient httpClient;
    private ScheduledExecutorService loginExecutor;
    private ArrayList<AccountData> accounts;
    private AtomicInteger successfulLogins;

    public class LoginReport {

        public PoGoAccount acc;
        public boolean success;

        LoginReport(boolean success, PoGoAccount acc) {
            this.success = success;
            this.acc = acc;
        }
    }

    public class LoginFinalReport {

        public boolean done;
        public int successfulLogins;

        LoginFinalReport(boolean done, int successfulLogins) {
            this.done = done;
            this.successfulLogins = successfulLogins;
        }
    }

    private class AsyncLoginTask extends MyRunnable {

        private LoginManager.LoginFinalReport loginFinalReport;
        private List<Callable<Object>> tasks;

        private AsyncLoginTask(List<Callable<Object>> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void runTask() {
            try {
                loginExecutor.invokeAll(tasks);

                loginFinalReport = new LoginFinalReport(true, successfulLogins.get());
            } catch (InterruptedException e) {
                e.printStackTrace();

                loginFinalReport = new LoginFinalReport(false, 0);
            }

            setChanged();
            notifyObservers(loginFinalReport);
        }
    }

    private class LoginTask extends MyRunnable {

        private PoGoAccount.AccType accType;
        private String username;
        private String password;
        private LoginReport loginReport;

        LoginTask(AccountData accData) {
            setAccountData(accData);
        }

        private void setAccountData(AccountData accData) {
            this.accType = accData.getType();
            this.username = accData.getUsername();
            this.password = accData.getPassword();
        }

        @Override
        public void runTask() throws InterruptedException {
            if (accType == null || username == null || password == null || httpClient == null) {
                throw new NullPointerException("Invalid account data.");
            }
            boolean done = false;
            int tries = 0;
            PoGoAccount acc = null;

            while (!done) {
                try {
                    acc = new PoGoAccount(httpClient, accType);
                    acc.login(username, password);

                    successfulLogins.getAndIncrement();
                    done = true;
                } catch (RemoteServerException | LoginFailedException e) {
                    tries++;

                    if (tries >= MAX_LOGIN_TRIES) {
                        done = true;
                        e.printStackTrace();
                    }
                    Thread.sleep(500);
                }
            }
            loginReport = (!acc.isReady()) ? new LoginReport(false, null) : new LoginReport(true, acc);

            setChanged();
            notifyObservers(loginReport);
        }
    }

    public LoginManager() {
        successfulLogins = new AtomicInteger(0);
        accounts = new ArrayList<>();

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .build();

        ThreadManager.addThread(this);
        loadAccounts();

        loginExecutor = Executors.newScheduledThreadPool(accounts.size() + 1);
    }

    public void startLogins() {
        ArrayList<Callable<Object>> tasks = accounts.stream()
                .map(accData -> Executors.callable(new LoginTask(accData)))
                .collect(Collectors.toCollection(ArrayList::new));

        loginExecutor.execute(new AsyncLoginTask(tasks));
    }

    public void reloadAccounts() {
        accounts.clear();
        loadAccounts();
    }

    private void loadAccounts() {
        try {
            String file = FileIO.readFile(ACCOUNTS_JSON);
            JSONObject dataObject = new JSONObject(file);
            JSONArray accArray = dataObject.getJSONArray("accounts");

            for (Object a : accArray) {
                AccountData data = new AccountData();
                JSONObject ad = (JSONObject) a;

                boolean active = ad.getBoolean("active");
                if (!active) continue;      //TODO Do not load inactive accounts (Maybe reconsider)
                data.setActive(true);

                String type = ad.getString("type").toLowerCase();
                PoGoAccount.AccType accType;
                switch (type) {
                    case "ptc":
                        accType = PoGoAccount.AccType.PTC;
                        break;
                    case "google":
                        accType = PoGoAccount.AccType.GOOGLE;
                        break;
                    default:
                        throw new JSONException("Invalid account type.");
                }
                data.setType(accType);

                String username = ad.getString("username");
                data.setUsername(username);

                String password = ad.getString("password");
                data.setPassword(password);

                accounts.add(data);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        loginExecutor.shutdown();
    }

    @Override
    public void forceStop() {
        loginExecutor.shutdownNow();
    }
}
