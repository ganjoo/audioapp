/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package facetracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.BuildConfig;
import com.fplay.audioapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import facetracker.ui.camera.CameraSourcePreview;
import facetracker.ui.camera.GraphicOverlay;
import github.nisrulz.screenshott.ScreenShott;

import static android.os.Environment.getExternalStorageDirectory;
import static java.io.File.separator;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    public static Context mctx;


    private final float OPEN_THRESHOLD = 0.85f;
    private final float CLOSE_THRESHOLD = 0.4f;

    private int state = 0;

    private Snackbar snackbar;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray(4);

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    void blink(float value) {
        switch (state) {
            case 0:
                if (value > OPEN_THRESHOLD) {
                    // Both eyes are initially open
                    state = 1;
                }
                break;

            case 1:
                if (value < CLOSE_THRESHOLD ) {
                    // Both eyes become closed
                    state = 2;
                }
                break;

            case 2:
                if (value > OPEN_THRESHOLD)  {
                    // Both eyes are open again
                    Log.i("BlinkTracker", "blink occurred!");

//                    mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
//                        @Override
//                        public void onPictureTaken(byte[] bytes) {
//                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                            Log.d("BITMAP", bmp.getWidth() + "x" + bmp.getHeight());
//                            capturePic(bytes);
//                            System.out.println(bmp.getWidth() + "x" + bmp.getHeight());
//                        }
//                    });
                    state = 0;
                }
                break;
        }


    }
    private void capturePic(byte[] bytes) {
        try {
            String mainpath = getExternalStorageDirectory() + separator + "MaskIt" + separator + "images" + separator;
            File basePath = new File(mainpath);
            if (!basePath.exists())
                Log.d("CAPTURE_BASE_PATH", basePath.mkdirs() ? "Success": "Failed");
            File captureFile = new File(mainpath + "photo_" + getPhotoTime() + ".jpg");
            if (!captureFile.exists())
                Log.d("CAPTURE_FILE_PATH", captureFile.createNewFile() ? "Success": "Failed");
            FileOutputStream stream = new FileOutputStream(captureFile);
            stream.write(bytes);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPhotoTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("ddMMyy_hhmmss");
        return sdf.format(new Date());
    }


    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }
    public Bitmap flipHorizontal(Bitmap source){

        Bitmap  bOutput;
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        bOutput = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return bOutput;
    }
    public static File store(Bitmap bm, String fileName){

        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

    private void    OpenImage(File file){
        //Uri uri =  Uri.fromFile(file);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        String mime = "*/*";
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (mimeTypeMap.hasExtension(
                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
            mime = mimeTypeMap.getMimeTypeFromExtension(
                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri,mime);
        startActivity(intent);
    }


    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        bmp1 = Bitmap.createScaledBitmap(bmp1,bmp2.getWidth(),bmp2.getHeight(),false);
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_facetracker);

        mPreview = (CameraSourcePreview) findViewById(R.id.face_tracker_preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.face_trackerfaceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
        mctx = this;

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();

        final Activity activity = this;




        final Button snap = findViewById(R.id.btn_snap);
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                Bitmap bmp = getScreenShot(rootView);
                View textureview = findViewById(R.id.face_tracker_preview);
                final Bitmap bmp1= ScreenShott.getInstance().takeScreenShotOfView(textureview);
                 mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        Bitmap bmp2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//                        android.hardware.Camera.getCameraInfo(mCurrentCameraId, info);
//                        Bitmap bitmap = rotate(realImage, info.orientation);

                        //capturePic(bytes);
                        int orientation = FaceTrackerActivity.getOrientation(activity);
                        String manufacturer = Build.MANUFACTURER;
                        Bitmap corrected_for_orientation;
                        if (Build.MANUFACTURER.equalsIgnoreCase("OnePlus")) {

                            corrected_for_orientation = bmp2;
                        }else {
                            corrected_for_orientation = rotate(bmp2, orientation);
                        }
                        showWarningDeviceUnsupported();
                        Bitmap finalbmp = overlay(flipHorizontal(corrected_for_orientation),bmp1);
                        //Bitmap finalbmp = overlay(corrected_for_orientation,bmp1);
                        File f = store(finalbmp,getPhotoTime() + ".png");
                        //shareImage(f);
                        OpenImage(f);

                    }
                });


            }
        });


        final Button snap2 = findViewById(R.id.btn_snap2);
        snap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                Bitmap bmp = getScreenShot(rootView);
                View textureview = findViewById(R.id.face_tracker_preview);
                final Bitmap bmp1= ScreenShott.getInstance().takeScreenShotOfView(textureview);
                mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        Bitmap bmp2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        //capturePic(bytes);
                        Bitmap finalbmp = overlay(bmp2,bmp1);
                        File f = store(finalbmp,getPhotoTime() + ".png");
                        //shareImage(f);
                        OpenImage(f);

                    }
                });


            }
        });
    }
    private void showWarningDeviceUnsupported(){
        snackbar = Snackbar
                .make( getWindow().getDecorView().getRootView(), "Your device model may not be fully supported. Please report in case of any issues on the feedback page..", Snackbar.LENGTH_LONG)
                .setDuration(15000);
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static int getOrientation(Activity act){
        final int device_rotation = act.getWindowManager().getDefaultDisplay().getRotation();
        android.hardware.camera2.CameraManager manager = (android.hardware.camera2.CameraManager)act.getSystemService(Context.CAMERA_SERVICE);
        String [] camera_ids;
        CameraCharacteristics characteristics;
        int sensorOrientation = 0;
        int surfaceRotation = 0;
        try {
            camera_ids = manager.getCameraIdList();
            characteristics = manager.getCameraCharacteristics(camera_ids[1]);
            sensorOrientation =  characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            surfaceRotation = ORIENTATIONS.get(device_rotation);




        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return (surfaceRotation + sensorOrientation + 270) % 360;
    }
    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        (this).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
//
//        mCameraSource = new CameraSource.Builder(context, detector)
//                .setRequestedPreviewSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
//                .setFacing(CameraSource.CAMERA_FACING_FRONT)
//                .setRequestedFps(30.0f)
//                .build();

        //
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame);

            mFaceGraphic = new FaceGraphic(overlay,bmp, FaceTrackerActivity.mctx);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {

            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);


            float left = face.getIsLeftEyeOpenProbability();
            float right = face.getIsRightEyeOpenProbability();
            if ((left == Face.UNCOMPUTED_PROBABILITY) ||
                    (right == Face.UNCOMPUTED_PROBABILITY)) {
                // One of the eyes was not detected.
                return;
            }

            float value = Math.min(left, right);
            blink(value);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}
