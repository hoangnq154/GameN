package com.hn.main.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hn.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import info.guardianproject.netcipher.NetCipher;

public class DataServerManager {
	
	// for fake
	public boolean isFake = false;			// co fake de qua mat hay khong
	private int versionCodeForFake = -1;  // Neu version code cua apk nay bang tren server thi se fake
	
	// for ADS
	// ADS co the hien thi o : 1. GameActivity khi open game va khi an START , 2. Luc tro ve Lobby
	public boolean tangADS = false;		// nhan doi hien thi ADS (luc' tro ve lobby)
	public int time_out_ads = Constant.TIME_NOT_SHOW_AD;  // thoi gian hien thi ads lan tiep theo (luc an START)
	public int time_ads_when_play = Constant.TIME_AD_PLAY; // khi back ve tu gameActivity , thoi gian hien ads o lobby
	public boolean enableADS = false;

	public boolean isPremium = false;

	// for hide Icon
	public int count_show_icon = Constant.COUNT_SHOW_ICON;

//	static {
//		System.loadLibrary("dog");
//	}
	
	// for update ROMs file and update apk
	public int newVerion = 0;				// version cua file ROM	
	public int newVersionCode = 1;		// version code tren server de check update APK
	public boolean forceUpdate = false;	// force update apk
	public String update_package = "";		// link update
	public String message_update = "";	// message update
	
	
	// for promotion
	public List<GameInfo> promoDatas = new ArrayList<>();


	Handler handler;
	
	public DataServerManager(Handler handler)
	{
		this.handler = handler;
	}

	public void loadPremium(Context context)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		int check  = shared.getInt("hien", 0);

