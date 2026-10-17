package com.audioscan.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AudioScanBg")
public class AudioScanBgPlugin extends Plugin {
    private final Handler h = new Handler(Looper.getMainLooper());
    private boolean activo = false;

    /** Evita que el WebView se pause al pasar la app a segundo plano. */
    private final Runnable mantener = new Runnable() {
        @Override
        public void run() {
            if (!activo) return;
            try {
                WebView wv = getBridge().getWebView();
                wv.onResume();
                wv.resumeTimers();
                try {
                    java.lang.reflect.Method m = View.class.getDeclaredMethod("dispatchWindowVisibilityChanged", int.class);
                    m.setAccessible(true);
                    m.invoke(wv, View.VISIBLE);
                } catch (Throwable ignored) { }
            } catch (Throwable ignored) { }
            h.postDelayed(this, 2000);
        }
    };

    @PluginMethod
    public void iniciar(PluginCall call) {
        Context c = getContext();
        Intent i = new Intent(c, AudioScanService.class);
        i.putExtra("texto", call.getString("texto", "Trabajando…"));
        ContextCompat.startForegroundService(c, i);
        if (!activo) {
            activo = true;
            h.postDelayed(mantener, 1000);
        }
        call.resolve();
    }

    @PluginMethod
    public void actualizar(PluginCall call) {
        Context c = getContext();
        AudioScanService.crearCanal(c);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(AudioScanService.ID, AudioScanService.crear(c, call.getString("texto", "Trabajando…")));
        call.resolve();
    }

    @PluginMethod
    public void detener(PluginCall call) {
        activo = false;
        h.removeCallbacks(mantener);
        Context c = getContext();
        c.stopService(new Intent(c, AudioScanService.class));
        call.resolve();
    }

    @PluginMethod
    public void notificar(PluginCall call) {
        Context c = getContext();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("audioscan_fin", "AudioScan terminado",
                    NotificationManager.IMPORTANCE_DEFAULT);
            c.getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, "audioscan_fin")
                .setContentTitle(call.getString("titulo", "AudioScan"))
                .setContentText(call.getString("texto", ""))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true);
        try {
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(4102, b.build());
        } catch (Throwable ignored) { }
        call.resolve();
    }

    @PluginMethod
    public void pedirNotificaciones(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 33 && getActivity() != null
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 4102);
        }
        call.resolve();
    }

    @PluginMethod
    public void ignorarBateria(PluginCall call) {
        try {
            Context c = getContext();
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 23 && !pm.isIgnoringBatteryOptimizations(c.getPackageName())) {
                Intent in = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + c.getPackageName()));
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(in);
            }
        } catch (Throwable ignored) { }
        call.resolve();
    }
}
