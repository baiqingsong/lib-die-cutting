package com.dawn.diecutting;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;

import com.hszn.sdk.CutSDKManager;
import com.hszn.sdk.beans.CutCompensateBean;
import com.hszn.sdk.interfaces.CutListener;
import com.hszn.sdk.interfaces.IDeviceDefaultResultCallBack;
import com.hszn.sdk.interfaces.IDeviceValueResultCallBack;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 模切机核心控制类（单例）
 * <p>
 * 封装汇森智诺 CutSDKManager，提供统一的模切机控制接口。
 * 所有方法均为同步调用（内部使用 CountDownLatch 等待 SDK 异步回调），
 * 遵循 lib-image 的工具类风格。
 * </p>
 *
 * <pre>
 * 使用示例：
 * LDieCuttingMachine machine = LDieCuttingMachine.getInstance();
 * machine.init(context);                              // 初始化 SDK
 * machine.setPressure(100);                           // 设置压力
 * machine.setSpeed(200);                              // 设置速度
 * machine.testCut();                                  // 测试切割
 * machine.sendCutData(pltData, "test.plt", callback); // 发送切割数据
 * machine.release();                                  // 释放资源
 * </pre>
 */
public final class LDieCuttingMachine {

    // ==================== 单例 ====================

    private static final LDieCuttingMachine INSTANCE = new LDieCuttingMachine();

    private LDieCuttingMachine() {
    }

    public static LDieCuttingMachine getInstance() {
        return INSTANCE;
    }

    // ==================== 内部状态 ====================

    /** 默认 SDK 授权密钥 */
    private static final String DEFAULT_AUTH_KEY = "hsznqmji13586472";

    /** 默认波特率 */
    private static final int DEFAULT_BAUD_RATE = 3000;

    /** 同步调用超时时间（毫秒），避免主线程长时间阻塞 */
    private static final long SYNC_TIMEOUT_MS = 2000;

    private Context appContext;
    private CutSDKManager sdk;
    private boolean initialized = false;

    private final AtomicBoolean cutting = new AtomicBoolean(false);
    private LDieCuttingConfig config = new LDieCuttingConfig();
    private LDieCuttingStatus currentStatus = new LDieCuttingStatus();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private LDieCuttingCallback callback;

    // ==================== 初始化 / 释放 ====================

    /**
     * 初始化 SDK（使用默认密钥和波特率）
     *
     * @param context Application Context
     */
    public void init(Context context) {
        init(context, DEFAULT_AUTH_KEY, DEFAULT_BAUD_RATE, false);
    }

    /**
     * 初始化 SDK
     *
     * @param context   Application Context
     * @param authKey   授权密钥
     * @param baudRate  波特率（默认 3000）
     * @param debug     是否开启调试模式
     */
    public void init(Context context, String authKey, int baudRate, boolean debug) {
        if (initialized) {
            return;
        }
        this.appContext = context.getApplicationContext();
        CutSDKManager.init(authKey, baudRate, debug, this.appContext);
        this.sdk = CutSDKManager.getInstance(this.appContext);
        this.initialized = true;
        changeStatus(LDieCuttingStatus.STATE_IDLE);
    }

    /**
     * 释放 SDK 资源
     */
    public void release() {
        if (!initialized) {
            return;
        }
        if (cutting.get()) {
            cancelCut();
        }
        sdk.close();
        initialized = false;
        sdk = null;
        cutting.set(false);
        changeStatus(LDieCuttingStatus.STATE_DISCONNECTED);
    }

    /**
     * 是否已初始化
     */
    public boolean isInitialized() {
        return initialized && sdk != null;
    }

    /**
     * 获取底层 SDK 实例（高级用法）
     */
    public CutSDKManager getSDK() {
        checkInit();
        return sdk;
    }

    // ==================== 压力（Pressure） ====================

