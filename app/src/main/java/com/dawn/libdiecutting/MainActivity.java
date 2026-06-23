package com.dawn.libdiecutting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dawn.diecutting.LDieCuttingCallback;
import com.dawn.diecutting.LDieCuttingConfig;
import com.dawn.diecutting.LDieCuttingMachine;
import com.dawn.diecutting.LDieCuttingStatus;

/**
 * 模切机控制演示 Activity
 * <p>
 * 展示如何使用 lib-die-cutting 库进行模切机的基本操作。
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    private LDieCuttingMachine machine;
    private TextView tvStatus;
    private TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        machine = LDieCuttingMachine.getInstance();

        initViews();
        initCallbacks();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvProgress = findViewById(R.id.tv_progress);

        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnDisconnect = findViewById(R.id.btn_disconnect);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnPause = findViewById(R.id.btn_pause);
        Button btnResume = findViewById(R.id.btn_resume);
        Button btnEmergency = findViewById(R.id.btn_emergency);
        Button btnConfig = findViewById(R.id.btn_config);

        btnConnect.setOnClickListener(v -> {
            machine.connect("192.168.1.100", 8080);
            updateUI();
        });

        btnDisconnect.setOnClickListener(v -> {
            machine.disconnect();
            updateUI();
        });

        btnStart.setOnClickListener(v -> {
            machine.startCut();
            updateUI();
        });

        btnStop.setOnClickListener(v -> {
            machine.stopCut();
            updateUI();
        });

        btnPause.setOnClickListener(v -> {
            machine.pause();
            updateUI();
        });

        btnResume.setOnClickListener(v -> {
            machine.resume();
            updateUI();
        });

        btnEmergency.setOnClickListener(v -> {
            machine.emergencyStop();
            updateUI();
        });

        btnConfig.setOnClickListener(v -> {
            LDieCuttingConfig config = LDieCuttingConfig.createDefault()
                    .setPressure(50.0f)
                    .setSpeed(60.0f)
                    .setDepth(0.3f)
                    .setCutCount(10);
            machine.setConfig(config);
            updateUI();
        });
    }

    private void initCallbacks() {
        machine.setCallback(new LDieCuttingCallback() {
            @Override
            public void onStatusChanged(LDieCuttingStatus status) {
                runOnUiThread(() -> updateUI());
            }

            @Override
            public void onProgressUpdated(float progress, int currentCount, int totalCount) {
                runOnUiThread(() -> {
                    tvProgress.setText("进度: " + (int) (progress * 100) + "%"
                            + " (" + currentCount + "/" + totalCount + ")");
                });
            }

            @Override
            public void onError(int errorCode, String message) {
                runOnUiThread(() -> {
                    tvStatus.setText("错误: [" + errorCode + "] " + message);
                });
            }

            @Override
            public void onConnectionChanged(boolean connected) {
                runOnUiThread(() -> updateUI());
            }
        });
    }

    private void updateUI() {
        LDieCuttingStatus status = machine.getFullStatus();
        tvStatus.setText("状态: " + status.getStateName()
                + (machine.isConnected() ? " (已连接)" : " (未连接)"));
        tvProgress.setText("进度: " + (int) (status.getProgress() * 100) + "%");
    }
}
