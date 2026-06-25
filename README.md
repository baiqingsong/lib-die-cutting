# lib-die-cutting

Android 打印切割一体机控制库（汇森智诺 MainSDK / CutSDK.aar 封装），支持普通切割、肖像切割、相框裁剪。

## 引用

```groovy
allprojects { repositories { maven { url 'https://jitpack.io' } } }
dependencies { implementation 'com.github.baiqingsong:lib-die-cutting:Tag' }
```

## 快速开始

```java
LDieCuttingPrintSDK sdk = LDieCuttingPrintSDK.getInstance();
sdk.init(context, apiKey, callback);  // 密钥外部传入

// ===== 普通切割 =====
sdk.printOnly();  // ① 仅打印
// ... 手动放纸 ...
Bitmap bmp = LDieCuttingPrintSDK.loadBitmap(ctx, R.drawable.test4);
sdk.cut(bmp, "test.png");  // ② 切割

// ===== 相框裁剪（SD 卡路径 → 一步完成）=====
sdk.frameCut("/sdcard/DCIM/photo.jpg");

sdk.release();
```

## API

### LDieCuttingPrintSDK (单例)

| 方法 | 说明 |
|------|------|
| `init(ctx, apiKey, cb)` | 默认配置初始化 |
| `init(ctx, apiKey, config, cb)` | 自定义配置初始化 |
| `release()` | 释放资源 |
| `cut(Bitmap, fileName)` | 普通切割 |
| `profileCut(Bitmap, fileName)` | 肖像切割 |
| `frameCut(filePath)` | 相框裁剪（SD卡路径 → 加载 → 预处理 → 切割，一步完成） |
| `printOnly()` | 仅打印 |
| `paperOut()/paperIn()` | 退纸/进纸 |
| `moveLeft()/moveRight()` | 左/右移刀座 |
| `stopMove()` | 停止移动 |
| `reboot()` | 重启下位机 |
| `quitFromEntry()` | 从入口退纸 |
| `calibration()` | 校准(6s后查结果) |
| `firmwareVersion()` | 查询固件版本 |
| `loadBitmap(ctx, resId)` | 96DPI 加载图片(static) |
| `invertAlpha(Bitmap)` | Alpha 反转：透明→黑/不透明→透(static) |

### 相框裁剪流程

```java
// 传入 SD 卡图片路径，内部自动完成：加载 → invertAlpha → 缩放1200×1800 → 切割
sdk.frameCut("/sdcard/DCIM/photo.jpg");
```

> `frameCut` 内部步骤：① `BitmapFactory.decodeFile(path)` 加载 ② `invertAlpha()` 透明反转 ③ `createScaledBitmap(1200, 1800)` 缩放 ④ `mainSDK.process()` 发送切割

### LDieCuttingConfig

| 参数 | 默认 | 说明 |
|------|------|------|
| pressure | 250 | 刀压 |
| speed | 90 | 速度(max 100) |
| expandVal | 3 | 轮廓扩大像素(1~4) |
| shrinkVal | 2 | 肖像缩小(mm) |
| finishPercent | 80 | 完成通知% |

### LDieCuttingStatus

| 状态 | 说明 |
|------|------|
| STATE_IDLE | 就绪 |
| STATE_CUTTING | 切割中 |
| STATE_CAN_PRINT | 可继续打印 |
| STATE_ERROR | 故障 |

### 错误码

| 码 | 说明 |
|----|------|
| 1001 | 前感应器遮挡 |
| 1002 | 后感应器无相片 |
| 1003 | USB连接失败 |
| 1004 | 无USB HID设备 |
| 1005 | 无标记点 |
| 1006 | 无切割数据 |
| 1007 | 指令发送失败 |
| 1008 | PLT发送失败 |
| 1009 | 切割异常终止 |
