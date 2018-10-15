package com.hn.main.helpers;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.hn.main.data.DataServerManager;

/**
 * Created by hoangnguyen on 3/8/18.
 */

public class AdmobHelper {
    public InterstitialAd pageView = null;
    private String pageID = "";
    private boolean pageLoaded = false;
    private Context mActivity = null;
    private DataServerManager dataServerManager;

    public AdmobHelper(Context activity,DataServerManager dataServerManager)
    {
        mActivity = activity;
        this.dataServerManager = dataServerManager;
    }
    private boolean force_show = false;

    AdListener lis = new AdListener(){
        @Override
        public void onAdLoaded() {
            // TODO Auto-generated method stub
            super.onAdLoaded();

            System.out.println("PAGE LOADED :" + pageView.getAdUnitId());
            pageLoaded = true;

            if(force_show)
            {
                pageView.show();
            }
        }

        @Override
        public void onAdFailedToLoad(int i) {
            Log.i("ADMOB","ads on failed");
            refreshPageAds();
        }

        @Override
        public void onAdClosed() {
            Log.i("ADMOB","ads onclose");
            force_show = false;
            refreshPageAds();
        }

    };

    public void refreshPageAds()
    {
        if(dataServerManager.isPremium)
            return;

        Log.i("ADMOB","start request refresh");
        pageLoaded = false;
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("1AA35CD745F91D7D042D463015C921D2")
                .build();
        pageView.loadAd(adRequest);
    }

    public void loadPage(final String pageID)
    {

        if(dataServerManager.isFake || !dataServerManager.enableADS || dataServerManager.isPremium )
            return;
        if(pageView == null)
        {
            pageView = new InterstitialAd(mActivity);
            pageView.setAdListener(lis);
            pageView.setAdUnitId(pageID);
            this.pageID = pageID;
            System.out.println("INIT PAGE ID: " + pageID);
        }
        refreshPageAds();
    }

    public void showPageAds() {
        if(dataServerManager.isPremium)
            return;
        if(pageView == null)
            return;
        this.force_show = true;
        if(pageLoaded)
        {
            pageView.show();
            refreshPageAds();
        }
    }

    public static native String getID();
}
