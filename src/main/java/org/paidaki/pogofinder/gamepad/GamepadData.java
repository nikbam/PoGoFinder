package org.paidaki.pogofinder.gamepad;

public class GamepadData {

    public enum Type {
        LEFT_STICK_X,
        LEFT_STICK_Y,
        RIGHT_STICK_X,
        RIGHT_STICK_Y,
        SHOULDERS_Z,
        BUTTON_A,
        BUTTON_X,
        BUTTON_Y,
        BUTTON_B,
        BUTTON_RB,
        BUTTON_LB,
        BUTTON_SELECT,
        BUTTON_START,
        BUTTON_LEFT_STICK,
        BUTTON_RIGHT_STICK
    }

    private static final float MIN_DATA_VALUE = -1.0F;
    private static final float DEAD_DATA_VALUE = 0.0F;
    private static final float MAX_DATA_VALUE = 1.0F;

    private Type dataType;
    private float data;
    private boolean pressed;

    public GamepadData(Type dataType) {
        this.dataType = dataType;
        setData(0);
    }

    public Type getDataType() {
        return dataType;
    }

    public float getData() {
        return data;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setData(float newData) {
        float oldDAta = data;

        if (dataType == Type.LEFT_STICK_X ||
                dataType == Type.LEFT_STICK_Y ||
                dataType == Type.RIGHT_STICK_X ||
                dataType == Type.RIGHT_STICK_Y ||
                dataType == Type.SHOULDERS_Z) {
            if (newData < MIN_DATA_VALUE) {
                data = MIN_DATA_VALUE;
            } else if (newData > MAX_DATA_VALUE) {
                data = MAX_DATA_VALUE;
            } else {
                data = newData;
            }
            pressed = (data != DEAD_DATA_VALUE);
        } else {
            if (newData == DEAD_DATA_VALUE || newData == MAX_DATA_VALUE) {
                data = newData;
            } else if (newData < MAX_DATA_VALUE / 2) {
                data = DEAD_DATA_VALUE;
            } else {
                data = MAX_DATA_VALUE;
            }
            pressed = (oldDAta == DEAD_DATA_VALUE && data == MAX_DATA_VALUE);
        }
    }
}
