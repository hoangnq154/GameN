package com.hn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;

import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;

public class Utils {
	
	public static String fileToMD5(String filePath) {
	    InputStream inputStream = null;
	    try {
	        inputStream = new FileInputStream(filePath);
	        byte[] buffer = new byte[1024];
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	        int numRead = 0;
	        while (numRead != -1) {
	            numRead = inputStream.read(buffer);
	            if (numRead > 0)
	                digest.update(buffer, 0, numRead);
	        }
	        byte [] md5Bytes = digest.digest();
	        return convertHashToString(md5Bytes);
	    } catch (Exception e) {
	        return null;
	    } finally {
	        if (inputStream != null) {
	            try {
	                inputStream.close();
	            } catch (Exception e) { }
	        }
	    }
	}

	private static String convertHashToString(byte[] md5Bytes) {
	    String returnVal = "";
	    for (int i = 0; i < md5Bytes.length; i++) {
	        returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
	    }
	    return returnVal.toLowerCase();
	}
	
	public static void close(Closeable obj) 
	{
		synchronized (obj) {
			try {
				obj.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Bitmap toBlackWhiteBitmap(Bitmap src){

	      int width = src.getWidth();
	        int height = src.getHeight();
	        // create output bitmap
	        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
	        // color information
	        int A, R, G, B;
	        int pixel;
	    	for (int x = 0; x < width; ++x) {
	            for (int y = 0; y < height; ++y) {
	                // get pixel color
	                pixel = src.getPixel(x, y);
	                A = Color.alpha(pixel);
	                R = Color.red(pixel);
	                G = Color.green(pixel);
	                B = Color.blue(pixel);
	                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
	                // use 128 as threshold, above -> white, below -> black
	                if (gray > 128) {
	                    gray = 255;
	                }
	                else{
	                    gray = 0;
	                }
	                    // set new pixel color to output bitmap
	                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
	            }
	        }
			return bmOut;
	}
	
	public static Bitmap toGrayscale(Bitmap bmpOriginal)
	{        
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();    

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    return bmpGrayscale;
	}
	
	public static String size2str(long size)
	{
		float mb = ((float)size) / 1048576;
		String tx = String.format("%.2f MB", mb);	
		return tx;
	}
	
	public static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
	
	public static String getIMEI(Context context) {
//		TelephonyManager tm = (TelephonyManager) FindColor.instance
//				.getSystemService(Context.TELEPHONY_SERVICE);
//		if (tm != null && tm.getDeviceId() != null)
//			return tm.getDeviceId();
//		return "" + (new Random()).nextInt() / 100000;
		
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}
	
	public static String getMac(Context context) {
		WifiManager wimanager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		String macAddress = wimanager.getConnectionInfo().getMacAddress();
		if (macAddress == null) {
			macAddress = "Device don't have mac address or wi-fi is disabled";
		}
		System.out.println("MAC ANDRESS : " + macAddress);
		return macAddress;
	}
	
	public static int getVersionCode(Context context)  
	{
		String version = "";
		int v = 0;
		try{
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = ""+ pInfo.versionCode;
			v = pInfo.versionCode;
		}
		catch(Exception e)
		{
			
		}

		return v;
	}
	
	public static void openApp(Context context,String packagee) {
		if(isAppInstalled(context, packagee))
		{
			PackageManager pm = context.getPackageManager();
			Intent launchIntent = pm.getLaunchIntentForPackage(packagee);
			context.startActivity(launchIntent);
		}
		else
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packagee));
			context.startActivity(intent);
		}
		

	}

	public static boolean isAppInstalled(Context context,String packageName) {
		Intent i;
		PackageManager manager = context.getPackageManager();
		i = manager.getLaunchIntentForPackage(packageName);
		if (i == null)
			return false;
		else
			return true;
	}


	public static String encryption(String strNormalText){
		String seedValue = "hienham";
		String normalTextEnc="";
		try {
			normalTextEnc = AESCrypt.encrypt(seedValue, strNormalText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return normalTextEnc;
	}
	public static String decryption(String strEncryptedText){
		String seedValue = "hienham";
		String strDecryptedText="";
		try {
			strDecryptedText = AESCrypt.decrypt(seedValue, strEncryptedText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strDecryptedText;
	}
	
}
