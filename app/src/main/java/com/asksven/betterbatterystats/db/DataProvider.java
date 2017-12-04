package com.asksven.betterbatterystats.db;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by ryan on 11/20/14.
 */
public class DataProvider extends ContentProvider {

    public static final String TAG = "DataProvider";

    public static final String AUTHORITY = "edu.jhu.order.appstatstracker.provider";

    public static final String DBNAME = "appstats.db";
    private static final int DATABASE_VERSION = 1;

    private static final int APPSTATS = 1;
    private static final int APPSTATS_ID = 2;

    private SQLiteDatabase db;

    private OpenDatabaseHelper mOpenDBHelper;
    private static final UriMatcher sUriMatcher;
    private static final HashMap<String, String> sAppStatsProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, AppStatsRecordSchema.TABLE_NAME, APPSTATS);
        sUriMatcher.addURI(AUTHORITY, AppStatsRecordSchema.TABLE_NAME + "/#", APPSTATS_ID);
        sAppStatsProjectionMap = new HashMap<String, String>();
        for (String str : AppStatsRecordSchema.DEFAULT_ENTRY_PROJECTION) {
            sAppStatsProjectionMap.put(str, str);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenDBHelper = new OpenDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qbuilder = new SQLiteQueryBuilder();
        int matchCode = sUriMatcher.match(uri);
        String orderBy = sortOrder;
        switch (matchCode) {
            case APPSTATS:
                qbuilder.setTables(AppStatsRecordSchema.TABLE_NAME);
                qbuilder.setProjectionMap(sAppStatsProjectionMap);
                if (TextUtils.isEmpty(orderBy))
                    orderBy = AppStatsRecordSchema.DEFAULT_SORT_ORDER;
                break;
            case APPSTATS_ID:
                qbuilder.setTables(AppStatsRecordSchema.TABLE_NAME);
                qbuilder.setProjectionMap(sAppStatsProjectionMap);
                qbuilder.appendWhere("_id = " +
                        uri.getPathSegments().get(AppStatsRecordSchema.ID_PATH_POSITION));
                if (TextUtils.isEmpty(orderBy))
                    orderBy = AppStatsRecordSchema.DEFAULT_SORT_ORDER;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String lastSeg = uri.getLastPathSegment();
        boolean distinct = false;
        if (lastSeg.equalsIgnoreCase("distinct")) {
            uri = removeLastPathSegment(uri);
            distinct = true;
            Log.d(TAG, "Request distinct query for " + uri.getPath());
        }
        qbuilder.setDistinct(distinct);

        SQLiteDatabase db = mOpenDBHelper.getReadableDatabase();

        Cursor cur = qbuilder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        cur.setNotificationUri(getContext().getContentResolver(), uri);

        return cur;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case APPSTATS:
                return AppStatsRecordSchema.CONTENT_TYPE;
            case APPSTATS_ID:
                return AppStatsRecordSchema.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ContentValues initValues = values;
        if (initValues == null)
            initValues = new ContentValues();
        SQLiteDatabase db = mOpenDBHelper.getWritableDatabase();
        int matchCode = sUriMatcher.match(uri);
        long rowID;
        switch (matchCode) {
            case APPSTATS:
                setDefaultStatsContent(initValues);
                rowID = db.insert(AppStatsRecordSchema.TABLE_NAME, AppStatsRecordSchema.COLUMN_APP, initValues);
                if (rowID > 0) {
                    Uri newUri = ContentUris.withAppendedId(AppStatsRecordSchema.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    public void setDefaultStatsContent(ContentValues values) {
        if (!values.containsKey(AppStatsRecordSchema.COLUMN_TIME))
            values.put(AppStatsRecordSchema.COLUMN_TIME, System.currentTimeMillis());

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenDBHelper.getWritableDatabase();
        int matchCode = sUriMatcher.match(uri);
        String where;
        int count;
        switch (matchCode) {
            case APPSTATS:
                count = db.delete(AppStatsRecordSchema.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case APPSTATS_ID:
                where = AppStatsRecordSchema._ID + " = " + uri.getPathSegments()
                        .get(AppStatsRecordSchema.ID_PATH_POSITION);
                if (selection != null)
                    where = where + " AND " + selection;
                count = db.delete(AppStatsRecordSchema.TABLE_NAME,
                        where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int matchCode = sUriMatcher.match(uri);
        int count;
        String where;
        switch (matchCode) {
            case APPSTATS:
                count = db.update(AppStatsRecordSchema.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case APPSTATS_ID:
                where = AppStatsRecordSchema._ID + " = " + uri.getPathSegments()
                        .get(AppStatsRecordSchema.ID_PATH_POSITION);
                if (selection != null)
                    where = where + " AND " + selection;
                count = db.update(AppStatsRecordSchema.TABLE_NAME,
                        values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Remove the last path segment in the uri. This is useful if the
     * last segment is used for additional request such as distinct.
     *
     * @param uri
     * @return
     */
    private Uri removeLastPathSegment(Uri uri) {
        List<String> segments = new Vector<String>(uri.getPathSegments());
        segments.remove(segments.size() - 1);
        String newPath = TextUtils.join("/", segments);
        return uri.buildUpon().path(newPath).build();
    }

    private static final String SQL_CREATE_APPSTATS = "CREATE TABLE " + AppStatsRecordSchema.TABLE_NAME + " ("
            + AppStatsRecordSchema._ID + " INTEGER PRIMARY KEY,"
            + AppStatsRecordSchema.COLUMN_TIME + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_APP + " TEXT,"
            + AppStatsRecordSchema.COLUMN_PACKAGE + " TEXT,"
            + AppStatsRecordSchema.COLUMN_VERSION + " TEXT,"
            + AppStatsRecordSchema.COLUMN_WAKELOCKTIME + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_WAKELOCKCOUNT + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_PROCESSSTARTS + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_PROCESSSYSTIME + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_PROCESSUSERTIME + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_BYTESRECEIVED + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_BYTESSENT + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_ALARMWAKEUPS + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_ALARMTIME + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_ALARMTOTALCOUNT + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_GPSTIME + " INTEGER,"
            + AppStatsRecordSchema.COLUMN_SENSORTIME + " INTEGER"
            + ");";

    protected static final class OpenDatabaseHelper extends SQLiteOpenHelper {

        OpenDatabaseHelper(Context context) {
            super(context, DBNAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_APPSTATS);
            Log.d(TAG, "AppStats table created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + AppStatsRecordSchema.TABLE_NAME);
            onCreate(db);
            Log.d(TAG, "AppStats table upgraded");
        }
    }
}
