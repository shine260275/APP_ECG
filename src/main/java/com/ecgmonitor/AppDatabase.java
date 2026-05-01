package com.ecgmonitor;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AbnormalRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AbnormalRecordDao abnormalRecordDao();
}