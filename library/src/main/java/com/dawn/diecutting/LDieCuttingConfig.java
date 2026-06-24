package com.dawn.diecutting;

/**
 * 打印切割一体机配置 — 对应 ConfigInfo + MaterialInfo
 */
public final class LDieCuttingConfig {

    private int pressure = 250;
    private int speed = 90;
    private int materialType = LDieCuttingConst.MATERIAL_SOFT_PAPER;
    private int expandVal = 3;
    private int shrinkVal = 2;
    private int finishPercent = 80;

    public static LDieCuttingConfig createDefault() { return new LDieCuttingConfig(); }

    public int getPressure() { return pressure; }
    public LDieCuttingConfig setPressure(int v) { pressure = v; return this; }
    public int getSpeed() { return speed; }
    public LDieCuttingConfig setSpeed(int v) { speed = v; return this; }
    public int getMaterialType() { return materialType; }
    public LDieCuttingConfig setMaterialType(int v) { materialType = v; return this; }
    public int getExpandVal() { return expandVal; }
    public LDieCuttingConfig setExpandVal(int v) { expandVal = Math.max(1, Math.min(4, v)); return this; }
    public int getShrinkVal() { return shrinkVal; }
    public LDieCuttingConfig setShrinkVal(int v) { shrinkVal = v; return this; }
    public int getFinishPercent() { return finishPercent; }
    public LDieCuttingConfig setFinishPercent(int v) { finishPercent = Math.max(0, Math.min(100, v)); return this; }
}
