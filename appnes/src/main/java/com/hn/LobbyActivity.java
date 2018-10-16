package com.hn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hn.downloader.DownloadThread;
import com.hn.downloader.Downloader;
import com.hn.main.RecyclingImageView;
import com.hn.main.data.Constant;
import com.hn.main.data.DataServerManager;
import com.hn.main.data.GameDataManager;
import com.hn.main.data.GameInfo;
import com.hn.main.data.StorageHelper;
import com.hn.main.helpers.AdmobHelper;
import com.hn.utils.Utils;
import com.hn.utils.util.ComputeMd5Task;
import com.hn.utils.util.ImageCache;
import com.hn.utils.util.ImageFetcher;
import com.hn.utils.util.ImageResizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;
import nostalgia.appnes.NesEmulatorActivity;
import nostalgia.appnes.R;
import nostalgia.framework.base.EmulatorActivity;
import nostalgia.framework.ui.gamegallery.GameDescription;

//import x0163.n64.nes.snes.gba.gbc.mame.R;
//import info.guardianproject.netcipher.NetCipher;
//import paulscode.android.mupen64plusae.billing.IabHelper;
//import paulscode.android.mupen64plusae.billing.IabResult;
//import paulscode.android.mupen64plusae.billing.Inventory;
//import paulscode.android.mupen64plusae.billing.Purchase;
//import paulscode.android.mupen64plusae.dialog.ConfirmationDialog;
//import paulscode.android.mupen64plusae.dialog.Popups;
//import paulscode.android.mupen64plusae.persistent.AppData;
//import paulscode.android.mupen64plusae.persistent.ConfigFile;
//import paulscode.android.mupen64plusae.persistent.GamePrefs;
//import paulscode.android.mupen64plusae.persistent.GlobalPrefs;
//import paulscode.android.mupen64plusae.task.ComputeMd5Task;
//import paulscode.android.mupen64plusae.util.FileUtil;
//import paulscode.android.mupen64plusae.util.LocaleContextWrapper;
//import paulscode.android.mupen64plusae.util.Notifier;
//import paulscode.android.mupen64plusae.util.RomDatabase;
//import paulscode.android.mupen64plusae.util.RomHeader;

/**
 * Created by hoangnguyen on 3/2/18.
 */

public class LobbyActivity extends AppCompatActivity /*implements MenuListView.OnClickListener,GameSidebar.GameSidebarActionHandler*/ {

    private static final String STATE_QUERY = "query";
    private static final String STATE_SIDEBAR = "sidebar";
    private static final String STATE_CACHE_ROM_INFO_FRAGMENT = "STATE_CACHE_ROM_INFO_FRAGMENT";
    private static final String STATE_DONATION_ITEM = "STATE_DONATION_ITEM";
    private static final String STATE_EXTRACT_TEXTURES_FRAGMENT = "STATE_EXTRACT_TEXTURES_FRAGMENT";
    private static final String STATE_EXTRACT_ROM_FRAGMENT = "STATE_EXTRACT_ROM_FRAGMENT";
    private static final String STATE_GALLERY_REFRESH_NEEDED = "STATE_GALLERY_REFRESH_NEEDED";
    private static final String STATE_GAME_STARTED_EXTERNALLY = "STATE_GAME_STARTED_EXTERNALLY";
    private static final String STATE_RESTART_CONFIRM_DIALOG = "STATE_RESTART_CONFIRM_DIALOG";
    private static final String STATE_CLEAR_CONFIRM_DIALOG = "STATE_CLEAR_CONFIRM_DIALOG";
    private static final String STATE_REMOVE_FROM_LIBRARY_DIALOG = "STATE_REMOVE_FROM_LIBRARY_DIALOG";
    private static final String STATE_DONATION_DIALOG = "STATE_DONATION_DIALOG";

    private static final String TAG = "LobbyActivity";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    public static final int RESTART_CONFIRM_DIALOG_ID = 0;
    public static final int CLEAR_CONFIRM_DIALOG_ID = 1;
    public static final int REMOVE_FROM_LIBRARY_DIALOG_ID = 2;

    public static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzab87txLhoMQuzoUSCfnjSbbd3LkbPd/qVxjL2VBWFUfdK45n8EgC5lkX1kArZ/ybCLeEzf6vJDwhBeao6Y4nLEnF2On7yKGs3U+u1sumfSQILZ8I+l7KRfvS0Fgx4Iz4T7WdtdtUP74iF0c+m3bap0Ds8XbLIMiZT07yatv8whuUNR5jCmI9z3YSSK88LzcMHg0QEWzrjGaVpORMGYTM7gZJj/1vGK3cD06uT/dlwdwvqthqg35Wdy+zv59DeftzfE3IzbKWRlXfqs67IyhHrWHkaINR8D0r7qjcX7YmRD+5dUipLKX3e3V7aJFqWlKHJecprmeUsX+/vST5uZ7bwIDAQAB";
    public static final String SKU_PREMIUM = "upgrade_pro";
    private static final int PURCHASE_REQUEST_CODE = 10;

    RecyclerView recyclerView;

    // Searching
    private SearchView mSearchView;
    private String mSearchQuery = "";

    private DrawerLayout mDrawerLayout = null;
    //    private MenuListView mMenu = null;
//    private GameSidebar mGameSidebar;
    private ImageResizer mImageResizer;
    MyAdapter adapter;


    // App data and user preferences
//    public AppData mAppData = null;
//    public GlobalPrefs mGlobalPrefs = null;
    DataServerManager dataServer;


    public GameInfo selectedGame;

//    private ScanRomsFragment mCacheRomInfoFragment = null;
//    private ExtractTexturesFragment mExtractTexturesFragment = null;
//    private ExtractRomFragment mExtractRomFragment = null;

    // for data
    List<GameInfo> promoDatas = new ArrayList<>();
    List<GameInfo> localDatas = new ArrayList<>();
    List<GameInfo> headingDatas = new ArrayList<>();
    List<GameInfo> libraryDatas = new ArrayList<>();
    List<GameInfo> finalDatas = new ArrayList<>();

    private boolean hide_icon = false;
    private boolean hide_game = false;
    Menu topMenu = null;
    private AdmobHelper admobHelper = null;

    //    IabHelper iabHelper = null;
    boolean isPremium = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();

