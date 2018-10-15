package com.hn.main.data;


public class GameInfo {

	public static final int TYPE_LIBRARY = 0;
	public static final int TYPE_LOCAL_DEVICE = 1;
	public static final int TYPE_PROMO = 2;
	public static final int TYPE_HEADING = 3;

	public int type = TYPE_LIBRARY;

	public int id = 0;

	public String name = "";
	
	public float rate = 0f;
	
	public String url = "";

	public String intro = "";

	public long totalSize = 0;
	
	public long downloaded = 0;

	public String md5 = "";

	public String localFile = ""; 
	
	public String fileName = "";
	
	public Object uerData = null;

//	public RomDatabase.RomDetail romDetail = null;

	public String artPath = "";

	public String artUrl = "";

	public String headerName = "";

	public void clone(GameInfo game)
	{
		game.name = name;
		game.id = id;
		game.url = url;
		game.artUrl = artUrl;
		game.artPath = artPath;
		game.md5 = md5;
		game.localFile = localFile;
		game.rate = rate;
		game.fileName = fileName;
		game.intro = intro;
		game.headerName = headerName;
	}

//	public void generateArtLink(AppData mAppData,GlobalPrefs mGlobalPrefs)
//	{
//		if(!md5.equals(""))
//		{
//			final RomDatabase database = RomDatabase.getInstance();
//
//			if(!database.hasDatabaseFile())
//			{
//				database.setDatabaseFile(mAppData.mupen64plus_ini);
//			}
//			romDetail= database.lookupByMd5(md5);
//
//			if(romDetail == null)
//				return;
//			artUrl = romDetail.artUrl;
//			artPath = mGlobalPrefs.coverArtDir + "/" + romDetail.artName;
//
//		}
//	}
	
	public String createLocalFile()
	{
		try {
			
			String[] list = this.url.split("\\?");
			String originalURL = list[0];
			String[] tmp = originalURL.split("\\/");
			
			fileName = tmp[tmp.length-1];
			
			localFile = StorageHelper.getDefaultROMsDIR() + "roms/" + fileName;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Log.i("LOCAL PATH" , localFile);
		return localFile;
	}
	
}
