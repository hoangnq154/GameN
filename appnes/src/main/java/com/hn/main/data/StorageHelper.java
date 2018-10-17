package com.hn.main.data;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class StorageHelper {

	private static String rom_dir = "";
	public static Context context = null;

	public static void checkRomDir(Context context)
	{
		
		StorageHelper.context = context;
		String packName = context.getPackageName();
		String PREFIX_PATH = "/droidgames/nesfc/";
		
		try {
			if(is_do())
				return;
			rom_dir = Environment.getExternalStorageDirectory().getCanonicalPath()+PREFIX_PATH;
		} catch (IOException e) {
			e.printStackTrace();
			
			File file = new File("/sdcard");
			if(file.exists())
			{
				rom_dir = "/sdcard" + PREFIX_PATH;
			}
			else
			{
				rom_dir = "/data/data/"+packName+PREFIX_PATH;
			}
			
			
		}
		File file = new File(rom_dir+"downloads/");
		if(!file.exists())
		{
			file.mkdirs();
		}

		file = new File(rom_dir+"roms/");
		if(!file.exists())
		{
			file.mkdirs();
		}
		file = new File(rom_dir+"promoarts/");
		if(!file.exists())
		{
			file.mkdirs();
		}
		file = new File(rom_dir+"saves/");
		if(!file.exists())
		{
			file.mkdirs();
		}
	
	}

	public static String getROMsDIR()
	{
		return rom_dir + "roms/";
	}
	
	public static String getBaseDIR()
	{
		return rom_dir;
	}
	
	public static String getDownloadDIR()
	{
		return rom_dir+"downloads/";
	}
	public static String getPromotionArtDIR()
	{
		return rom_dir+"promoarts/";
	}
	public static String getSaveDIR(){return rom_dir +"saves/";}
	public static boolean is_do()
	{

//		String check = "x016"+"3.n64.nes.sn"+"es.gba.gbc.ma"+"me";
//
//		File file = new File("/data/data/" + check);
//		return !file.exists();
		return false;
	}
	//e mu. arca de.n64 .h
	
	private static String t = "em";
	private static String k = "ar";
	private static String a = "n6";
	
}