        // Don't let the activity sleep in the middle of extraction
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Get app data and user preferences
        StorageHelper.checkRomDir(getApplicationContext());
//        mAppData = new AppData(this);
//        mGlobalPrefs = new GlobalPrefs(this, mAppData);
        dataServer = new DataServerManager(null);
        dataServer.loadPreference(getApplicationContext());

        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(this, "thumbs");
        params.setMemCacheSizePercent(.25f);
        if (mImageResizer == null) {
            mImageResizer = new ImageResizer(this, 256);
            mImageResizer.setLoadingImage(R.drawable.default_coverart);
            mImageResizer.addImageCache(getSupportFragmentManager(), params);
            mImageResizer.setTypeDecode(ImageResizer.TYPE_DECODE_HTTP);
        }


        setContentView(R.layout.lobby_activity);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new MyAdapter(this, finalDatas, false, mImageResizer);
        adapter.setDefault_cover(getResources().getDrawable(R.drawable.default_coverart));


        //setup menu
        mDrawerLayout = findViewById(R.id.lobbyDrawerLayout);
//        mMenu = findViewById(R.id.drawerNavigation);
//        mMenu.setMenuResource(R.menu.gallery_drawer);
//        mMenu.setBackground(new DrawerDrawable(mGlobalPrefs.displayActionBarTransparency));
//        mMenu.setOnClickListener(this);
//
//        // Configure the game information drawer
//        mGameSidebar = findViewById(R.id.gameSidebar);
//        mGameSidebar.setBackground(new DrawerDrawable(mGlobalPrefs.displayActionBarTransparency));
//
//        // Handle events from the side bar
//        mGameSidebar.setActionHandler(this, R.menu.gallery_game_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("N64 Emulator");
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Hide the game information sidebar
//                mMenu.setVisibility(View.VISIBLE);
//                mGameSidebar.setVisibility(View.GONE);
                recyclerView.requestFocus();

                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        //
        // find the retained fragment on activity restarts
        final FragmentManager fm = getSupportFragmentManager();
//        mCacheRomInfoFragment = (ScanRomsFragment) fm.findFragmentByTag(STATE_CACHE_ROM_INFO_FRAGMENT);
//        mExtractTexturesFragment = (ExtractTexturesFragment) fm.findFragmentByTag(STATE_EXTRACT_TEXTURES_FRAGMENT);
//        mExtractRomFragment = (ExtractRomFragment) fm.findFragmentByTag(STATE_EXTRACT_ROM_FRAGMENT);
//
//        if (mCacheRomInfoFragment == null) {
//            mCacheRomInfoFragment = new ScanRomsFragment();
//            fm.beginTransaction().add(mCacheRomInfoFragment, STATE_CACHE_ROM_INFO_FRAGMENT).commit();
//        }
//
//        if (mExtractTexturesFragment == null) {
//            mExtractTexturesFragment = new ExtractTexturesFragment();
//            fm.beginTransaction().add(mExtractTexturesFragment, STATE_EXTRACT_TEXTURES_FRAGMENT).commit();
//        }
//
//        if (mExtractRomFragment == null) {
//            mExtractRomFragment = new ExtractRomFragment();
//            fm.beginTransaction().add(mExtractRomFragment, STATE_EXTRACT_ROM_FRAGMENT).commit();
//        }


