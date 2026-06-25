package com.dawn.libdiecutting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
            btnCut, btnProfile, btnPrint, btnReboot, btnQuit, btnEnter, btnCalib,
            btnFrameCut;

    private static final String API_KEY = "";
    private static final int REQ_PICK_IMAGE = 1001;

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

        // ==== 相框裁剪按钮 ====
        btnFrameCut = findViewById(R.id.btn_frame_cut);
        btnFrameCut.setOnClickListener(v -> openImagePicker());

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
                btnCut, btnProfile, btnPrint, btnReboot, btnQuit, btnEnter, btnCalib, btnFrameCut}) b.setEnabled(true);
    }
    private void disableAll() {
        for (Button b : new Button[]{btnFw, btnRelease, btnLeft, btnRight, btnDown, btnStop,
                btnCut, btnProfile, btnPrint, btnReboot, btnQuit, btnEnter, btnCalib, btnFrameCut}) b.setEnabled(false);
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
                Bitmap result = LDieCuttingPrintSDK.invertAlpha(src);
                src.recycle();

                // 缩放到 1200x1800
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

    private void logBg(String msg) { runOnUiThread(() -> log(msg)); }

    // ==================== 相框裁剪：SD卡图片 → 图片处理 → 切割 ====================

    /** 打开系统文件选择器，选取 SD 卡上的图片 */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 持久化读取权限（重启后仍可访问）
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                processAndCutImageFromUri(uri);
            }
        }
    }

    /**
     * 从 Uri 加载 SD 卡图片 → invertAlpha → 缩放 1200x1800 → 传给设备切割
     * 演示用（Content URI），生产环境直接用 sdk.frameCut(filePath) 一步完成
     */
    private void processAndCutImageFromUri(Uri uri) {
        new Thread(() -> {
            try {
                String fileName = "frame_cut.png";
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIdx >= 0 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIdx);
                    }
                    cursor.close();
                }

                // ① 从 SD 卡加载图片
                Bitmap src = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                if (src == null) { logBg("❌ 无法加载图片: " + fileName); return; }
                logBg("📷 " + fileName + ": " + src.getWidth() + "x" + src.getHeight());

                // ② 图片预处理（invertAlpha + 缩放到 1200x1800）
                Bitmap result = LDieCuttingPrintSDK.invertAlpha(src);
                src.recycle();
                Bitmap processed = Bitmap.createScaledBitmap(result, 1200, 1800, true);
                result.recycle();
                logBg("处理后: " + processed.getWidth() + "x" + processed.getHeight());

                // ③ 传给设备进行切割
                logBg("🔪 开始相框切割...");
                sdk.cut(processed, "frame_cut.png");
                // processed 由 CutSDK 内部管理，不要在这里 recycle

            } catch (Exception e) {
                logBg("❌ " + e.getMessage());
            }
        }).start();
    }
}
