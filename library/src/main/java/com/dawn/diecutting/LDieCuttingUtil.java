package com.dawn.diecutting;

import android.graphics.Point;
import android.util.Log;

/**
 * 模切机工具类
 * <p>
 * 提供 PLT 数据生成、坐标转换、CRC 校验、字节转换等工具方法。
 * </p>
 */
public final class LDieCuttingUtil {

    private static final String TAG = "LDieCuttingUtil";

    private LDieCuttingUtil() {
    }

    // ==================== PLT 指令生成 ====================

    /**
     * 生成 PLT 初始化指令
     *
     * @return PLT 初始化字符串
     */
    public static String pltInit() {
        return "IN;";
    }

    /**
     * 生成 PLT 速度设置指令
     *
     * @param speed 速度值
     * @return PLT 速度指令
     */
    public static String pltSetSpeed(int speed) {
        return "VS" + speed + ";";
    }

    /**
     * 生成 PLT 压力设置指令
     *
     * @param force 压力值
     * @return PLT 压力指令
     */
    public static String pltSetForce(int force) {
        return "FS" + force + ";";
    }

    /**
     * 生成 PLT 抬笔移动指令
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @return PLT 移动指令
     */
    public static String pltPenUp(int x, int y) {
        return "PU" + x + "," + y + ";";
    }

    /**
     * 生成 PLT 落笔切割指令
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @return PLT 切割指令
     */
    public static String pltPenDown(int x, int y) {
        return "PD" + x + "," + y + ";";
    }

    /**
     * 将点序列转换为 PLT 切割路径
     *
     * @param points     坐标点序列
     * @param isClosed   是否闭合路径
     * @return PLT 路径字符串
     */
    public static String pointsToPltPath(Point[] points, boolean isClosed) {
        if (points == null || points.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // 移动到起点
        sb.append(pltPenUp(points[0].x, points[0].y));
        // 落笔并依次切割
        for (Point p : points) {
            sb.append(pltPenDown(p.x, p.y));
        }
        // 闭合回到起点
        if (isClosed && points.length > 1) {
            sb.append(pltPenDown(points[0].x, points[0].y));
        }
        // 抬笔
        sb.append(pltPenUp(points[points.length - 1].x, points[points.length - 1].y));
        return sb.toString();
    }

    /**
     * 构建完整的 PLT 切割数据
     *
     * @param speed   速度
     * @param force   压力
     * @param paths   PLT 路径字符串数组
     * @return 完整 PLT 数据
     */
    public static String buildPltData(int speed, int force, String... paths) {
        StringBuilder sb = new StringBuilder();
        sb.append(pltInit());
        sb.append(pltSetSpeed(speed));
        sb.append(pltSetForce(force));
        for (String path : paths) {
            if (path != null) {
                sb.append(path);
            }
        }
        return sb.toString();
    }

    // ==================== 坐标转换 ====================

    /**
     * 毫米转 PLT 坐标（需要知道齿轮比）
     *
     * @param mm    毫米值
     * @param gear  齿轮比
     * @return PLT 坐标值
     */
    public static int mmToPlt(float mm, int gear) {
        return Math.round(mm * gear);
    }

    /**
     * PLT 坐标转毫米
     *
     * @param plt   PLT 坐标值
     * @param gear  齿轮比
     * @return 毫米值
     */
    public static float pltToMm(int plt, int gear) {
        if (gear == 0) return 0;
        return (float) plt / gear;
    }

    // ==================== 参数校验 ====================

    /**
     * 校验压力参数
     */
    public static boolean isValidPressure(int pressure) {
        return pressure > 0 && pressure <= 1000;
    }

    /**
     * 校验速度参数
     */
    public static boolean isValidSpeed(int speed) {
        return speed > 0 && speed <= 1000;
    }

    /**
     * 校验自动间距
     */
    public static boolean isValidAutoSpace(float mm) {
        return mm >= 0 && mm <= 50;
    }

    // ==================== 字节转换 ====================

    /**
     * 浮点数转字节数组（小端序）
     */
    public static byte[] floatToBytes(float value) {
        int bits = Float.floatToIntBits(value);
        return new byte[]{
                (byte) (bits & 0xFF),
                (byte) ((bits >> 8) & 0xFF),
                (byte) ((bits >> 16) & 0xFF),
                (byte) ((bits >> 24) & 0xFF)
        };
    }

    /**
     * 字节数组转浮点数（小端序）
     */
    public static float bytesToFloat(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return 0f;
        }
        int bits = (bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF) << 8)
                | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24);
        return Float.intBitsToFloat(bits);
    }

    /**
     * 整数转 2 字节数组（小端序）
     */
    public static byte[] shortToBytes(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
    }

    /**
     * 2 字节数组转整数（小端序）
     */
    public static int bytesToShort(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return 0;
        }
        return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8);
    }

    // ==================== CRC 校验 ====================

    /**
     * 计算 CRC-16/MODBUS
     */
    public static byte[] crc16Modbus(byte[] data) {
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        return new byte[]{(byte) (crc & 0xFF), (byte) ((crc >> 8) & 0xFF)};
    }

    /**
     * 校验 CRC-16/MODBUS
     */
    public static boolean verifyCrc16Modbus(byte[] data) {
        if (data == null || data.length < 3) {
            return false;
        }
        byte[] dataPart = new byte[data.length - 2];
        System.arraycopy(data, 0, dataPart, 0, dataPart.length);
        byte[] crc = crc16Modbus(dataPart);
        return crc[0] == data[data.length - 2] && crc[1] == data[data.length - 1];
    }

    /**
     * 构建带 CRC 的命令帧
     */
    public static byte[] buildCommand(byte address, byte functionCode, byte[] data) {
        int dataLen = (data != null) ? data.length : 0;
        byte[] frame = new byte[4 + dataLen];
        frame[0] = address;
        frame[1] = functionCode;
        frame[2] = (byte) (dataLen & 0xFF);
        frame[3] = (byte) ((dataLen >> 8) & 0xFF);
        if (data != null) {
            System.arraycopy(data, 0, frame, 4, dataLen);
        }
        byte[] crc = crc16Modbus(frame);
        byte[] result = new byte[frame.length + 2];
        System.arraycopy(frame, 0, result, 0, frame.length);
        result[frame.length] = crc[0];
        result[frame.length + 1] = crc[1];
        return result;
    }

    // ==================== 字节工具 ====================

    /**
     * 字节数组转十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制字符串转字节数组
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            return new byte[0];
        }
        hex = hex.replaceAll("\\s+", "");
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 拼接字节数组
     */
    public static byte[] concatBytes(byte[]... arrays) {
        int totalLen = 0;
        for (byte[] arr : arrays) {
            if (arr != null) {
                totalLen += arr.length;
            }
        }
        byte[] result = new byte[totalLen];
        int offset = 0;
        for (byte[] arr : arrays) {
            if (arr != null) {
                System.arraycopy(arr, 0, result, offset, arr.length);
                offset += arr.length;
            }
        }
        return result;
    }

    // ==================== 日志工具 ====================

    public static void logDebug(String msg) {
        Log.d(TAG, msg);
    }

    public static void logError(String msg, Throwable e) {
        Log.e(TAG, msg, e);
    }
}