        loadRomLocal();
        refreshLibrary();
//        mMenu.setPremium(dataServer.isPremium);

//        admobHelper = new AdmobHelper(this, dataServer);
        //admobHelper.loadPage(AdmobHelper.getID());

//        if(hide_game)
//            Notifier.showToast2(this,"Please add game from Menu");

//        if (dataServer.newVersionCode > Utils.getVersionCode(getApplicationContext()) && !dataServer.isPremium) {
//            this.createDialogUpdate(dataServer.message_update, dataServer.forceUpdate).show();
//        }

    }


    void checkOpenAllGame() {
        // check cho nguoi choi cu±
        boolean da_cai_game_tu_truoc = false;
        if (dataServer.getIntForKey(this, "da_tung_bat", 0) == 1) {
            da_cai_game_tu_truoc = true;
        }
        hide_game = dataServer.getIntForKey(this, "hide_game", 0) == 0;
//        mMenu.setHideGame(hide_game);

        if (topMenu != null)
            topMenu.setGroupVisible(0, true);

        if (da_cai_game_tu_truoc) {
            dataServer.saveIntForKey(this, "hide_game", 1);
            hide_game = false;
//            mMenu.setHideGame(hide_game);


        }
//        if(hide_game)
//        {
//            if(topMenu != null)
//                topMenu.setGroupVisible(0,false);
//            finalDatas.clear();
//
//        }

    }

    void pressAddAllGame() {
        dataServer.saveIntForKey(this, "hide_game", 1);
        hide_game = false;
//        mMenu.setHideGame(hide_game);

        mDrawerLayout.closeDrawer(GravityCompat.START);

        refreshLibrary();
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    private String downloadIMG(ImageFetcher.ImageData dataImg) {
        String ret = "";
        String tmpFile;

        String realPathArt = dataImg.imageSavePath;

        if (realPathArt.contains(".png") || realPathArt.contains(".jpg")) {
            tmpFile = realPathArt.substring(0, realPathArt.length() - 4);
        } else
            tmpFile = realPathArt;

        RandomAccessFile raf = null;

        HttpURLConnection conection = null;
        try {
            //conection = (HttpsURLConnection)(new URL(url)).openConnection();
            conection = NetCipher.getHttpURLConnection(dataImg.imageURL);
            conection.setConnectTimeout(10000);
            conection.setRequestMethod("GET");

            File file = new File(tmpFile);
            if (file.exists() && file.canWrite() && file.delete()) {
                file.createNewFile();
            }

            raf = new RandomAccessFile(tmpFile, "rwd");

            raf.seek(0);

            int count = 0;
            byte[] buffer = new byte[4096];
            boolean inter = false;

            BufferedInputStream is = new BufferedInputStream(conection.getInputStream(), 4096);
            while ((count = is.read(buffer)) != -1) {
                if (Thread.interrupted()) {
                    inter = true;
                    break;
                }

                raf.write(buffer, 0, count);
            }
            if (!inter) {
                file.renameTo(new File(dataImg.imageSavePath));
                return dataImg.imageSavePath;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null)
                com.hn.utils.Utils.close(raf);
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate( R.menu.gallery_activity_2, menu );
//        this.topMenu = menu;
//        //this.topMenu.setGroupVisible(0,!hide_game);
//        final MenuItem searchItem = menu.findItem( R.id.menuItem_search );
//
//        searchItem.setOnActionExpandListener( new MenuItem.OnActionExpandListener()
//        {
//            @Override
//            public boolean onMenuItemActionCollapse( MenuItem item )
//            {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionExpand( MenuItem item )
//            {
//                return true;
//            }
//        } );
//
//        mSearchView = (SearchView) searchItem.getActionView();
//        mSearchView.setOnQueryTextListener( new SearchView.OnQueryTextListener()
//        {
//            @Override
//            public boolean onQueryTextSubmit( String query )
//            {
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange( String query )
//            {
//                mSearchQuery = query;
//                refreshLibrary();
//                return false;
//            }
//        } );
//
//        if( !"".equals( mSearchQuery ) )
//        {
//            final String query = mSearchQuery;
//            searchItem.expandActionView();
//            mSearchView.setQuery( query, true );
//        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Show the navigation drawer when the user presses the Menu button
            // http://stackoverflow.com/q/22220275
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (dataServer.getIntForKey(getApplicationContext(), Constant.KEY_STORAGE_RATE, 0) == 0) {
            if (!hide_icon) {
                createDialogRate(this).show();
                return;
            }

        }

        GameInfo gamePromo = null;
        for (int i = 0; i < dataServer.promoDatas.size(); i++) {
            if (!Utils.isAppInstalled(this, dataServer.promoDatas.get(i).url)) {
                gamePromo = dataServer.promoDatas.get(i);
                break;
            }
        }
        if (gamePromo != null && (Boolean) gamePromo.uerData && !dataServer.isFake) {
            if (!hide_icon)
                this.createDialogPromotion(gamePromo, true).show();
            else
                quit();
        } else
            quit();

    }

    public android.app.Dialog createDialogRate(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle("Rate for us!")
                .setMessage("If you like our game, please rate 5 star for us. Many thanks!")
                .setNeutralButton("Remind later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //remindMeLater();
                        quit();
                    }
                })
                .setPositiveButton("Rate now", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                rateNow();


                            }
                        }

                )
                .setCancelable(false)
                .create();
    }

    private void rateNow() {
        openURLUpdate();

        if (dataServer != null) {
            dataServer.saveIntForKey(getApplicationContext(), Constant.KEY_STORAGE_RATE, 1);
        }
    }


    public Dialog createDialogUpdate(final String update_message, final boolean force) {
        if (force)
            return new AlertDialog.Builder(this)
                    .setTitle("Version Update")
                    .setMessage(update_message)
                    .setPositiveButton("Update now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    updateNow();

                                    LobbyActivity.this.createDialogUpdate(update_message, force).show();
                                }
                            }


                    )
                    .setCancelable(false)
                    .create();
        else
            return new AlertDialog.Builder(this)
                    .setTitle("Version Update")
                    .setMessage(update_message)
                    .setNeutralButton("Remind later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            quit();
                        }
                    })
                    .setPositiveButton("Update now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    updateNow();

                                    LobbyActivity.this.createDialogUpdate(update_message, force).show();

                                }
                            }

                    )
                    .setCancelable(false)
                    .create();
    }

    private void updateNow() {
        if (dataServer != null) {
            final Intent t = new Intent(Intent.ACTION_VIEW);
            String URL = dataServer.update_package.equals("") ? ("market://details?id=" + getApplicationContext().getPackageName()) : ("market://details?id=" + dataServer.update_package);
            t.setData(Uri.parse(URL));
            startActivity(t);
        } else {
            openURLUpdate();
        }
    }

    void openURLUpdate() {
        final Intent t = new Intent(Intent.ACTION_VIEW);
        String URL = "market://details?id=" + getApplicationContext().getPackageName();
        t.setData(Uri.parse(URL));
        startActivity(t);
    }

    private void quit() {
        moveTaskToBack(true);
    }

//    @Override
//    public boolean onKey(View view, int i, KeyEvent keyEvent) {
//        return false;
//    }


    public boolean DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) for (File child : fileOrDirectory.listFiles())
            DeleteRecursive(child);
        fileOrDirectory.delete();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId())
