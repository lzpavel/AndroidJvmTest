package ru.pfl.jvmtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class HardwareCameraActivity extends AppCompatActivity {

    private Button buttonHardCamShot;
    private Spinner spinnerHardCam;
    private TextureView textureViewHardCam;

    private CameraManager cameraManager = null;
    private CurrentCamera currentCamera = null;


    private HandlerThread backgroundThread = null;
    private Handler backgroundHandler = null;


    private static int dbg = 0;
    private static int camReady = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_camera);

        buttonHardCamShot = (Button) findViewById(R.id.buttonHardCamShot);
        spinnerHardCam = (Spinner) findViewById(R.id.spinnerHardCam);
        textureViewHardCam = (TextureView) findViewById(R.id.textureViewHardCam);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        List<String> camerasList = new ArrayList<>();
        //camerasList.add("Select cam ID");

        if(!checkPermissions()) {
            getPermissions();
        }



        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String camId : cameraManager.getCameraIdList()) {
                camerasList.add(camId);
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(camId);//Do not work on OnePlus7pro
                Set<String> physCamIds = cameraCharacteristics.getPhysicalCameraIds();
                for(String physCamId : physCamIds) {
                    camerasList.add(physCamId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentCamera = new CurrentCamera(cameraManager);



        initSpinner(camerasList);

        spinnerHardCam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    currentCamera.openCamera(camerasList.get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonHardCamShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCamera.makePhoto();
                //finish();
                //currentCamera.openCamera("12");
            }
        });



    }







    @Override
    public void onPause() {
        /*if(myCameras[CAMERA1].isOpen()) {
            myCameras[CAMERA1].closeCamera();
        }
        if(myCameras[CAMERA2].isOpen()){
            myCameras[CAMERA2].closeCamera();
        }*/
        currentCamera.closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        currentCamera.openCamera(null);

    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("HardCamBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!checkPermissions()) {
            Toast.makeText(getApplicationContext(), "Please allow permissions!!!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    boolean checkPermissions() {
        //API 30 Build.VERSION_CODES.R
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                return false;
            }
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        )
        {
            return false;
        }
        return true;

    }

    void getPermissions() {
        if (Build.VERSION.SDK_INT >= 30) {
            startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
        }
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    void initSpinner(List<String> spinnerList) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHardCam.setAdapter(adapter);

    }

    public class CurrentCamera {

        private String mCameraId;
        private CameraManager mCameraManager;

        private ImageReader mImageReader = null;
        private CameraDevice mCameraDevice = null;
        private CameraCaptureSession mCameraCaptureSession = null;
        //private CameraDevice.StateCallback camDevStateCallback;

        public CurrentCamera (CameraManager camManager) {
            mCameraManager = camManager;
        }

        private CameraDevice.StateCallback mCamDevStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                mCameraDevice = camera;
                while (!textureViewHardCam.isAvailable());
                createCameraPreviewSession();

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        };
        private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();

                Toast.makeText(getApplicationContext(), "IMG Available", Toast.LENGTH_SHORT).show();

                writeToFile(NV21toJPEG(YUV420toNV21(image), image.getWidth(), image.getHeight(), 100));

            }

        };

        public void makePhoto() {
            try {
                // This is the CaptureRequest.Builder that we use to take a picture.
                CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(mImageReader.getSurface());


                CameraCaptureSession.CaptureCallback camCapSesCaptureCallback = new CameraCaptureSession.CaptureCallback() {

                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                   @NonNull CaptureRequest request,
                                                   @NonNull TotalCaptureResult result) {
                        Toast.makeText(getApplicationContext(), "Capture complete", Toast.LENGTH_SHORT).show();




                    }

                    @Override
                    public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                        super.onCaptureFailed(session, request, failure);
                        Toast.makeText(getApplicationContext(), "Capture Failed", Toast.LENGTH_SHORT).show();
                    }
                };

                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.abortCaptures();
                mCameraCaptureSession.capture(captureBuilder.build(), camCapSesCaptureCallback, backgroundHandler);


            }
            catch (CameraAccessException e) {
                e.printStackTrace();

            }
        }
        private void createCameraPreviewSession() {
            try {
                Size[] pictureSizes = mCameraManager.getCameraCharacteristics(mCameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                int pictureWidth = pictureSizes[0].getWidth();
                int pictureHeight = pictureSizes[0].getHeight();
                //mImageReader = ImageReader.newInstance(1080,1920, ImageFormat.JPEG,1);
                mImageReader = ImageReader.newInstance(pictureWidth,pictureHeight, ImageFormat.YUV_420_888,1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler); //??? For saving
                /*mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Toast.makeText(getApplicationContext(), "IMG Available", Toast.LENGTH_SHORT).show();
                    }
                }, backgroundHandler); //??? For saving*/

                SurfaceTexture texture = textureViewHardCam.getSurfaceTexture();

                texture.setDefaultBufferSize(pictureWidth,pictureHeight);
                Surface surface = new Surface(texture);
                //-----SessionConfig-----
                List<OutputConfiguration> outputs = new ArrayList<>();
                outputs.add(new OutputConfiguration(mImageReader.getSurface()));
                outputs.add(new OutputConfiguration(surface));


                CaptureRequest.Builder crBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);


                crBuilder.addTarget(surface);
                Executor executor = new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        //command.run();
                        //new Thread(command).start();
                        backgroundHandler.post(command);
                    }
                };
                CameraCaptureSession.StateCallback camCapSesStateCallback = new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        mCameraCaptureSession = session;
                        try {
                            mCameraCaptureSession.setRepeatingRequest(crBuilder.build(),null, backgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                };
                SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputs, executor, camCapSesStateCallback);
                mCameraDevice.createCaptureSession(sessionConfiguration);

                //-----End-----
                //try {
            /*CaptureRequest.Builder builder3 = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            builder3.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                captureSession.setRepeatingRequest(builder3.build(),null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    }, backgroundHandler);*/



            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        public void openCamera(String cameraId) {
            if (cameraId != null) {
                mCameraId = cameraId;
            }
            if (mCameraId != null) {
                try {
                    closeCamera();
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        mCameraManager.openCamera(mCameraId, mCamDevStateCallback, backgroundHandler);
                    }

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        public void closeCamera() {

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
        public boolean isAvailable() {
            if (mCameraDevice != null) {
                return true;
            } else {
                return false;
            }
        }



        private byte[] YUV420toNV21(Image image) {
            Rect crop = image.getCropRect();
            int format = image.getFormat();
            int width = crop.width();
            int height = crop.height();
            Image.Plane[] planes = image.getPlanes();
            byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
            byte[] rowData = new byte[planes[0].getRowStride()];

            int channelOffset = 0;
            int outputStride = 1;
            for (int i = 0; i < planes.length; i++) {
                switch (i) {
                    case 0:
                        channelOffset = 0;
                        outputStride = 1;
                        break;
                    case 1:
                        channelOffset = width * height + 1;
                        outputStride = 2;
                        break;
                    case 2:
                        channelOffset = width * height;
                        outputStride = 2;
                        break;
                }

                ByteBuffer buffer = planes[i].getBuffer();
                int rowStride = planes[i].getRowStride();
                int pixelStride = planes[i].getPixelStride();

                int shift = (i == 0) ? 0 : 1;
                int w = width >> shift;
                int h = height >> shift;
                buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
                for (int row = 0; row < h; row++) {
                    int length;
                    if (pixelStride == 1 && outputStride == 1) {
                        length = w;
                        buffer.get(data, channelOffset, length);
                        channelOffset += length;
                    } else {
                        length = (w - 1) * pixelStride + 1;
                        buffer.get(rowData, 0, length);
                        for (int col = 0; col < w; col++) {
                            data[channelOffset] = rowData[col * pixelStride];
                            channelOffset += outputStride;
                        }
                    }
                    if (row < h - 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                }
            }
            return data;
        }

        private byte[] NV21toJPEG(byte[] nv21, int width, int height, int quality) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
            return out.toByteArray();
        }



        private void writeToFile(byte[] bytes) {
            //File root = Environment.getExternalStorageDirectory();
            File dir = new File("/storage/emulated/0/JVM Test");
            dir.mkdirs();
            File file = new File(dir, "HardwareCamera.jpg");

            try {
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(bytes);
                stream.close();
                Toast.makeText(getApplicationContext(), "Written to " + file, Toast.LENGTH_SHORT).show();
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }


    }

}