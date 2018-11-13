package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.R;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Cons;
import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class ActivityCamera extends AppCompatActivity {

    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String CameraCacheCurrentID = null;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Util.saveToSP(this,(Boolean.FALSE),Cons.CAMERA_PictureTaked);
        CameraCacheCurrentID = (String) Util.loadFromSP(this,String.class, Cons.CAMERA_CacheCurrentID);
        if(CameraCacheCurrentID != null) {
            textureView = findViewById(R.id.texture);
            assert textureView != null;
            textureView.setSurfaceTextureListener(textureListener);
            ImageView takePictureButton = findViewById(R.id.btn_takepicture);
            assert takePictureButton != null;
            takePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture();
                }
            });
        }
        else
        {
            Toast.makeText(this,"ID Incorrecto",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    /*final CameraCaptureSession.CaptureCallback captureCallbackListener =
            new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            createCameraPreview();
        }
    };*/

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
        }
    }
    protected void takePicture() {
        if(null == cameraDevice) {
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics;
            if (manager != null) {
                characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes = Objects.requireNonNull(characteristics.
                        get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).
                        getOutputSizes(ImageFormat.JPEG);
                int width = 640;
                int height = 480;
                if (jpegSizes != null)
                {
                    final int max = 1280;
                    int cur_max = 640;
                    for (Size jpegSize : jpegSizes) {
                        if (jpegSize.getWidth() > cur_max && jpegSize.getWidth() <= max) {
                            width = jpegSize.getWidth();
                            height = jpegSize.getHeight();
                            cur_max = width;
                        }
                    }
                }
                ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                List<Surface> outputSurfaces = new ArrayList<>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image;
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        image.close();
                        closeCamera();
                        finish();
                    }
                    @SuppressLint("ApplySharedPref")
                    private void save(byte[] bytes)
                    {
                        Util.saveToSP(getApplicationContext(),("data:image/jpeg;base64,"+Base64.encodeToString(bytes,
                                Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE)),CameraCacheCurrentID);
                        Util.saveToSP(getApplicationContext(),(Boolean.TRUE),Cons.CAMERA_PictureTaked);
                    }
                };
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                   @NonNull CaptureRequest request,
                                                   @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        createCameraPreview();
                    }
                };
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                        }
                    }
                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    }
                }, mBackgroundHandler);
            }
        } catch (CameraAccessException | NullPointerException e) {
        }
    }
    protected void createCameraPreview() {
        try {
            if(cameraDevice != null) {
                SurfaceTexture texture = textureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
                Surface surface = new Surface(texture);
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        if (cameraDevice == null) {
                            return;
                        }
                        cameraCaptureSessions = cameraCaptureSession;
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    }
                }, null);
            }
        } catch (CameraAccessException e) {
        }
    }
    @SuppressLint("MissingPermission")
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId;
            if (manager != null) {
                cameraId = manager.getCameraIdList()[0];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
                manager.openCamera(cameraId, stateCallback, null);
            }
        } catch (CameraAccessException e) {
        }
    }
    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}
