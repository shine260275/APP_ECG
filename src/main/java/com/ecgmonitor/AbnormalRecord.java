package com.ecgmonitor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AbnormalRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long startTimestamp;
    public long endTimestamp;
    public String triggerReason;
    public int hrAtEvent;
    public double sdnnAtEvent;
    public String waveformData;
    public String motionState;

    public AbnormalRecord(long startTimestamp, long endTimestamp, String triggerReason, int hrAtEvent, double sdnnAtEvent, String waveformData, String motionState) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.triggerReason = triggerReason;
        this.hrAtEvent = hrAtEvent;
        this.sdnnAtEvent = sdnnAtEvent;
        this.waveformData = waveformData;
        this.motionState = motionState;
    }
}