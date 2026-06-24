package com.dawn.libdiecutting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dawn.diecutting.LDieCuttingCallback;
import com.dawn.diecutting.LDieCuttingConst;
import com.dawn.diecutting.LDieCuttingPrintSDK;
import com.dawn.diecutting.LDieCuttingStatus;

import java.io.File;
import java.io.FileOutputStream;

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

        // ==== 图片处理按钮 ====
        Button btnProcessImg = findViewById(R.id.btn_process_img);
        btnProcessImg.setOnClickListener(v -> processImage());

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

    // ==================== 图片处理 ====================

    private void processImage() {
        new Thread(() -> {
            try {
                Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.frame);
                if (src == null) { logBg("❌ 无法加载 frame.png"); return; }
                logBg("frame.png: " + src.getWidth() + "x" + src.getHeight());

                // 透明→黑色, 不透明→透明
                Bitmap result = invertAlpha(src);
                src.recycle();

                // 缩放到 1800x1200
                Bitmap scaled = Bitmap.createScaledBitmap(result, 1200, 1800, true);
                result.recycle();
                logBg("缩放: " + scaled.getWidth() + "x" + scaled.getHeight());

                // 保存到应用私有目录（无需权限）
                File dir = getExternalFilesDir("diecutting");
                if (dir != null && !dir.exists()) dir.mkdirs();
                File out = new File(dir, "finish.png");
                FileOutputStream fos = new FileOutputStream(out);
                scaled.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                scaled.recycle();

                logBg("✅ " + out.getAbsolutePath());
            } catch (Exception e) {
                logBg("❌ " + e.getMessage());
            }
        }).start();
    }

    private Bitmap invertAlpha(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] >> 24) & 0xFF;
            pixels[i] = (alpha == 0) ? 0xFF000000 : 0x00000000;
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    private void logBg(String msg) { runOnUiThread(() -> log(msg)); }
}
