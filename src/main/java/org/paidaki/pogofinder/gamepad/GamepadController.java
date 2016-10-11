package org.paidaki.pogofinder.gamepad;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import org.paidaki.pogofinder.exceptions.ControllerException;
import org.paidaki.pogofinder.util.threading.MyRunnable;
import org.paidaki.pogofinder.util.threading.Stoppable;
import org.paidaki.pogofinder.util.threading.ThreadManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GamepadController extends Observable implements Stoppable {

    private static GamepadController instance;

    private static final String MY_CONTROLLER = "F310";
    private static final float DEAD_ZONE = 0.06f;
    private static final long POLLING_INTERVAL = 50L;

    private Controller controller;
    private ScheduledExecutorService pollExecutor, scanExecutor;
    private MyRunnable pollTask;
    private MyRunnable scanTask;
    private HashMap<GamepadData.Type, GamepadData> inputData;

    protected class PollingReport {
        protected HashMap<GamepadData.Type, GamepadData> inputData;
        protected boolean ready;
        protected boolean polling;

        protected PollingReport(HashMap<GamepadData.Type, GamepadData> inputData, boolean ready, boolean polling) {
            this.inputData = inputData;
            this.ready = ready;
            this.polling = polling;
        }
    }

    protected class ScanReport {
        protected boolean connected;

        protected ScanReport(boolean success) {
            connected = success;
        }
    }

    private class PollTask extends MyRunnable {

        private PollingReport pollingReport;

        @Override
        public void runTask() {
            if (!controller.poll()) {
                System.err.println("WARNING: " + GamepadController.this.getClass().getSimpleName() +
                        " - Controller disconnected.");
                controller = null;
                pollingReport = new PollingReport(null, false, false);

                setChanged();
                notifyObservers(pollingReport);
                stopPolling(false);
                return;
            }
            for (Component c : controller.getComponents()) {
                handleInput(c);
            }
            pollingReport = new PollingReport(inputData, true, true);

            setChanged();
            notifyObservers(pollingReport);
        }
    }

    private class ScanTask extends MyRunnable {

        private ScanReport scanReport;

        @Override
        public void runTask() {
            boolean result = false;

            if (controller == null) {
                try {
                    ControllerEnvironment environment = ControllerEnvironment.getDefaultEnvironment();
                    Field controllers = environment.getClass().getDeclaredField("controllers");
                    Field loadedPlugins = environment.getClass().getDeclaredField("loadedPlugins");

                    controllers.setAccessible(true);
                    loadedPlugins.setAccessible(true);

                    controllers.set(environment, null);
                    loadedPlugins.set(environment, new ArrayList());

                    initialize();

                    result = true;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ControllerException e) {
                    System.err.println("WARNING: " + GamepadController.this.getClass().getSimpleName() +
                            " - " + e.getMessage());
                }
            }
            scanReport = new ScanReport(result);

            setChanged();
            notifyObservers(scanReport);
        }
    }

    private GamepadController() {
        inputData = new HashMap<>();

        inputData.put(GamepadData.Type.LEFT_STICK_X, new GamepadData(GamepadData.Type.LEFT_STICK_X));
        inputData.put(GamepadData.Type.LEFT_STICK_Y, new GamepadData(GamepadData.Type.LEFT_STICK_Y));
        inputData.put(GamepadData.Type.RIGHT_STICK_X, new GamepadData(GamepadData.Type.RIGHT_STICK_X));
        inputData.put(GamepadData.Type.RIGHT_STICK_Y, new GamepadData(GamepadData.Type.RIGHT_STICK_Y));
        inputData.put(GamepadData.Type.SHOULDERS_Z, new GamepadData(GamepadData.Type.SHOULDERS_Z));
        inputData.put(GamepadData.Type.BUTTON_A, new GamepadData(GamepadData.Type.BUTTON_A));
        inputData.put(GamepadData.Type.BUTTON_B, new GamepadData(GamepadData.Type.BUTTON_B));
        inputData.put(GamepadData.Type.BUTTON_X, new GamepadData(GamepadData.Type.BUTTON_X));
        inputData.put(GamepadData.Type.BUTTON_Y, new GamepadData(GamepadData.Type.BUTTON_Y));
        inputData.put(GamepadData.Type.BUTTON_RB, new GamepadData(GamepadData.Type.BUTTON_RB));
        inputData.put(GamepadData.Type.BUTTON_LB, new GamepadData(GamepadData.Type.BUTTON_LB));
        inputData.put(GamepadData.Type.BUTTON_START, new GamepadData(GamepadData.Type.BUTTON_START));
        inputData.put(GamepadData.Type.BUTTON_SELECT, new GamepadData(GamepadData.Type.BUTTON_SELECT));
        inputData.put(GamepadData.Type.BUTTON_LEFT_STICK, new GamepadData(GamepadData.Type.BUTTON_LEFT_STICK));
        inputData.put(GamepadData.Type.BUTTON_RIGHT_STICK, new GamepadData(GamepadData.Type.BUTTON_RIGHT_STICK));

        scanExecutor = Executors.newSingleThreadScheduledExecutor();
        scanTask = new ScanTask();

        pollExecutor = Executors.newSingleThreadScheduledExecutor();
        pollTask = new PollTask();

        ThreadManager.addThread(this);
    }

    protected static GamepadController getInstance() {
        if (instance == null) {
            instance = new GamepadController();
        }
        return instance;
    }

    protected void initialize() throws ControllerException {
        ArrayList<Controller> foundControllers = new ArrayList<>();
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        controller = null;

        for (Controller c : controllers) {
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                foundControllers.add(c);
            }
        }
        if (foundControllers.isEmpty()) {
            throw new ControllerException("Found no gamepad controllers.");
        }
        for (Controller c : foundControllers) {
            if (c.getName().contains(MY_CONTROLLER)) {
                controller = c;
                break;
            }
        }
        if (controller == null) {
            throw new ControllerException("Error finding the Logitech F310 controller.");
        }
    }

    protected void scanControllers() {
        if (!scanTask.isRunning()) {
            scanExecutor.schedule(scanTask, 0, TimeUnit.MILLISECONDS);
        }
    }

    protected void stopScanning(boolean force) {
        if (force) scanExecutor.shutdownNow();
        else scanExecutor.shutdown();
    }

    protected void startPolling() throws ControllerException {
        if (controller == null) {
            throw new ControllerException("No controller selected, try GamepadController.start() first.");
        }
        if (!pollTask.isRunning()) {
            pollExecutor.scheduleWithFixedDelay(pollTask, 0, POLLING_INTERVAL, TimeUnit.MILLISECONDS);
        }

    }

    protected void stopPolling(boolean force) {
        if (force) pollExecutor.shutdownNow();
        else pollExecutor.shutdown();
    }

    private void handleInput(Component component) {
        Component.Identifier id = component.getIdentifier();
        float data = component.getPollData();
        GamepadData gamepadData = null;

        if (component.isAnalog()) {
            data = (Math.abs(data) <= DEAD_ZONE) ? 0 : data;

            if (id == Component.Identifier.Axis.X) {
                gamepadData = inputData.get(GamepadData.Type.LEFT_STICK_X);
            } else if (id == Component.Identifier.Axis.Y) {
                gamepadData = inputData.get(GamepadData.Type.LEFT_STICK_Y);
            } else if (id == Component.Identifier.Axis.RX) {
                gamepadData = inputData.get(GamepadData.Type.RIGHT_STICK_X);
            } else if (id == Component.Identifier.Axis.RY) {
                gamepadData = inputData.get(GamepadData.Type.RIGHT_STICK_Y);
            } else if (id == Component.Identifier.Axis.Z) {
                gamepadData = inputData.get(GamepadData.Type.SHOULDERS_Z);
            }
        } else {
            if (id == Component.Identifier.Button._0) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_A);
            } else if (id == Component.Identifier.Button._1) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_B);
            } else if (id == Component.Identifier.Button._2) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_X);
            } else if (id == Component.Identifier.Button._3) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_Y);
            } else if (id == Component.Identifier.Button._4) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_LB);
            } else if (id == Component.Identifier.Button._5) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_RB);
            } else if (id == Component.Identifier.Button._6) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_SELECT);
            } else if (id == Component.Identifier.Button._7) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_START);
            } else if (id == Component.Identifier.Button._8) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_LEFT_STICK);
            } else if (id == Component.Identifier.Button._9) {
                gamepadData = inputData.get(GamepadData.Type.BUTTON_RIGHT_STICK);
            }
        }

        if (gamepadData != null) {
            gamepadData.setData(data);
        }
    }

    @Override
    public void stop() {
        stopPolling(false);
        stopScanning(false);
    }

    @Override
    public void forceStop() {
        stopPolling(true);
        stopScanning(true);
    }
}
