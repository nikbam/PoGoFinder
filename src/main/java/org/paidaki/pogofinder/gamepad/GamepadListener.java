package org.paidaki.pogofinder.gamepad;

import org.paidaki.pogofinder.exceptions.ControllerException;
import org.paidaki.pogofinder.web.Bridge;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import static org.paidaki.pogofinder.gamepad.GamepadData.Type.*;

public class GamepadListener implements Observer {

    private Bridge bridge;
    private GamepadController gamepadController;
    private boolean connected;
    private boolean polling;

    public GamepadListener(Bridge bridge) {
        this.bridge = bridge;
        gamepadController = GamepadController.getInstance();
        gamepadController.addObserver(this);
        connected = false;
        polling = false;
    }

    public void connectController() throws ControllerException {
        if (connected) {
            throw new ControllerException("Controller is still connected. No need for hardware rescan.");
        }
        gamepadController.scanControllers();
    }

    public void start() throws ControllerException {
        if (!connected) {
            connectController();
            return;
        }
        gamepadController.startPolling();
    }

    public void stop() {
        gamepadController.stopPolling(false);
        polling = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isPolling() {
        return polling;
    }

    private void disconnected() {
        bridge.controllerDisconnected();
    }

    private void connected() throws ControllerException {
        if (bridge.isDocumentReady()) {
            gamepadController.startPolling();
        }
        bridge.controllerConnected();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof GamepadController.ScanReport) {

            GamepadController.ScanReport report = (GamepadController.ScanReport) arg;

            connected = report.connected;

            if (connected) try {
                connected();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            return;
        }

        if (arg instanceof GamepadController.PollingReport) {

            GamepadController.PollingReport report = (GamepadController.PollingReport) arg;

            HashMap<GamepadData.Type, GamepadData> inputData = report.inputData;
            connected = report.ready;
            polling = report.polling;

            if (inputData == null) {
                if (!connected) disconnected();
                return;
            }
            GamepadData leftStickX = inputData.get(LEFT_STICK_X);
            GamepadData leftStickY = inputData.get(LEFT_STICK_Y);
            GamepadData rightStickX = inputData.get(RIGHT_STICK_X);
            GamepadData rightStickY = inputData.get(RIGHT_STICK_Y);
            GamepadData shouldersZ = inputData.get(SHOULDERS_Z);
            GamepadData buttonA = inputData.get(BUTTON_A);
            GamepadData buttonB = inputData.get(BUTTON_B);
            GamepadData buttonX = inputData.get(BUTTON_X);
            GamepadData buttonY = inputData.get(BUTTON_Y);
            GamepadData buttonRB = inputData.get(BUTTON_RB);
            GamepadData buttonLB = inputData.get(BUTTON_LB);
            GamepadData buttonSelect = inputData.get(BUTTON_SELECT);
            GamepadData buttonStart = inputData.get(BUTTON_START);
            GamepadData buttonLeftStick = inputData.get(BUTTON_LEFT_STICK);
            GamepadData buttonRightStick = inputData.get(BUTTON_RIGHT_STICK);

            if (leftStickX.isPressed() || leftStickY.isPressed()) {
                bridge.offsetMarker(-leftStickY.getData(), leftStickX.getData());
            }
            if (rightStickX.isPressed() || rightStickY.isPressed()) {
                bridge.offsetMap(-rightStickY.getData(), rightStickX.getData());
            }
            if (shouldersZ.isPressed()) {
                bridge.changeSpeed(-shouldersZ.getData());
            }
            if (buttonA.isPressed()) {
                bridge.requestScanForPokemon();
            }
            if (buttonB.isPressed()) {
                bridge.toggleShowScanCircles();
            }
            if (buttonX.isPressed()) {
                bridge.toggleScanOnly();
            }
            if (buttonY.isPressed()) {
                bridge.toggleFollowMarker();
            }
            if (buttonRB.isPressed()) {
                bridge.mapZoomIn();
            }
            if (buttonLB.isPressed()) {
                bridge.mapZoomOut();
            }
            if (buttonSelect.isPressed()) {
                bridge.toggleSpeedBoost();
            }
            if (buttonStart.isPressed()) {
                bridge.toggleForts();
            }
            if (buttonLeftStick.isPressed()) {
                //TODO
            }
            if (buttonRightStick.isPressed()) {
                //TODO
            }
        }
    }
}
