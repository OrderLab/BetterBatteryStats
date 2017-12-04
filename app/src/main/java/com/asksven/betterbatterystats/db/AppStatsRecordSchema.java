package com.asksven.betterbatterystats.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ryan on 11/20/14.
 */
public class AppStatsRecordSchema implements BaseColumns {

    public static final String AUTHORITY = DataProvider.AUTHORITY;

    public static final String TABLE_NAME = "stats";

    private static final String SCHEME = "content://";

    public static final int ID_PATH_POSITION = 1;

    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_NAME);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;

    public static final String DEFAULT_SORT_ORDER = "package DESC";


    public static final String COLUMN_TIME = "time";

    public static final String COLUMN_APP = "app";

    public static final String COLUMN_PACKAGE = "package";

    public static final String COLUMN_VERSION = "version";

    public static final String COLUMN_WAKELOCKTIME = "wakelockTime";

    public static final String COLUMN_WAKELOCKCOUNT = "wakelockCount";

    public static final String COLUMN_PROCESSSTARTS = "processStarts";

    public static final String COLUMN_PROCESSUSERTIME = "processUserTime";

    public static final String COLUMN_PROCESSSYSTIME = "processSysTime";

    public static final String COLUMN_BYTESRECEIVED = "bytesRecv";

    public static final String COLUMN_BYTESSENT = "bytesSent";

    public static final String COLUMN_ALARMWAKEUPS = "alarmWakeups";

    public static final String COLUMN_ALARMTIME = "alarmTime";

    public static final String COLUMN_ALARMTOTALCOUNT = "alarmTotalCount";

    public static final String COLUMN_GPSTIME = "gpsTime";

    public static final String COLUMN_SENSORTIME = "sensorTime";


    public static final String[] DEFAULT_ENTRY_PROJECTION = new String[]{
            AppStatsRecordSchema._ID,
            AppStatsRecordSchema.COLUMN_TIME,
            AppStatsRecordSchema.COLUMN_APP,
            AppStatsRecordSchema.COLUMN_PACKAGE,
            AppStatsRecordSchema.COLUMN_VERSION,
            AppStatsRecordSchema.COLUMN_WAKELOCKTIME,
            AppStatsRecordSchema.COLUMN_WAKELOCKCOUNT,
            AppStatsRecordSchema.COLUMN_PROCESSSTARTS,
            AppStatsRecordSchema.COLUMN_PROCESSUSERTIME,
            AppStatsRecordSchema.COLUMN_PROCESSSYSTIME,
            AppStatsRecordSchema.COLUMN_BYTESRECEIVED,
            AppStatsRecordSchema.COLUMN_BYTESSENT,
            AppStatsRecordSchema.COLUMN_ALARMWAKEUPS,
            AppStatsRecordSchema.COLUMN_ALARMTIME,
            AppStatsRecordSchema.COLUMN_ALARMTOTALCOUNT,
            AppStatsRecordSchema.COLUMN_GPSTIME,
            AppStatsRecordSchema.COLUMN_SENSORTIME,
    };
}

