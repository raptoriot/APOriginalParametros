package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj.WidthHeight;

public class Util {

    @SuppressLint("ApplySharedPref")
    public static void saveToSP(Context context, Serializable object, String tag)
    {
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences("utilserializables"
                , Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        prefsEditor.putString("UTLS_" + tag, json);
        prefsEditor.commit();
    }

    public static Serializable loadFromSP(Context context, Class type, String tag)
    {
        Gson gson = new Gson();
        SharedPreferences prefs = context.getSharedPreferences("utilserializables"
                , Context.MODE_PRIVATE);
        String json = prefs.getString("UTLS_" + tag, null);
        if(json == null)
        {
            return null;
        }
        else {
            return (Serializable) gson.fromJson(json, type);
        }
    }

    public static String getFechaActual() {
        Date ahora = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
        return formateador.format(ahora);
    }

    public static String getHoraActual() {
        Date ahora = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formateador = new SimpleDateFormat("HH:mm:ss");
        return formateador.format(ahora);
    }

    public static String getFechaFullActual() {
        Date ahora = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formateador.format(ahora);
    }

    public static String getFechaCustom(String format){
        String formato = format;
        Date momento = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formateador = new SimpleDateFormat(formato);
        return formateador.format(momento);
    }

    public static String getSha512(String toHash) {
        MessageDigest md;
        byte[] hash = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            hash = md.digest(toHash.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ignored) {
        }
        if (hash != null) {
            return convertToHex(hash);
        }
        else
        {
            return null;
        }
    }

    private static String convertToHex(byte[] raw) {
        StringBuilder sb = new StringBuilder();
        for (byte aRaw : raw) {
            sb.append(Integer.toString((aRaw & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String getJSONStringOrNull(JSONObject jsonobj, String key)
    {
        try
        {
            if(jsonobj.isNull(key))
            {
                return null;
            }
            else
            {
                return jsonobj.getString(key);
            }
        }
        catch(JSONException ignored){return null;}
    }

    public static String getJSONStringNotNull(JSONObject jsonobj, String key)
    {
        try
        {
            if(jsonobj.isNull(key))
            {
                return "";
            }
            else
            {
                return jsonobj.getString(key);
            }
        }
        catch(JSONException ignored){return "";}
    }

    public static ArrayList<String> getFilesDirList(Context context)
    {
        ArrayList<String> files_list = new ArrayList<>();
        if(context.getExternalFilesDirs(null) != null &&
                context.getExternalFilesDirs(null).length > 0) {
            for(int x = context.getExternalFilesDirs(null).length - 1;x >= 0;x--)
            {
                files_list.add(context.getExternalFilesDirs(null)[x].getAbsolutePath());
            }
        }
        else
        {
            File fdir = context.getExternalFilesDir(null);
            if (fdir != null) {
                files_list.add(fdir.getAbsolutePath());
            }
        }
        boolean repeated = false;
        String fallback_path = context.getFilesDir().getAbsolutePath();
        for(String file_path : files_list)
        {
            if(fallback_path.contentEquals(file_path))
            {
                repeated = true;
                break;
            }
        }
        if(!repeated) {
            files_list.add(fallback_path);
        }
        return files_list;
    }

    public static String getCurrentDatabaseLocation(Context context)
    {
        boolean pass = false;
        ArrayList<String> paths = getFilesDirList(context);
        String current_database_location = ((String) loadFromSP(context,String.class,Cons.Current_Database_Location));
        if(current_database_location != null)
        {
            for(String path : paths)
            {
                if(path.contentEquals(current_database_location))
                {
                    pass = true;
                    break;
                }
            }
        }
        if(pass)
        {
            return current_database_location;
        }
        else
        {
            return null;
        }
    }

    public static String getCurrentDatabaseLocationSafe(Context context) {
        String current_database_location = Util.getCurrentDatabaseLocation(context);
        if (current_database_location == null) {
            ArrayList<String> paths = Util.getFilesDirList(context);
            if (paths.size() > 0) {
                current_database_location = paths.get(0);
                Util.saveToSP(context, current_database_location, Cons.Current_Database_Location);
            }
        }
        if (current_database_location != null && current_database_location.trim().endsWith("/")) {
            return current_database_location.trim();
        } else if (current_database_location != null) {
            return current_database_location.trim() + "/";
        } else {
            return "";
        }
    }

    public static String getDatabaseLocationSafe(String location)
    {
        if(location != null && location.trim().endsWith("/")) {
            return location.trim();
        } else if(location != null) {
            return location.trim() + "/";
        } else {
            return "";
        }
    }

    public static String formatSize(long size) {
        String suffix = null;
        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, '.');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private static String removeBase64ImgWebHeader(String rawbase64img)
    {
        String base64img;
        if(rawbase64img.contains("base64,")) { // Remover header web si existe
            base64img = rawbase64img.substring(rawbase64img.indexOf("base64,") + 7);
        }
        else
        {
            base64img = rawbase64img;
        }
        return base64img;
    }

    public static WidthHeight loadBase64Img(String rawbase64img, final ImageView targetView, final Activity act)
    {
        try {
            if(rawbase64img != null && !rawbase64img.equalsIgnoreCase("null")
                    && rawbase64img.length() > 24) { // data:image/jpeg;base64,
                byte[] chartData = Base64.decode(removeBase64ImgWebHeader(rawbase64img), Base64.NO_WRAP |
                    Base64.URL_SAFE  | Base64.NO_PADDING);
                final Bitmap bm = BitmapFactory.decodeByteArray(chartData, 0, chartData.length);
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        targetView.setImageBitmap(bm);
                        targetView.invalidate();
                    }
                });
                if(bm != null)
                {
                    return new WidthHeight(bm.getWidth(),bm.getHeight());
                }
                return null;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e){return null;}
    }

    public static boolean copyFile(File src, File dst)  {
        boolean ret = false;
        InputStream in = null;
        OutputStream out = null;
        try {
            if(src != null && dst != null) {
                if(!src.getAbsolutePath().contentEquals(dst.getAbsolutePath())) {
                    in = new FileInputStream(src);
                    out = new FileOutputStream(dst);
                    IOUtils.copyStream(in, out);
                }
                ret = true;
            }
        } catch (IOException ignored) {
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
        return ret;
    }

    public static int safeGetMatchedScreenSizeLayoutWidth(View parent, Activity act, int margin)
    {
        if(parent != null && parent.getWidth() > 0)
        {
            return parent.getWidth();
        }
        else if(act != null)
        {
            DisplayMetrics metrics = new DisplayMetrics();
            act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels - margin;
        }
        else
        {
            return 0;
        }
    }

    public static String leftZeros(int num,int minlen)
    {
        boolean negative = (num < 0);
        String ret = ""+num;
        if(negative)
        {
            ret = ret.replace("-","");
        }
        while(ret.length() < minlen)
        {
            ret = "0" + num;
        }
        if(negative)
        {
            ret = "-" + ret;
        }
        return ret;
    }
}
