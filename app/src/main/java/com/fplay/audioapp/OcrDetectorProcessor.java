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
package com.fplay.audioapp;

import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fplay.audioapp.utils.Utils;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay2<OcrGraphic> mGraphicOverlay;

    OcrDetectorProcessor(GraphicOverlay2<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */

    private String capitalizer(String word){

        String[] words = word.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return  sb.toString();

    }
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            mGraphicOverlay.add(graphic);

            LayoutInflater inflater = (LayoutInflater) mGraphicOverlay.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            String animal_name = capitalizer(item.getValue());
            if(Utils.OCRTagFound(animal_name,mGraphicOverlay.getContext())) {

                final View view_1;
                view_1 = inflater.inflate(R.layout.animal_details_ar_popup, null);
                TextView desc = (TextView) view_1.findViewById(R.id.tv_animal_description);
                desc.setMovementMethod(new ScrollingMovementMethod());
                desc.setText(Utils.getTextFromOCRTag(animal_name, mGraphicOverlay.getContext()));
                Button dismiss = (Button) view_1.findViewById(R.id.button_dismiss);
                view_1.setVisibility(View.VISIBLE);
                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view_1.setVisibility(View.INVISIBLE);

                    }
                });
                Context context = mGraphicOverlay.getContext();
                ((Activity) context).addContentView(view_1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));



            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
