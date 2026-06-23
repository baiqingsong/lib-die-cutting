package com.dawn.diecutting;

/**
 * 模切机状态回调接口
 * <p>
 * 用于异步接收模切机的状态变化、进度更新、错误等通知。
 * 所有回调方法在主线程执行。
 * </p>
 */
public interface LDieCuttingCallback {

    /**
     * 状态变化回调
     *
     * @param status 当前状态信息
     */
    void onStatusChanged(LDieCuttingStatus status);

    /**
     * 切割进度回调
     *
     * @param progress     进度（0.0 ~ 1.0）
     * @param currentCount 当前已完成次数
     * @param totalCount   总次数
     */
    void onProgressUpdated(float progress, int currentCount, int totalCount);

    /**
     * 错误回调
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    void onError(int errorCode, String message);
}
