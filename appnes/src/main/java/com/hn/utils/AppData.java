/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2013 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Mupen64PlusAE. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: Paul Lamb, littleguy77
 */
package com.hn.utils;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.opengl.EGL14;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


/**
 * A convenience class for retrieving and persisting data defined internally by the application.
 * <p>
 * <b>Developers:</b> Use this class to persist small bits of application data across sessions and
 * reboots. (For large data sets, consider using databases or files.) To add a new variable to
 * persistent storage, use the following pattern:
 * 
 * <pre>
 * {@code
 * // Define keys for each variable
 * private static final String KEY_FOO = "foo";
 * private static final String KEY_BAR = "bar";
 * 
 * // Define default values for each variable
 * private static final float   DEFAULT_FOO = 3.14f;
 * private static final boolean DEFAULT_BAR = false;
 * 
 * // Create getters
 * public float getFoo()
 * {
 *     return mPreferences.getFloat( KEY_FOO, DEFAULT_FOO );
 * }
 * 
 * public boolean getBar()
 * {
 *     return mPreferences.getBoolean( KEY_BAR, DEFAULT_BAR );
 * }
 * 
 * // Create setters
 * public void setFoo( float value )
 * {
 *     mPreferences.edit().putFloat( KEY_FOO, value ).commit();
 * }
 * 
 * public void setBar( boolean value )
 * {
 *     mPreferences.edit().putBoolean( KEY_BAR, value ).commit();
 * }
 * </pre>
 */
public class AppData
{
    /** True if device is running Lollipop or later (21 - Android 5.0.x) */
    public static final boolean IS_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    
    /** True if device is running Lollipop MR1 or later (22 - Android 5.1.x) */
    public static final boolean IS_LOLLIPOP_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    
    /** True if device is running marshmallow or later (23 - Android 6.0.x) */
    public static final boolean IS_MARSHMELLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    /** True if device is running marshmallow or later (24 - Android 7.0.x) */
    public static final boolean IS_NOUGAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    
    /** The hardware info, refreshed at the beginning of every session. */

    private static String openGlVersion = null;
    
    // Shared preferences keys
    private static final String KEY_ASSET_VERSION = "assetVersion";
    private static final String KEY_APP_VERSION = "appVersion";
    // ... add more as needed
    
    // Shared preferences default values
    private static final int DEFAULT_ASSET_VERSION = 0;
    
    // ... add more as needed

    /** The package name. */
    public final String packageName;

    /** The app version string. */
    public final String appVersion;

    /** The app version code. */
    public final int appVersionCode;

    /** The object used to persist the settings. */
    private SharedPreferences mPreferences = null;
    
    /**
     * Instantiates a new object to retrieve and persist app data.
     * 
     * @param context The application context.
     */
    public AppData(Context context )
    {
    // Preference object for persisting app data
        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        packageName = context.getPackageName();
        PackageInfo info;
        String version = "";
        int versionCode = -1;
        try
        {
            info = context.getPackageManager().getPackageInfo( packageName, 0 );
            version = info.versionName;
            versionCode = info.versionCode;
        }
        catch( NameNotFoundException e )
        {
            Log.e( "AppData", e.getMessage() );
        }
        appVersion = version;
        appVersionCode = versionCode;
    }

    /**
     * Gets the asset version.
     *
     * @return The asset version.
     */
    public int getAssetVersion()
    {
        return getInt( KEY_ASSET_VERSION, DEFAULT_ASSET_VERSION );
    }

    /**
     * Persists the asset version.
     *
     * @param value The asset version.
     */
    public void putAssetVersion( int value )
    {
        putInt( KEY_ASSET_VERSION, value );
    }

    /**
     * Gets the asset version.
     *
     * @return The asset version.
     */
    public int getAppVersion()
    {
        return getInt( KEY_APP_VERSION, 0 );
    }

    /**
     * Persists the asset version.
     *
     * @param value The asset version.
     */
    public void putAppVersion( int value )
    {
        putInt( KEY_APP_VERSION, value );
    }

    private int getInt( String key, int defaultValue )
    {
        return mPreferences.getInt( key, defaultValue );
    }

    private void putInt( String key, int value )
    {
        mPreferences.edit().putInt( key, value ).apply();
    }


    private static int getMajorVersion(int glEsVersion) {
        return ((glEsVersion & 0xffff0000) >> 16);
    }

    private static int getMinorVersion(int glEsVersion) {
        return glEsVersion & 0xffff;
    }




    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source)
    {
        if (IS_NOUGAT) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }
}