		isPremium = (check > 0);
	}
	public void savePremium(Context context, boolean premium)
	{
		isPremium = premium;
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("hien", isPremium?1:0);
		editor.commit();
	}

	public void loadPreference(Context context)
	{
		loadPremium(context);
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		String v = shared.getString(Constant.KEY_STORAGE_CHECK_GAME, "{}");
		
		try {
			JSONObject obj = new JSONObject(v);
			versionCodeForFake = obj.getInt("vc_fake");
	
			int currentVCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			isFake = (versionCodeForFake == currentVCode);

//			if(!isFake)
//				saveIntForKey(context,"da_tung_bat",1);
			if(getIntForKey(context,"da_tung_bat",0) == 1)
			{
				Log.i("HIEN HAM","da tung bat game");
				isFake = false;
			}
			
			tangADS = obj.getBoolean("tangADS");
			time_out_ads = obj.getInt("time_ads");
			newVerion = obj.getInt("version_roms");
			newVersionCode = obj.getInt("new_version_code");
			forceUpdate = obj.getBoolean("force_update");
			update_package = obj.getString("update_package");
			message_update = obj.getString("message_update");
			count_show_icon = obj.getInt("count_show_icon");
			JSONArray array = obj.getJSONArray("promo");
			promoDatas.clear();
			for(int i=0;i<array.length();i++)
			{
				JSONObject o = array.getJSONObject(i);
				String package_promo = o.getString("promotion_package");
				String name_promo = o.getString("promotion_name");
				String message_promo = o.getString("promotion_message");
				String image_promo = o.getString("promotion_image");

				boolean force_display = true;
				try{
					force_display = o.getBoolean("force_display");
				}
				catch (Exception e){
					e.printStackTrace();
				}

				if(package_promo !=null && name_promo != null && message_promo != null && image_promo != null && !package_promo.equals(""))
				{
					GameInfo game = new GameInfo();
					game.type = GameInfo.TYPE_PROMO;
					game.name = name_promo;
					game.url = package_promo;
					game.artUrl = image_promo;
					game.uerData = new Boolean(force_display);
					if(image_promo.contains("http"))
					{
						String[] tmp = image_promo.split("\\/");
						String name = tmp[tmp.length-1];
						game.artPath = StorageHelper.getPromotionArtDIR() + name;
					}

					game.intro = message_promo;
					promoDatas.add(game);
				}

			}

			
			Log.i("HIENHAM load: ", "vc_fake :" + versionCodeForFake +", current :" + currentVCode +"," +isFake );

			time_ads_when_play = obj.getInt("lobby_ads");
			enableADS = obj.getBoolean("enable_ads");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void savePreference(Context context,String version)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		
		int first = version.indexOf("{");
		int last = version.lastIndexOf("}");
		
		if(first > -1 && last > -1)
		{
			String v = version.substring(first, last+1);
			
			SharedPreferences.Editor editor = shared.edit();
			editor.putString(Constant.KEY_STORAGE_CHECK_GAME, v);
			editor.commit();
		}
		
	}
	
	public void saveStringForKey(Context context,String key,String data)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		
		SharedPreferences.Editor editor = shared.edit();
		editor.putString(key, data);
		editor.commit();
	}
	public String getStringForKey(Context context,String key,String default_)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		String v = shared.getString(key, default_);
		return v;
	}
	public void saveIntForKey(Context context,String key,int data)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt(key, data);
		editor.commit();
	}
	public int getIntForKey(Context context,String key,int default_)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		int v = shared.getInt(key, default_);
		return v;
	}
	
	public boolean daTungCheckVersion(Context context)
	{
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		return shared.contains(Constant.KEY_STORAGE_CHECK_GAME);
	}
	
	public void checkServerFake()            // check fake de lua google
	{
		Log.i("HOANG","CHECK HTTP");
		String URL = "https://raw.githubusercontent.com/hoangnq154/acc_giangnh0163/master/n64/n64_check";
		GetMethodDemo get = new GetMethodDemo(handler,3000,3000);
		get.execute(URL);

	}
	
	public void saveNewVersionToCurrentVersion(Context context)
	{
		loadPreference(context);
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt(Constant.KEY_STORAGE_CURRENT_VERSION_ROM, newVerion);
		editor.commit();
		
	}
	
	public boolean checkRomData(Context context, DownloadROMThread.DownloadTaskListener listener)				// Cap nhat them game moi' 
	{
		loadPreference(context);
		
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		int current_version = shared.getInt(Constant.KEY_STORAGE_CURRENT_VERSION_ROM, 0);
		
		if(newVerion > current_version)
		{
			Log.i("HOANG","CO NEW VERSION CHO ROM , download nhe");
			GameInfo gameInfo = new GameInfo();
			gameInfo.id = -1;

			gameInfo.url = "https://raw.githubusercontent.com/hoangnq154/n64_lock/master/rom_update_server";
			gameInfo.name = "ROM DATA SERVER";
			gameInfo.fileName = "rom_data_server";
			gameInfo.localFile = StorageHelper.getBaseDIR() + "rom_update_server";
			
			DownloadROMThread thread = new DownloadROMThread(gameInfo, listener);
			thread.start();
			
			return true;
		}
		return false;
	}
	
	
	static class GetMethodDemo extends AsyncTask<String , Void ,String> {
	    String server_response;
	    
	    Handler handler;
	    int timeout_connect = 2000;
	    int timeout_read = 3000;
	    
	    public GetMethodDemo(Handler handler,int tout_connect,int tout_read)
	    {
	    	this.handler = handler;
	    	timeout_connect = tout_connect;
	    	timeout_read = tout_read;
	    }
	    
	    @Override
	    protected String doInBackground(String... strings) {

	        URL url;
	        HttpURLConnection urlConnection = null;

	        try {
	            url = new URL(strings[0]);
	            //urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection = NetCipher.getHttpsURLConnection(url);
	            urlConnection.setConnectTimeout(timeout_connect);
	            urlConnection.setReadTimeout(timeout_read);
	            
	            int responseCode = urlConnection.getResponseCode();
	            if(responseCode == HttpURLConnection.HTTP_OK){
	                server_response = Utils.readStream(urlConnection.getInputStream());
	                //Log.v("CatalogClient", server_response);
	                
	                Message mess = handler.obtainMessage();
	                mess.what = 3;
	                mess.obj = server_response;
	                mess.sendToTarget();
	                
	            }

	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	            Message mess = handler.obtainMessage();
                mess.what = 3;
                mess.obj = "";
                mess.sendToTarget();
                
                
	        } catch (IOException e) {
	            e.printStackTrace();
	            
	            Message mess = handler.obtainMessage();
                mess.what = 3;
                mess.obj = "";
                mess.sendToTarget();
	        }

	        return "";
	    }

	    @Override
	    protected void onPostExecute(String s) {
	        super.onPostExecute(s);

	        //Log.e("Response", "" + server_response);

	    }
	}
	
	public static class DownloadROMThread extends Thread
	{
		
		public static interface DownloadTaskListener {
			public void onStart(DownloadROMThread thread);
			public void onEnd(DownloadROMThread thread);
			public void onProgress(DownloadROMThread thread);
		}


		String url;
		String localFile;
		String tmpFile;
		Handler handler;
		long totalSize;
		long downloadedSize;
		public int status;
		boolean pause;
		GameInfo gameInfo;
		public GameInfo getGameInfo() {
			return gameInfo;
		}
		public void setGameInfo(GameInfo gameInfo) {
			this.gameInfo = gameInfo;
		}
		int id;
		
		DownloadTaskListener listener;
		
		
		public boolean isPause() {
			return pause;
		}
		public void setPause(boolean pause) {
			this.pause = pause;
			if(this.pause)
				status = STATUS_PAUSED;
		}
		public void resumeDownload()
		{
			status = STATUS_DOWNLOADING;
			pause = false;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public static final int STATUS_INIT = 0;
		public static final int STATUS_DOWNLOADING = 1;
		public static final int STATUS_PAUSED = 2;
		public static final int STATUS_DONE = 3;
		public static final int STATUS_INTERRUPTED = 4;
		public static final int STATUS_DOWNLOAD_FAILED = 5;
		
		public DownloadROMThread(GameInfo gameInfo, DownloadTaskListener listener)
		{
			this.id = gameInfo.id;
			this.url = gameInfo.url;
			this.localFile = gameInfo.localFile;
			this.tmpFile = StorageHelper.getDownloadDIR() + gameInfo.fileName;
			this.totalSize = gameInfo.totalSize;
			this.downloadedSize = 0;
			this.gameInfo = gameInfo;
			status = STATUS_INIT;
			pause = false;
			this.listener = listener;
		}
		@Override
		public void run() {
			Log.i("THREAD DOWNLOAD", "" + this.getId() +"  " + this.url);
			this.listener.onStart(this);
			status = STATUS_DOWNLOADING;
			RandomAccessFile raf = null;
			
			HttpURLConnection conection = null;
			try {
				//conection = (HttpsURLConnection)(new URL(url)).openConnection();
				conection = NetCipher.getHttpsURLConnection(url);
				conection.setConnectTimeout(3000);
				conection.setReadTimeout(3000);
				conection.setRequestMethod("GET");
				
				this.totalSize = conection.getContentLength();
				File file = new File(tmpFile);
				if(file.exists() && file.canWrite() && file.delete())
				{
					file.createNewFile();
				}
				
				raf = new RandomAccessFile(tmpFile, "rwd");
				
				raf.seek(0);
				
				int count = 0;
				byte[] buffer = new byte[4096];
				
				BufferedInputStream is = new BufferedInputStream(conection.getInputStream(),4096);
				while((count = is.read(buffer)) != -1)
				{
					if(status == STATUS_PAUSED)
						continue;
					if(Thread.interrupted())
					{
						status = STATUS_INTERRUPTED;
						break;
					}
						
					downloadedSize += count;
					raf.write(buffer,0,count);
								
					Log.i("Thread",""+this.id +" down");
					
					listener.onProgress(this);
					
				}
				
				File local = new File(localFile);
				if(local.exists())
				{
					//local.delete();
				}
				
				file.renameTo(local);
				status = STATUS_DONE;
				
			}
			catch (IOException e) {
				status = STATUS_DOWNLOAD_FAILED;
				e.printStackTrace();
			}
			finally{
				if(raf != null)
					Utils.close(raf);	
				this.listener.onEnd(this);
			}
			
		}
		
	}

}


