package com.dawn.libdiecutting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dawn.diecutting.LDieCuttingCallback;
import com.dawn.diecutting.LDieCuttingConfig;
import com.dawn.diecutting.LDieCuttingMachine;
import com.dawn.diecutting.LDieCuttingStatus;
import com.hszn.sdk.interfaces.CutListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模切机控制演示 Activity
 * <p>所有串口操作均在后台线程执行，避免阻塞主线程导致 ANR</p>
 */
public class MainActivity extends AppCompatActivity {

    private LDieCuttingMachine machine;
    private TextView tvStatus;
    private TextView tvProgress;
    private TextView tvLog;

    /** 后台串行线程，所有同步操作排队执行，避免指令冲突 */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** 测试 PLT 数据：画一个 100x100 的方框 */
    private static final String TEST_PLT =
            "IN;VS80;FS260;" +
            "PU100,100;" +
            "PD100,200;PD200,200;PD200,100;PD100,100;" +
            "PU0,0;";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        machine = LDieCuttingMachine.getInstance();

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvProgress = findViewById(R.id.tv_progress);
        tvLog = findViewById(R.id.tv_log);

        Button btnInit = findViewById(R.id.btn_connect);
        Button btnRelease = findViewById(R.id.btn_disconnect);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnPause = findViewById(R.id.btn_pause);
        Button btnResume = findViewById(R.id.btn_resume);
        Button btnEmergency = findViewById(R.id.btn_emergency);
        Button btnConfig = findViewById(R.id.btn_config);
        Button btnSendPlt = findViewById(R.id.btn_send_plt);

        // 初始化 SDK（主线程操作，很快）
        btnInit.setOnClickListener(v -> {
            appendLog("正在初始化 SDK...");
            machine.init(getApplicationContext());
            machine.setCallback(mainCallback);
            appendLog("SDK 初始化完成");
            updateUI();
        });

        // 释放 SDK
        btnRelease.setOnClickListener(v -> {
            executor.execute(() -> {
                machine.release();
                runOnUiThread(() -> {
                    appendLog("SDK 已释放");
                    updateUI();
                });
            });
        });

        // 测试切割
        btnStart.setOnClickListener(v -> runOnBg("测试切割", () -> {
            boolean ok = machine.testCut();
            return ok ? "指令已发送" : "发送失败";
        }));

        // 取消切割
        btnStop.setOnClickListener(v -> runOnBg("取消切割", () -> {
            boolean ok = machine.cancelCut();
            return ok ? "指令已发送" : "发送失败";
        }));

        // 出纸
        btnPause.setOnClickListener(v -> executor.execute(() -> {
            machine.outPager(true);
            runOnUiThread(() -> appendLog("出纸指令已发送"));
        }));

        // 获取设备状态
        btnResume.setOnClickListener(v -> runOnBg("查询状态", () -> {
            int st = machine.getDeviceStatus();
            return "设备状态: " + (st == 1 ? "刻绘中" : st == 0 ? "空闲" : "未知(" + st + ")");
        }));

        // 查询参数
        btnEmergency.setOnClickListener(v -> runOnBg("查询参数", () -> {
            int pressure = machine.getPressure();
            int speed = machine.getSpeed();
            String devId = machine.getDeviceId();
            String version = machine.getDeviceVersion();
            return "压力:" + pressure + " 速度:" + speed
                    + " ID:" + devId + " 版本:" + version;
        }));

        // 应用配置
        btnConfig.setOnClickListener(v -> runOnBg("应用配置", () -> {
            LDieCuttingConfig config = LDieCuttingConfig.createDefault()
                    .setPressure(250)
                    .setSpeed(90)
                    .setWideFormat(208);
            boolean ok = machine.setConfig(config);
            return ok ? "配置成功" : "配置失败";
        }));

        // 发送 PLT 切割数据
        btnSendPlt.setOnClickListener(v -> {
            appendLog("发送 PLT 切割数据...");
            runOnUiThread(() -> tvProgress.setText("进度: 0%"));
            machine.sendCutData(TEST_PLT, "test.plt", new CutListener() {
                @Override
                public void onComplete() {
                    runOnUiThread(() -> {
                        appendLog("切割完成");
                        tvProgress.setText("进度: 100% (完成)");
                    });
                }
                @Override
                public void onProgress(int percent) {
                    runOnUiThread(() -> {
                        tvProgress.setText("进度: " + percent + "%");
                        appendLog("进度: " + percent + "%");
                    });
                }
                @Override
                public void onError(int code, String msg) {
                    runOnUiThread(() -> appendLog("切割出错: [" + code + "] " + msg));
                }
            });
        });
    }

    /** 在后台线程执行同步操作并更新 UI */
    private void runOnBg(String label, BgTask task) {
        executor.execute(() -> {
            appendLogBg(label + "...");
            String result = task.run();
            runOnUiThread(() -> appendLog(label + ": " + result));
        });
    }

    /** 后台线程中追加日志 */
    private void appendLogBg(String msg) {
        runOnUiThread(() -> appendLog(msg));
    }

    private interface BgTask {
        String run();
    }

    // ==================== 回调 ====================

    private final LDieCuttingCallback mainCallback = new LDieCuttingCallback() {
        @Override
        public void onStatusChanged(LDieCuttingStatus status) {
            runOnUiThread(() ->
                tvStatus.setText("状态: " + status.getStateName()));
        }
        @Override
        public void onProgressUpdated(float progress, int currentCount, int totalCount) {
            runOnUiThread(() ->
                tvProgress.setText("进度: " + (int) (progress * 100) + "%"));
        }
        @Override
        public void onError(int errorCode, String message) {
            runOnUiThread(() ->
                appendLog("错误 [" + errorCode + "]: " + message));
        }
    };

    // ==================== UI 辅助 ====================

    private void updateUI() {
        LDieCuttingStatus status = machine.getCurrentStatus();
        tvStatus.setText("状态: " + status.getStateName()
                + (machine.isInitialized() ? " (已初始化)" : " (未初始化)"));
        tvProgress.setText("进度: " + (int) (status.getProgress() * 100) + "%");
    }

    private void appendLog(String msg) {
        String current = tvLog.getText().toString();
        tvLog.setText(current + "\n" + msg);
    }
}