//        {
//            case R.id.menuItem_refreshRoms:
//                ActivityHelper.startRomScanActivity(this);
//                return true;
//            case R.id.menuItem_library:
//                if(dataServer.isFake)
//                {
//                    mDrawerLayout.closeDrawer(GravityCompat.START);
//                }
//                else
//                {
//                    pressAddAllGame();
//                }
//                return true;
//            case R.id.menuItem_categoryLibrary:
//                ActivityHelper.startLibraryPrefsActivity( this );
//                return true;
//            case R.id.menuItem_categoryDisplay:
//                ActivityHelper.startDisplayPrefsActivity( this );
//                return true;
//            case R.id.menuItem_categoryAudio:
//                ActivityHelper.startAudioPrefsActivity( this );
//                return true;
//            case R.id.menuItem_categoryTouchscreen:
//                ActivityHelper.startTouchscreenPrefsActivity( this );
//                return true;
//            case R.id.menuItem_categoryInput:
//                ActivityHelper.startInputPrefsActivity( this );
//                return true;
//            case R.id.menuItem_categoryData:
//                ActivityHelper.startDataPrefsActivity( this );
//                return true;
//            case R.id.menuItem_emulationProfiles:
//                ActivityHelper.startManageEmulationProfilesActivity(this);
//                return true;
//            case R.id.menuItem_touchscreenProfiles:
//                ActivityHelper.startManageTouchscreenProfilesActivity(this);
//                return true;
//            case R.id.menuItem_controllerProfiles:
//                ActivityHelper.startManageControllerProfilesActivity(this);
//                return true;
////        case R.id.menuItem_faq:
////            Popups.showFaq(this);
////            return true;
////        case R.id.menuItem_helpForum:
////            ActivityHelper.launchUri(this, R.string.uri_forum);
////            return true;
////        case R.id.menuItem_controllerDiagnostics:
////            ActivityHelper.startDiagnosticActivity(this);
////            return true;
////        case R.id.menuItem_reportBug:
////            ActivityHelper.launchUri(this, R.string.uri_bugReport);
////            return true;
//            case R.id.menuItem_appVersion:
//                Popups.showAppVersion(this);
//                return true;
//            case R.id.menuItem_logcat:
//                ActivityHelper.startLogcatActivity(this);
//                return true;
//            case R.id.menuItem_hardwareInfo:
//                Popups.showHardwareInfo(this);
//                return true;
//            case R.id.menuItem_credits:
//                //ActivityHelper.launchUri(GalleryActivity.this, R.string.uri_credits);
//                openURLUpdate();
//
//                return true;
////        case R.id.menuItem_localeOverride:
////            mGlobalPrefs.changeLocale(this);
////            return true;
//            case R.id.menuItem_extract:
//                ActivityHelper.starExtractTextureActivity(this);
//                return true;
//            case R.id.menuItem_clear:
//            {
//                String title = getString( R.string.confirm_title );
//                String message = getString( R.string.confirmClearData_message );
//
//                ConfirmationDialog confirmationDialog =
//                        ConfirmationDialog.newInstance(CLEAR_CONFIRM_DIALOG_ID, title, message);
//
//                FragmentManager fm = getSupportFragmentManager();
//                confirmationDialog.show(fm, STATE_CLEAR_CONFIRM_DIALOG);
//            }
//            return true;
////        case R.id.menuItem_donate: {
////
////            if(mIapHelper != null && !mDonationDialogBeingShown)
////                showInAppPurchases();
////        }
////            return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
        return super.onOptionsItemSelected(item);
    }

    public void launchGameOnCreation(GameInfo gameInfo, boolean restart) {

        if (new File(gameInfo.localFile).exists()) {
//            if(gameInfo.uerData == null)
//                gameInfo.uerData = new RomHeader(gameInfo.localFile);
//            if(gameInfo.romDetail == null)
//                gameInfo.romDetail = RomDatabase.getInstance().lookupByMd5(gameInfo.md5);
//            final RomHeader header = (RomHeader)gameInfo.uerData;
//            launchGameActivity(gameInfo,gameInfo.localFile, null, gameInfo.md5, header.crc, header.name,
//                    header.countryCode.getValue(), gameInfo.artPath, gameInfo.romDetail.goodName, restart);
//            int count_open_app = dataServer.getIntForKey(getApplicationContext(), Constant.KEY_STORAGE_OPEN_APP, 0);
//            if(count_open_app < 100)
//            {
//                count_open_app++;
//                dataServer.saveIntForKey(getApplicationContext(),Constant.KEY_STORAGE_OPEN_APP,count_open_app);
//            }
        }
    }

    public void launchGameActivity(GameInfo gameInfo, String romPath, String zipPath, String romMd5, String romCrc,
                                   String romHeaderName, byte romCountryCode, String romArtPath, String romGoodName, boolean isRestarting) {
//        Log.i("GalleryActivity", "launchGameActivity");
//
//        // Make sure that the storage is accessible
//        if (!mAppData.isSdCardAccessible()) {
//            Log.e("GalleryActivity", "SD Card not accessible");
//            Notifier.showToast(this, R.string.toast_sdInaccessible);
//
//            mAppData.putAssetVersion(0);
//            ActivityHelper.startSplashActivity(this);
//            finish();
//            return;
//        }
//
//        // Update the ConfigSection with the new value for lastPlayed
//        final String lastPlayed = Integer.toString((int) (new Date().getTime() / 1000));
//        final ConfigFile config = new ConfigFile(mGlobalPrefs.romInfoCache_cfg);
//        File romFileName = new File(romPath);
//
//        String romLegacySaveFileName;
//
//        //Convoluted way of moving legacy save file names to the new format
//        if (zipPath != null) {
//            File zipFile = new File(zipPath);
//            romLegacySaveFileName = zipFile.getName();
//        } else {
//            File romFile = new File(romPath);
//            romLegacySaveFileName = romFile.getName();
//        }
//
//
//        config.put(romMd5, "lastPlayed", lastPlayed);
//        config.save();
//
//        ///Drawer layout can be null if this method is called from onCreate
//        if (mDrawerLayout != null) {
//            //Close drawer without animation
//            mDrawerLayout.closeDrawer(GravityCompat.START, false);
//        }
//        //mRefreshNeeded = true;
//
//        //mSelectedItem = null;
//
//        if (romFileName.exists()) {
//            // Notify user that the game activity is starting
//            String text = gameInfo.name +" is starting, please wait...";
//            Notifier.showToast(this,text);
//            // Launch the game activity
//            ActivityHelper.startGameActivity(this, romPath, romMd5, romCrc, romHeaderName, romCountryCode,
//                    romArtPath, romGoodName, romLegacySaveFileName, isRestarting);
//        } else {
//            if (config.get(romMd5) != null) {
//                if (!TextUtils.isEmpty(zipPath)) {
////					mExtractRomFragment.ExtractRom(zipPath, mGlobalPrefs.unzippedRomsDir, romPath, romMd5, romCrc,
////							romHeaderName, romCountryCode, romArtPath, romGoodName, romLegacySaveFileName,
////							isRestarting);
//                }
//            }
//        }
    }

    public android.app.Dialog createDialogPromotion(final GameInfo infoPromo, final boolean need_quit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.promo_image, null);
        ImageView img = (ImageView) view.findViewById(R.id.dialog_imageview);

        img.setImageDrawable(getResources().getDrawable(R.drawable.default_coverart));
        ImageFetcher.ImageData imgData = new ImageFetcher.ImageData();
        imgData.imageURL = infoPromo.artUrl;
        imgData.imageSavePath = infoPromo.artPath;

        BitmapTask bitmapTask = new BitmapTask(this, imgData, img);
        bitmapTask.execute();


        DialogInterface.OnClickListener lis = null;
        if (need_quit)
            lis = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    quit();
                }
            };

        builder.setView(view);
        builder.setTitle(infoPromo.name)
                .setMessage(infoPromo.intro)
                .setNeutralButton("Remind later", lis
                )
                .setPositiveButton("Install now", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Utils.openApp(LobbyActivity.this, infoPromo.url);
                            }
                        }

                )
                .setCancelable(true);
        return builder.create();
    }

