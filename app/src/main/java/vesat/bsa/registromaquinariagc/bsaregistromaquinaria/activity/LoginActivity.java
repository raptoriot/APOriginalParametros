package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            findViewById(R.id.loginForm).setVisibility(View.GONE);
            ActivityCompat.requestPermissions
                    (this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.INTERNET,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
        }
        else
        {
            checkLogin(true, null, null);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                boolean pass = true;
                for(int result : grantResults)
                {
                    if(result == PackageManager.PERMISSION_DENIED)
                    {
                        pass = false;
                        break;
                    }
                }
                if(pass)
                {
                    findViewById(R.id.loginForm).setVisibility(View.VISIBLE);
                    checkLogin(true, null, null);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Debe habilitar todos los permisos.",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void checkLogin(final boolean use_saved, String v_user, String v_pass)
    {
        findViewById(R.id.loginProgress).setVisibility(View.VISIBLE);
        findViewById(R.id.loginForm).setVisibility(View.GONE);
        String user;
        String pass;
        if(use_saved)
        {
            user = (String) Util.loadFromSP(this,String.class,Cons.User_Email);
            pass = (String) Util.loadFromSP(this,String.class,Cons.User_Pass);
        }
        else
        {
            user = v_user;
            pass = v_pass;
        }
        if(user != null && pass != null && user.length() > 0 && pass.length() > 0)
        {
            final String n_user = user;
            final String n_pass = pass;
            Thread net = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte login_status = 0;
                    boolean offline_succesfully;
                    try {
                        String ans_raw = API.readWs(API.QUERY_LOGIN, n_user, n_pass, "0","0",null);
                        if(ans_raw != null && ans_raw.length() > 0) {
                            JSONObject ans = new JSONObject(ans_raw);
                            String status = Util.getJSONStringOrNull(ans, "status");
                            if (status != null && status.contentEquals("ok")) {
                                Util.saveToSP(getApplicationContext(), Util.getJSONStringNotNull(ans, "id"), Cons.User_ID);
                                Util.saveToSP(getApplicationContext(), Util.getJSONStringNotNull(ans, "nombre"), Cons.User_Name);
                                if (!use_saved) {
                                    Util.saveToSP(getApplicationContext(), n_user, Cons.User_Email);
                                    Util.saveToSP(getApplicationContext(), n_pass, Cons.User_Pass);
                                }
                                login_status = 1;
                            }
                        }
                        else
                        {
                            login_status = 2; // Offline!
                        }
                    }
                    catch (JSONException ignored){}
                    if(login_status == 1)
                    {
                        Util.saveToSP(getApplicationContext(),Boolean.TRUE,Cons.Last_Online_Login_Succesfully);
                    }
                    offline_succesfully = ((Boolean) Util.loadFromSP(getApplicationContext(),Boolean.class
                            ,Cons.Last_Online_Login_Succesfully)) == Boolean.TRUE;
                    if(login_status == 0 || (login_status == 2 && !offline_succesfully)) // Online Fail o Fail Without Last Online Succesfully
                    {
                        if(use_saved)
                        {
                            Util.saveToSP(getApplicationContext(),null,Cons.User_Email);
                            Util.saveToSP(getApplicationContext(),null,Cons.User_Pass);
                            Util.saveToSP(getApplicationContext(),null,Cons.Last_Online_Login_Succesfully);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.loginProgress).setVisibility(View.GONE);
                                findViewById(R.id.loginForm).setVisibility(View.VISIBLE);
                                if(!use_saved)
                                {
                                    Toast.makeText(getApplicationContext(),"Email/Contraseña Erroneo.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else // Login OK
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent mainact = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(mainact);
                                finish();
                            }
                        });
                    }
                }
            });
            net.start();
        }
        else
        {
            findViewById(R.id.loginProgress).setVisibility(View.GONE);
            findViewById(R.id.loginForm).setVisibility(View.VISIBLE);
            if(!use_saved) {
                Toast.makeText(this, "Debe Rellenar Todos Los Campos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonLogin:
            {
                checkLogin(false,((EditText) findViewById(R.id.loginEmail)).getText().toString(),
                        Util.getSha512(((EditText) findViewById(R.id.loginPassword)).getText().toString()));
            }
            break;
            case R.id.buttonRecovery:
            {
                Intent act = new Intent(this,RecuperarCuentaActivity.class);
                startActivity(act);
            }
            break;
        }
    }
}
