package com.dawn.libdiecutting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dawn.diecutting.LDieCuttingCallback;
import com.dawn.diecutting.LDieCuttingConfig;
import com.dawn.diecutting.LDieCuttingPrintSDK;
import com.dawn.diecutting.LDieCuttingStatus;

/**
 * 打印切割一体机演示（MainSDK）
 */
public class MainActivity extends AppCompatActivity {

    private LDieCuttingPrintSDK sdk;
    private TextView tvStatus, tvProgress, tvLog;

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

        Button b1 = findViewById(R.id.btn_connect);
        Button b2 = findViewById(R.id.btn_disconnect);
        Button b3 = findViewById(R.id.btn_start);
        Button b4 = findViewById(R.id.btn_stop);
        Button b5 = findViewById(R.id.btn_pause);
        Button b6 = findViewById(R.id.btn_resume);
        Button b7 = findViewById(R.id.btn_emergency);
        Button b8 = findViewById(R.id.btn_config);
        Button b9 = findViewById(R.id.btn_send_plt);

        b1.setText("初始化");
        b1.setOnClickListener(v -> {
            sdk.init(getApplicationContext(), API_KEY);
            sdk.setCallback(cb);
            log("初始化完成 (等待Status.OK)");
        });

        b2.setText("释放");
        b2.setOnClickListener(v -> { sdk.release(); log("已释放"); });

        b3.setText("普通切割");
        b3.setOnClickListener(v -> cut(R.drawable.test4, false));

        b4.setText("肖像切割");
        b4.setOnClickListener(v -> cut(R.drawable.profile, true));

        b5.setText("仅打印");
        b5.setOnClickListener(v -> { sdk.processPrintOnly(); log("仅打印"); });

        b6.setText("进纸");
        b6.setOnClickListener(v -> { sdk.paperIn(); log("进纸"); });

        b7.setText("退纸");
        b7.setOnClickListener(v -> { sdk.paperOut(); log("退纸"); });

        b8.setText("左移");
        b8.setOnClickListener(v -> { sdk.moveLeft(); log("左移刀座"); });

        b9.setText("右移");
        b9.setOnClickListener(v -> { sdk.moveRight(); log("右移刀座"); });
    }

    private void cut(int resId, boolean profile) {
        Bitmap bmp = LDieCuttingPrintSDK.loadBitmapForCut(this, resId);
        if (profile) {
            sdk.processProfileCut(bmp, "profile.png");
        } else {
            sdk.processCut(bmp, "test.png");
        }
        log((profile ? "肖像" : "普通") + "切割已发送");
    }

    private final LDieCuttingCallback cb = new LDieCuttingCallback() {
        @Override public void onStatusChanged(LDieCuttingStatus s) {
            runOnUiThread(() -> tvStatus.setText(s.getStateName()));
        }
        @Override public void onProgressUpdated(float p, int c, int t) {
            runOnUiThread(() -> tvProgress.setText((int)(p*100) + "%"));
        }
        @Override public void onError(int code, String msg) {
            runOnUiThread(() -> log("错误[" + code + "]: " + msg));
        }
    };

    private void log(String msg) {
        tvLog.setText(tvLog.getText() + "\n" + msg);
    }
}
