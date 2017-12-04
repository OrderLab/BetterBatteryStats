package com.asksven.betterbatterystats;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.asksven.android.common.privateapiproxies.*;
import com.asksven.android.common.privateapiproxies.AppStatsReader;
import com.asksven.android.common.privateapiproxies.BatteryStatsProxy;
import com.asksven.android.common.privateapiproxies.BatteryStatsTypes;
import com.asksven.betterbatterystats.db.AppStatsRecord;
import com.asksven.betterbatterystats.db.AppStatsRecordSchema;

/**
 * Created by ryan on 11/20/14.
 */
public class AppStatsCollector {

    public static final String TAG = "AppStatsCollector";

    public final static class CollectResult implements Serializable {
        public boolean success;
        public long timeStamp;
        public long records;

        private static SimpleDateFormat SDF = new
                SimpleDateFormat("HH:mm");

        public CollectResult(long time) {
            success = false;
            timeStamp = time;
            records = 0;
        }

        @Override
        public String toString() {
            String timeStr = SDF.format(new Date(timeStamp));
            if (!success)
                return "Collection failed at " + timeStr;
            else
                return "Collected " + records + " records at " + timeStr;
        }
    }

    public static CollectResult collectStats(Context context) {
        BatteryStatsProxy proxy = BatteryStatsProxy.getInstance(context);
        long timeStamp = System.currentTimeMillis();
        CollectResult result = new CollectResult(timeStamp);
        try {
            AppStatsReader reader = new AppStatsReader(proxy);
            List<AppStatsRecord> records = reader.readAllAppStats(context,
                    BatteryStatsTypes.STATS_SINCE_CHARGED);
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            writeRecords(context, records, timestamp);
            result.records = records.size();
            result.success = true;
        } catch (Exception e) {
            result.success = false;
            Log.e(TAG, "Exception when collecting stats: " + e);
        }
        return result;
    }

    public static void writeRecords(Context context, List<AppStatsRecord> records, Timestamp timestamp) {
        for (AppStatsRecord record : records) {
            ContentValues values = new ContentValues();
            values.put(AppStatsRecordSchema.COLUMN_TIME, String.valueOf(timestamp));
            values.put(AppStatsRecordSchema.COLUMN_APP, record.uidInfo.getName());
            values.put(AppStatsRecordSchema.COLUMN_PACKAGE, record.uidInfo.getNamePackage());
            values.put(AppStatsRecordSchema.COLUMN_VERSION, record.uidInfo.getVersion());
            values.put(AppStatsRecordSchema.COLUMN_WAKELOCKTIME, record.wakelockTime);
            values.put(AppStatsRecordSchema.COLUMN_WAKELOCKCOUNT, record.wakelockCount);
            values.put(AppStatsRecordSchema.COLUMN_PROCESSSTARTS, record.processStarts);
            values.put(AppStatsRecordSchema.COLUMN_PROCESSUSERTIME, record.processUserTime);
            values.put(AppStatsRecordSchema.COLUMN_PROCESSSYSTIME, record.processSysTime);
            values.put(AppStatsRecordSchema.COLUMN_BYTESRECEIVED, record.bytesReceived);
            values.put(AppStatsRecordSchema.COLUMN_BYTESSENT, record.bytesSent);
            values.put(AppStatsRecordSchema.COLUMN_ALARMWAKEUPS, record.alarmWakeups);
            values.put(AppStatsRecordSchema.COLUMN_ALARMTIME, record.alarmRunningTime);
            values.put(AppStatsRecordSchema.COLUMN_ALARMTOTALCOUNT, record.alarmTotalCount);
            values.put(AppStatsRecordSchema.COLUMN_GPSTIME, record.gpsTime);
            values.put(AppStatsRecordSchema.COLUMN_SENSORTIME, record.sensorTime);
            context.getContentResolver().insert(AppStatsRecordSchema.CONTENT_URI, values);
        }
    }

    public static int deletedatabase(Context context, String selection, String[] selectionArgs) {
        int delrow = context.getContentResolver().delete(AppStatsRecordSchema.CONTENT_URI, selection, selectionArgs);
        Log.d(TAG, "Deleted rows are: " + delrow);
        return delrow;
    }

}
