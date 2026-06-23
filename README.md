# lib-die-cutting

Android 模切机控制工具库（基于汇森智诺 CutSDKManager 封装）

## 引用

Step 1. Add the JitPack repository to your build file

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```groovy
dependencies {
    implementation 'com.github.baiqingsong:lib-die-cutting:Tag'
}
```

## 快速开始

```java
// 1. 获取单例
LDieCuttingMachine machine = LDieCuttingMachine.getInstance();

// 2. 初始化（建议在 Application.onCreate() 中调用）
machine.init(context);

// 3. 设置参数
machine.setPressure(100);      // 压力
machine.setSpeed(200);         // 速度

// 4. 发送切割数据
String pltData = LDieCuttingUtil.buildPltData(200, 100,
    LDieCuttingUtil.pointsToPltPath(points, true));
machine.sendCutData(pltData, "test.plt", new CutListener() {
    @Override public void onComplete() { /* 切割完成 */ }
    @Override public void onProgress(int percent) { /* 进度 */ }
    @Override public void onError(int code, String msg) { /* 错误 */ }
});

// 5. 释放资源
machine.release();
```

## 类说明

`com.dawn.diecutting.LDieCuttingMachine` 模切机核心控制类（单例）

### 初始化

| 方法 | 说明 |
|------|------|
| `init(Context)` | 使用默认配置初始化 SDK |
| `init(Context, authKey, baudRate, debug)` | 自定义参数初始化 |
| `release()` | 释放 SDK 资源 |
| `isInitialized()` | 是否已初始化 |
| `getSDK()` | 获取底层 CutSDKManager 实例（高级用法） |

### 参数设置（同步调用）

| 方法 | 说明 |
|------|------|
| `setPressure(int)` | 设置切割压力 |
| `setSpeed(int)` | 设置切割速度 |
| `setWideFormat(int)` | 设置幅宽 |
| `setGear(int x, int y)` | 设置齿轮比 |
| `setLimit(boolean)` | 设置限位使能 |
| `setAutoPager(boolean)` | 设置自动送纸使能 |
| `setAutoSpace(float mm)` | 设置自动间距（0~50mm） |
| `setCutCompensate(int x, int x1, int y, int y1)` | 设置切割补偿 |
| `setPressureCompensate(int)` | 设置刀压补偿 |
| `setConfig(LDieCuttingConfig)` | 批量设置参数 |

### 参数查询（同步调用）

| 方法 | 返回值 | 说明 |
|------|------|------|
| `getPressure()` | int | 获取当前压力值 |
| `getSpeed()` | int | 获取当前速度值 |
| `getWideFormat()` | int | 获取幅宽 |
| `getGear()` | Point | 获取齿轮比 |
| `getLimit()` | boolean | 获取限位使能状态 |
| `getAutoPager()` | boolean | 获取自动送纸状态 |
| `getAutoSpace()` | float | 获取自动间距（mm） |
| `getCutCompensate()` | CutCompensateBean | 获取切割补偿值 |
| `getPressureCompensate()` | int | 获取刀压补偿值 |
| `getDeviceId()` | String | 获取设备 ID |
| `getDeviceVersion()` | String | 获取固件版本 |
| `getDeviceStatus()` | int | 获取设备状态（0=空闲, 1=刻绘中） |

### 操作指令

| 方法 | 说明 |
|------|------|
| `testCut()` | 测试切割 |
| `cancelCut()` | 取消切割（复位刻刀） |
| `outPager(boolean direction)` | 出纸控制 |
| `sendCutData(pltData, fileName, callback)` | 发送 PLT 切割数据（异步，带进度） |

### 回调

| 方法 | 说明 |
|------|------|
| `setCallback(LDieCuttingCallback)` | 设置状态变化回调 |

---

`com.dawn.diecutting.LDieCuttingConfig` 配置参数类（链式调用）

| 参数 | 类型 | 默认值 | 说明 |
|------|------|------|------|
| pressure | int | 100 | 切割压力 |
| speed | int | 200 | 切割速度 |
| wideFormat | int | 208 | 幅宽 |
| gearX / gearY | int | 2000 / 2000 | X/Y 齿轮比 |
| limit | boolean | true | 限位使能 |
| autoFeed | boolean | true | 自动送纸使能 |
| autoSpace | float | 0 | 自动间距（mm） |
| cutCompensateX/X1/Y/Y1 | int | 0 | 切割补偿 |
| pressureCompensate | int | 0 | 刀压补偿 |

---

`com.dawn.diecutting.LDieCuttingStatus` 状态信息类

| 常量 | 值 | 说明 |
|------|------|------|
| `STATE_DISCONNECTED` | 0 | 未初始化 |
| `STATE_IDLE` | 1 | 空闲就绪 |
| `STATE_CUTTING` | 2 | 正在切割 |
| `STATE_PAUSED` | 3 | 已暂停 |
| `STATE_EMERGENCY` | 4 | 急停状态 |
| `STATE_ERROR` | 5 | 故障状态 |
| `STATE_REBOOTING` | 6 | 复位中 |
| `STATE_CAN_PRINT` | 7 | 可继续打印 |

| 属性 | 类型 | 说明 |
|------|------|------|
| state | int | 状态码 |
| progress | float | 切割进度（0.0~1.0） |
| errorCode | int | 错误码 |
| errorMessage | String | 错误描述 |

---

`com.dawn.diecutting.LDieCuttingCallback` 状态回调接口

| 方法 | 说明 |
|------|------|
| `onStatusChanged(LDieCuttingStatus)` | 状态变化 |
| `onProgressUpdated(float, int, int)` | 进度更新 |
| `onError(int, String)` | 错误通知 |

