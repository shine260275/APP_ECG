package com.ecgmonitor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AbnormalRecordDao {
    @Insert
    void insert(AbnormalRecord record);

    @Query("SELECT * FROM AbnormalRecord ORDER BY startTimestamp DESC")
    List<AbnormalRecord> getAll();

    @Delete
    void delete(AbnormalRecord record);

    @Query("DELETE FROM AbnormalRecord")
    void deleteAll();
}