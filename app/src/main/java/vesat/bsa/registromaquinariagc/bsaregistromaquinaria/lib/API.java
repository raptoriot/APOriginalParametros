package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.net.ssl.HttpsURLConnection;
//import java.net.HttpURLConnection;
import java.net.URL;



public class API {

    //private final static String ws_location = "http://172.25.70.9/BSARegistroMaquinariaGCWebWS/public_html/ws/";
    private final static String ws_location = "https://aridos.vesat.cl/bsa_registro_maq_gc/ws/index.php";
    public final static int PING = 0;
    public final static int QUERY_LOGIN = 1;
    public final static int REGISTER_LOGIN = 2;
    public final static int SEND_RECOVERY_CODE = 3;
    public final static int CHECK_RECOVERY_CODE = 4;
    public final static int CHANGE_PASS_WITH_RECOVERY_CODE = 5;
    public final static int CHANGE_PASS_WITH_CURRENT_PASS = 6;
    public final static int CREATE_DEVICE = 10;
    public final static int GET_FORMULARIOS_LIST = 20;
    public final static int SYNC_REGISTROS_DATA_FROM_DEVICE = 30;
    public final static int SYNC_RONDAS_DATA_FROM_DEVICE = 31;

    private final static String auth_username = "h9gau89ioZ12398Zaoe1278oe@@aoeaz!";
    private final static String auth_password = "AOZEOERK01@12euced((oeu09u8ueooaZ";

    private static String getAuthHeader()
    {
        String encoding = Base64.encodeToString((auth_username + ":" + auth_password).getBytes(),Base64.NO_WRAP);
        return "Basic " + encoding;
    }

    public static String readWs(int query,String user,String hpass,String dev_reg_id,String dev_id,String extra) {
        if(extra == null){extra = "";}
        String ret;
        try {
            String auth = Util.getSha512("[" + Util.getFechaActual() + "]09gfiyi7382CRHZES");
            String urlParameters = "query="+query;
            urlParameters += "&user="+user;
            urlParameters += "&pass="+hpass;
            urlParameters += "&auth="+auth;
            urlParameters += "&device_reg_id="+dev_reg_id;
            urlParameters += "&device_id="+dev_id;
            urlParameters += "&extra=" + extra;


            //if(ws_location.startsWith("https")) {
                URL ws_url = new URL(ws_location);
                HttpsURLConnection conn = (HttpsURLConnection) ws_url.openConnection();
                conn.setRequestProperty("Authorization",getAuthHeader());
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(urlParameters);
                writer.flush();
                writer.close();
                os.close();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder retBuilder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    retBuilder.append(line);
                }
                ret = retBuilder.toString();
                //System.out.println("WEB SERVICE CALL:" + ws_url + "\nWEB SERVICE PARAM:" + urlParameters.replace("&","\n")
                //        +"\nWEB SERVICE ANS:" + ret);
          //  }
            //else
            //{
                /*URL ws_url = new URL(ws_location);
                HttpURLConnection conn = (HttpURLConnection) ws_url.openConnection();
                conn.setRequestProperty("Authorization",getAuthHeader());
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(urlParameters);
                writer.flush();
                writer.close();
                os.close();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder retBuilder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    retBuilder.append(line);
                }
                ret = retBuilder.toString();
                System.out.println("WEB SERVICE CALL:" + ws_url + "\nWEB SERVICE PARAM:" + urlParameters.replace("&","\n")
                        +"\nWEB SERVICE ANS:" + ret);*/
            //}
        }
        catch (IOException ignore){
            ret = null;
            //System.out.println("WEB SERVICE IO EXCEPTION");
        }
        return ret;
    }
    
}
