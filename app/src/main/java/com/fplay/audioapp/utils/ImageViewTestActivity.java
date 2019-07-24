package com.fplay.audioapp.utils;


import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.fplay.audioapp.R;

import java.io.IOException;
import java.io.InputStream;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ImageViewTestActivity extends Activity {

    private static final String LOG_TAG = "image-test";

    ImageViewTouch mImage;
    Button mButton1;
    Button mButton2;
    Button mButtonscroll1;
    Button mButtonscroll2;
    CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        Toast.makeText(this, "ImageViewTouch.VERSION: " + ImageViewTouch.VERSION, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mImage = (ImageViewTouch) findViewById(R.id.image);

        // set the default image display type
        mImage.setDisplayType(ImageViewTouchBase.DisplayType.NONE);
        mImage.setScaleX(4.9f);
        mButton1 = (Button) findViewById(R.id.button);
        mButton2 = (Button) findViewById(R.id.button2);
        mButtonscroll1 = (Button) findViewById(R.id.scroll_left);
        mButtonscroll2 = (Button) findViewById(R.id.scroll_right);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox1);

        mButton1.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        selectRandomImage(mCheckBox.isChecked());
                    }
                }
        );

        mButton2.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        int current = mImage.getDisplayType().ordinal() + 1;
                        if (current >= ImageViewTouchBase.DisplayType.values().length) {
                            current = 0;
                        }

                        mImage.setDisplayType(ImageViewTouchBase.DisplayType.values()[current]);
                    }
                }
        );
        mButtonscroll1.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mImage.scrollBy(-2,0);
                    }
                }
        );
        mButtonscroll2.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mImage.scrollBy(-2,0);
                    }
                }
        );


        mImage.setSingleTapListener(
                new ImageViewTouch.OnImageViewTouchSingleTapListener() {

                    @Override
                    public void onSingleTapConfirmed() {
                        Log.d(LOG_TAG, "onSingleTapConfirmed");
                    }
                }
        );

        mImage.setDoubleTapListener(
                new ImageViewTouch.OnImageViewTouchDoubleTapListener() {

                    @Override
                    public void onDoubleTap() {
                        Log.d(LOG_TAG, "onDoubleTap");
                    }
                }
        );

        mImage.setOnDrawableChangedListener(
                new ImageViewTouchBase.OnDrawableChangeListener() {

                    @Override
                    public void onDrawableChanged(Drawable drawable) {
                        Log.i(LOG_TAG, "onBitmapChanged: " + drawable);
                    }
                }
        );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    Matrix imageMatrix;

    public void selectRandomImage(boolean small) {
        Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (c != null) {
            int count = c.getCount();
            //int position = (int) (Math.random() * count);
            int position = 0;
            if (c.moveToPosition(position)) {
                long id = c.getLong(c.getColumnIndex(MediaStore.Images.Media._ID));
                Uri imageUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);

                Log.d("image", imageUri.toString());

                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                int size = (int) (Math.min(metrics.widthPixels, metrics.heightPixels) / 0.55);

                if (small) {
                    size /= 3;
                }
                //size = 1200;

                Bitmap bitmap = DecodeUtils.decode(this, imageUri, size, size);
                Bitmap overlay = getOverlayBitmap("circle-black-medium.png");

                if (null != bitmap) {
                    Log.d(LOG_TAG, "screen size: " + metrics.widthPixels + "x" + metrics.heightPixels);
                    Log.d(LOG_TAG, "bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    mImage.setOnDrawableChangedListener(
                            new ImageViewTouchBase.OnDrawableChangeListener() {
                                @Override
                                public void onDrawableChanged(final Drawable drawable) {
                                    Log.v(LOG_TAG, "image scale: " + mImage.getScale() + "/" + mImage.getMinScale());
                                    Log.v(LOG_TAG, "scale type: " + mImage.getDisplayType() + "/" + mImage.getScaleType());

                                }
                            }
                    );
                    mImage.setImageBitmap(bitmap, null, -1, -1);

                } else {
                    Toast.makeText(this, "Failed to load the image", Toast.LENGTH_LONG).show();
                }
            }
            c.close();
            return;
        }
    }

    private Bitmap getOverlayBitmap(String name) {
        String file = null;

        if (TextUtils.isEmpty(name)) {
            try {
                String[] files = getAssets().list("images");

                if (null != files && files.length > 0) {
                    int position = (int) (Math.random() * files.length);
                    file = files[position];
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            file = name;
        }

        try {
            InputStream stream = getAssets().open("images/" + file);
            try {
                return BitmapFactory.decodeStream(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
