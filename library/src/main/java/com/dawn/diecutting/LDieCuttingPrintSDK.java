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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * 打印切割一体机 SDK 封装（单例）
 *
 * <pre>
 * LDieCuttingPrintSDK sdk = LDieCuttingPrintSDK.getInstance();
 * sdk.init(ctx, apiKey, callback);
 * sdk.printOnly();                          // ① 打印
 * // ... 手动放纸 ...
 * Bitmap bmp = LDieCuttingPrintSDK.loadBitmap(ctx, R.drawable.test4);
 * sdk.cut(bmp, "test.png");                // ② 切割
 * sdk.release();
 * </pre>
 */
public final class LDieCuttingPrintSDK {

    private static final LDieCuttingPrintSDK INSTANCE = new LDieCuttingPrintSDK();
    public static LDieCuttingPrintSDK getInstance() { return INSTANCE; }
    private LDieCuttingPrintSDK() {}

    private MainSDK mainSDK;
    private boolean initialized;
    private LDieCuttingStatus status = new LDieCuttingStatus();
    private LDieCuttingCallback callback;
    private final Handler h = new Handler(Looper.getMainLooper());

    // ==================== 初始化 ====================

    /** 默认配置初始化 */
    public void init(Context ctx, String apiKey, LDieCuttingCallback cb) {
        if (initialized) return;
        this.callback = cb;
        mainSDK = new MainSDK(apiKey, ctx.getApplicationContext(), new Listener(), false);
        initialized = true;
    }

    /** 自定义配置初始化 */
    public void init(Context ctx, String apiKey, LDieCuttingConfig cfg, LDieCuttingCallback cb) {
        if (initialized) return;
        this.callback = cb;
        if (cfg == null) cfg = new LDieCuttingConfig();

        MaterialInfo mi = new MaterialInfo();
        mi.setMaterialType(MaterialTyepEnum.SoftPaper);
        mi.setPressure(cfg.getPressure());
        mi.setSpeed(cfg.getSpeed());
        ArrayList<MaterialInfo> list = new ArrayList<>();
        list.add(mi);

        ConfigInfo ci = new ConfigInfo();
        ci.setMaterialInfoList(list);
        ci.setExpandVal(cfg.getExpandVal());
        ci.setShrinkVal(cfg.getShrinkVal());
        ci.setFinishPercent(cfg.getFinishPercent());

        mainSDK = new MainSDK(apiKey, ctx.getApplicationContext(), ci, new Listener());
        initialized = true;
    }

    /** 释放 SDK 资源 */
    public void release() {
        if (mainSDK != null) { mainSDK.release(); mainSDK = null; }
        initialized = false;
    }

    public boolean isInitialized() { return initialized && mainSDK != null; }
    public LDieCuttingStatus getStatus() { return status; }

    // ==================== 操作（一一对应厂商 demo 的方法） ====================

    /** 普通切割 — 对应 cutTest() */
    public void cut(Bitmap bitmap, String fileName) {
        checkInit();
        InputInfo info = new InputInfo();
        info.setImgBitmap(bitmap);
        info.setPic(fileName);
        setCutting();
        mainSDK.process(info);
    }

    /** 肖像切割 — 对应 profileTest() */
    public void profileCut(Bitmap bitmap, String fileName) {
        checkInit();
        InputInfo info = new InputInfo();
        info.setImgBitmap(bitmap);
        info.setPic(fileName);
        info.setProfile(true);
        setCutting();
        mainSDK.process(info);
    }

    /** 相框裁剪 — 从 SD 卡路径加载图片 → 图片预处理 → 发送设备切割（一步完成） */
    public void frameCut(String filePath) {
        checkInit();
        // ① 从 SD 卡加载图片
        Bitmap src = loadBitmapFromPath(filePath);
        if (src == null) throw new IllegalArgumentException("无法加载图片: " + filePath);
        // ② 图片预处理：invertAlpha + 缩放到 1200×1800
        Bitmap processed = processFrameBitmap(src);
        // ③ 发送设备切割
        InputInfo info = new InputInfo();
        info.setImgBitmap(processed);
        info.setPic("frame_cut.png");
        setCutting();
        mainSDK.process(info);
    }

    /** 仅打印 — 对应 printTest() */
    public void printOnly() {
        checkInit();
        InputInfo info = new InputInfo();
        info.setAction(ActionTyepEnum.PrintOnly);
        setCutting();
        mainSDK.process(info);
    }

    /** 固件版本 — 对应 getFirmwareVersion() */
    public void firmwareVersion() { checkInit(); act(ActionTyepEnum.FirmwareVersion); }
    /** 退纸 — 对应 moveDown() */
    public void paperOut() { checkInit(); act(ActionTyepEnum.PaperOut); }
    /** 进纸 — 对应 moveUp() */
    public void paperIn() { checkInit(); act(ActionTyepEnum.PaperIn); }
    /** 左移刀座 — 对应 moveLeft() */
    public void moveLeft() { checkInit(); act(ActionTyepEnum.MoveLeft); }
    /** 右移刀座 — 对应 moveRight() */
    public void moveRight() { checkInit(); act(ActionTyepEnum.MoveRight); }
    /** 停止移动 — 对应 stopRoll() */
    public void stopMove() { checkInit(); act(ActionTyepEnum.StopMove); }
    /** 重启下位机 — 对应 reboot() */
    public void reboot() { checkInit(); act(ActionTyepEnum.Reboot); }
    /** 从入口退纸 — 对应 quitOnly() */
    public void quitFromEntry() { checkInit(); act(ActionTyepEnum.QuitOnly); }

