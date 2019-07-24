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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.fplay.audioapp.R;
import com.google.android.gms.vision.face.Face;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import facetracker.ui.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    private Bitmap mBitmap;
    MediaPlayer player = new MediaPlayer();

    private long lastScreenshot = 0;

    private Context mContext;

    FaceGraphic(GraphicOverlay overlay, Bitmap bm, Context ctx) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];



        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);

        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);


        mBitmap = bm;
        mContext = ctx;

    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }


    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;

        if (face == null) {
            return;
        }


        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        //canvas.drawRect(left, top, right, bottom, mBoxPaint);

        RectF dest = new RectF((int) left, (int) top, (int) right, (int) bottom);
        Matrix m = new Matrix();
        m.setRotate(face.getEulerZ(),dest.centerX(),dest.centerY());
        m.mapRect(dest);
        if(face.getIsSmilingProbability() > 0.3f){
             mBitmap = Bitmap.createScaledBitmap(mBitmap,(int)(right-left), (int)(bottom-top),true);
            canvas.drawBitmap(mBitmap, null,canvas.getClipBounds(),null);
            //takeScreenshot();
            MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.tiger);
            mPlayer.start();
        }
        else
        {
            canvas.drawBitmap(mBitmap, null,canvas.getClipBounds(),null);
        }



        //canvas.drawBitmap(rotate_bitmap(mBitmap,face.getEulerZ()), null, dest, null);


        Runnable audiorunnable = new Runnable(){

            public void run(){



                try {
                    playAudio("file:///android_asset/audio/Tiger2.mp3");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(audiorunnable);
        thread.start();

    }


    public Bitmap rotate_bitmap(Bitmap bmp,float degree){
        Matrix matrix = new Matrix();

        matrix.postRotate(degree);

        return Bitmap.createBitmap(bmp , 0, 0, bmp .getWidth(), bmp .getHeight(), matrix, true);
    }
    private void playAudio(String url) throws Exception
    {

        killMediaPlayer();

        player = new MediaPlayer();
        player.setDataSource(url);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Loading Audio from the server...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.VISIBLE);
            }
        });
        player.prepare();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Playing...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.GONE);
            }
        });
        player.start();
    }
    private void killMediaPlayer() {
        if(player!=null) {
            try {
                player.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

//    public void playSoundFromAssets(String file) {
//        try {
//            if (player.isPlaying()) {
//                player.stop();
//                player.release();
//                player = new MediaPlayer();
//            }
//
//            AssetFileDescriptor descriptor = getAssets().openFd("beepbeep.mp3");
//            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
//            descriptor.close();
//
//            m.prepare();
//            m.setVolume(1f, 1f);
//            m.setLooping(true);
//            m.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    private void takeScreenshot() {
        if(lastScreenshot != 0){
            if(Math.abs(System.currentTimeMillis() - lastScreenshot) < 200){
                return;

            }

        }
        lastScreenshot = System.currentTimeMillis();
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = ((Activity)mContext).getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        mContext.startActivity(intent);
    }
}
