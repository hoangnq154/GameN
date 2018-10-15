package com.hn.downloader;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hn.main.data.GameInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Downloader implements DownloadTaskListener {
	
	public static interface DownloadListener
	{
		public void onDownloadStarted(DownloadThread thread, int error);

		public void onDownloadProgress(DownloadThread thread);

		public void onDownloadEnd(DownloadThread thread, int error);
	}
	
	public static final int MAX_THREAD = 10;
	public static final int STATUS_TIMEOUT = 0;
	
	private static Downloader instance;
	public static Context staticContext = null;
	
	public static Downloader getInstance()
	{
		if(instance == null)
			instance = new Downloader(staticContext);
		return instance;
	}
	
	
	Map<Integer, DownloadThread> mapDownload = new HashMap<Integer, DownloadThread>();
	private DownloadListener listener;
	
	
	
	public DownloadListener getListener() {
		return listener;
	}

	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	public Downloader(Context context)
	{
		listener = null;
		instance = this;
	}
	
	public DownloadThread addTask(GameInfo game, Handler handler)
	{
		if(mapDownload.get(game.id) != null)
			return mapDownload.get(game.id);
		Log.i("DOWN","Add Task");

		DownloadThread dt = new DownloadThread(game, handler,this);
		mapDownload.put(game.id, dt);
		dt.start();

		return dt;

	}
	public DownloadThread getDownloadThread(int id)
	{
		return mapDownload.get(id);
	}
	public boolean pauseDownloadThread(int id)
	{
		DownloadThread thread = getDownloadThread(id);
		if(thread == null)
			return false;
		thread.setPause(true);
		return true;
	}
	
	public boolean resumeDownloadThread(int id)
	{
		DownloadThread thread = getDownloadThread(id);
		if(thread == null)
			return false;
		thread.resumeDownload();
		return true;
	}
	
	public void removeTask( int id)
	{
		if(mapDownload.get(id) != null)
		{
			mapDownload.get(id).interrupt();
			mapDownload.remove(id);
		}
	}
	
	public void quitAllDownload()
	{
		Collection<DownloadThread> list = mapDownload.values();
		for(Iterator<DownloadThread> iter = list.iterator();iter.hasNext();)
		{
			DownloadThread thread = iter.next();
			Log.i("QUIT DOWNLOAD ALL", "" + thread.id);
			thread.interrupt();
		}
		mapDownload.clear();
	}

	@Override
	public void onStart(DownloadThread thread) {
		if(listener != null)
		{
			listener.onDownloadStarted(thread, 0);
		}
	}

	@Override
	public void onEnd(DownloadThread thread) {
		Log.i("DOWN","on end");
		int error = 0;
		if(thread.status == DownloadThread.STATUS_DOWNLOAD_FAILED)
			error = 1;
		else if(thread.status == DownloadThread.STATUS_INTERRUPTED)
			error = 2;
		if(listener != null)
		{
			listener.onDownloadEnd(thread, error);
		}
		
		this.removeTask(thread.id);	
		
		
	}

	@Override
	public void onProgress(DownloadThread thread) {
		if(listener != null)
		{
			listener.onDownloadProgress(thread);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		Log.i("DOWNLOADER","SUPPENDED");
	}
	
	
	
}

