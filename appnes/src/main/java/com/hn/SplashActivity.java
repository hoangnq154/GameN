/*
 * Mupen64PlusAE, an N64 emulator for the Android platform
 *
 * Copyright (C) 2012 Paul Lamb
 *
 * This file is part of Mupen64PlusAE.
 *
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with Mupen64PlusAE. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors: paulscode, lioncash, littleguy77
 */

package com.hn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hn.main.data.DataServerManager;
import com.hn.main.data.GameDataManager;
import com.hn.main.data.StorageHelper;
import com.hn.main.helpers.AdmobHelper;
import com.hn.utils.AppData;
import com.hn.utils.Utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import nostalgia.appnes.R;


/**
 * The main activity that presents the splash screen, extracts the assets if necessary, and launches
 * the main menu activity.
 */
public class SplashActivity extends Activity implements OnRequestPermissionsResultCallback, DataServerManager.DownloadROMThread.DownloadTaskListener
{
    //Permission request ID
    static final int PERMISSION_REQUEST = 177;

    //Total number of permissions requested
    static final int NUM_PERMISSIONS = 2;

    /**
     * Asset version number, used to determine stale assets. Increment this number every time the
     * assets are updated on disk.
     */
    private static final int ASSET_VERSION = 136;

    /**
     * The subdirectory within the assets directory to extract. A subdirectory is necessary to avoid
     * extracting all the default system assets in addition to ours.
     */
    private static final String SOURCE_DIR = "mupen64plus_data";

    /** The text view that displays extraction progress info. */
    private TextView mTextView;

    /** The running count of assets extracted. */
    private int mAssetsExtracted;

    // App data and user preferences
    public AppData mAppData = null;
//    public GlobalPrefs mGlobalPrefs = null;

    // These constants must match the keys used in res/xml/preferences*.xml
    private static final String DISPLAY_SCALING = "displayScaling";
    private static final String VIDEO_HARDWARE_TYPE = "videoHardwareType";
    private static final String AUDIO_PLUGIN = "audioPlugin";
    private static final String TOUCHSCREEN_AUTO_HOLD = "touchscreenAutoHold";
    private static final String NAVIGATION_MODE = "navigationMode";
    private static final String GAME_DATA_PATH = "pathGameSaves";
    private static final String APP_DATA_PATH = "pathAppData";

    final static public  int BUFFER_SIZE = 1024*48;
    final static int TAG_LOAD = 1;
    final static int TAG_SPLASH = 2;
    final static int TAG_HTTP = 3;
    final static int TAG_DOWN_ROM = 4;
    final static int TAG_LOAD_ROM_DB = 5;
    final static int TAG_COPY_MK4 = 6;



    private static final long SPLASH_MIN_TIME = 1000;

    boolean done_load = false;
    boolean done_splash = false;
    boolean done_http = false;
    boolean done_load_rom = false;
    boolean done_mk4 = false;

    int count_http_failed = 0;

    DataServerManager dataServerMgr;


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        StorageHelper.checkRomDir(getApplicationContext());
        // Don't let the activity sleep in the middle of extraction
        getWindow().setFlags( LayoutParams.FLAG_KEEP_SCREEN_ON, LayoutParams.FLAG_KEEP_SCREEN_ON );
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);


        final Resources res = getResources();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        //Check for invalid data path
//        String defaultRelPath = res.getString(R.string.pathGameSaves_default);
//        String gameDataPathString = prefs.getString( GAME_DATA_PATH, null );
//        if(TextUtils.isEmpty(gameDataPathString) || gameDataPathString.contains(defaultRelPath))
//        {
////            String newDefValue = PathPreference.validate(defaultRelPath);
////            prefs.edit().putString( GAME_DATA_PATH, newDefValue ).apply();
////            gameDataPathString = prefs.getString( GAME_DATA_PATH, null );
//        }
//
//        String appDataPathString = prefs.getString( APP_DATA_PATH, null );
//        if(TextUtils.isEmpty(appDataPathString) || appDataPathString.contains(defaultRelPath))
//        {
//            prefs.edit().putString( APP_DATA_PATH, gameDataPathString ).apply();
//        }

