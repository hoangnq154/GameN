package com.hn.main.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.hn.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//import paulscode.android.mupen64plusae.SplashActivity;


public class GameDataManager {
	
	final String TAG = GameDataManager.class.getSimpleName();
	
	Map<Integer,GameInfo> datas;
	public Map<String,GameInfo> md5Datas;

	int start = 10000;

	private Context context;
	
	private GameDataManager(){
		datas = new HashMap<Integer, GameInfo>();
		md5Datas = new HashMap<>();
	}
	
	public int getSize()
	{
		return datas.size();
	}
	
	public Map<Integer,GameInfo> getRawData()
	{
		return datas;
	}

	public void setDownloadInfo(int id,long download)
	{
		datas.get(id).downloaded = download;
	}


	private static GameDataManager instance = null;
	public static GameDataManager getInstance()
	{
		if(instance == null)
		{
			instance = new GameDataManager();
		}
		return instance;
	}
	
	private void addRom(InputStream is)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String save_body = "";
		try {

			while((line = br.readLine()) != null)
			{
				line = Utils.decryption(line);

				String[] list = line.split("\\^");
				if(list.length < 3)
					continue;
				GameInfo game= new GameInfo();
				game.type = GameInfo.TYPE_LIBRARY;
				game.id = (start += 10);

				if(list.length >= 2)
					game.name = list[0];
				if(list.length >= 2)
					game.md5 = list[1];
				if(list.length >= 3)
					game.artUrl = list[2];
				if(list.length >= 4)
					game.url = list[3];
				game.createLocalFile();
//				if(context instanceof SplashActivity)
//				{
//					SplashActivity splash = (SplashActivity) context;
//					game.generateArtLink(splash.mAppData,splash.mGlobalPrefs);
//
//				}
				datas.put(game.id, game);
				if(!game.md5.equals("") && md5Datas.get(game.md5)==null)
				{
					md5Datas.put(game.md5,game);
				}
				else
				{
					Log.e("DUPPLICATE GAME :" , game.name);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//generateNoteOnSD(context,"hien.txt",save_body);
	}

	private void addRom_no_encyrpt(InputStream is)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {

			while((line = br.readLine()) != null)
			{
				//line = Utils.decryption(line);

				String[] list = line.split("\\^");
				if(list.length < 3)
					continue;
				GameInfo game= new GameInfo();
				game.type = GameInfo.TYPE_LIBRARY;
				game.id = (start += 10);

				if(list.length >= 2)
					game.name = list[0];
				if(list.length >= 2)
					game.md5 = list[1];
				if(list.length >= 3)
					game.artUrl = list[2];
				if(list.length >= 4)
					game.url = list[3];
				game.createLocalFile();
//				if(context instanceof SplashActivity)
//				{
//					SplashActivity splash = (SplashActivity) context;
//					game.generateArtLink(splash.mAppData,splash.mGlobalPrefs);
//
//				}
				datas.put(game.id, game);
				if(!game.md5.equals("") && md5Datas.get(game.md5)==null)
				{
					md5Datas.put(game.md5,game);
				}
				else
				{
					Log.e("DUPPLICATE GAME :" , game.name);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void generateNoteOnSD(Context context, String sFileName, String sBody) {
		try {

			File root = new File(Environment.getExternalStorageDirectory(), "Notes");
			if (!root.exists()) {
				root.mkdirs();
			}
			File gpxfile = new File(root, sFileName);
			FileWriter writer = new FileWriter(gpxfile);
			writer.append(sBody);
			writer.flush();
			writer.close();
			//Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadData(Context context) throws IOException
	{	
		this.context = context;
		if(datas.size() == 0) {
			if (StorageHelper.is_do())
				return;
			InputStream is = this.context.getAssets().open("key.txt");
			addRom(is);
		}
	}

	public void loadMoreGameServer()
	{
		File file = new File(StorageHelper.getDefaultROMsDIR() + "rom_update_server");
		if (file.exists()) {
			try{
				FileInputStream is2 = new FileInputStream(file);
				addRom_no_encyrpt(is2);
			}
			catch (IOException e)
			{}

		}
	}

}
