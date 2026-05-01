package com.ecgmonitor;

import android.content.Context;

public class AnomalyDetector {

    private Context context;
    private long abnormalHrStartTime = -1;
    private AnomalyListener anomalyListener;

    public interface AnomalyListener {
        void onAnomalyDetected(String reason, int hr, double sdnn, long timestamp);
    }

    public AnomalyDetector(Context context) {
        this.context = context;
    }

    public void setAnomalyListener(AnomalyListener listener) {
        this.anomalyListener = listener;
    }

    public void checkAnomaly(int hr, double sdnn) {
        long currentTime = System.currentTimeMillis();
        
        // 检查心率异常
        checkHeartRateAnomaly(hr, sdnn, currentTime);
        
        // 检查SDNN异常
        checkSdnnAnomaly(hr, sdnn, currentTime);
        
        // 检查组合规则异常
        checkCombinedRuleAnomaly(hr, sdnn, currentTime);
    }

    private void checkHeartRateAnomaly(int hr, double sdnn, long timestamp) {
        if (hr < Config.HR_BRADY_BPM || hr > Config.HR_TACHY_BPM) {
            if (abnormalHrStartTime == -1) {
                abnormalHrStartTime = timestamp;
            } else {
                long duration = timestamp - abnormalHrStartTime;
                if (duration >= Config.HR_ABNORMAL_DURATION_MS) {
                    String reason = hr < Config.HR_BRADY_BPM ? "心动过缓" : "心动过速";
                    if (anomalyListener != null) {
                        anomalyListener.onAnomalyDetected(reason, hr, sdnn, timestamp);
                    }
                    abnormalHrStartTime = -1;
                }
            }
        } else {
            abnormalHrStartTime = -1;
        }
    }

    private void checkSdnnAnomaly(int hr, double sdnn, long timestamp) {
        if (sdnn < Config.SDNN_ABNORMAL_MS) {
            String reason = "低SDNN";
            if (anomalyListener != null) {
                anomalyListener.onAnomalyDetected(reason, hr, sdnn, timestamp);
            }
        }
    }

    private void checkCombinedRuleAnomaly(int hr, double sdnn, long timestamp) {
        if (Config.USE_COMBINED_RULE && hr > Config.COMBINED_HR_THRESHOLD && sdnn < Config.COMBINED_SDNN_THRESHOLD) {
            String reason = "室速风险";
            if (anomalyListener != null) {
                anomalyListener.onAnomalyDetected(reason, hr, sdnn, timestamp);
            }
        }
    }

    public void saveTestAnomaly() {
        long timestamp = System.currentTimeMillis();
        if (anomalyListener != null) {
            anomalyListener.onAnomalyDetected("测试异常", 120, 15, timestamp);
        }
    }
}