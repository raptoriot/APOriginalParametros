package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.PaintView;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class ActivityCanvas extends AppCompatActivity {

    private PaintView paintView ;
    private String CameraCacheCurrentID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        Util.saveToSP(this,(Boolean.FALSE),Cons.CAMERA_PictureTaked);
        CameraCacheCurrentID = (String) Util.loadFromSP(this,String.class, Cons.CAMERA_CacheCurrentID);
        if(CameraCacheCurrentID != null) {
            paintView = (PaintView) findViewById(R.id.paintView);
            final DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            paintView.init(metrics);
        }
        else
        {
            Toast.makeText(this,"ID Incorrecto",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas_act_menu, menu);
        MenuItem saveItem = menu.findItem(R.id.menu_save);
        if (saveItem != null) {
            saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    byte[] bytes = paintView.getJPEG();
                    if(bytes != null) {
                        Util.saveToSP(getApplicationContext(), ("data:image/jpeg;base64," +
                                        Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING)),
                                CameraCacheCurrentID);
                        Util.saveToSP(getApplicationContext(), (Boolean.TRUE), Cons.CAMERA_PictureTaked);

                    }
                    finish();
                    return true;
                }
            });
        }
        MenuItem delItem = menu.findItem(R.id.menu_delete);
        if (delItem != null){
            delItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    finish();
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