    /** 校准 — 对应 calibration()，6 秒后自动查询结果 */
    public void calibration() {
        checkInit();
        act(ActionTyepEnum.Calibration);
        h.postDelayed(() -> { if (mainSDK != null) act(ActionTyepEnum.QueryCalibrationResult); }, 6000);
    }

    // ==================== 工具 ====================

    /** 按 96DPI 加载图片（官方推荐，不缩放，保持透明） */
    public static Bitmap loadBitmap(Context ctx, int resId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = 96;
        opts.inTargetDensity = 96;
        opts.inScaled = false;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(ctx.getResources(), resId, opts);
    }

    /** 从文件路径加载图片（内部使用，支持 SD 卡、外部存储等） */
    private static Bitmap loadBitmapFromPath(String filePath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        File f = new File(filePath);
        if (!f.exists()) return null;
        try {
            return BitmapFactory.decodeStream(new FileInputStream(f), null, opts);
        } catch (Exception e) {
            return null;
        }
    }

    /** 相框图片预处理：透明→黑色, 不透明→透明（Alpha 反转） */
    public static Bitmap invertAlpha(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] >> 24) & 0xFF;
            pixels[i] = (alpha == 0) ? 0xFF000000 : 0x00000000;
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    /**
     * 相框图片预处理：invertAlpha → 缩放到 1200×1800（内部使用）
     * @return 处理后的 Bitmap
     */
    private static Bitmap processFrameBitmap(Bitmap src) {
        Bitmap result = invertAlpha(src);
        src.recycle();
        Bitmap scaled = Bitmap.createScaledBitmap(result, 1200, 1800, true);
        result.recycle();
        return scaled;
    }

    // ==================== 内部 ====================

    private void act(ActionTyepEnum action) {
        InputInfo info = new InputInfo();
        info.setAction(action);
        mainSDK.process(info);
    }

    private void setCutting() { status.setState(LDieCuttingStatus.STATE_CUTTING); emitStatus(); }

    private void checkInit() {
        if (!initialized || mainSDK == null)
            throw new IllegalStateException("SDK 未初始化，请先调用 init()");
    }

    // ==================== SDK 回调 → 我们的回调 ====================

    private class Listener implements IReturnMsgListener {
        @Override public void onReceived(ReturnInfo ri) {
            if (ri == null) return;
            switch (ri.getAction()) {
                case FirmwareVersion:
                    status.setFirmwareVersion(ri.getFirmwareVersion());
                    emitStatus();
                    break;
                case Status:
                    handleStatus(ri.getCode().name());
                    break;
                case CanPrint:
                    status.setState(LDieCuttingStatus.STATE_CAN_PRINT);
                    status.setProgress(0.8f);
                    emitStatus();
                    emitProgress(0.8f);
                    break;
                case Finish:
                    status.setState(LDieCuttingStatus.STATE_IDLE);
                    status.setProgress(1f);
                    emitStatus();
                    emitProgress(1f);
                    break;
                case Error:
                    handleError(ri.getCode().name());
                    break;
            }
        }
    }

    private void handleStatus(String name) {
        switch (name) {
            case "OK":
                status.setState(LDieCuttingStatus.STATE_IDLE);
                break;
            case "Release":
                status.setState(LDieCuttingStatus.STATE_DISCONNECTED);
                mainSDK = null;
                break;
            case "RebootFail": case "RebootOk":
                status.setState(LDieCuttingStatus.STATE_REBOOTING);
                break;
            case "CalibrationFail": case "CalibrationOk":
                status.setState(LDieCuttingStatus.STATE_IDLE);
                break;
        }
        emitStatus();
    }

    private void handleError(String name) {
        int code = mapError(name);
        status.setState(LDieCuttingStatus.STATE_ERROR);
        status.setErrorCode(code);
        status.setErrorMessage(LDieCuttingConst.getErrorDesc(code));
        emitStatus();
        if (callback != null) h.post(() -> callback.onError(code, LDieCuttingConst.getErrorDesc(code)));
    }

    private static int mapError(String name) {
        switch (name) {
            case "SensorIsCovered":     return LDieCuttingConst.E_SENSOR_COVERED;
            case "NoPhoto":             return LDieCuttingConst.E_NO_PHOTO;
            case "DisconnetFromDevice": return LDieCuttingConst.E_DISCONNECT;
            case "NoFoundUSBDevice":    return LDieCuttingConst.E_NO_USB;
            case "NoMarker":            return LDieCuttingConst.E_NO_MARKER;
            case "NoCutData":           return LDieCuttingConst.E_NO_CUT_DATA;
            case "ExecuteCommandFail":  return LDieCuttingConst.E_COMMAND_FAIL;
            case "ExecutePltFail":      return LDieCuttingConst.E_PLT_FAIL;
            case "CutException":        return LDieCuttingConst.E_CUT_EXCEPTION;
            default:                    return -1;
        }
    }

    private void emitStatus() {
        if (callback != null) h.post(() -> callback.onStatusChanged(status));
    }

    private void emitProgress(float p) {
        if (callback != null) h.post(() -> callback.onProgressUpdated(p, 0, 0));
    }
}
