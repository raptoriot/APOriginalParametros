package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class Depurador extends Application {

    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
                handleUncaughtException(thread, ex);
            }
        });
    }

    public void handleUncaughtException(Thread thread, Throwable e)
    {
        String stackTrace = Log.getStackTraceString(e);
        String message = e.getMessage();
        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"camilo.gonzalez@vesat.cl"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "Vesat Reporte de Error de App");
        intent.putExtra (Intent.EXTRA_TEXT,
                "DEVICE: " + Build.MODEL + " - " + Build.MANUFACTURER + " - " +
                Build.BRAND + " - " + Build.VERSION.CODENAME + " - " +
                        Build.VERSION.SDK_INT + " - " + Build.VERSION.RELEASE + "\n\n" +
                        "STACKTRACE: " + Base64.encodeToString(stackTrace.getBytes(),Base64.DEFAULT));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(1);
    }
}

