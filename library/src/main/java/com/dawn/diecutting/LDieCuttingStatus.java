package com.dawn.diecutting;

/**
 * 打印切割一体机状态
 */
public final class LDieCuttingStatus {

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_IDLE = 1;
    public static final int STATE_CUTTING = 2;
    public static final int STATE_CAN_PRINT = 3;
    public static final int STATE_ERROR = 4;
    public static final int STATE_REBOOTING = 5;

    private int state = STATE_DISCONNECTED;
    private float progress;
    private int errorCode;
    private String errorMessage = "";
    private String firmwareVersion = "";

    public int getState() { return state; }
    /*pkg*/ void setState(int v) { state = v; }
    public float getProgress() { return progress; }
    /*pkg*/ void setProgress(float v) { progress = v; }
    public int getErrorCode() { return errorCode; }
    /*pkg*/ void setErrorCode(int v) { errorCode = v; }
    public String getErrorMessage() { return errorMessage; }
    /*pkg*/ void setErrorMessage(String v) { errorMessage = v; }
    public String getFirmwareVersion() { return firmwareVersion; }
    /*pkg*/ void setFirmwareVersion(String v) { firmwareVersion = v; }

    public boolean isRunning() { return state == STATE_CUTTING; }
    public boolean isError() { return state == STATE_ERROR; }

    public String getStateName() {
        switch (state) {
            case STATE_DISCONNECTED: return "未连接";
            case STATE_IDLE:         return "就绪";
            case STATE_CUTTING:      return "切割中";
            case STATE_CAN_PRINT:    return "可继续打印";
            case STATE_ERROR:        return "故障";
            case STATE_REBOOTING:    return "复位中";
            default:                 return "未知";
        }
    }
}
