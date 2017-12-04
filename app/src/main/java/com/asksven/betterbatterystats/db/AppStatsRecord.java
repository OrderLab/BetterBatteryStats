package com.asksven.betterbatterystats.db;

import com.asksven.android.common.nameutils.UidInfo;

/**
 * Created by ryan on 11/20/14.
 */
public class AppStatsRecord {
    public UidInfo uidInfo;

    public int wakelockCount;
    public long wakelockTime;

    public int processStarts;
    public long processUserTime;
    public long processSysTime;

    public long bytesSent;
    public long bytesReceived;

    public long alarmWakeups;
    public long alarmRunningTime;
    public long alarmTotalCount;

    public long gpsTime;
    public long sensorTime;


    public AppStatsRecord() {
    }
}
