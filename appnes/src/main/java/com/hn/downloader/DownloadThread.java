package com.hn.downloader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hn.main.data.GameInfo;
import com.hn.main.data.StorageHelper;
import com.hn.utils.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;

public class DownloadThread extends Thread
{
	String url;
	String localFile;
	String tmpFile;
	volatile Handler handler = null;
	long time_start;
	long totalSize;
	long downloadedSize;
	public volatile int status;
	int currentPercentDownload;
	volatile boolean pause;
	GameInfo gameInfo;
	public GameInfo getGameInfo() {
		return gameInfo;
	}
	public void setGameInfo(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
	}
	public int id;

	volatile boolean pushUI = true;

	public boolean isPushUI() {
		return pushUI;
	}

	public void setPushUI(boolean pushUI) {
		this.pushUI = pushUI;
	}

	DownloadTaskListener listener;

	public boolean isPause() {
		return pause;
	}
	public Handler getHandler() {
		return handler;
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	public synchronized void setPause(boolean pause) {
		this.pause = pause;
		if(this.pause)
			status = STATUS_PAUSED;
	}
	public synchronized void resumeDownload()
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
	
	public DownloadThread(GameInfo gameInfo, Handler handler, DownloadTaskListener listener)
	{
		this.id = gameInfo.id;
		this.url = gameInfo.url;
		this.localFile = gameInfo.localFile;
		this.tmpFile = StorageHelper.getDownloadDIR() + gameInfo.fileName;
		this.handler = handler;
		gameInfo.downloaded = 0;
		this.totalSize = gameInfo.totalSize;
		this.downloadedSize = 0;
		this.gameInfo = gameInfo;
		status = STATUS_INIT;
		pause = false;
		this.listener = listener;
		currentPercentDownload = 0;
	}
	@Override
	public void run() {
		Log.i("THREAD DOWNLOAD", "" + this.id +"  " + this.url);
		this.listener.onStart(this);
		status = STATUS_DOWNLOADING;
		RandomAccessFile raf = null;
		time_start = System.currentTimeMillis();
		
		HttpsURLConnection conection = null;
		try {
			conection = NetCipher.getHttpsURLConnection(url);
			conection.setConnectTimeout(10000);
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
			while(true)
			{

				if(status == STATUS_PAUSED)
				{
					continue;
				}
				if((count = is.read(buffer)) == -1)
					break;
				if(Thread.interrupted())
				{
					status = STATUS_INTERRUPTED;
					break;
				}

				downloadedSize += count;
				raf.write(buffer,0,count);
				gameInfo.downloaded = downloadedSize;
				gameInfo.totalSize = totalSize;

				long current_time = System.currentTimeMillis();
					if(pushUI && handler != null)
					{
						Message mess = handler.obtainMessage();
						mess.what = 0;
						mess.obj = gameInfo;
						mess.sendToTarget();
					}

					time_start = current_time;
			}
			file.renameTo(new File(localFile));
			status = STATUS_DONE;
			
		}
		catch (IOException e) {

			if(pushUI && handler != null)
			{
				Message mess = handler.obtainMessage();
				mess.what = 1;
				gameInfo.downloaded = downloadedSize;
				gameInfo.totalSize = totalSize;
				mess.obj = gameInfo;
				mess.sendToTarget();
			}


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