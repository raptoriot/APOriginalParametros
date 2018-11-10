package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Perfil");
    }

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonChangePass:
            {
                findViewById(R.id.buttonChangePass).setEnabled(false);
                final String curPass = ((EditText) findViewById(R.id.changePassCurrent)).getText().toString();
                final String newPass = ((EditText) findViewById(R.id.changePassNew)).getText().toString();
                final String newPassV = ((EditText) findViewById(R.id.changePassNewVerify)).getText().toString();
                if(curPass.length() == 0 || newPass.length() == 0 || newPassV.length() == 0)
                {
                    Toast.makeText(this,"Debe Rellenar Todos Los Campos.",Toast.LENGTH_SHORT).show();
                    findViewById(R.id.buttonChangePass).setEnabled(true);
                }
                else if(!newPass.contentEquals(newPassV))
                {
                    Toast.makeText(this,"La Nueva Contraseña No Coincide.",Toast.LENGTH_SHORT).show();
                    findViewById(R.id.buttonChangePass).setEnabled(true);
                }
                else
                {
                    Thread net = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean pass = false;
                            String user_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.User_ID);
                            String old_pass = Util.getSha512(curPass);
                            String new_pass = Util.getSha512(newPass);
                            String device_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_ID);
                            String device_register_id = (String) Util.loadFromSP(getApplicationContext(),String.class,Cons.Device_Register_ID);
                            try {
                                JSONObject ans = new JSONObject(API.readWs(API.CHANGE_PASS_WITH_CURRENT_PASS
                                        , user_id, old_pass, device_register_id, device_id, "&new_pass=" + new_pass));
                                String status = Util.getJSONStringOrNull(ans, "status");
                                if (status != null && status.contentEquals("ok")) {
                                    pass = true;
                                }
                            }
                            catch (JSONException ignored){}
                            if(pass) {
                                Util.saveToSP(getApplicationContext(),new_pass,Cons.User_Pass);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Contraseña Actualizada Correctamente.",
                                                Toast.LENGTH_SHORT).show();
                                        ((EditText) findViewById(R.id.changePassCurrent)).setText("");
                                        ((EditText) findViewById(R.id.changePassNew)).setText("");
                                        ((EditText) findViewById(R.id.changePassNewVerify)).setText("");
                                        findViewById(R.id.buttonChangePass).setEnabled(true);
                                    }
                                });
                            } else  {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Ha Ocurrido Un Error Al Cambiar La Contraseña.",
                                                Toast.LENGTH_SHORT).show();
                                        findViewById(R.id.buttonChangePass).setEnabled(true);
                                    }
                                });
                            }
                        }
                    });
                    net.start();
                }
            }
            break;
        }
    }

}