//        // Get app data and user preferences
        mAppData = new AppData( this );
//        mGlobalPrefs = new GlobalPrefs( this, mAppData );



//        // Ensure that any missing preferences are populated with defaults (e.g. preference added to
//        // new release)
//        PreferenceManager.setDefaultValues( this, R.xml.preferences_audio, false );
//        PreferenceManager.setDefaultValues( this, R.xml.preferences_data, false );
//        PreferenceManager.setDefaultValues( this, R.xml.preferences_display, false );
//        PreferenceManager.setDefaultValues( this, R.xml.preferences_input, false );
//        PreferenceManager.setDefaultValues( this, R.xml.preferences_library, false );
//        PreferenceManager.setDefaultValues( this, R.xml.preferences_touchscreen, false );
//
//        // Ensure that selected plugin names and other list preferences are valid
//        // @formatter:off
//
//        PrefUtil.validateListPreference( res, prefs, DISPLAY_SCALING,          R.string.displayScaling_default,        R.array.displayScaling_values );
//        PrefUtil.validateListPreference( res, prefs, VIDEO_HARDWARE_TYPE,      R.string.videoHardwareType_default,     R.array.videoHardwareType_values );
//        PrefUtil.validateListPreference( res, prefs, AUDIO_PLUGIN,             R.string.audioPlugin_default,           R.array.audioPlugin_values );
//        PrefUtil.validateListPreference( res, prefs, TOUCHSCREEN_AUTO_HOLD,    R.string.touchscreenAutoHold_default,   R.array.touchscreenAutoHold_values );
//        PrefUtil.validateListPreference( res, prefs, NAVIGATION_MODE,          R.string.navigationMode_default,        R.array.navigationMode_values );
//
//        // @formatter:on
//
//        // Refresh the preference data wrapper
//        mGlobalPrefs = new GlobalPrefs( this, mAppData );
//
//        // Make sure custom skin directory exist
//        FileUtil.makeDirs(mGlobalPrefs.touchscreenCustomSkinsDir);
//        FileUtil.makeDirs(mGlobalPrefs.coverArtDir);
//
//
//        // Initialize the toast/status bar notifier
//        Notifier.initialize( this );


        // Lay out the content
        setContentView( R.layout.intro_scene );
        mTextView = findViewById( R.id.lb_loading );

