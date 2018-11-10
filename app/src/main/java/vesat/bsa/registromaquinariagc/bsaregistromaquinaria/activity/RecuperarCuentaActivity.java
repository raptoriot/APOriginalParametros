package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.API;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class RecuperarCuentaActivity extends AppCompatActivity {

    private String current_code = null;
    private String current_email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_cuenta);
    }

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.recovery_btn_send_code:
            {
                findViewById(R.id.recovery_btn_send_code).setEnabled(false);
                findViewById(R.id.recovery_btn_verify_code).setEnabled(false);
                findViewById(R.id.recovery_btn_change_pass).setEnabled(false);
                findViewById(R.id.recovery_pass_forms).setVisibility(View.GONE);
                findViewById(R.id.recovery_loading).setVisibility(View.VISIBLE);
                final String email = ((EditText) findViewById(R.id.recovery_email_to_send)).getText().toString();
                if(email.length() > 0) {
                    Thread net = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean pass = false;
                            try {
                                JSONObject ans = new JSONObject(API.readWs(API.SEND_RECOVERY_CODE
                                        , email, "null", "0","0",null));
                                String status = Util.getJSONStringOrNull(ans, "status");
                                if (status != null && status.contentEquals("ok")) {
                                    pass = true;
                                }
                            } catch (JSONException ignored) {
                            }
                            if (pass) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Código Enviado.",
                                                Toast.LENGTH_SHORT).show();
                                        ((EditText) findViewById(R.id.recovery_email_to_send_2)).setText(email);
                                        findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                                        findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Error Al Enviar Código.",
                                                Toast.LENGTH_SHORT).show();
                                        findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                                        findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    });
                    net.start();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Debe Rellenar Todos Los Campos.",
                            Toast.LENGTH_SHORT).show();
                    findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                    findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                    findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                }
            }
            break;
            case R.id.recovery_btn_verify_code:
            {
                findViewById(R.id.recovery_btn_send_code).setEnabled(false);
                findViewById(R.id.recovery_btn_verify_code).setEnabled(false);
                findViewById(R.id.recovery_btn_change_pass).setEnabled(false);
                findViewById(R.id.recovery_pass_forms).setVisibility(View.GONE);
                findViewById(R.id.recovery_loading).setVisibility(View.VISIBLE);
                final String email = ((EditText) findViewById(R.id.recovery_email_to_send_2)).getText().toString();
                final String codigo = ((EditText) findViewById(R.id.recovery_code_verify)).getText().toString();
                if(email.length() > 0 && codigo.length() > 0) {
                    Thread net = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean pass = false;
                            try {
                                JSONObject ans = new JSONObject(API.readWs(API.CHECK_RECOVERY_CODE
                                        , email, "null", "0","0","&recovery_code="+codigo));
                                String status = Util.getJSONStringOrNull(ans, "status");
                                if (status != null && status.contentEquals("ok")) {
                                    pass = true;
                                }
                            } catch (JSONException ignored) {
                            }
                            if (pass) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Código Valido.",
                                                Toast.LENGTH_SHORT).show();
                                        current_email = email;
                                        current_code = codigo;
                                        findViewById(R.id.recovery_pass1).setVisibility(View.GONE);
                                        findViewById(R.id.recovery_pass2).setVisibility(View.GONE);
                                        findViewById(R.id.recovery_pass3).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                                        findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Código Invalido.",
                                                Toast.LENGTH_SHORT).show();
                                        findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                                        findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    });
                    net.start();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Debe Rellenar Todos Los Campos.",
                            Toast.LENGTH_SHORT).show();
                    findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                    findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                    findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                }
            }
            break;
            case R.id.recovery_btn_change_pass:
            {
                findViewById(R.id.recovery_btn_send_code).setEnabled(false);
                findViewById(R.id.recovery_btn_verify_code).setEnabled(false);
                findViewById(R.id.recovery_btn_change_pass).setEnabled(false);
                findViewById(R.id.recovery_pass_forms).setVisibility(View.GONE);
                findViewById(R.id.recovery_loading).setVisibility(View.VISIBLE);
                final String new_pass = ((EditText) findViewById(R.id.recovery_new_pass)).getText().toString();
                final String new_pass_v = ((EditText) findViewById(R.id.recovery_new_pass_verify)).getText().toString();
                if(new_pass.length() == 0 || new_pass_v.length() == 0 || current_code == null ||
                        current_email == null || current_code.length() == 0 || current_email.length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "Debe Rellenar Todos Los Campos.",
                            Toast.LENGTH_SHORT).show();
                    findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                    findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                    findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                }
                else if(!new_pass.contentEquals(new_pass_v)) {
                    Toast.makeText(getApplicationContext(),
                            "La Nueva Contraseña No Coincide.",
                            Toast.LENGTH_SHORT).show();
                    findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                    findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                    findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                    findViewById(R.id.recovery_loading).setVisibility(View.GONE);
                } else {
                    Thread net = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean pass = false;
                            try {
                                JSONObject ans = new JSONObject(API.readWs(API.CHANGE_PASS_WITH_RECOVERY_CODE
                                        , current_email, Util.getSha512(new_pass), "0","0","&recovery_code="+current_code));
                                String status = Util.getJSONStringOrNull(ans, "status");
                                if (status != null && status.contentEquals("ok")) {
                                    pass = true;
                                }
                            } catch (JSONException ignored) {
                            }
                            if (pass) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Contraseña Recuperada Correctamente.",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Error Al Cambiar Contraseña.",
                                                Toast.LENGTH_SHORT).show();
                                        findViewById(R.id.recovery_btn_send_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_verify_code).setEnabled(true);
                                        findViewById(R.id.recovery_btn_change_pass).setEnabled(true);
                                        findViewById(R.id.recovery_pass_forms).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recovery_loading).setVisibility(View.GONE);
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
