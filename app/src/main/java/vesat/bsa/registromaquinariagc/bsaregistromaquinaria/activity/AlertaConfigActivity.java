package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class AlertaConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerta_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Envío de Alertas");
    }

    protected void onResume()
    {
        super.onResume();
        String email_alertas_1 = (String) Util.loadFromSP(getApplicationContext(),String.class, Cons.Email_Alertas_1);
        String email_alertas_2 = (String) Util.loadFromSP(getApplicationContext(),String.class, Cons.Email_Alertas_2);
        String email_alertas_3 = (String) Util.loadFromSP(getApplicationContext(),String.class, Cons.Email_Alertas_3);
        if(email_alertas_1 == null){email_alertas_1 = "";}
        if(email_alertas_2 == null){email_alertas_2 = "";}
        if(email_alertas_3 == null){email_alertas_3 = "";}
        ((EditText) findViewById(R.id.niveldealerta1)).setText(email_alertas_1);
        ((EditText) findViewById(R.id.niveldealerta2)).setText(email_alertas_2);
        ((EditText) findViewById(R.id.niveldealerta3)).setText(email_alertas_3);
    }

    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.saveButton:
            {
                String email_alertas_1 = ((EditText) findViewById(R.id.niveldealerta1)).getText().toString();
                String email_alertas_2 = ((EditText) findViewById(R.id.niveldealerta2)).getText().toString();
                String email_alertas_3 = ((EditText) findViewById(R.id.niveldealerta3)).getText().toString();
                Util.saveToSP(getApplicationContext(),email_alertas_1,Cons.Email_Alertas_1);
                Util.saveToSP(getApplicationContext(),email_alertas_2,Cons.Email_Alertas_2);
                Util.saveToSP(getApplicationContext(),email_alertas_3,Cons.Email_Alertas_3);
                Toast.makeText(getApplicationContext(),"Cambios Guardados",Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }
}
