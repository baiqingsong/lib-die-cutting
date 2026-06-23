package com.dawn.diecutting;

import android.graphics.Point;

/**
 * 模切机配置参数类
 * <p>
 * 封装模切机的各项可配置参数，与汇森智诺 CutSDKManager 的参数体系对应。
 * 所有 setter 方法返回自身，支持链式调用。
 * </p>
 */
public final class LDieCuttingConfig {

    // ==================== 基础切割参数 ====================

    /** 切割压力，默认 100 */
    private int pressure = 100;

    /** 切割速度，默认 200 */
    private int speed = 200;

    // ==================== 幅宽 / 齿轮比 ====================

    /** 幅宽，默认 208 */
    private int wideFormat = 208;

    /** X 轴齿轮比，默认 2000 */
    private int gearX = 2000;

    /** Y 轴齿轮比，默认 2000 */
    private int gearY = 2000;

    // ==================== 开关参数 ====================

    /** 限位使能，默认 true */
    private boolean limit = true;

    /** 自动送纸使能，默认 true */
    private boolean autoFeed = true;

    // ==================== 间距 ====================

    /** 自动间距（mm），默认 0 */
    private float autoSpace = 0f;

    // ==================== 补偿参数 ====================

    /** 切割补偿 X */
    private int cutCompensateX = 0;

    /** 切割补偿 X1 */
    private int cutCompensateX1 = 0;

    /** 切割补偿 Y */
    private int cutCompensateY = 0;

    /** 切割补偿 Y1 */
    private int cutCompensateY1 = 0;

    /** 刀压补偿 */
    private int pressureCompensate = 0;

    // ==================== 构造方法 ====================

    public LDieCuttingConfig() {
    }

    // ==================== Getter / Setter ====================

    public int getPressure() {
        return pressure;
    }

    public LDieCuttingConfig setPressure(int pressure) {
        this.pressure = pressure;
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    public LDieCuttingConfig setSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    public int getWideFormat() {
        return wideFormat;
    }

    public LDieCuttingConfig setWideFormat(int wideFormat) {
        this.wideFormat = wideFormat;
        return this;
    }

    public int getGearX() {
        return gearX;
    }

    public LDieCuttingConfig setGearX(int gearX) {
        this.gearX = gearX;
        return this;
    }

    public int getGearY() {
        return gearY;
    }

    public LDieCuttingConfig setGearY(int gearY) {
        this.gearY = gearY;
        return this;
    }

    /**
     * 获取齿轮比
     */
    public Point getGear() {
        return new Point(gearX, gearY);
    }

    /**
     * 设置齿轮比
     */
    public LDieCuttingConfig setGear(int x, int y) {
        this.gearX = x;
        this.gearY = y;
        return this;
    }

    public boolean isLimit() {
        return limit;
    }

    public LDieCuttingConfig setLimit(boolean limit) {
        this.limit = limit;
        return this;
    }

    public boolean isAutoFeed() {
        return autoFeed;
    }

    public LDieCuttingConfig setAutoFeed(boolean autoFeed) {
        this.autoFeed = autoFeed;
        return this;
    }

    public float getAutoSpace() {
        return autoSpace;
    }

    public LDieCuttingConfig setAutoSpace(float autoSpace) {
        this.autoSpace = autoSpace;
        return this;
    }

    public int getCutCompensateX() {
        return cutCompensateX;
    }

    public LDieCuttingConfig setCutCompensateX(int cutCompensateX) {
        this.cutCompensateX = cutCompensateX;
        return this;
    }

    public int getCutCompensateX1() {
        return cutCompensateX1;
    }

    public LDieCuttingConfig setCutCompensateX1(int cutCompensateX1) {
        this.cutCompensateX1 = cutCompensateX1;
        return this;
    }

    public int getCutCompensateY() {
        return cutCompensateY;
    }

    public LDieCuttingConfig setCutCompensateY(int cutCompensateY) {
        this.cutCompensateY = cutCompensateY;
        return this;
    }

    public int getCutCompensateY1() {
        return cutCompensateY1;
    }

    public LDieCuttingConfig setCutCompensateY1(int cutCompensateY1) {
        this.cutCompensateY1 = cutCompensateY1;
        return this;
    }

    /**
     * 设置切割补偿
     */
    public LDieCuttingConfig setCutCompensate(int x, int x1, int y, int y1) {
        this.cutCompensateX = x;
        this.cutCompensateX1 = x1;
        this.cutCompensateY = y;
        this.cutCompensateY1 = y1;
        return this;
    }

    public int getPressureCompensate() {
        return pressureCompensate;
    }

    public LDieCuttingConfig setPressureCompensate(int pressureCompensate) {
        this.pressureCompensate = pressureCompensate;
        return this;
    }

    // ==================== 工具方法 ====================

    /**
     * 创建默认配置
     */
    public static LDieCuttingConfig createDefault() {
        return new LDieCuttingConfig();
    }

    /**
     * 复制当前配置
     */
    public LDieCuttingConfig copy() {
        LDieCuttingConfig copy = new LDieCuttingConfig();
        copy.pressure = this.pressure;
        copy.speed = this.speed;
        copy.wideFormat = this.wideFormat;
        copy.gearX = this.gearX;
        copy.gearY = this.gearY;
        copy.limit = this.limit;
        copy.autoFeed = this.autoFeed;
        copy.autoSpace = this.autoSpace;
        copy.cutCompensateX = this.cutCompensateX;
        copy.cutCompensateX1 = this.cutCompensateX1;
        copy.cutCompensateY = this.cutCompensateY;
        copy.cutCompensateY1 = this.cutCompensateY1;
        copy.pressureCompensate = this.pressureCompensate;
        return copy;
    }

    @Override
    public String toString() {
        return "LDieCuttingConfig{" +
                "pressure=" + pressure +
                ", speed=" + speed +
                ", wideFormat=" + wideFormat +
                ", gear=(" + gearX + "," + gearY + ")" +
                ", limit=" + limit +
                ", autoFeed=" + autoFeed +
                ", autoSpace=" + autoSpace + "mm" +
                ", cutComp=(" + cutCompensateX + "," + cutCompensateX1 +
                "," + cutCompensateY + "," + cutCompensateY1 + ")" +
                ", pressureComp=" + pressureCompensate +
                '}';
    }
}

