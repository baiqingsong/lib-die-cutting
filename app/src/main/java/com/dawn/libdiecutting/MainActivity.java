package com.dawn.libdiecutting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dawn.diecutting.LDieCuttingCallback;
import com.dawn.diecutting.LDieCuttingConst;
import com.dawn.diecutting.LDieCuttingPrintSDK;
import com.dawn.diecutting.LDieCuttingStatus;

/**
 * 打印切割一体机演示 — 完全对应厂商 printthencutDemo
 *
 * 流程: 初始化 → Status.OK → 仅打印 → 手动放纸 → 切割
 */
public class MainActivity extends AppCompatActivity {

    private LDieCuttingPrintSDK sdk;
    private TextView tvStatus, tvProgress, tvLog;
    private Button btnInit, btnFw, btnRelease, btnLeft, btnRight, btnDown, btnStop,
            btnCut, btnProfile, btnPrint, btnReboot, btnQuit, btnEnter, btnCalib;

    private static final String API_KEY = "saikt13agrt6i13h";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sdk = LDieCuttingPrintSDK.getInstance();
        initViews();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvProgress = findViewById(R.id.tv_progress);
        tvLog = findViewById(R.id.tv_log);

        btnInit = findViewById(R.id.btn_init);
        btnFw = findViewById(R.id.btn_fw);
        btnRelease = findViewById(R.id.btn_release);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        btnDown = findViewById(R.id.btn_down);
        btnStop = findViewById(R.id.btn_stop);
        btnCut = findViewById(R.id.btn_cut);
        btnProfile = findViewById(R.id.btn_profile);
        btnPrint = findViewById(R.id.btn_print);
        btnReboot = findViewById(R.id.btn_reboot);
        btnQuit = findViewById(R.id.btn_quit);
        btnEnter = findViewById(R.id.btn_enter);
        btnCalib = findViewById(R.id.btn_calib);

        btnInit.setOnClickListener(v -> {
            log("初始化...");
            sdk.init(getApplicationContext(), API_KEY, cb);
        });
        btnFw.setOnClickListener(v -> sdk.firmwareVersion());
        btnRelease.setOnClickListener(v -> { sdk.release(); log("已释放"); disableAll(); btnInit.setEnabled(true); });
        btnLeft.setOnClickListener(v -> sdk.moveLeft());
        btnRight.setOnClickListener(v -> sdk.moveRight());
        btnDown.setOnClickListener(v -> sdk.paperOut());
        btnStop.setOnClickListener(v -> sdk.stopMove());
        btnCut.setOnClickListener(v -> {
            log("普通切割");
            sdk.cut(LDieCuttingPrintSDK.loadBitmap(this, R.drawable.test4), "test.png");
        });
        btnProfile.setOnClickListener(v -> {
            log("肖像切割");
            sdk.profileCut(LDieCuttingPrintSDK.loadBitmap(this, R.drawable.profile), "profile.png");
        });
        btnPrint.setOnClickListener(v -> { log("仅打印"); sdk.printOnly(); });
        btnReboot.setOnClickListener(v -> sdk.reboot());
        btnQuit.setOnClickListener(v -> sdk.quitFromEntry());
        btnEnter.setOnClickListener(v -> sdk.paperIn());
        btnCalib.setOnClickListener(v -> sdk.calibration());

        disableAll(); btnInit.setEnabled(true);
        log("①初始化 → ②仅打印 → ③手动放纸 → ④切割");
    }

    private final LDieCuttingCallback cb = new LDieCuttingCallback() {
        @Override public void onStatusChanged(LDieCuttingStatus s) {
            runOnUiThread(() -> {
                String fw = s.getFirmwareVersion();
                tvStatus.setText(s.getStateName() + (fw.isEmpty() ? "" : " | FW:" + fw));
                if (s.getState() == LDieCuttingStatus.STATE_IDLE && sdk.isInitialized()) {
                    enableAll(); btnInit.setEnabled(false);
                }
            });
        }
        @Override public void onProgressUpdated(float p, int c, int t) {
            runOnUiThread(() -> tvProgress.setText((int)(p*100) + "%"));
        }
        @Override public void onError(int code, String msg) {
            runOnUiThread(() -> log("❌" + LDieCuttingConst.getErrorDesc(code)));
        }
    };

    private void enableAll() {
        for (Button b : new Button[]{btnFw, btnRelease, btnLeft, btnRight, btnDown, btnStop,
                btnCut, btnProfile, btnPrint, btnReboot, btnQuit, btnEnter, btnCalib}) b.setEnabled(true);
    }
    private void disableAll() {
        for (Button b : new Button[]{btnFw, btnRelease, btnLeft, btnRight, btnDown, btnStop,
                btnCut, btnProfile, btnPrint, btnReboot, btnQuit, btnEnter, btnCalib}) b.setEnabled(false);
    }
    private void log(String msg) { tvLog.setText(tvLog.getText() + "\n" + msg); }
}
