package com.ecgmonitor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLE_PERMISSIONS = 1;
    private TextView hrTextView, sdnnTextView, motionStateTextView, bleStatusTextView;
    private Button startButton, stopButton, saveAnomalyButton, historyButton, settingsButton;
    private BleManager bleManager;
    private DataProcessor dataProcessor;
    private AnomalyDetector anomalyDetector;
    private DataStorage dataStorage;
    private EcgChart ecgChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        checkPermissions();
        initComponents();
    }

    private void initUI() {
        hrTextView = findViewById(R.id.hr_text_view);
        sdnnTextView = findViewById(R.id.sdnn_text_view);
        motionStateTextView = findViewById(R.id.motion_state_text_view);
        bleStatusTextView = findViewById(R.id.ble_status_text_view);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        saveAnomalyButton = findViewById(R.id.save_anomaly_button);
        historyButton = findViewById(R.id.history_button);
        settingsButton = findViewById(R.id.settings_button);

        startButton.setOnClickListener(v -> startMonitoring());
        stopButton.setOnClickListener(v -> stopMonitoring());
        saveAnomalyButton.setOnClickListener(v -> saveTestAnomaly());
        historyButton.setOnClickListener(v -> openHistory());
        settingsButton.setOnClickListener(v -> openSettings());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLE_PERMISSIONS);
        }
    }

    private void initComponents() {
        bleManager = new BleManager(this);
        dataProcessor = new DataProcessor();
        anomalyDetector = new AnomalyDetector(this);
        dataStorage = new DataStorage(this);
        ecgChart = new EcgChart(this);

        bleManager.setDataListener(new BleManager.DataListener() {
            @Override
            public void onEcgData(int adcValue, long timestamp) {
                processEcgData(adcValue, timestamp);
            }

            @Override
            public void onAccData(float x, float y, float z, long timestamp) {
                processAccData(x, y, z, timestamp);
            }

            @Override
            public void onConnectionStatusChanged(boolean connected) {
                updateBleStatus(connected);
            }
        });

        dataProcessor.setDataListener(new DataProcessor.DataListener() {
            @Override
            public void onProcessedEcg(double voltage, long timestamp) {
                updateEcgChart(voltage, timestamp);
            }

            @Override
            public void onHeartRateCalculated(int hr) {
                updateHR(hr);
            }

            @Override
            public void onSdnnCalculated(double sdnn) {
                updateSDNN(sdnn);
            }

            @Override
            public void onMotionStateChanged(boolean isMotion) {
                updateMotionState(isMotion);
            }
        });

        anomalyDetector.setAnomalyListener(new AnomalyDetector.AnomalyListener() {
            @Override
            public void onAnomalyDetected(String reason, int hr, double sdnn, long timestamp) {
                handleAnomaly(reason, hr, sdnn, timestamp);
            }
        });
    }

    private void startMonitoring() {
        bleManager.startScan();
    }

    private void stopMonitoring() {
        bleManager.stopScan();
    }

    private void saveTestAnomaly() {
        anomalyDetector.saveTestAnomaly();
    }

    private void openHistory() {
        // 打开历史记录界面
    }

    private void openSettings() {
        // 打开设置界面
    }

    private void processEcgData(int adcValue, long timestamp) {
        dataProcessor.processEcgData(adcValue, timestamp);
        anomalyDetector.checkAnomaly(dataProcessor.getLatestHR(), dataProcessor.getLatestSDNN());
    }

    private void processAccData(float x, float y, float z, long timestamp) {
        dataProcessor.processAccData(x, y, z, timestamp);
    }

    private void updateBleStatus(boolean connected) {
        bleStatusTextView.setText(connected ? "已连接" : "未连接");
    }

    private void updateEcgChart(double voltage, long timestamp) {
        ecgChart.addDataPoint(voltage, timestamp);
    }

    private void updateHR(int hr) {
        hrTextView.setText(hr + " bpm");
    }

    private void updateSDNN(double sdnn) {
        sdnnTextView.setText(sdnn + " ms");
    }

    private void updateMotionState(boolean isMotion) {
        motionStateTextView.setText(isMotion ? "运动" : "静息");
    }

    private void handleAnomaly(String reason, int hr, double sdnn, long timestamp) {
        Toast.makeText(this, "异常: " + reason, Toast.LENGTH_LONG).show();
        // 闪烁背景
        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(0xFFFF0000);
        rootView.postDelayed(() -> rootView.setBackgroundColor(0xFFFAFAFA), 500);
        // 保存异常数据
        dataStorage.saveAnomalyRecord(reason, hr, sdnn, timestamp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.disconnect();
    }
}