    /**
     * 获取当前压力值
     *
     * @return 压力值，失败返回 -1
     */
    public int getPressure() {
        checkInit();
        AtomicInteger result = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachinePressure(new IDeviceValueResultCallBack<Integer>() {
            @Override
            public void onSuccessful(Integer value) {
                result.set(value);
                config.setPressure(value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(-1);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置压力
     *
     * @param pressure 压力值
     * @return true 成功
     */
    public boolean setPressure(int pressure) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachinePressure(pressure, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setPressure(pressure);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 速度（Speed） ====================

    /**
     * 获取当前速度值
     */
    public int getSpeed() {
        checkInit();
        AtomicInteger result = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineSpeed(new IDeviceValueResultCallBack<Integer>() {
            @Override
            public void onSuccessful(Integer value) {
                result.set(value);
                config.setSpeed(value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(-1);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置速度
     */
    public boolean setSpeed(int speed) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachineSpeed(speed, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setSpeed(speed);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 幅宽（Wide Format） ====================

    /**
     * 获取幅宽
     */
    public int getWideFormat() {
        checkInit();
        AtomicInteger result = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineWide(new IDeviceValueResultCallBack<Integer>() {
            @Override
            public void onSuccessful(Integer value) {
                result.set(value);
                config.setWideFormat(value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(-1);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置幅宽
     */
    public boolean setWideFormat(int wide) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachineWide(wide, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setWideFormat(wide);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 齿轮比（Gear） ====================

    /**
     * 获取齿轮比
     *
     * @return Point(x, y)，失败返回 null
     */
    public Point getGear() {
        checkInit();
        AtomicReference<Point> result = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineGear(new IDeviceValueResultCallBack<Point>() {
            @Override
            public void onSuccessful(Point point) {
                result.set(point);
                if (point != null) {
                    config.setGearX(point.x);
                    config.setGearY(point.y);
                }
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置齿轮比
     */
    public boolean setGear(int x, int y) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachineGear(new Point(x, y), new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setGearX(x);
                config.setGearY(y);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 限位使能（Limit） ====================

    /**
     * 获取限位使能状态
     */
    public boolean getLimit() {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineLimit(new IDeviceValueResultCallBack<Boolean>() {
            @Override
            public void onSuccessful(Boolean value) {
                result.set(value != null && value);
                config.setLimit(value != null && value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置限位使能
     */
    public boolean setLimit(boolean enabled) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachineLimit(enabled, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setLimit(enabled);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 自动送纸（Auto Pager） ====================

    /**
     * 获取自动送纸使能状态
     */
    public boolean getAutoPager() {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineAutoPager(new IDeviceValueResultCallBack<Boolean>() {
            @Override
            public void onSuccessful(Boolean value) {
                result.set(value != null && value);
                config.setAutoFeed(value != null && value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置自动送纸使能
     */
    public boolean setAutoPager(boolean enabled) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachineAutoPager(enabled, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setAutoFeed(enabled);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 自动间距（Auto Space） ====================

    /**
     * 获取自动间距
     *
     * @return 间距值（mm），失败返回 -1
     */
    public float getAutoSpace() {
        checkInit();
        AtomicReference<Float> result = new AtomicReference<>(-1f);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineAutoSpace(new IDeviceValueResultCallBack<Integer>() {
            @Override
            public void onSuccessful(Integer value) {
                float space = value / 10f;
                result.set(space);
                config.setAutoSpace(space);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置自动间距
     *
     * @param spaceMm 间距（mm），范围 0 ~ 50
     */
    public boolean setAutoSpace(float spaceMm) {
        checkInit();
        if (spaceMm < 0 || spaceMm > 50) {
            return false;
        }
        int value = (int) (spaceMm * 10);
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setMachineAutoSpace(value, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setAutoSpace(spaceMm);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 切割补偿（Cut Compensate） ====================

    /**
     * 获取切割补偿值
     *
     * @return CutCompensateBean，失败返回 null
     */
    public CutCompensateBean getCutCompensate() {
        checkInit();
        AtomicReference<CutCompensateBean> result = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryCutCompensate(new IDeviceValueResultCallBack<CutCompensateBean>() {
            @Override
            public void onSuccessful(CutCompensateBean bean) {
                result.set(bean);
                if (bean != null) {
                    config.setCutCompensateX(bean.getX());
                    config.setCutCompensateX1(bean.getX1());
                    config.setCutCompensateY(bean.getY());
                    config.setCutCompensateY1(bean.getY1());
                }
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置切割补偿值
     */
    public boolean setCutCompensate(int x, int x1, int y, int y1) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setCutCompensate(x, x1, y, y1, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setCutCompensateX(x);
                config.setCutCompensateX1(x1);
                config.setCutCompensateY(y);
                config.setCutCompensateY1(y1);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 刀压补偿（Pressure Compensate） ====================

    /**
     * 获取刀压补偿值
     */
    public int getPressureCompensate() {
        checkInit();
        AtomicInteger result = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryPressureCompensate(new IDeviceValueResultCallBack<Integer>() {
            @Override
            public void onSuccessful(Integer value) {
                result.set(value != null ? value : 0);
                config.setPressureCompensate(value != null ? value : 0);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 设置刀压补偿值
     */
    public boolean setPressureCompensate(int value) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.setPressureCompensate(value, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                config.setPressureCompensate(value);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 设备信息 ====================

    /**
     * 获取设备 ID
     */
    public String getDeviceId() {
        checkInit();
        AtomicReference<String> result = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineCode(new IDeviceValueResultCallBack<String>() {
            @Override
            public void onSuccessful(String value) {
                result.set(value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 获取设备固件版本
     */
    public String getDeviceVersion() {
        checkInit();
        AtomicReference<String> result = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineVersion(new IDeviceValueResultCallBack<String>() {
            @Override
            public void onSuccessful(String value) {
                result.set(value);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 获取设备状态
     *
     * @return 0=空闲, 1=刻绘中
     */
    public int getDeviceStatus() {
        checkInit();
        AtomicInteger result = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.queryMachineStatus(new IDeviceValueResultCallBack<Integer>() {
            @Override
            public void onSuccessful(Integer value) {
                result.set(value != null ? value : -1);
                currentStatus.setState(value != null && value == 1
                        ? LDieCuttingStatus.STATE_CUTTING
                        : LDieCuttingStatus.STATE_IDLE);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(-1);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 操作指令 ====================

    /**
     * 测试切割
     */
    public boolean testCut() {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        cutting.set(true);
        changeStatus(LDieCuttingStatus.STATE_CUTTING);
        sdk.testMachine(new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                cutting.set(false);
                changeStatus(LDieCuttingStatus.STATE_IDLE);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                cutting.set(false);
                changeStatus(LDieCuttingStatus.STATE_IDLE);
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 取消切割（复位刻刀）
     */
    public boolean cancelCut() {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.resetKnife(new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                cutting.set(false);
                changeStatus(LDieCuttingStatus.STATE_IDLE);
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    /**
     * 出纸控制
     *
     * @param direction 出纸方向
     */
    public boolean outPager(boolean direction) {
        checkInit();
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        sdk.outPager(direction, new IDeviceDefaultResultCallBack() {
            @Override
            public void onSuccessful() {
                result.set(true);
                latch.countDown();
            }
            @Override
            public void onError(int code, String msg) {
                result.set(false);
                latch.countDown();
            }
        });
        await(latch);
        return result.get();
    }

    // ==================== 发送切割数据 ====================

    /**
     * 发送 PLT 切割数据（异步，带进度回调）
     *
     * @param pltData  PLT 格式的切割数据
     * @param fileName 文件名
     * @param callback 回调（可为 null）
     */
    public void sendCutData(String pltData, String fileName, final CutListener callback) {
        checkInit();
        cutting.set(true);
        changeStatus(LDieCuttingStatus.STATE_CUTTING);
        sdk.sendDefault(pltData, fileName, new CutListener() {
            @Override
            public void onComplete() {
                cutting.set(false);
                changeStatus(LDieCuttingStatus.STATE_IDLE);
                if (callback != null) {
                    mainHandler.post(callback::onComplete);
                }
                notifyProgress(1f, 1, 1);
            }

            @Override
            public void onProgress(int percent) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onProgress(percent));
                }
                notifyProgress(percent / 100f, 0, 0);
            }

            @Override
            public void onError(int code, String msg) {
                cutting.set(false);
                changeStatus(LDieCuttingStatus.STATE_ERROR);
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(code, msg));
                }
                notifyError(code, msg);
            }
        });
    }

    // ==================== 批量设置 ====================

    /**
     * 批量设置参数
     */
    public boolean setConfig(LDieCuttingConfig config) {
        if (config == null) {
            return false;
        }
        checkInit();
        boolean success = true;
        if (config.getPressure() > 0) {
            success &= setPressure(config.getPressure());
        }
        if (config.getSpeed() > 0) {
            success &= setSpeed(config.getSpeed());
        }
        if (config.getWideFormat() > 0) {
            success &= setWideFormat(config.getWideFormat());
        }
        if (config.getGearX() > 0 && config.getGearY() > 0) {
            success &= setGear(config.getGearX(), config.getGearY());
        }
        if (config.getAutoSpace() >= 0) {
            success &= setAutoSpace(config.getAutoSpace());
        }
        if (config.getCutCompensateX() != 0 || config.getCutCompensateY() != 0) {
            success &= setCutCompensate(
                    config.getCutCompensateX(), config.getCutCompensateX1(),
                    config.getCutCompensateY(), config.getCutCompensateY1());
        }
        if (config.getPressureCompensate() != 0) {
            success &= setPressureCompensate(config.getPressureCompensate());
        }
        success &= setLimit(config.isLimit());
        success &= setAutoPager(config.isAutoFeed());
        return success;
    }

    // ==================== 回调 ====================

    public void setCallback(LDieCuttingCallback callback) {
        this.callback = callback;
    }

    public LDieCuttingConfig getConfig() {
        return config;
    }

    public LDieCuttingStatus getCurrentStatus() {
        return currentStatus;
    }

    // ==================== 私有辅助方法 ====================

    private void checkInit() {
        if (!initialized || sdk == null) {
            throw new IllegalStateException("LDieCuttingMachine 未初始化，请先调用 init(context)");
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await(SYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void changeStatus(int state) {
        currentStatus.setState(state);
        if (callback != null) {
            mainHandler.post(() -> callback.onStatusChanged(currentStatus));
        }
    }

    private void notifyProgress(float progress, int current, int total) {
        currentStatus.setProgress(progress);
        if (callback != null) {
            mainHandler.post(() -> callback.onProgressUpdated(progress, current, total));
        }
    }

    private void notifyError(int code, String msg) {
        currentStatus.setErrorCode(code);
        currentStatus.setErrorMessage(msg);
        if (callback != null) {
            mainHandler.post(() -> callback.onError(code, msg));
        }
    }
}

