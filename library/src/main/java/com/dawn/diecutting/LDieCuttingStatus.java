package com.dawn.diecutting;

/**
 * 模切机状态信息类
 * <p>
 * 封装模切机的实时状态数据，包括运行状态、进度、温度、位置等。
 * </p>
 */
public final class LDieCuttingStatus {

    // ==================== 状态常量 ====================

    /** 未连接 */
    public static final int STATE_DISCONNECTED = 0;

    /** 空闲就绪 */
    public static final int STATE_IDLE = 1;

    /** 正在切割 */
    public static final int STATE_CUTTING = 2;

    /** 已暂停 */
    public static final int STATE_PAUSED = 3;

    /** 急停状态 */
    public static final int STATE_EMERGENCY = 4;

    /** 故障状态 */
    public static final int STATE_ERROR = 5;

    /** 正在复位/校准 */
    public static final int STATE_CALIBRATING = 6;

    // ==================== 状态字段 ====================

    /** 机器状态码 */
    private int state = STATE_DISCONNECTED;

    /** 切割进度（0.0 ~ 1.0） */
    private float progress = 0f;

    /** 已完成切割次数 */
    private int completedCount = 0;

    /** 机器温度（摄氏度） */
    private float temperature = 0f;

    /** 当前 X 轴位置（mm） */
    private float currentX = 0f;

    /** 当前 Y 轴位置（mm） */
    private float currentY = 0f;

    /** 当前 Z 轴位置（mm） */
    private float currentZ = 0f;

    /** 错误码 */
    private int errorCode = 0;

    /** 错误信息 */
    private String errorMessage = "";

    /** 状态更新时间戳 */
    private long timestamp = 0L;

    // ==================== 构造方法 ====================

    public LDieCuttingStatus() {
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== Getter / Setter ====================

    public int getState() {
        return state;
    }

    /* package */ void setState(int state) {
        this.state = state;
        this.timestamp = System.currentTimeMillis();
    }

    public float getProgress() {
        return progress;
    }

    /* package */ void setProgress(float progress) {
        this.progress = progress;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    /* package */ void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public float getTemperature() {
        return temperature;
    }

    /* package */ void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getCurrentX() {
        return currentX;
    }

    /* package */ void setCurrentX(float currentX) {
        this.currentX = currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    /* package */ void setCurrentY(float currentY) {
        this.currentY = currentY;
    }

    public float getCurrentZ() {
        return currentZ;
    }

    /* package */ void setCurrentZ(float currentZ) {
        this.currentZ = currentZ;
    }

    public int getErrorCode() {
        return errorCode;
    }

    /* package */ void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /* package */ void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ==================== 工具方法 ====================

    /**
     * 判断是否处于运行状态（切割中或暂停中）
     *
     * @return true 表示正在运行
     */
    public boolean isRunning() {
        return state == STATE_CUTTING || state == STATE_PAUSED;
    }

    /**
     * 判断是否处于故障状态
     *
     * @return true 表示有故障
     */
    public boolean isError() {
        return state == STATE_ERROR || state == STATE_EMERGENCY;
    }

    /**
     * 判断是否已连接
     *
     * @return true 表示已连接
     */
    public boolean isConnected() {
        return state != STATE_DISCONNECTED;
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称字符串
     */
    public String getStateName() {
        switch (state) {
            case STATE_DISCONNECTED:
                return "未连接";
            case STATE_IDLE:
                return "就绪";
            case STATE_CUTTING:
                return "切割中";
            case STATE_PAUSED:
                return "已暂停";
            case STATE_EMERGENCY:
                return "急停";
            case STATE_ERROR:
                return "故障";
            case STATE_CALIBRATING:
                return "校准中";
            default:
                return "未知";
        }
    }

    @Override
    public String toString() {
        return "LDieCuttingStatus{" +
                "state=" + getStateName() +
                ", progress=" + progress +
                ", temperature=" + temperature +
                ", position=(" + currentX + ", " + currentY + ", " + currentZ + ")" +
                ", errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
