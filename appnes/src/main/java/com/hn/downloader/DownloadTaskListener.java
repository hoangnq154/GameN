package com.hn.downloader;

public interface DownloadTaskListener {
	public void onStart(DownloadThread thread);
	public void onEnd(DownloadThread thread);
	public void onProgress(DownloadThread thread);
}