---

`com.dawn.diecutting.LDieCuttingUtil` 工具类

### PLT 指令生成

| 方法 | 说明 |
|------|------|
| `pltInit()` | 生成初始化指令 `IN;` |
| `pltSetSpeed(int)` | 生成速度指令 `VS{n};` |
| `pltSetForce(int)` | 生成压力指令 `FS{n};` |
| `pltPenUp(int x, int y)` | 抬笔移动 `PU{x},{y};` |
| `pltPenDown(int x, int y)` | 落笔切割 `PD{x},{y};` |
| `pointsToPltPath(Point[], boolean)` | 点序列 → PLT 切割路径 |
| `buildPltData(int speed, int force, String... paths)` | 构建完整 PLT 数据 |

### 坐标转换

| 方法 | 说明 |
|------|------|
| `mmToPlt(float mm, int gear)` | 毫米 → PLT 坐标 |
| `pltToMm(int plt, int gear)` | PLT 坐标 → 毫米 |

### 字节 / CRC 工具

| 方法 | 说明 |
|------|------|
| `floatToBytes(float)` / `bytesToFloat(byte[])` | 浮点数 ↔ 字节数组 |
| `shortToBytes(int)` / `bytesToShort(byte[])` | 整数 ↔ 2字节数组 |
| `crc16Modbus(byte[])` / `verifyCrc16Modbus(byte[])` | CRC-16 计算/校验 |
| `buildCommand(byte, byte, byte[])` | 构建带 CRC 的命令帧 |
| `bytesToHex(byte[])` / `hexToBytes(String)` | 十六进制字符串 ↔ 字节数组 |
| `concatBytes(byte[]...)` | 拼接字节数组 |

## PLT 数据格式

模切机使用 HPGL 指令集：

```
IN;            初始化
VS200;         速度 200
FS100;         压力 100
PU100,200;     抬笔移动到 (100,200)
PD100,300;     落笔切割到 (100,300)
PD200,300;     切割到 (200,300)
...
PU0,0;         抬笔移动到 (0,0)
```

---

## 打印切割一体机 API（LDieCuttingPrintSDK）

`com.dawn.diecutting.LDieCuttingPrintSDK` 打印切割一体机控制类（单例）

### 初始化

| 方法 | 说明 |
|------|------|
| `init(Context, apiKey)` | 使用默认配置初始化 |
| `init(Context, apiKey, config)` | 使用自定义配置初始化 |
| `release()` | 释放 SDK 资源 |
| `isInitialized()` | 是否已初始化 |
| `getSDK()` | 获取底层 MainSDK 实例 |

### 切割操作

| 方法 | 说明 |
|------|------|
| `processCut(Bitmap, fileName)` | 普通切割图片 |
| `processCut(Bitmap, fileName, dpi)` | 按指定 DPI 切割图片 |
| `processProfileCut(Bitmap, fileName)` | 肖像照切割 |
| `processPrintOnly()` | 仅打印（不切割） |
| `process(InputInfo)` | 使用自定义 InputInfo 操作 |

### 使用示例

```java
LDieCuttingPrintSDK printSDK = LDieCuttingPrintSDK.getInstance();
printSDK.init(context, "your-api-key");

// 设置回调
printSDK.setCallback(new LDieCuttingCallback() {
    @Override public void onStatusChanged(LDieCuttingStatus s) {
        Log.d("TAG", "状态: " + s.getStateName());
    }
    @Override public void onProgressUpdated(float p, int c, int t) {}
    @Override public void onError(int code, String msg) {
        Log.e("TAG", LDieCuttingConst.getErrorDescription(code));
    }
});

// 切割图片
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
printSDK.processCut(bitmap, "test.png");

// 释放
printSDK.release();
```

---

## 常量参考（LDieCuttingConst）

### 操作类型

| 常量 | 值 | 说明 |
|------|------|------|
| `ACTION_CUT` | 0 | 普通切割 |
| `ACTION_PRINT_ONLY` | 1 | 仅打印 |
| `ACTION_PAPER_BACK` | 2 | 退纸 |
| `ACTION_PAPER_FORWARD` | 3 | 进纸 |
| `ACTION_MOVE_LEFT_RIGHT` | 4 | 左右移动 |
| `ACTION_TEST_CUT` | 5 | 测试切割 |

### 物料类型

| 常量 | 值 | 说明 |
|------|------|------|
| `MATERIAL_SOFT_PAPER` | 0 | 软纸 |
| `MATERIAL_HARD_PAPER` | 1 | 硬纸 |
| `MATERIAL_PHOTO_PAPER` | 2 | 相纸 |
| `MATERIAL_STICKER` | 3 | 不干胶 |
| `MATERIAL_CUSTOM` | 4 | 自定义 |

### 错误码

| 常量 | 值 | 说明 |
|------|------|------|
| `ERROR_SENSOR_COVERED` | 1001 | 前感应器被遮挡 |
| `ERROR_NO_PHOTO` | 1002 | 后感应器检测不到相片 |
| `ERROR_DISCONNECT` | 1003 | USB 连接失败 |
| `ERROR_NO_USB_DEVICE` | 1004 | 未发现 USB HID 设备 |
| `ERROR_NO_MARKER` | 1005 | 无法检测标记点 |
| `ERROR_NO_CUT_DATA` | 1006 | 无法生成切割数据 |
| `ERROR_EXECUTE_COMMAND_FAIL` | 1007 | 无法发送指令 |
| `ERROR_EXECUTE_PLT_FAIL` | 1008 | 无法发送 PLT 数据 |
| `ERROR_CUT_EXCEPTION` | 1009 | 切割异常终止 |

