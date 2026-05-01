package com.ecgmonitor;

import android.content.Context;
import androidx.room.Room;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class DataStorage {

    private AppDatabase database;
    private Gson gson;

    public DataStorage(Context context) {
        database = Room.databaseBuilder(context, AppDatabase.class, "ecg-database").build();
        gson = new Gson();
    }

    public void saveAnomalyRecord(String reason, int hr, double sdnn, long timestamp) {
        // 构建波形数据JSON
        JsonArray waveformArray = new JsonArray();
        // 这里应该从DataProcessor获取前后30秒的波形数据
        // 简化实现，实际应用中需要填充真实数据
        
        String waveformJson = waveformArray.toString();
        
        AbnormalRecord record = new AbnormalRecord(
                timestamp - 30000, // 开始时间：异常前30秒
                timestamp + 30000, // 结束时间：异常后30秒
                reason,
                hr,
                sdnn,
                waveformJson,
                "静息" // 简化实现，实际应用中需要获取真实运动状态
        );
        
        new Thread(() -> {
            database.abnormalRecordDao().insert(record);
        }).start();
    }

    public List<AbnormalRecord> getAllAbnormalRecords() {
        return database.abnormalRecordDao().getAll();
    }

    public void deleteAbnormalRecord(AbnormalRecord record) {
        new Thread(() -> {
            database.abnormalRecordDao().delete(record);
        }).start();
    }

    public void deleteAllAbnormalRecords() {
        new Thread(() -> {
            database.abnormalRecordDao().deleteAll();
        }).start();
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }
}