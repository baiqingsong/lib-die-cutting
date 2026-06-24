package com.dawn.diecutting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.hszn.cutsdk.MainSDK;
import com.hszn.cutsdk.entities.ConfigInfo;
import com.hszn.cutsdk.entities.InputInfo;
import com.hszn.cutsdk.entities.MaterialInfo;
import com.hszn.cutsdk.entities.ReturnInfo;
import com.hszn.cutsdk.enums.ActionTyepEnum;
import com.hszn.cutsdk.enums.MaterialTyepEnum;
import com.hszn.cutsdk.interfaces.IReturnMsgListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 打印切割一体机 SDK 封装类（单例）
 * <p>
 * 封装汇森智诺 MainSDK（USB HID 通信），支持：
 * - 传入 Bitmap 图片，SDK 自动识别标记点并生成切割路径
 * - 仅打印模式（PrintOnly）
 * - 肖像照切割（自动缩小轮廓）
 * - 刀座移动、进纸/退纸、校准、重启下位机等操作
 * </p>
 *
 * <p><b>安全说明：</b>API 密钥通过 {@link #init(Context, String)} 外部传入，不会硬编码在库中。</p>
 *
 * <pre>
 * 使用示例：
 * LDieCuttingPrintSDK sdk = LDieCuttingPrintSDK.getInstance();
 * sdk.init(context, "your-api-key");                    // 密钥外部传入
 * sdk.setCallback(callback);
 *
 * // 切割图片
 * Bitmap bitmap = LDieCuttingPrintSDK.loadBitmapForCut(context, R.drawable.test4);
 * sdk.processCut(bitmap, "test.png");
 *
 * // 释放
 * sdk.release();
 * </pre>
 */
public final class LDieCuttingPrintSDK {

    private static final LDieCuttingPrintSDK INSTANCE = new LDieCuttingPrintSDK();

    private LDieCuttingPrintSDK() {
    }

    public static LDieCuttingPrintSDK getInstance() {
        return INSTANCE;
    }

    // ==================== 内部状态 ====================

    private Context appContext;
    private MainSDK mainSDK;
    private boolean initialized = false;

    private LDieCuttingConfig config = new LDieCuttingConfig();
    private LDieCuttingStatus status = new LDieCuttingStatus();
    private String apiKey;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private LDieCuttingCallback callback;

    // ==================== 初始化 / 释放 ====================

    /**
     * 初始化 SDK（默认配置）
     *
     * @param context Application Context
     * @param apiKey  切割密钥（由厂商提供，外部传入，不硬编码）
     */
    public void init(Context context, String apiKey) {
        if (initialized) {
            return;
        }
        this.apiKey = apiKey;
        this.appContext = context.getApplicationContext();
        mainSDK = new MainSDK(apiKey, this.appContext, new PrintCutListener());
        initialized = true;
        status.setState(LDieCuttingStatus.STATE_IDLE);
        notifyStatusChanged();
    }

    /**
     * 初始化 SDK（自定义配置）
     *
     * @param context Application Context
     * @param apiKey  切割密钥
     * @param config  自定义配置（材料/压力/速度/轮廓扩大缩小等）
     */
    public void init(Context context, String apiKey, LDieCuttingConfig config) {
        if (initialized) {
            return;
        }
        this.apiKey = apiKey;
        this.appContext = context.getApplicationContext();
        this.config = config != null ? config : new LDieCuttingConfig();

        // 构建材料信息
        MaterialInfo materialInfo = new MaterialInfo();
        materialInfo.setMaterialType(toMaterialEnum(this.config.getMaterialType()));
        materialInfo.setPressure(this.config.getPressure());
        materialInfo.setSpeed(this.config.getSpeed());

        List<MaterialInfo> materialList = new ArrayList<>();
        materialList.add(materialInfo);

        // 构建配置
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setMaterialInfoList(materialList);
        configInfo.setExpandVal(this.config.getExpandVal());
        configInfo.setShrinkVal((int) this.config.getShrinkVal());
        configInfo.setFinishPercent(this.config.getFinishPercent());

        mainSDK = new MainSDK(apiKey, this.appContext, configInfo, new PrintCutListener());
        initialized = true;
        status.setState(LDieCuttingStatus.STATE_IDLE);
        notifyStatusChanged();
    }

    /**
     * 释放 SDK 资源
     */
    public void release() {
        if (mainSDK != null) {
            mainSDK.release();
            mainSDK = null;
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized && mainSDK != null;
    }

    /** 获取底层 MainSDK 实例（高级用法） */
    public MainSDK getSDK() {
        checkInit();
        return mainSDK;
    }

    // ==================== 核心操作 ====================

    /**
     * 普通切割图片
     * <p>SDK 自动识别标记点，生成切割路径并执行。</p>
     */
    public void processCut(Bitmap bitmap, String fileName) {
        checkInit();
        InputInfo inputInfo = new InputInfo();
        inputInfo.setImgBitmap(bitmap);
        inputInfo.setPic(fileName);
        status.setState(LDieCuttingStatus.STATE_CUTTING);
        notifyStatusChanged();
        mainSDK.process(inputInfo);
    }

    /**
     * 肖像照切割（自动缩小轮廓）
     */
    public void processProfileCut(Bitmap bitmap, String fileName) {
        checkInit();
        InputInfo inputInfo = new InputInfo();
        inputInfo.setImgBitmap(bitmap);
        inputInfo.setPic(fileName);
        inputInfo.setProfile(true);
        status.setState(LDieCuttingStatus.STATE_CUTTING);
        notifyStatusChanged();
        mainSDK.process(inputInfo);
    }

    /** 仅打印（不切割） */
    public void processPrintOnly() {
        checkInit();
        InputInfo inputInfo = new InputInfo();
        inputInfo.setAction(ActionTyepEnum.PrintOnly);
        status.setState(LDieCuttingStatus.STATE_CUTTING);
        notifyStatusChanged();
        mainSDK.process(inputInfo);
    }

    // ==================== 机器控制 ====================

    /** 退纸（出纸） */
    public void paperOut() {
        checkInit();
        sendAction(ActionTyepEnum.PaperOut);
    }

    /** 进纸 */
    public void paperIn() {
        checkInit();
        sendAction(ActionTyepEnum.PaperIn);
    }

    /** 左移刀座 */
    public void moveLeft() {
        checkInit();
        sendAction(ActionTyepEnum.MoveLeft);
    }

    /** 右移刀座 */
    public void moveRight() {
        checkInit();
        sendAction(ActionTyepEnum.MoveRight);
    }

    /** 停止移动 */
    public void stopMove() {
        checkInit();
        sendAction(ActionTyepEnum.StopMove);
    }

    /**
     * 重启下位机
     * <p>仅致命切割异常时需要重启，通信问题无效。</p>
     */
    public void reboot() {
        checkInit();
        sendAction(ActionTyepEnum.Reboot);
    }

    /** 从入口处退纸（与普通退纸不同） */
    public void quitFromEntry() {
        checkInit();
        sendAction(ActionTyepEnum.QuitOnly);
    }

    /** 校准 */
    public void calibration() {
        checkInit();
        sendAction(ActionTyepEnum.Calibration);
        // 6 秒后自动查询校准结果
        mainHandler.postDelayed(() -> {
            if (mainSDK != null) {
                sendAction(ActionTyepEnum.QueryCalibrationResult);
            }
        }, 6000);
    }

    /** 查询固件版本 */
    public void queryFirmwareVersion() {
        checkInit();
        sendAction(ActionTyepEnum.FirmwareVersion);
    }

    // ==================== DPI 感知图片加载 ====================

    /**
     * 按 96 DPI 加载图片（官方推荐，保持原始尺寸不缩放）
     *
     * @param context Context
     * @param resId   图片资源 ID
     */
    public static Bitmap loadBitmapForCut(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = 96;
        options.inTargetDensity = 96;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    // ==================== 状态 / 回调 ====================

    public LDieCuttingStatus getStatus() {
        return status;
    }

    public LDieCuttingConfig getConfig() {
        return config;
    }

    public void setCallback(LDieCuttingCallback callback) {
        this.callback = callback;
    }

    public String getApiKey() {
        return apiKey;
    }

    // ==================== 内部 Listener ====================

    private class PrintCutListener implements IReturnMsgListener {
        @Override
        public void onReceived(ReturnInfo returnInfo) {
            if (returnInfo == null) return;
            handleReturnInfo(returnInfo);
        }
    }

    private void handleReturnInfo(ReturnInfo returnInfo) {
        switch (returnInfo.getAction()) {
            case FirmwareVersion:
                notifyFirmware(returnInfo.getFirmwareVersion());
                break;
            case Status:
                handleMachineStatus(returnInfo);
                break;
            case CanPrint:
                status.setState(LDieCuttingStatus.STATE_CAN_PRINT);
                notifyStatusChanged();
                notifyProgress(0.8f, 0, 0);
                break;
            case Finish:
                status.setState(LDieCuttingStatus.STATE_IDLE);
                notifyStatusChanged();
                notifyProgress(1.0f, 1, 1);
                break;
            case Error:
                handleError(returnInfo);
                break;
        }
    }

    private void handleMachineStatus(ReturnInfo info) {
        // ReturnInfo.getCode() 返回 StatusCodeEnum，通过 name() 比较
        String codeName = info.getCode().name();
        if ("OK".equals(codeName)) {
            status.setState(LDieCuttingStatus.STATE_IDLE);
        } else if ("Release".equals(codeName)) {
            status.setState(LDieCuttingStatus.STATE_DISCONNECTED);
            mainSDK = null;
        } else if ("RebootFail".equals(codeName) || "RebootOk".equals(codeName)) {
            status.setState(LDieCuttingStatus.STATE_REBOOTING);
        } else if ("CalibrationFail".equals(codeName) || "CalibrationOk".equals(codeName)) {
            status.setState(LDieCuttingStatus.STATE_IDLE);
        }
        notifyStatusChanged();
    }

    private void handleError(ReturnInfo info) {
        String codeName = info.getCode().name();
        int errorCode = mapErrorCode(codeName);
        String desc = LDieCuttingConst.getErrorDescription(errorCode);
        status.setState(LDieCuttingStatus.STATE_ERROR);
        status.setErrorCode(errorCode);
        status.setErrorMessage(desc);

        if ("NoFoundUSBDevice".equals(codeName) && mainSDK != null) {
            mainSDK.release();
            mainSDK = null;
        }

        notifyStatusChanged();
        notifyError(errorCode, desc);
    }

    /** 将 SDK StatusCodeEnum 的 name 映射到 LDieCuttingConst 错误码 */
    private static int mapErrorCode(String name) {
        switch (name) {
            case "SensorIsCovered":      return LDieCuttingConst.ERROR_SENSOR_COVERED;
            case "NoPhoto":              return LDieCuttingConst.ERROR_NO_PHOTO;
            case "DisconnetFromDevice":  return LDieCuttingConst.ERROR_DISCONNECT;
            case "NoFoundUSBDevice":     return LDieCuttingConst.ERROR_NO_USB_DEVICE;
            case "NoMarker":             return LDieCuttingConst.ERROR_NO_MARKER;
            case "NoCutData":            return LDieCuttingConst.ERROR_NO_CUT_DATA;
            case "ExecuteCommandFail":   return LDieCuttingConst.ERROR_EXECUTE_COMMAND_FAIL;
            case "ExecutePltFail":       return LDieCuttingConst.ERROR_EXECUTE_PLT_FAIL;
            case "CutException":         return LDieCuttingConst.ERROR_CUT_EXCEPTION;
            default:                     return -1;
        }
    }

    private void notifyFirmware(String version) {
        if (callback != null) {
            status.setErrorMessage("固件版本: " + version);
            mainHandler.post(() -> callback.onStatusChanged(status));
        }
    }

    private void notifyStatusChanged() {
        if (callback != null) {
            mainHandler.post(() -> callback.onStatusChanged(status));
        }
    }

    private void notifyProgress(float progress, int current, int total) {
        status.setProgress(progress);
        if (callback != null) {
            mainHandler.post(() -> callback.onProgressUpdated(progress, current, total));
        }
    }

    private void notifyError(int code, String msg) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(code, msg));
        }
    }

    // ==================== 辅助 ====================

    private void sendAction(ActionTyepEnum action) {
        InputInfo inputInfo = new InputInfo();
        inputInfo.setAction(action);
        mainSDK.process(inputInfo);
    }

    private void checkInit() {
        if (!initialized || mainSDK == null) {
            throw new IllegalStateException(
                    "LDieCuttingPrintSDK 未初始化，请先调用 init(context, apiKey)");
        }
    }

    private MaterialTyepEnum toMaterialEnum(int type) {
        switch (type) {
            case LDieCuttingConst.MATERIAL_SOFT_PAPER:
                return MaterialTyepEnum.SoftPaper;
            default:
                return MaterialTyepEnum.SoftPaper;
        }
    }
}
