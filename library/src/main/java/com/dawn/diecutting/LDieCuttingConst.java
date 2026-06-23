package com.dawn.diecutting;

/**
 * 模切机常量定义
 * <p>
 * 包含操作类型、物料类型、相纸尺寸、状态码、错误码等所有常量。
 * 来源于厂商"打印切割一体机 SDK 使用说明文档"。
 * </p>
 */
public final class LDieCuttingConst {

    private LDieCuttingConst() {
    }

    // ==================== 操作类型（Action） ====================

    /** 普通切割 */
    public static final int ACTION_CUT = 0;

    /** 仅打印 */
    public static final int ACTION_PRINT_ONLY = 1;

    /** 退纸（执行 time 秒） */
    public static final int ACTION_PAPER_BACK = 2;

    /** 进纸（执行 time 秒） */
    public static final int ACTION_PAPER_FORWARD = 3;

    /** 左右移动 */
    public static final int ACTION_MOVE_LEFT_RIGHT = 4;

    /** 测试切割 */
    public static final int ACTION_TEST_CUT = 5;

    // ==================== 物料类型（Material Type） ====================

    /** 软纸 */
    public static final int MATERIAL_SOFT_PAPER = 0;

    /** 硬纸 */
    public static final int MATERIAL_HARD_PAPER = 1;

    /** 相纸 */
    public static final int MATERIAL_PHOTO_PAPER = 2;

    /** 不干胶 */
    public static final int MATERIAL_STICKER = 3;

    /** 自定义材料 */
    public static final int MATERIAL_CUSTOM = 4;

    // ==================== 相纸尺寸 ====================

    /** 6x4 寸 */
    public static final int SIZE_64 = 0;

    /** 5x3 寸 */
    public static final int SIZE_53 = 1;

    /** A4 */
    public static final int SIZE_A4 = 2;

    /** A5 */
    public static final int SIZE_A5 = 3;

    /** 自定义尺寸 */
    public static final int SIZE_CUSTOM = 4;

    // ==================== SDK 返回消息 Action 类型 ====================

    /** 固件版本号 */
    public static final int MSG_FIRMWARE_VERSION = 100;

    /** 切割机状态 */
    public static final int MSG_STATUS = 101;

    /** 可以继续打印下一张 */
    public static final int MSG_CAN_PRINT = 102;

    /** 完成本次切割 */
    public static final int MSG_FINISH = 103;

    /** 错误信息 */
    public static final int MSG_ERROR = 104;

    // ==================== 状态码（Status Code） ====================

    /** 状态正常（初始化成功，可以调用 process 方法） */
    public static final int STATUS_OK = 0;

    /** 尝试启动下位机失败 */
    public static final int STATUS_REBOOT_FAIL = 1;

    /** 尝试启动下位机成功 */
    public static final int STATUS_REBOOT_OK = 2;

    /** SDK 已释放资源 */
    public static final int STATUS_RELEASE = 3;

    // ==================== 错误码 ====================

    /** 前感应器被遮挡 */
    public static final int ERROR_SENSOR_COVERED = 1001;

    /** 后感应器检测不到相片 */
    public static final int ERROR_NO_PHOTO = 1002;

    /** USB 连接切割机失败 */
    public static final int ERROR_DISCONNECT = 1003;

    /** 没有发现 USB HID 设备 */
    public static final int ERROR_NO_USB_DEVICE = 1004;

    /** 无法检测到图片标记点 */
    public static final int ERROR_NO_MARKER = 1005;

    /** 无法生成切割数据 */
    public static final int ERROR_NO_CUT_DATA = 1006;

    /** 无法发送指令 */
    public static final int ERROR_EXECUTE_COMMAND_FAIL = 1007;

    /** 无法发送 PLT 数据 */
    public static final int ERROR_EXECUTE_PLT_FAIL = 1008;

    /** 切割异常终止 */
    public static final int ERROR_CUT_EXCEPTION = 1009;

    // ==================== 默认参数 ====================

    /** 默认切割刀压 */
    public static final int DEFAULT_PRESSURE = 250;

    /** 默认切割速度（最高 100） */
    public static final int DEFAULT_SPEED = 90;

    /** 默认轮廓扩大像素值（1~4） */
    public static final int DEFAULT_EXPAND_VAL = 3;

    /** 默认肖像切割轮廓缩小值（mm） */
    public static final float DEFAULT_SHRINK_VAL = 2.0f;

    /** 默认完成通知百分比 */
    public static final int DEFAULT_FINISH_PERCENT = 80;

    /** 默认波特率 */
    public static final int DEFAULT_BAUD_RATE = 3000;

    // ==================== 工具方法 ====================

    /**
     * 获取错误码的中文描述
     */
    public static String getErrorDescription(int errorCode) {
        switch (errorCode) {
            case ERROR_SENSOR_COVERED:
                return "前感应器被遮挡，请及时取走相片";
            case ERROR_NO_PHOTO:
                return "后感应器检测不到待切割的相片";
            case ERROR_DISCONNECT:
                return "通过 USB 连接切割机失败";
            case ERROR_NO_USB_DEVICE:
                return "没有发现 USB HID 设备，请检查接线";
            case ERROR_NO_MARKER:
                return "无法检测到图片上的标记点信息";
            case ERROR_NO_CUT_DATA:
                return "无法生成切割数据";
            case ERROR_EXECUTE_COMMAND_FAIL:
                return "无法发送指令";
            case ERROR_EXECUTE_PLT_FAIL:
                return "无法发送 PLT 数据";
            case ERROR_CUT_EXCEPTION:
                return "切割异常终止";
            default:
                return "未知错误: " + errorCode;
        }
    }

    /**
     * 获取操作类型的中文描述
     */
    public static String getActionName(int action) {
        switch (action) {
            case ACTION_CUT:
                return "普通切割";
            case ACTION_PRINT_ONLY:
                return "仅打印";
            case ACTION_PAPER_BACK:
                return "退纸";
            case ACTION_PAPER_FORWARD:
                return "进纸";
            case ACTION_MOVE_LEFT_RIGHT:
                return "左右移动";
            case ACTION_TEST_CUT:
                return "测试切割";
            default:
                return "未知操作";
        }
    }

    /**
     * 获取物料类型的中文描述
     */
    public static String getMaterialName(int materialType) {
        switch (materialType) {
            case MATERIAL_SOFT_PAPER:
                return "软纸";
            case MATERIAL_HARD_PAPER:
                return "硬纸";
            case MATERIAL_PHOTO_PAPER:
                return "相纸";
            case MATERIAL_STICKER:
                return "不干胶";
            case MATERIAL_CUSTOM:
                return "自定义材料";
            default:
                return "未知材料";
        }
    }

    /**
     * 获取相纸尺寸的中文描述
     */
    public static String getSizeName(int size) {
        switch (size) {
            case SIZE_64:
                return "6x4寸";
            case SIZE_53:
                return "5x3寸";
            case SIZE_A4:
                return "A4";
            case SIZE_A5:
                return "A5";
            case SIZE_CUSTOM:
                return "自定义";
            default:
                return "未知尺寸";
        }
    }
}
