package com.asksven.betterbatterystats;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.asksven.android.common.privateapiproxies.BatteryStatsProxy;

public class AppStatsCollectionService extends Service {
    public static final String TAG = "AppStatsCollectService";
    public final static String ACTION_PREFIX = "edu.jhu.order.appstatstracker.action";
    public final static String ACTION_COLLECT_STATS = ACTION_PREFIX + ".COLLECT_STATS";
    public final static String ACTION_COLLECT_STATS_START = ACTION_PREFIX + ".COLLECT_STATS_START";
    public final static String ACTION_COLLECT_STATS_FINISHED = ACTION_PREFIX + ".COLLECT_STATS_FINISHED";
    public final static String ACTION_COLLECT_STATS_STOP = ACTION_PREFIX + ".COLLECT_STATS_STOP";
    public final static String COLLECT_RESULT_KEY = "result";

    private boolean mHasBatteryStats = true;
    private BatteryStatsProxy mStatsProxy = null;
    private final static int MSG_COLLECT_STATS = 1;
    private final static int MILLISECONDS_PER_SECOND = 1000;
    private final static int SECONDS_PER_MINUTE = 60;
    private final static long SAMPLE_RATE = 1 * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;

    @Override
    public void onCreate() {
        Log.d(TAG, "Starting service...");
        mHasBatteryStats = (PackageManager.PERMISSION_GRANTED ==
                getPackageManager().checkPermission("android.permission.BATTERY_STATS",
                        getPackageName()));
        if (mHasBatteryStats) {
            mStatsProxy = BatteryStatsProxy.getInstance(this);
            Toast.makeText(this, "Successfully get BatteryStatsProxy!", Toast.LENGTH_LONG);
        }
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(ACTION_COLLECT_STATS);
        registerReceiver(mActionReceiver, ifilter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stoping service...");
        unregisterReceiver(mActionReceiver);
        cancelAlarm();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        schedulAlarm();
        return START_STICKY;
    }

    private Runnable mCollectStats = new Runnable() {
        @Override
        public void run() {
            if (mStatsProxy != null) {
                Log.d(TAG, "Starting collect app stats...");
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                wakelock.acquire();
                Intent startIntent = new Intent(ACTION_COLLECT_STATS_START);
                sendBroadcast(startIntent);
                AppStatsCollector.CollectResult result = AppStatsCollector.collectStats(AppStatsCollectionService.this);
                Intent finishIntent = new Intent(ACTION_COLLECT_STATS_FINISHED);
                finishIntent.putExtra(COLLECT_RESULT_KEY, result);
                sendBroadcast(finishIntent);
                wakelock.release();
                Log.d(TAG, "App stats collection finished.");
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void schedulAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_COLLECT_STATS), 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                SAMPLE_RATE, operation);
    }

    private void cancelAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_COLLECT_STATS), 0);
        am.cancel(operation);
    }

    private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr.equalsIgnoreCase(ACTION_COLLECT_STATS)) {
                mHandler.sendEmptyMessage(MSG_COLLECT_STATS);
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COLLECT_STATS:
                    Thread t = new Thread(mCollectStats);
                    t.start();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };
}