//    @Override
//    public void onClick(MenuItem menuItem) {
//        this.onOptionsItemSelected( menuItem );
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(iabHelper != null)
//            iabHelper.handleActivityResult(requestCode,resultCode,data);
//        if (requestCode == ActivityHelper.SCAN_ROM_REQUEST_CODE) {
//            // Make sure the request was successful
//            if (resultCode == RESULT_OK && data != null)
//            {
//                final Bundle extras = data.getExtras();
//                final String searchPath = extras.getString( ActivityHelper.Keys.SEARCH_PATH );
//                final boolean searchZips = extras.getBoolean( ActivityHelper.Keys.SEARCH_ZIPS );
//                final boolean downloadArt = extras.getBoolean( ActivityHelper.Keys.DOWNLOAD_ART );
//                final boolean clearGallery = extras.getBoolean( ActivityHelper.Keys.CLEAR_GALLERY );
//                final boolean searchSubdirectories = extras.getBoolean( ActivityHelper.Keys.SEARCH_SUBDIR );
//
//                if (searchPath != null)
//                {
//                    refreshRoms(new File(searchPath), searchZips, downloadArt, clearGallery, searchSubdirectories);
//                }
//            }
//        }
//        else if(requestCode == ActivityHelper.GAME_ACTIVITY_CODE)
//        {
//            if (resultCode == RESULT_OK && data != null)
//            {
//                final long time_play = data.getLongExtra("time_play",-1);
//                //Notifier.showToast(this,"TIME PLAYT : " + time_play);
//                hide_icon = (dataServer.getIntForKey(getApplicationContext(), Constant.KEY_STORAGE_OPEN_APP, 1) < dataServer.count_show_icon);
//
//                if(time_play > dataServer.time_ads_when_play && !hide_icon)
//                {
//                    admobHelper.showPageAds();
//                }
//
//                MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();
//                adapter.setHide_icon(hide_icon);
//                adapter.notifyDataSetChanged();
//            }
//        }

    }

    private void refreshRoms(final File startDir, boolean searchZips, boolean downloadArt, boolean clearGallery, boolean searchSubdirectories) {
//        mCacheRomInfoFragment.refreshRoms(startDir, searchZips, downloadArt, clearGallery, searchSubdirectories, mAppData, mGlobalPrefs);
    }

    public void loadRomLocal() {
        int start = 10;
        localDatas.clear();
//        final ConfigFile config = new ConfigFile( mGlobalPrefs.romInfoCache_cfg );
//        for( final String md5 : config.keySet() ) {
//            if (!ConfigFile.SECTIONLESS_NAME.equals(md5)) {
//
//                GameInfo g = GameDataManager.getInstance().md5Datas.get(md5);
//
//                if(GameDataManager.getInstance().md5Datas.get(md5) != null && !dataServer.isFake)
//                    continue;
//                final String romPath = config.get( md5, "romPath" );
//                if(romPath == null)
//                    continue;
//                GameInfo game = new GameInfo();
//                game.type = GameInfo.TYPE_LOCAL_DEVICE;
//                game.id = (start ++);
//
//                final RomDatabase database = RomDatabase.getInstance();
//                if(!database.hasDatabaseFile())
//                {
//                    database.setDatabaseFile(mAppData.mupen64plus_ini);
//                }
//                game.romDetail = database.lookupByMd5(md5);
//                if(game.romDetail == null)
//                    continue;
//                game.artPath = mGlobalPrefs.coverArtDir + "/" + game.romDetail.artName;
//                game.artUrl = game.romDetail.artUrl;
//                game.name = game.romDetail.goodName;
//                game.localFile = romPath;
//                game.md5 = md5;
//
//                if(romPath != null)
//                    localDatas.add(game);
//
//            }
//        }

    }

    private void setPremium(boolean premium) {
        if (dataServer != null) {
            dataServer.savePremium(getApplicationContext(), premium);
//            mMenu.setPremium(premium);
//            mGameSidebar.setPremium(premium);
        }
    }

    public void refreshLibrary() {
        hide_icon = (dataServer.getIntForKey(getApplicationContext(), Constant.KEY_STORAGE_OPEN_APP, 1) < dataServer.count_show_icon);
        checkOpenAllGame();
        final String query = mSearchQuery.toLowerCase(Locale.US);
        String[] searches = null;
        if (query.length() > 0)
            searches = query.split(" ");

        finalDatas.clear();
        promoDatas.clear();
        for (int i = 0; i < dataServer.promoDatas.size(); i++) {
            if (!Utils.isAppInstalled(this, dataServer.promoDatas.get(i).url) || (Boolean) dataServer.promoDatas.get(i).uerData) {
                promoDatas.add(dataServer.promoDatas.get(i));
            }
        }

        if (promoDatas.size() > 0 && !hide_game) {
            GameInfo heading = new GameInfo();
            heading.type = GameInfo.TYPE_HEADING;
            heading.name = "Promo Games";
            if (!dataServer.isFake && !hide_icon)
                finalDatas.add(heading);

            for (GameInfo game : promoDatas) {
                String goodName = game.name;
                boolean matchesSearch = true;
                if (searches != null && searches.length > 0 && goodName != null) {
                    // Make sure the ROM name contains every token in the query
                    final String lowerName = goodName.toLowerCase(Locale.US);
                    for (final String search : searches) {
                        if (search.length() > 0 && !lowerName.contains(search)) {
                            matchesSearch = false;
                            break;
                        }
                    }
                }
                if (matchesSearch) {
                    if (!dataServer.isFake && !hide_icon)
                        finalDatas.add(game);
                }

            }
        }
        if (localDatas.size() > 0) {
            GameInfo heading = new GameInfo();
            heading.type = GameInfo.TYPE_HEADING;
            heading.name = "Games in Device";
            finalDatas.add(heading);

            //finalDatas.addAll(localDatas);
            for (GameInfo game : localDatas) {
                String goodName = game.name;
                boolean matchesSearch = true;
                if (searches != null && searches.length > 0 && goodName != null) {
                    // Make sure the ROM name contains every token in the query
                    final String lowerName = goodName.toLowerCase(Locale.US);
                    for (final String search : searches) {
                        if (search.length() > 0 && !lowerName.contains(search)) {
                            matchesSearch = false;
                            break;
                        }
                    }
                }
                if (matchesSearch) {
                    finalDatas.add(game);
                }

            }
        }

        if (localDatas.size() > 0 || promoDatas.size() > 0) {
            GameInfo heading = new GameInfo();
            heading.type = GameInfo.TYPE_HEADING;
            heading.name = "Library";
            finalDatas.add(heading);
        }

        libraryDatas.clear();
        Map<Integer, GameInfo> map = GameDataManager.getInstance().getRawData();
        if (!hide_game || true)
            for (int id : map.keySet()) {
                libraryDatas.add(map.get(id));
            }
        Collections.sort(libraryDatas, new Comparator<GameInfo>() {

            @Override
            public int compare(GameInfo lhs, GameInfo rhs) {
                // TODO Auto-generated method stub
                return lhs.id - rhs.id;
            }
        });

        for (GameInfo game : libraryDatas) {
            String goodName = game.name;
            boolean matchesSearch = true;
            if (searches != null && searches.length > 0 && goodName != null) {
                // Make sure the ROM name contains every token in the query
                final String lowerName = goodName.toLowerCase(Locale.US);
                for (final String search : searches) {
                    if (search.length() > 0 && !lowerName.contains(search)) {
                        matchesSearch = false;
                        break;
                    }
                }
            }
            if (matchesSearch) {
                if (!dataServer.isFake)
                    finalDatas.add(game);
            }

        }

        // Update the grid layout
        int galleryMaxWidth = (int) (getResources().getDimension(R.dimen.galleryImageWidth) * 1.0f);
        int galleryHalfSpacing = (int) getResources().getDimension(R.dimen.galleryHalfSpacing);
        float galleryAspectRatio = galleryMaxWidth * 1.0f
                / getResources().getDimension(R.dimen.galleryImageHeight) / 1.0f;

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final int width = metrics.widthPixels - galleryHalfSpacing * 2;
        final int galleryColumns = (int) Math
                .ceil(width * 1.0 / (galleryMaxWidth + galleryHalfSpacing * 2));
        int galleryWidth = width / galleryColumns - galleryHalfSpacing * 2;

        final GridLayoutManager layoutManager = new GridLayoutManagerBetterScrolling(this, 2);
        layoutManager.setSpanCount(galleryColumns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Headings will take up every span (column) in the grid
                if (finalDatas.get(position).type == GameInfo.TYPE_HEADING)
                    return galleryColumns;

                // Games will fit in a single column
                return 1;
            }
        });


        recyclerView.setLayoutManager(layoutManager);
        adapter.setHide_icon(hide_icon);
        recyclerView.setAdapter(adapter);

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public boolean onGameSelected(GameDescription game, int slot) {
        Intent intent = new Intent(this, NesEmulatorActivity.class);
        intent.putExtra(EmulatorActivity.EXTRA_GAME, game);
        intent.putExtra(EmulatorActivity.EXTRA_SLOT, slot);
        intent.putExtra(EmulatorActivity.EXTRA_FROM_GALLERY, true);
        startActivity(intent);
        return true;
    }

    public void openSlideBar(GameInfo game) {

        GameDescription des = new GameDescription();
        des.name = game.name;
        des.path = game.localFile;
        onGameSelected(des,0);
        //GameInfo game = GameDataManager.getInstance().getRawData().get(gameID);
//        if(game.uerData == null)
//            game.uerData = new RomHeader(game.localFile);
//        RomHeader header = (RomHeader)game.uerData;
//        selectedGame = game;
//
//        // Show the game info sidebar
//        mMenu.setVisibility(View.GONE);
//        mGameSidebar.setVisibility(View.VISIBLE);
//        mGameSidebar.scrollTo(0, 0);
//
//
//        if(!new File(game.artPath).exists())
//            mGameSidebar.setImage(null);
//        else
//            mGameSidebar.setImage(new BitmapDrawable(getResources(),game.artPath));
//
//        // Set the game title
//        mGameSidebar.setTitle(game.name);
//
//        // If there are no saves for this game, disable the resume
//        // option
//        final String gameDataPath = GamePrefs.getGameDataPath(game.md5, header.name,
//                header.countryCode.toString(), mAppData);
//        final String autoSavePath = gameDataPath + "/" + GamePrefs.AUTO_SAVES_DIR + "/";
//
//        final File autoSavePathFile = new File(autoSavePath);
//        final File[] allFilesInSavePath = autoSavePathFile.listFiles();
//
//        //No saves, go ahead and remove it
//        final boolean visible = allFilesInSavePath != null && allFilesInSavePath.length != 0 &&
//                mGlobalPrefs.maxAutoSaves > 0;
//
//
//        if (visible)
//        {
//            // Restore the menu
//            mGameSidebar.setActionHandler(LobbyActivity.this, R.menu.gallery_game_drawer);
//        }
//        else
//        {
//            // Disable the action handler
//            mGameSidebar.getMenu().removeItem(R.id.menuItem_resume);
//
//            final MenuItem restartItem = mGameSidebar.getMenu().findItem(R.id.menuItem_restart);
//            restartItem.setTitle(getString(R.string.actionStart_title));
//            restartItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
//
//            mGameSidebar.reload();
//        }
//
//        // Open the navigation drawer
//        mDrawerLayout.openDrawer(GravityCompat.START);
//
//        mGameSidebar.requestFocus();
//        mGameSidebar.setSelection(0);
//        mGameSidebar.setPremium(dataServer.isPremium);
    }

