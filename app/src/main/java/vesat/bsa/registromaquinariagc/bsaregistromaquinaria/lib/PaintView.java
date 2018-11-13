package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {

    public class FingerPath {

        public int color;
        public boolean emboss;
        public boolean blur;
        public int strokeWidth;
        public Path path;

        public FingerPath(int color, boolean emboss, boolean blur, int strokeWidth, Path path) {
            this.color = color;
            this.emboss = emboss;
            this.blur = blur;
            this.strokeWidth = strokeWidth;
            this.path = path;
        }
    }

    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Bitmap mBitmapBackground;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapBackground = null;
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void init(DisplayMetrics metrics, Bitmap background) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if (background != null) {

            float ratio = ((float) width / (float) background.getWidth());
            int new_height = (int) (((float) background.getHeight()) * ratio);
            if (new_height <= 0) {
                new_height = height;
            }
            mBitmapBackground = Bitmap.createScaledBitmap(background, width
                    , new_height, false);
        }
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    public void emboss() {
        emboss = true;
        blur = false;
    }

    public void blur() {
        emboss = false;
        blur = true;
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        normal();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);
        if (mBitmapBackground != null) {
            int left = Math.max(0, (mBitmap.getWidth() - mBitmapBackground.getWidth()) / 2);
            int top = Math.max(0, (mBitmap.getHeight() - mBitmapBackground.getHeight()) / 2);
            mCanvas.drawBitmap(mBitmapBackground, left, top, null);
        }
        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(fp.path, mPaint);

        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }

    public byte[] getJPEG() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] ret = out.toByteArray();
            out.close();
            return ret;
        } catch (IOException ignored) {
            return null;
        }
    }
}