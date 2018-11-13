package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    @SuppressLint("StaticFieldLeak")
    private ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }


    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            if(urldisplay != null && urldisplay.length() > 0) {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                in.close();
            }
        } catch (Exception e) {}// No se encontro foto
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        if(result != null) {
            bmImage.setImageBitmap(result);
            View parentView = null;
            int max_try = 10;
            while(parentView == null && max_try >= 0)
            {
                parentView = (View) bmImage.getParent();
                if(parentView == null)
                {
                    SystemClock.sleep(250); // Esperar hasta que la view se ancle en ActivityTarea
                    max_try--;
                }
            }
            if (parentView != null) {
                int cur_width = Util.safeGetMatchedScreenSizeLayoutWidth(parentView,null,0);
                if(cur_width > 0) {
                    float factor = ((float) cur_width) / ((float) result.getWidth());
                    int new_height = (int) (result.getHeight() * factor);
                    LinearLayout.LayoutParams params2 =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    new_height);
                    params2.setMargins(0, 0, 0, 16);
                    bmImage.setLayoutParams(params2);
                }
            }
        }
    }
}