//    @Override
//    public void onGameSidebarAction(MenuItem menuItem) {
//        if(selectedGame == null)
//            return;
////        switch( menuItem.getItemId() )
////        {
////            case R.id.menuItem_resume:
////                launchGameOnCreation(selectedGame,false);
////                break;
////            case R.id.menuItem_restart:
////                //Don't show the prompt if this is the first time we start a game
////                launchGameOnCreation(selectedGame,true);
////
////
////                break;
////            case R.id.menuItem_settings:
////            {
////                String romLegacySaveFileName;
////                romLegacySaveFileName = new File(selectedGame.localFile).getName();
////                RomHeader header = (RomHeader)selectedGame.uerData;
////
////
////                ActivityHelper.startGamePrefsActivity( LobbyActivity.this, selectedGame.localFile,
////                        selectedGame.md5, header.crc, header.name, selectedGame.romDetail.goodName, header.countryCode.getValue(),
////                        romLegacySaveFileName);
////                break;
////
////            }
////            case R.id.menuItem_remove:
////            {
////
////
//////                final CharSequence title = getText( R.string.confirm_title );
//////                final CharSequence message = getText( R.string.confirmRemoveFromLibrary_message );
//////
//////                final ConfirmationDialog confirmationDialog =
//////                        ConfirmationDialog.newInstance(REMOVE_FROM_LIBRARY_DIALOG_ID, title.toString(), message.toString());
//////
//////                final FragmentManager fm = getSupportFragmentManager();
//////                confirmationDialog.show(fm, STATE_REMOVE_FROM_LIBRARY_DIALOG);
////
////                if(dataServer.isPremium)
////                {
////                    mGameSidebar.setPremium(true);
////                }
////                else if(iabHelper != null)
////                {
////                    try {
////                        iabHelper.launchPurchaseFlow(this, SKU_PREMIUM, PURCHASE_REQUEST_CODE, mPurchaseFinishedListener, "");
////                    }
////                    catch (IabHelper.IabAsyncInProgressException e)
////                    {
////                        e.printStackTrace();
////                    }
////                }
////            }
////            default:
////        }
//    }
//}


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context mContext;
        RecyclingImageView imgView;
        TextView name;
        TextView mInfo;
        ProgressBar download_bar;
        RelativeLayout layout;
        List<GameInfo> listGame;

        BitmapTask bitmapTask = null;

        DownloadThread downloadThread = null;

        public DownloadThread getDownloadThread() {
            return downloadThread;
        }

        public void setDownloadThread(DownloadThread downloadThread) {
            this.downloadThread = downloadThread;
        }

        public MyViewHolder(View view, List<GameInfo> list, Context context) {
            super(view);
            this.listGame = list;
            mContext = context;

            view.setOnClickListener(this);

            layout = (RelativeLayout) view.findViewById(R.id.layout);
            imgView = view.findViewById(R.id.imageArt);
            name = view.findViewById(R.id.gameTitle);
            mInfo = view.findViewById(R.id.gameDesc);
            download_bar = view.findViewById(R.id.download_bar);

        }

        @Override
        public void onClick(View view) {
            int idx = this.getLayoutPosition();

            GameInfo game = listGame.get(idx);

            if (game.type == GameInfo.TYPE_PROMO) {
                //Toast.makeText(mContext,""+game.intro,Toast.LENGTH_SHORT);
                //Notifier.showToast((Activity) mContext,game.intro);
                if (Utils.isAppInstalled(mContext, game.url))
                    Utils.openApp(mContext, game.url);
                else
                    ((LobbyActivity) mContext).createDialogPromotion(game, false).show();
                return;
            }

            File file = new File(game.localFile);
            if (file.exists()) {
                Log.i("N64", "Open Game :" + game.localFile);

                ((LobbyActivity) mContext).openSlideBar(game);

            } else {
                DownloadThread threadDownloadForGame = Downloader.getInstance().getDownloadThread(listGame.get(idx).id);
                if (threadDownloadForGame != null) {

                    if (threadDownloadForGame.isPause())    // neu dang pause thi resume
                    {
                        GameInfo i = threadDownloadForGame.getGameInfo();
                        float d = i.downloaded;
                        float t = i.totalSize;

                        mInfo.setText("(" + Utils.size2str(i.downloaded) + "/" + Utils.size2str(i.totalSize) + ")");
                        threadDownloadForGame.resumeDownload();
                    } else    // neu dang download thi pause
                    {
                        mInfo.setText("(download paused)");
                        threadDownloadForGame.setPause(true);
                        threadDownloadForGame.getHandler().removeCallbacksAndMessages(null);
                    }

                } else {
                    download_bar.setVisibility(View.VISIBLE);
                    download_bar.setProgress(0);
                    mInfo.setText("(downloading)");
                    GameInfo info = new GameInfo();
                    game.clone(info);

                    MyHandler handler = new MyHandler();
                    handler.setKeep_holder(this);
                    this.setDownloadThread(Downloader.getInstance().addTask(info, handler));

                }
            }
        }


    }


    class MyHandler extends Handler {
        volatile MyViewHolder keep_holder = null;
        Context mContext;

        public void setmContext(Context mContext) {
            this.mContext = mContext;
        }

        public MyViewHolder getKeep_holder() {
            return keep_holder;
        }

        public void setKeep_holder(MyViewHolder keep_holder) {
            this.keep_holder = keep_holder;
        }

        @Override
        public void handleMessage(Message msg) {
            if (keep_holder == null || !(msg.obj instanceof GameInfo))
                return;
            GameInfo info = (GameInfo) msg.obj;
            if (msg.what == 0) {
                keep_holder.mInfo.setText("(" + Utils.size2str(info.downloaded) + "/" + Utils.size2str(info.totalSize) + ")");
                if (info.downloaded == info.totalSize) {
                    keep_holder.mInfo.setText("(download error)");
                    File file_check = new File(info.localFile);
                    if (file_check != null) {
                        String md5 = ComputeMd5Task.computeMd5(file_check);
                        if (md5.equals(info.md5)) {
//                        RomHeader header = new RomHeader(info.localFile);
//                        GameDataManager.getInstance().getRawData().get(info.id).uerData = header;
                            keep_holder.mInfo.setText("(download finished)");

                        } else {
                            file_check.delete();
                        }

                    }

                }
            } else if (msg.what == 1) {
                keep_holder.mInfo.setText("(need download)");
                Toast.makeText(mContext, "Download error!! Check your network and try again.", Toast.LENGTH_LONG);

            }

        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private List<GameInfo> listGame;
        private Context mContext;
        private ImageResizer imageResizer;

        public void setHide_icon(boolean hide_icon) {
            this.hide_icon = hide_icon;
        }

        private boolean hide_icon = true;

        public Drawable getDefault_cover() {
            return default_cover;
        }

        public void setDefault_cover(Drawable default_cover) {
            this.default_cover = default_cover;
        }

        private Drawable default_cover;


        public MyAdapter(Context context, List<GameInfo> list, boolean hide, ImageResizer resizer) {
            listGame = list;
            mContext = context;
            hide_icon = hide;
            imageResizer = resizer;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            return new MyViewHolder(view, listGame, mContext);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            GameInfo game = listGame.get(position);
            holder.name.setText(game.name);
            holder.itemView.setClickable(true);
            holder.download_bar.setVisibility(View.INVISIBLE);
            if (game.type == GameInfo.TYPE_HEADING) {
                holder.itemView.setClickable(false);
                holder.layout.setPadding(0, 0, 0, 0);
                holder.name.setPadding(0, 0, 0, 0);
                holder.name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);

                holder.mInfo.setVisibility(View.GONE);
                holder.imgView.setVisibility(View.GONE);
                holder.download_bar.setVisibility(View.GONE);
                return;
            } else if (game.type == GameInfo.TYPE_PROMO) {
                holder.mInfo.setText("");

            } else if (game.type == GameInfo.TYPE_LOCAL_DEVICE) {
                holder.mInfo.setText("(ready to Play)");

            } else if (game.type == GameInfo.TYPE_LIBRARY) {
                holder.mInfo.setText("(need download)");
                if (holder.getDownloadThread() != null) {
                    MyHandler handler = (MyHandler) holder.getDownloadThread().getHandler();
                    handler.setKeep_holder(null);
                    holder.setDownloadThread(null);
                }
                File file = new File(game.localFile);
                if (!file.exists()) {
                    DownloadThread thread = Downloader.getInstance().getDownloadThread(game.id);
                    if (thread != null) {
                        MyHandler handler = (MyHandler) thread.getHandler();
                        handler.setKeep_holder(holder);
                        holder.setDownloadThread(thread);
                        if (thread.isPause())
                            holder.mInfo.setText("(download paused)");
                        else
                            holder.mInfo.setText("(" + Utils.size2str(thread.getGameInfo().downloaded) + "/" + Utils.size2str(thread.getGameInfo().totalSize) + ")");
                    }
                } else {
                    holder.mInfo.setText("");
                }
            }

            holder.name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
            holder.mInfo.setVisibility(View.VISIBLE);
            holder.imgView.setVisibility(View.VISIBLE);
            ImageFetcher.ImageData imgData = new ImageFetcher.ImageData();
            imgData.imageURL = listGame.get(position).artUrl;
            imgData.imageSavePath = listGame.get(position).artPath;

        if(holder.bitmapTask != null)
            holder.bitmapTask.cancel(true);
            if(!hide_icon || true)
            {
                holder.bitmapTask = new BitmapTask(mContext,imgData,holder.imgView);
                holder.bitmapTask.execute();
            }
            imageResizer.cancelWork(holder.imgView);
            holder.imgView.setImageDrawable(default_cover);
            if (!hide_icon)
                imageResizer.loadImage(imgData, holder.imgView);

            holder.name.setText(game.name);
            holder.layout.setPadding(10, 0, 10, 0);


        }

        @Override
        public int getItemCount() {
            return listGame.size();
        }



    }

    class BitmapTask extends AsyncTask<String, String, String> {

        private final String mBitmapPath;
        private final String mBitmapURL;

        private ImageFetcher.ImageData imgData;

        private final ImageView mArtView;
        private BitmapDrawable mArtBitmap;

        private void setImageDrawable(ImageView imageView, Drawable drawable) {
            if (true) {
                // Transition drawable with a transparent drawable and the final drawable
                final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[]{
                                new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)),
                                drawable
                        });
                // Set background to loading bitmap

