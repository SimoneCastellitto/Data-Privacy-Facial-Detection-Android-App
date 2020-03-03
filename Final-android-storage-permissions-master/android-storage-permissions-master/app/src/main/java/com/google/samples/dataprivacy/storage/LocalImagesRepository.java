/*
 * Copyright 2017 Google Inc. All rights reserved.
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

package com.google.samples.dataprivacy.storage;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
//to use Android Api:
//import android.media.*;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
//to use google vision
import com.google.android.gms.vision.face.FaceDetector;
import com.google.samples.dataprivacy.model.Image;
import com.google.samples.dataprivacy.util.MyContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository of images that stores data on the device. The initial implementation is using
 * the external storage directory (see {@link Environment#getExternalStorageDirectory()}.
 */
public class LocalImagesRepository implements ImagesRepository {

    private static final String TAG = "LocalImagesRepository";
    private static final String PATH = "secureimages/";

    private File mStorage;

    public LocalImagesRepository(Context context) {
        File internalStorage = context.getFilesDir();
        mStorage = new File(internalStorage, PATH);

        if (!mStorage.exists()) {
            if (!mStorage.mkdirs()) {
                Log.e(TAG, "Could not create storage directory: " + mStorage.getAbsolutePath());

            }
        }
    }

    /**
     * Generates a file name for the png image and stores it in local storage.
     *
     * @param image The bitmap to store.
     * @return The name of the image file.
     */
    @Override
    public String saveImage(Bitmap image) {
        //code to get the number of faces before to save it (Android API)
        /*Bitmap bmp = image.copy(Bitmap.Config.RGB_565, true);
        int maxFaces = 3;
        FaceDetector faceDetector = new FaceDetector(image.getWidth(),image.getHeight(),maxFaces);
        FaceDetector.Face[] faces = new FaceDetector.Face[maxFaces];
        int numberFaces = faceDetector.findFaces(bmp,faces);*/

        //Code to get the number of faces (max 3) using google vision
        Frame.Builder myFrameBuilder = new Frame.Builder().setBitmap(image);
        Frame myFrame = myFrameBuilder.build();
        FaceDetector faceDetector = new FaceDetector.Builder(MyContext.getContext()).build();
        SparseArray<Face> arrayOfFaces= faceDetector.detect(myFrame);
        int numberFaces = arrayOfFaces.size();
        if (numberFaces > 3) numberFaces = 3;

        //code to save the fileName with the first letter the number of faces
        final String fileName = numberFaces + UUID.randomUUID().toString() + ".png";
        File file = new File(mStorage, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            image.compress(Bitmap.CompressFormat.PNG, 85, fos);
        } catch (IOException e) {
            Log.e(TAG, "Error during saving of image: " + e.getMessage());
            return null;
        }

        return fileName;
    }

    /**
     * Deletes the given image.
     *
     * @param fileName Filename of the image to delete.
     */
    @Override
    public void deleteImage(String fileName) {
        File file = new File(fileName);
        if (!file.delete()) {
            Log.e(TAG, "File could not be deleted: " + fileName);
        }
    }

    /**
     * Returns a list of all images stored in this repository.
     * An {@link Image} contains a {@link Bitmap} and a string with its filename.
     *
     * @return
     */
    @Override
    public List<Image> getImages() {
        File[] files = mStorage.listFiles();
        if (files == null) {
            Log.e(TAG, "Could not list files.");
            return null;
        }
        ArrayList<Image> list = new ArrayList<>(files.length);
        for (File f : files) {
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            list.add(new Image(f.getAbsolutePath(), bitmap));
        }
        return list;
    }
    //Returns a list of the images saved in the repository with the number of faces I want.
    public List<Image> getSomeImages(int numberFaces){
        File[] files = mStorage.listFiles();
        if (files == null) {
            Log.e(TAG, "Could not list files.");
            return null;
        }
        String prefixToCompare = String.valueOf(numberFaces);
        ArrayList<Image> list = new ArrayList<>(files.length);
        for (File f : files) {
            if(f.getName().startsWith(prefixToCompare)) {
                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                list.add(new Image(f.getAbsolutePath(), bitmap));
            }
        }
        return list;
    }

    /**
     * Loads the given file as a bitmap.
     */
    @Override
    public Bitmap getImage(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "File could not opened. It does not exist: " + path);

            return null;
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());

    }
}
