# 保留模切机厂商 SDK 的所有类和方法
# CutSDKManager（串口刻绘版）
-keep class com.hszn.sdk.** { *; }
-keep class com.hszn.sdk.beans.** { *; }
-keep class com.hszn.sdk.interfaces.** { *; }

# MainSDK（打印切割一体机版，需 CutSDK.aar）
-keep class com.hszn.cutsdk.** { *; }
-keep class com.hszn.cutsdk.entities.** { *; }
-keep class com.hszn.cutsdk.enums.** { *; }
-keep class com.hszn.cutsdk.interfaces.** { *; }
