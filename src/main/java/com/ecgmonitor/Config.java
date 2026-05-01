package com.ecgmonitor;

public class Config {
    // 心电硬件参数
    public static final int ECG_SAMPLE_RATE_HZ = 100;
    public static final double ADC_VREF = 3.3;
    public static final int ADC_RESOLUTION = 1024;
    
    // BLE 参数
    public static final String ECG_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String ECG_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String ACC_SERVICE_UUID = "0000ffa0-0000-1000-8000-00805f9b34fb";
    public static final String ACC_CHARACTERISTIC_UUID = "0000ffa1-0000-1000-8000-00805f9b34fb";
    
    // 运动检测阈值
    public static final double ACC_MOTION_THRESHOLD_G = 0.05;
    
    // 自适应滤波参数
    public static final int ADAPTIVE_FILTER_ORDER = 4;
    public static final double RLS_LAMBDA = 0.99;
    public static final double LMS_MU = 0.01;
    
    // R波检测参数 (Pan-Tompkins)
    public static final int INTEGRATION_WINDOW_MS = 150;
    public static final double QRS_THRESHOLD_FACTOR = 0.6;
    
    // 异常监测参数
    public static final int HR_BRADY_BPM = 60;
    public static final int HR_TACHY_BPM = 100;
    public static final int HR_ABNORMAL_DURATION_MS = 10000;
    public static final double SDNN_ABNORMAL_MS = 20.0;
    public static final boolean USE_COMBINED_RULE = true;
    public static final int COMBINED_HR_THRESHOLD = 100;
    public static final double COMBINED_SDNN_THRESHOLD = 30.0;
    
    // 数据缓存
    public static final int CACHE_DURATION_MINUTES = 10;
    public static final int ANOMALY_BUFFER_SECONDS = 30;
}