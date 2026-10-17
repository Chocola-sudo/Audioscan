# AudioScan (APK)

App Android (Capacitor) con tu HTML. La librería y el modelo Whisper van **dentro** de la app:
no hay caché de navegador que se borre y no se necesita Termux ni servidor.

## Compilar el APK (una vez, con GitHub)
1. En github.com crea un repositorio nuevo (privado está bien).
2. Sube TODO el contenido de esta carpeta, incluida `.github` (con git o arrastrando la carpeta en la web).
3. Ve a **Actions** -> **Compilar APK** -> **Run workflow**.
   (También se compila solo cada vez que subes cambios a `main`.)
4. Espera ~8-15 min. Entra a la ejecución terminada y descarga **AudioScan-apk** (zip con `app-debug.apk`).
5. Pasa el APK al teléfono, ábrelo y permite "instalar apps desconocidas".

## Modelo
- Por defecto el APK incluye **small** (el más preciso, ~250 MB; el APK pesará ~300 MB).
- Para añadir más: Actions -> Run workflow -> escribe `tiny base small`.
- En la app, el selector de modelo marca cuáles vienen "incluidos".

## Segundo plano
Al pulsar **Ejecutar**, la app inicia un servicio en primer plano (notificación "AudioScan") que mantiene
la CPU y el proceso activos aunque apagues la pantalla o cambies de app. La notificación muestra el avance
y, al terminar, llega otra con "Transcripción terminada".
- La primera vez te pedirá permiso de notificaciones y que ignores la optimización de batería. Acepta ambos.
- En Xiaomi/Huawei/Samsung agresivos con la batería, pon también AudioScan en "Sin restricciones" y fija la app en recientes.
- Si el sistema aun así la cierra, al volver a transcribir el mismo audio te ofrece **continuar** desde el último bloque guardado.

## Dónde guarda
- `.md` -> `Documentos/AudioScan/nombre-del-audio.md`
- Sesiones y avances (para reanudar) -> datos internos de la app (no se pierden con el caché; solo si desinstalas o borras los datos de la app).

## Notas
- APK "debug" (clave de depuración): sirve para instalar en tu propio teléfono.
- small necesita bastante RAM; si la app se cierra al cargarlo, compila con `base`.