//                imageView.setBackgroundDrawable(
//                        mContext.getResources().getDrawable(R.drawable.default_coverart));

                imageView.setImageDrawable(td);
                td.startTransition(150);
            } else {
                imageView.setImageDrawable(drawable);
            }
        }

        private final Context mContext;
        private boolean mIsCancelled;

        public BitmapTask(Context context, ImageFetcher.ImageData imgData, ImageView artView) {
            mBitmapPath = imgData.imageSavePath;
            mBitmapURL = imgData.imageURL;
            this.imgData = imgData;

            mArtView = artView;
            mArtBitmap = null;
            mContext = context;
            mIsCancelled = false;
        }

        @Override
        protected String doInBackground(String... params) {
//            if (!new File(mBitmapPath).exists()) {
//                downloadIMG(imgData);
//            }
//            if (new File(mBitmapPath).exists()) {
//                mArtBitmap = new BitmapDrawable(mContext.getResources(), mBitmapPath);
//            }
            try {
                mArtBitmap = new BitmapDrawable(mContext.getAssets().open(mBitmapURL));

            }
            catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!mIsCancelled) {
                if (mArtBitmap != null)
                    setImageDrawable(mArtView, mArtBitmap);
                else
                    mArtView.setImageResource(R.drawable.default_coverart);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            mIsCancelled = true;
        }

        private String downloadIMG(ImageFetcher.ImageData dataImg) {
            String ret = "";
            String tmpFile;

            String realPathArt = dataImg.imageSavePath;

            if (realPathArt.contains(".png") || realPathArt.contains(".jpg")) {
                tmpFile = realPathArt.substring(0, realPathArt.length() - 4);
            } else
                tmpFile = realPathArt;

            RandomAccessFile raf = null;

            HttpsURLConnection conection = null;
            try {
                //conection = (HttpsURLConnection)(new URL(url)).openConnection();
                conection = NetCipher.getHttpsURLConnection(dataImg.imageURL);
                conection.setConnectTimeout(10000);
                conection.setRequestMethod("GET");

                File file = new File(tmpFile);
                if (file.exists() && file.canWrite() && file.delete()) {
                    file.createNewFile();
                }

                raf = new RandomAccessFile(tmpFile, "rwd");

                raf.seek(0);

                int count = 0;
                byte[] buffer = new byte[4096];
                boolean inter = false;

                BufferedInputStream is = new BufferedInputStream(conection.getInputStream(), 4096);
                while ((count = is.read(buffer)) != -1) {
                    if (Thread.interrupted()) {
                        inter = true;
                        break;
                    }

                    raf.write(buffer, 0, count);
                }
                if (!inter) {
                    file.renameTo(new File(dataImg.imageSavePath));
                    return dataImg.imageSavePath;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (raf != null)
                    com.hn.utils.Utils.close(raf);
            }
            return ret;
        }

    }

}
