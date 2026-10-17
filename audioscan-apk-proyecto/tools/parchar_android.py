#!/usr/bin/env python3
"""Ajustes al proyecto Android generado por Capacitor:
permisos, servicio en primer plano (segundo plano), plugin propio y memoria de Gradle."""
import re, pathlib, shutil

PKG_DIR = pathlib.Path("android/app/src/main/java/com/audioscan/app")
PKG_DIR.mkdir(parents=True, exist_ok=True)

# 1) Java: servicio + plugin + MainActivity que registra el plugin
for f in ("AudioScanService.java", "AudioScanBgPlugin.java"):
    shutil.copy(pathlib.Path("tools/android") / f, PKG_DIR / f)
(PKG_DIR / "MainActivity.java").write_text('''package com.audioscan.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AudioScanBgPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
''', encoding="utf-8")

# 2) Manifest
man = pathlib.Path("android/app/src/main/AndroidManifest.xml")
t = man.read_text(encoding="utf-8")
perms = [
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_DATA_SYNC",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.WAKE_LOCK",
    "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
]
for p in perms:
    if p not in t:
        t = t.replace("</manifest>", f'    <uses-permission android:name="{p}" />\n</manifest>')
if "requestLegacyExternalStorage" not in t:
    t = t.replace("<application", '<application android:requestLegacyExternalStorage="true"', 1)
if "AudioScanService" not in t:
    t = t.replace("</application>",
        '    <service android:name=".AudioScanService" android:exported="false"\n'
        '        android:foregroundServiceType="dataSync" />\n    </application>', 1)
man.write_text(t, encoding="utf-8")

# 3) Gradle: más memoria para empaquetar los modelos
gp = pathlib.Path("android/gradle.properties")
g = gp.read_text(encoding="utf-8")
g = re.sub(r"^org\.gradle\.jvmargs=.*$", "", g, flags=re.M)
g += "\norg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8\n"
gp.write_text(g, encoding="utf-8")
print("Android parcheado")
