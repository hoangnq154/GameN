/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.hn.utils.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.hn.main.BuildConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import info.guardianproject.netcipher.NetCipher;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */



public class ImageResizer extends ImageWorker {
    private static final String TAG = "ImageResizer";
    protected int mImageWidth;
    protected int mImageHeight;

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        setImageSize(imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageResizer(Context context, int imageSize) {
        super(context);
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     *
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     *
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    /**
     * The main processing method. This happens in a background task. In this case we are just
     * sampling down the bitmap and returning it from a resource.
     *
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + resId);
        }
        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth,
                mImageHeight, getImageCache());
    }
    
    private AssetManager assetMgr = null;
    
    public static int TYPE_DECOCE_ASSETS = 1;
    public static int TYPE_DECOCE_FILE = 2;
    public static int TYPE_DECOCE_RESOURCES = 3;
    public static int TYPE_DECODE_HTTP = 4;
    
    private int type = TYPE_DECOCE_RESOURCES;
       
    public void setDecodeWithAsset(AssetManager mgr)
    {
    	type = TYPE_DECOCE_ASSETS;
    	assetMgr = mgr;
    }

    public void setTypeDecode(int type)
    {
        this.type = type;
    }
    
    private Bitmap progessBitmapAsset(String path) throws IOException
    {
    	if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + path);
        }
        return decodeSampledBitmapFromAssets(assetMgr, path, mImageWidth,
                mImageHeight, getImageCache());
    }

    private Bitmap progressBitmapFile(String path)
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + path);
        }
        return decodeSampledBitmapFromFile(path, mImageWidth, mImageHeight , getImageCache());

    }

    private Bitmap progressBitmapHttp(ImageFetcher.ImageData path)
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + path);
        }
        return deocdeSampledBitmapFromHttp(path, mImageWidth, mImageHeight , getImageCache());

    }

    @Override
    protected Bitmap processBitmap(Object data) {
    	if(type == TYPE_DECOCE_RESOURCES)
    		return processBitmap(Integer.parseInt(String.valueOf(data)));
    	else if(type == TYPE_DECOCE_FILE)
        {
            return progressBitmapFile(((ImageFetcher.ImageData) data).imageSavePath);
        }
        else if(type == TYPE_DECODE_HTTP)
        {
            return progressBitmapHttp((ImageFetcher.ImageData) data);
        }
    	else if(type == TYPE_DECOCE_ASSETS)
			try {
				return progessBitmapAsset(String.valueOf(data));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
    	return processBitmap(Integer.parseInt(String.valueOf(data)));
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    
    // decode bitmap from assets file
    public static Bitmap decodeSampledBitmapFromAssets(AssetManager assets, String path,
            int reqWidth, int reqHeight, ImageCache cache) throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(assets.open(path));

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(assets.open(path));
    }


    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
            int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    public static Bitmap deocdeSampledBitmapFromHttp(ImageFetcher.ImageData dataImg,int reqWidth, int reqHeight, ImageCache cache)
    {
        if(new File(dataImg.imageSavePath).exists())
        {
            return decodeSampledBitmapFromFile(dataImg.imageSavePath,reqWidth,reqHeight,cache);
        }
        else
        {
            String ret = downloadIMG(dataImg);
            if(!ret.equals(""))
                return decodeSampledBitmapFromFile(ret,reqWidth,reqHeight,cache);

            return null;
        }
    }

    private static String downloadIMG(ImageFetcher.ImageData dataImg)
    {
        String ret = "";
        String tmpFile;

        String realPathArt = dataImg.imageSavePath;

        if(realPathArt.contains(".png") || realPathArt.contains(".jpg"))
        {
            tmpFile = realPathArt.substring(0,realPathArt.length()-4);
        }
        else
            tmpFile = realPathArt;

        RandomAccessFile raf = null;

        HttpURLConnection conection = null;
        try {
            //conection = (HttpsURLConnection)(new URL(url)).openConnection();
            conection = NetCipher.getHttpURLConnection(dataImg.imageURL);
            conection.setConnectTimeout(10000);
            conection.setRequestMethod("GET");

            File file = new File(tmpFile);
            if(file.exists() && file.canWrite() && file.delete())
            {
                file.createNewFile();
            }

            raf = new RandomAccessFile(tmpFile, "rwd");

            raf.seek(0);

            int count = 0;
            byte[] buffer = new byte[4096];
            boolean inter = false;

            BufferedInputStream is = new BufferedInputStream(conection.getInputStream(),4096);
            while((count = is.read(buffer)) != -1)
            {
                if(Thread.interrupted())
                {
                    inter = true;
                    break;
                }

                raf.write(buffer,0,count);
            }
            if(!inter)
            {
                file.renameTo(new File(dataImg.imageSavePath));
                return  dataImg.imageSavePath;
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            if(raf != null)
                com.hn.utils.Utils.close(raf);
        }
        return ret;
    }


    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
            }
        }
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
     * having a width and height equal to or larger than the requested width and height.
     *
     * @param options An options object with out* params already populated (run through a decode*
     *            method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
    }
}

