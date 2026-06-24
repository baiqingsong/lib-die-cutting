package com.dawn.diecutting;

/**
 * 状态回调接口 — 对应 IReturnMsgListener 的上层封装
 */
public interface LDieCuttingCallback {
    void onStatusChanged(LDieCuttingStatus status);
    void onProgressUpdated(float progress, int currentCount, int totalCount);
    void onError(int errorCode, String message);
}
