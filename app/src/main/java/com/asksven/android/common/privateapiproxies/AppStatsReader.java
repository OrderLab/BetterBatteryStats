package com.asksven.android.common.privateapiproxies;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.asksven.betterbatterystats.db.AppStatsRecord;
import com.asksven.android.common.nameutils.UidInfo;

/**
 * Created by ryan on 11/20/14.
 */
public class AppStatsReader {
    public static final String TAG = "AppStatsReader";
    private BatteryStatsProxy mProxy;


    public AppStatsReader(BatteryStatsProxy proxy) {
        mProxy = proxy;
    }

    public List<AppStatsRecord> readAllAppStats(Context context, int statType) {
        if (mProxy == null)
            return null;
        HashMap<String, AppStatsRecord> recordMap = new HashMap<String,
                AppStatsRecord>();
        try {
            long elapsedTime = mProxy.computeBatteryRealtime(
                    SystemClock.elapsedRealtime() * 1000,
                    statType);
            Log.d(TAG, "ElapsedTime: " + elapsedTime);

            ArrayList<StatElement> stats = mProxy.getAllAppStats(context,
                    BatteryStatsTypes.WAKE_TYPE_PARTIAL, statType, 0);
            for (StatElement stat : stats) {
                if (stat instanceof WakelockStat) {
                    WakelockStat wstat = (WakelockStat) stat;
                    String key = GetUidKey(wstat.getUidInfo());
                    AppStatsRecord record = recordMap.get(key);
                    if (record == null) {
                        record = new AppStatsRecord();
                        record.uidInfo = wstat.getUidInfo();
                        recordMap.put(key, record);
                    }
                    record.wakelockCount += wstat.getCount();
                    record.wakelockTime += wstat.getDuration();
                    Log.d(TAG, wstat.toString());
                    Log.d(TAG, "APP [app name:" + record.uidInfo.getName() + ", app wakelock count:" + record.wakelockCount + ", app wakelock time:" + record.wakelockTime + "]");

                } else if (stat instanceof ProcessStat) {
                    ProcessStat pstat = (ProcessStat) stat;
                    String key = GetUidKey(pstat.getUidInfo());
                    AppStatsRecord record = recordMap.get(key);
                    if (record == null) {
                        record = new AppStatsRecord();
                        record.uidInfo = pstat.getUidInfo();
                        recordMap.put(key, record);
                    }
                    record.processStarts = pstat.getStarts();
                    record.processSysTime = pstat.getSystemTime();
                    record.processUserTime = pstat.getUserTime();
                } else if (stat instanceof NetworkUsage) {
                    NetworkUsage netstat = (NetworkUsage) stat;
                    String key = GetUidKey(netstat.getUidInfo());
                    AppStatsRecord record = recordMap.get(key);
                    if (record == null) {
                        record = new AppStatsRecord();
                        record.uidInfo = netstat.getUidInfo();
                        recordMap.put(key, record);
                    }
                    record.bytesReceived = netstat.getBytesReceived();
                    record.bytesSent = netstat.getBytesSent();
                } else if (stat instanceof AlarmStat) {
                    AlarmStat astat = (AlarmStat) stat;
                    String key = GetUidKey(astat.getUidInfo());
                    AppStatsRecord record = recordMap.get(key);
                    if (record == null) {
                        record = new AppStatsRecord();
                        record.uidInfo = astat.getUidInfo();
                        recordMap.put(key, record);
                    }
                    record.alarmRunningTime = astat.getTimeRunning();
                    record.alarmTotalCount = astat.getCount();
                    record.alarmWakeups = astat.getWakeups();
                } else {
                    Log.e(TAG, "Unknown stat: " + stat);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return new ArrayList<AppStatsRecord>(recordMap.values());
    }

    private static String GetUidKey(UidInfo uidInfo) {
        if (uidInfo == null)
            return "unknown";
        String pkg = uidInfo.getNamePackage();
        if (pkg == null)
            pkg = String.valueOf(uidInfo.getUid());
        return pkg;
    }
}
