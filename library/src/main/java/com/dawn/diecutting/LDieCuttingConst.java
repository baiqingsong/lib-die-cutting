package com.dawn.diecutting;

/**
 * 状态常量 & 错误码
 */
public final class LDieCuttingConst {
    private LDieCuttingConst() {}

    public static final int MATERIAL_SOFT_PAPER = 0;

    // StatusCodeEnum name
    public static final String S_OK = "OK";
    public static final String S_RELEASE = "Release";
    public static final String S_REBOOT_FAIL = "RebootFail";
    public static final String S_REBOOT_OK = "RebootOk";
    public static final String S_CALIBRATION_FAIL = "CalibrationFail";
    public static final String S_CALIBRATION_OK = "CalibrationOk";

    // 错误码
    public static final int E_SENSOR_COVERED = 1001;
    public static final int E_NO_PHOTO = 1002;
    public static final int E_DISCONNECT = 1003;
    public static final int E_NO_USB = 1004;
    public static final int E_NO_MARKER = 1005;
    public static final int E_NO_CUT_DATA = 1006;
    public static final int E_COMMAND_FAIL = 1007;
    public static final int E_PLT_FAIL = 1008;
    public static final int E_CUT_EXCEPTION = 1009;

    public static String getErrorDesc(int code) {
        switch (code) {
            case E_SENSOR_COVERED: return "前感应器被遮挡";
            case E_NO_PHOTO:       return "后感应器检测不到相片";
            case E_DISCONNECT:     return "USB连接失败";
            case E_NO_USB:         return "未发现USB HID设备";
            case E_NO_MARKER:      return "无法检测标记点";
            case E_NO_CUT_DATA:    return "无法生成切割数据";
            case E_COMMAND_FAIL:   return "无法发送指令";
            case E_PLT_FAIL:       return "无法发送PLT";
            case E_CUT_EXCEPTION:  return "切割异常终止";
            default:               return "未知错误:" + code;
        }
    }
}