//        requestPermissions();


        dataServerMgr = new DataServerManager(handler);
        dataServerMgr.checkServerFake();
       requestPermissions();


    }

    public void requestPermissions()
    {
        //This doesn't work reliably with older Android versions
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                AppData.IS_LOLLIPOP)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                //Show dialog asking for permissions
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.assetExtractor_permissions_title))
                        .setMessage(getString(R.string.assetExtractor_permissions_rationale))
                        .setPositiveButton(getString(android.R.string.ok), new OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                actuallyRequestPermissions();
                            }

                        }).setNegativeButton(getString(android.R.string.cancel), new OnClickListener()
                {
                    //Show dialog stating that the app can't continue without proper permissions
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new AlertDialog.Builder(SplashActivity.this).setTitle(getString(R.string.assetExtractor_error))
                                .setMessage(getString(R.string.assetExtractor_failed_permissions))
                                .setPositiveButton(getString( android.R.string.ok ), new OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        SplashActivity.this.finish();
                                    }

                                }).setCancelable(false).show();
                    }
                }).setCancelable(false).show();
            }
            else
            {
                // No explanation needed, we can request the permission.
                actuallyRequestPermissions();
            }
        }
        else
        {
            checkExtractAssets();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message mess= handler.obtainMessage();
                    mess.what = TAG_SPLASH;
                    mess.sendToTarget();
                }
            }, SPLASH_MIN_TIME);
        }
    }

    @SuppressLint("InlinedApi")
    public void actuallyRequestPermissions()
    {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST:
            {
                // If request is cancelled, the result arrays are empty.
                boolean good = true;
                if (permissions.length != NUM_PERMISSIONS || grantResults.length != NUM_PERMISSIONS)
                {
                    good = false;
                }

                for (int i = 0; i < grantResults.length && good; i++)
                {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        good = false;
                    }
                }

                if (!good)
                {
                    // permission denied, boo! Disable the app.
                    new AlertDialog.Builder(SplashActivity.this).setTitle(getString(R.string.assetExtractor_error))
                            .setMessage(getString(R.string.assetExtractor_failed_permissions))
                            .setPositiveButton(getString( android.R.string.ok ), new OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    SplashActivity.this.finish();
                                }

                            }).setCancelable(false).show();
                }
                else
                {
                    //Permissions already granted, continue
                    checkExtractAssets();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Message mess= handler.obtainMessage();
                            mess.what = TAG_SPLASH;
                            mess.sendToTarget();
                        }
                    }, SPLASH_MIN_TIME);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    private void checkExtractAssets()
    {
        if (mAppData.getAppVersion() != mAppData.appVersionCode)
        {
            mAppData.putAppVersion(mAppData.appVersionCode);

        }


        if( mAppData.getAssetVersion() != ASSET_VERSION )
        {
            // Extract the assets in a separate thread and launch the menu activity
            // Handler.postDelayed ensures this runs only after activity has resumed
            new Thread(extractAssetsTaskLauncher).start();

        }
        else
        {
            new Thread(loadROMDB).start();
            Message mess= handler.obtainMessage();
            mess.what = TAG_LOAD;
            mess.sendToTarget();
        }
        new Thread(copyMK4).start();
    }

    /** Runnable that launches the non-UI thread from the UI thread after the activity has resumed. */
    private final Runnable extractAssetsTaskLauncher = new Runnable()
    {
        @Override
        public void run()
        {
            //extractAssets();
            copyFiles();
            Message mess= handler.obtainMessage();
            mess.what = TAG_LOAD;
            mess.sendToTarget();
            new Thread(loadROMDB).start();
        }
    };

    private final Runnable loadROMDB = new Runnable() {
        @Override
        public void run() {
            loadROMData();
            Message mess= handler.obtainMessage();
            mess.what = TAG_LOAD_ROM_DB;
            mess.sendToTarget();
        }
    };

    private final Runnable copyMK4 = new Runnable() {
        @Override
        public void run() {
            copyROM();
            Message mess= handler.obtainMessage();
            mess.what = TAG_COPY_MK4;
            mess.sendToTarget();
        }
    };

    Handler handler = new Handler(Looper.getMainLooper())
    {

        @Override
        public void handleMessage(Message msg) {
            Log.i("HOANG","done :" + msg.what);
            switch (msg.what) {
                case TAG_LOAD:      // copy Assets done!
                    mAppData.putAssetVersion( ASSET_VERSION );
                    done_load = true;
                    checkMoveMain();
                    break;
                case TAG_COPY_MK4:      // copy MK4
                    done_mk4 = true;
                    checkMoveMain();
                    break;
                case TAG_SPLASH:    // tag Splash
                    done_splash = true;
                    checkMoveMain();
                    break;
                case TAG_HTTP:
                {
                    String http_response = (String)msg.obj;
                    if(http_response.equals(""))			// http failed
                    {
                        if(!dataServerMgr.daTungCheckVersion(getApplicationContext()))
                        {
                            dataServerMgr.checkServerFake();

                            count_http_failed++;
                            if(count_http_failed > 2)
                            {

                                Toast.makeText(SplashActivity.this, "You need network for first init game.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        else
                        {
                            Log.i("CHECK VERSION","FAILED NHUNG DA LUU TRUOC DO");
                            done_http = true;
                            checkMoveMain();
                        }

                    }
                    else
                    {
                        Log.e("CHECK VERSION DONE :", http_response);
                        dataServerMgr.savePreference(getApplicationContext(), http_response);

                        boolean needDownloadROM = dataServerMgr.checkRomData(SplashActivity.this.getApplicationContext(), SplashActivity.this);
                        if(!needDownloadROM)
                        {
                            Log.i("CHECK DOWNLOAD ROM","ROM SERVER DA DUOC CAP NHAT");
                            done_http = true;
                            checkMoveMain();
                        }
                    }


                    break;
                }
                case TAG_DOWN_ROM:
                {
                    int status = (Integer)msg.obj;
                    if(status == DataServerManager.DownloadROMThread.STATUS_DONE)
                    {
                        dataServerMgr.saveNewVersionToCurrentVersion(getApplicationContext());
                    }
                    done_http = true;
                    GameDataManager.getInstance().loadMoreGameServer();
                    checkMoveMain();

                    break;
                }
                case TAG_LOAD_ROM_DB:
                {
                    done_load_rom = true;
                    checkMoveMain();
                    break;
                }

                default:
                    break;
            }
        }

    };

    private void checkMoveMain()
    {
        if(done_load && done_splash && done_http && done_load_rom && done_mk4)
        {
            mTextView.setText( R.string.assetExtractor_finished );
            startActivity(new Intent(this,LobbyActivity.class));
            finish();
        }
    }

    private void loadROMData() {
        try {
            GameDataManager.getInstance().loadData(this);
//            GameDataManager.getInstance().loadMoreGameServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void copyROM()
    {
//        File file = new File(StorageHelper.getDefaultROMsDIR() +"/roms");
//        if(!file.exists())
//            return;
//        file = new File(StorageHelper.getDefaultROMsDIR() +"/roms/mk4.n64");
//        if(file.exists())
//            return;
//        RandomAccessFile raf = null;
//        try
//        {
//            file.createNewFile();
//            raf =  new RandomAccessFile(file, "rwd");
//            raf.seek(0);
//
//            int count = 0;
//            byte[] buffer = new byte[8192];
//
//            InputStream is = getAssets().open("data");
//            BufferedInputStream bis = new BufferedInputStream(is,8192);
//
//            while ((count = bis.read(buffer)) != -1)
//                raf.write(buffer,0,count);
//
//        }
//        catch (IOException e)
//        {
//
//        }
//        finally {
//            if(raf != null)
//                Utils.close(raf);
//        }


    }


    public void copyFiles(){
        try {

            // Create a ZipInputStream to read the zip file
            BufferedOutputStream dest = null;
            InputStream fis = getResources().openRawResource(R.raw.dat);
            ZipInputStream zis = new ZipInputStream(

                    new BufferedInputStream(fis));
            // Loop over all of the entries in the zip file
            int count;
            byte data[] = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Log.e("HOANG","" + entry);
                if (!entry.isDirectory()) {

                    String destination = StorageHelper.getROMsDIR();
                    String destFN = destination + File.separator + entry.getName();
                    // Write the file to the file system
                    FileOutputStream fos = new FileOutputStream(destFN);
                    dest = new BufferedOutputStream(fos, BUFFER_SIZE);
                    while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
                else
                {
                    File f = new File( StorageHelper.getROMsDIR()+ File.separator + entry.getName());
                    f.mkdirs();
                }

            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(DataServerManager.DownloadROMThread thread) {

    }

    @Override
    public void onEnd(DataServerManager.DownloadROMThread thread) {
        Message mess = handler.obtainMessage();
        mess.what = TAG_DOWN_ROM;
        mess.obj = thread.status;
        mess.sendToTarget();

        Log.i("DOWNLOAD ROM" , thread.status +"  " + thread.getGameInfo().localFile);
    }

    @Override
    public void onProgress(DataServerManager.DownloadROMThread thread) {

    }
}
