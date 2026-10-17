package com.audioscan.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

/** Servicio en primer plano: mantiene vivo el proceso y la CPU mientras se transcribe. */
public class AudioScanService extends Service {
    static final String CANAL = "audioscan_bg";
    static final int ID = 4101;
    private PowerManager.WakeLock wl;

    static void crearCanal(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CANAL, "AudioScan en segundo plano",
                    NotificationManager.IMPORTANCE_LOW);
            c.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    static Notification crear(Context c, String texto) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CANAL)
                .setContentTitle("AudioScan")
                .setContentText(texto)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        Intent abrir = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (abrir != null) {
            b.setContentIntent(PendingIntent.getActivity(c, 0, abrir,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        }
        return b.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String texto = (intent != null && intent.getStringExtra("texto") != null)
                ? intent.getStringExtra("texto") : "Trabajando…";
        crearCanal(this);
        Notification n = crear(this, texto);
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(ID, n);
        }
        if (wl == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioScan:trabajo");
            wl.acquire(6 * 60 * 60 * 1000L);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (wl != null && wl.isHeld()) wl.release();
        stopForeground(true);
        super.onDestroy();
    }
}
