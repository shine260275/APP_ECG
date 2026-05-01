package com.ecgmonitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataProcessor {

    private static final int CACHE_SIZE = Config.CACHE_DURATION_MINUTES * 60 * Config.ECG_SAMPLE_RATE_HZ;
    private LinkedList<EcgDataPoint> ecgDataCache;
    private LinkedList<AccDataPoint> accDataCache;
    private LinkedList<Double> rrIntervals;
    private double[] filterCoefficients;
    private boolean isMotion = false;
    private int latestHR = 0;
    private double latestSDNN = 0;
    private DataListener dataListener;

    public interface DataListener {
        void onProcessedEcg(double voltage, long timestamp);
        void onHeartRateCalculated(int hr);
        void onSdnnCalculated(double sdnn);
        void onMotionStateChanged(boolean isMotion);
    }

    public DataProcessor() {
        ecgDataCache = new LinkedList<>();
        accDataCache = new LinkedList<>();
        rrIntervals = new LinkedList<>();
        filterCoefficients = new double[Config.ADAPTIVE_FILTER_ORDER];
    }

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    public void processEcgData(int adcValue, long timestamp) {
        // 转换ADC值为电压
        double voltage = adcValue * (Config.ADC_VREF / Config.ADC_RESOLUTION);
        
        // 应用自适应滤波
        double filteredVoltage = applyAdaptiveFilter(voltage, timestamp);
        
        // 检测R波
        detectRWave(filteredVoltage, timestamp);
        
        // 更新缓存
        updateEcgCache(filteredVoltage, timestamp);
        
        // 计算心率
        calculateHeartRate();
        
        // 计算SDNN
        calculateSDNN();
        
        if (dataListener != null) {
            dataListener.onProcessedEcg(filteredVoltage, timestamp);
        }
    }

    public void processAccData(float x, float y, float z, long timestamp) {
        // 计算加速度幅值
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        
        // 判断运动状态
        boolean newMotionState = magnitude > Config.ACC_MOTION_THRESHOLD_G;
        if (newMotionState != isMotion) {
            isMotion = newMotionState;
            if (dataListener != null) {
                dataListener.onMotionStateChanged(isMotion);
            }
        }
        
        // 更新加速度缓存
        updateAccCache(x, y, z, timestamp);
    }

    private double applyAdaptiveFilter(double ecgValue, long timestamp) {
        // 简化的自适应滤波实现
        // 实际应用中应使用RLS或LMS算法
        return ecgValue;
    }

    private void detectRWave(double voltage, long timestamp) {
        // 简化的R波检测
        // 实际应用中应使用Pan-Tompkins算法
    }

    private void updateEcgCache(double voltage, long timestamp) {
        ecgDataCache.add(new EcgDataPoint(voltage, timestamp));
        if (ecgDataCache.size() > CACHE_SIZE) {
            ecgDataCache.removeFirst();
        }
    }

    private void updateAccCache(float x, float y, float z, long timestamp) {
        accDataCache.add(new AccDataPoint(x, y, z, timestamp));
        if (accDataCache.size() > CACHE_SIZE) {
            accDataCache.removeFirst();
        }
    }

    private void calculateHeartRate() {
        if (rrIntervals.size() >= 5) {
            double sum = 0;
            for (double rr : rrIntervals.subList(rrIntervals.size() - 5, rrIntervals.size())) {
                sum += rr;
            }
            double meanRR = sum / 5;
            latestHR = (int) (60000 / meanRR);
            if (dataListener != null) {
                dataListener.onHeartRateCalculated(latestHR);
            }
        }
    }

    private void calculateSDNN() {
        if (rrIntervals.size() >= 300) { // 5分钟 @ 100Hz
            double sum = 0;
            double mean = 0;
            for (double rr : rrIntervals) {
                sum += rr;
            }
            mean = sum / rrIntervals.size();
            
            double variance = 0;
            for (double rr : rrIntervals) {
                variance += Math.pow(rr - mean, 2);
            }
            variance /= rrIntervals.size();
            latestSDNN = Math.sqrt(variance);
            
            if (dataListener != null) {
                dataListener.onSdnnCalculated(latestSDNN);
            }
        }
    }

    public int getLatestHR() {
        return latestHR;
    }

    public double getLatestSDNN() {
        return latestSDNN;
    }

    public List<EcgDataPoint> getEcgDataWindow(long timestamp, int seconds) {
        List<EcgDataPoint> window = new ArrayList<>();
        long startTime = timestamp - seconds * 1000;
        long endTime = timestamp + seconds * 1000;
        
        for (EcgDataPoint point : ecgDataCache) {
            if (point.timestamp >= startTime && point.timestamp <= endTime) {
                window.add(point);
            }
        }
        return window;
    }

    private static class EcgDataPoint {
        double voltage;
        long timestamp;
        
        EcgDataPoint(double voltage, long timestamp) {
            this.voltage = voltage;
            this.timestamp = timestamp;
        }
    }

    private static class AccDataPoint {
        float x, y, z;
        long timestamp;
        
        AccDataPoint(float x, float y, float z, long timestamp) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
        }
    }
}