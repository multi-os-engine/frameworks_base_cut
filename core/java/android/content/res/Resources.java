/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.res;

import android.util.TypedValue;
import android.util.DisplayMetrics;
import android.annotation.RawRes;
import android.annotation.AnyRes;
/*import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.annotation.AnimRes;
import android.annotation.AnyRes;
import android.annotation.ArrayRes;
import android.annotation.BoolRes;
import android.annotation.ColorRes;
import android.annotation.DimenRes;
import android.annotation.DrawableRes;
import android.annotation.FractionRes;
import android.annotation.IntegerRes;
import android.annotation.LayoutRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.PluralsRes;

import android.annotation.StringRes;
import android.annotation.XmlRes;
import android.content.pm.ActivityInfo;
import android.graphics.Movie;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pools.SynchronizedPool;
import android.util.Slog;
import android.util.TypedValue;
import android.view.ViewDebug;
import android.view.ViewHierarchyEncoder;*/

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Class for accessing an application's resources.  This sits on top of the
 * asset manager of the application (accessible through {@link #getAssets}) and
 * provides a high-level API for getting typed data from the assets.
 *
 * <p>The Android resource system keeps track of all non-code assets associated with an
 * application. You can use this class to access your application's resources. You can generally
 * acquire the {@link android.content.res.Resources} instance associated with your application
 * with {@link android.content.Context#getResources getResources()}.</p>
 *
 * <p>The Android SDK tools compile your application's resources into the application binary
 * at build time.  To use a resource, you must install it correctly in the source tree (inside
 * your project's {@code res/} directory) and build your application.  As part of the build
 * process, the SDK tools generate symbols for each resource, which you can use in your application
 * code to access the resources.</p>
 *
 * <p>Using application resources makes it easy to update various characteristics of your
 * application without modifying code, and&mdash;by providing sets of alternative
 * resources&mdash;enables you to optimize your application for a variety of device configurations
 * (such as for different languages and screen sizes). This is an important aspect of developing
 * Android applications that are compatible on different types of devices.</p>
 *
 * <p>For more information about using resources, see the documentation about <a
 * href="{@docRoot}guide/topics/resources/index.html">Application Resources</a>.</p>
 */
public class Resources {
    static final String TAG = "Resources";

    private static final boolean DEBUG_LOAD = false;
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;

    private static final int ID_OTHER = 0x01000004;

    private static final Object sSync = new Object();

    private static final String CACHE_NOT_THEMED = "";
    private static final String CACHE_NULL_THEME = "null_theme";

    // Used by BridgeResources in layoutlib
    static Resources mSystem = null;

    // These are protected by mAccessLock.
    private final Object mAccessLock = new Object();
    private final Configuration mTmpConfig = new Configuration();

    private TypedValue mTmpValue = new TypedValue();
    private boolean mPreloading;

    private int mLastCachedXmlBlockIndex = -1;
    private final int[] mCachedXmlBlockIds = { 0, 0, 0, 0 };

    final AssetManager mAssets;

    private final Configuration mConfiguration = new Configuration();

    /**
     * This exception is thrown by the resource APIs when a requested resource
     * can not be found.
     */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException() {
        }

        public NotFoundException(String name) {
            super(name);
        }
    }

    /**
     * Create a new Resources object on top of an existing set of assets in an
     * AssetManager.
     *
     * @param assets Previously created AssetManager.
     * @param metrics Current display metrics to consider when
     *                selecting/computing resource values.
     * @param config Desired device configuration to consider when
     *               selecting/computing resource values (optional).
     */
    public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        mAssets = assets;
        updateConfiguration(config, metrics);
       // assets.ensureStringBlocks();
    }


     /**
     * Open a data stream for reading a raw resource.  This can only be used
     * with resources whose value is the name of an asset files -- that is, it can be
     * used to open drawable, sound, and raw resources; it will fail on string
     * and color resources.
     * 
     * @param id The resource identifier to open, as generated by the appt
     *           tool.
     * 
     * @return InputStream Access to the resource data.
     *
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     * 
     */
    public InputStream openRawResource(@RawRes int id) throws NotFoundException {
        TypedValue value;
        synchronized (mAccessLock) {
            value = mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                mTmpValue = null;
            }
        }
        InputStream res = openRawResource(id, value);
        synchronized (mAccessLock) {
            if (mTmpValue == null) {
                mTmpValue = value;
            }
        }
        return res;
    }


    /**
     * Return a resource identifier for the given resource name.  A fully
     * qualified resource name is of the form "package:type/entry".  The first
     * two components (package and type) are optional if defType and
     * defPackage, respectively, are specified here.
     *
     * <p>Note: use of this function is discouraged.  It is much more
     * efficient to retrieve resources by identifier than by name.
     *
     * @param name The name of the desired resource.
     * @param defType Optional default resource type to find, if "type/" is
     *                not included in the name.  Can be null to require an
     *                explicit type.
     * @param defPackage Optional default package to find, if "package:" is
     *                   not included in the name.  Can be null to require an
     *                   explicit package.
     *
     * @return int The associated resource identifier.  Returns 0 if no such
     *         resource was found.  (0 is not a valid resource ID.)
     */
    public int getIdentifier(String name, String defType, String defPackage) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        try {
            return Integer.parseInt(name);
        } catch (Exception e) {
            // Ignore
        }
        return mAssets.getResourceIdentifier(defType, name, defPackage);
    }

    /**
     * Return the raw data associated with a particular resource ID.
     *
     * @param id The desired resource identifier, as generated by the aapt
     *           tool. This integer encodes the package, type, and resource
     *           entry. The value 0 is an invalid identifier.
     * @param outValue Object in which to place the resource data.
     * @param resolveRefs If true, a resource that is a reference to another
     *                    resource will be followed so that you receive the
     *                    actual final resource data.  If false, the TypedValue
     *                    will be filled in with the reference itself.
     *
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     *
     */
    public void getValue(@AnyRes int id, TypedValue outValue, boolean resolveRefs)
    throws NotFoundException {
        boolean found = mAssets.getResourceValue(id, 0, outValue, resolveRefs);
        if (found) {
            return;
        }
        throw new NotFoundException("Resource ID #0x"
                                    + Integer.toHexString(id));
    }
    
    /**
     * Open a data stream for reading a raw resource.  This can only be used
     * with resources whose value is the name of an asset file -- that is, it can be
     * used to open drawable, sound, and raw resources; it will fail on string
     * and color resources.
     *
     * @param id The resource identifier to open, as generated by the appt tool.
     * @param value The TypedValue object to hold the resource information.
     *
     * @return InputStream Access to the resource data.
     *
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     */
    public InputStream openRawResource(@RawRes int id, TypedValue value)
            throws NotFoundException {
        getValue(id, value, true);

        try {
            return mAssets.openNonAsset(value.assetCookie, value.string.toString(),
                    AssetManager.ACCESS_STREAMING);
        } catch (Exception e) {
            NotFoundException rnf = new NotFoundException("File " + value.string.toString() +
                    " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

    /**
     * Store the newly updated configuration.
     */
    public void updateConfiguration(Configuration config,
            DisplayMetrics metrics)  {
        synchronized (mAccessLock) {
            final int configChanges = calcConfigChanges(config);
            if (mConfiguration.locale == null) {
                mConfiguration.locale = Locale.getDefault();
            }


            String locale = null;
            if (mConfiguration.locale != null) {
                //locale = adjustLanguageTag(mConfiguration.locale.toLanguageTag());
            }


            final int keyboardHidden;
            if (mConfiguration.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO
                    && mConfiguration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
                keyboardHidden = Configuration.KEYBOARDHIDDEN_SOFT;
            } else {
                keyboardHidden = mConfiguration.keyboardHidden;
            }

            mAssets.setConfiguration(mConfiguration.mcc, mConfiguration.mnc,
                    locale, mConfiguration.orientation,
                    mConfiguration.touchscreen,
                    mConfiguration.densityDpi, mConfiguration.keyboard,
                    keyboardHidden, mConfiguration.navigation, 0, 0,
                    mConfiguration.smallestScreenWidthDp,
                    mConfiguration.screenWidthDp, mConfiguration.screenHeightDp,
                    mConfiguration.screenLayout, mConfiguration.uiMode,
                    0);

        }
    }

    /**
     * Called by ConfigurationBoundResourceCacheTest via reflection.
     */
    private int calcConfigChanges(Configuration config) {
        int configChanges = 0xfffffff;
        if (config != null) {
            mTmpConfig.setTo(config);

            if (mTmpConfig.locale == null) {
                mTmpConfig.locale = Locale.getDefault();
            }
            configChanges = mConfiguration.updateFrom(mTmpConfig);
        }
        return configChanges;
    }

       /**
     * Update the system resources configuration if they have previously
     * been initialized.
     *
     * @hide
     */
    public static void updateSystemConfiguration(Configuration config, DisplayMetrics metrics) {
        if (mSystem != null) {
            mSystem.updateConfiguration(config, metrics);
            //Log.i(TAG, "Updated system resources " + mSystem
            //        + ": " + mSystem.getConfiguration());
        }
    }

      /**
     * Return the current configuration that is in effect for this resource 
     * object.  The returned object should be treated as read-only.
     * 
     * @return The resource's current configuration. 
     */
    public Configuration getConfiguration() {
        return mConfiguration;
    }
    
    /**
     * Return the entry name for a given resource identifier.
     * 
     * @param resid The resource identifier whose entry name is to be
     * retrieved.
     * 
     * @return A string holding the entry name of the resource.
     * 
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     * 
     * @see #getResourceName
     */
    public String getResourceEntryName(@AnyRes int resid) throws NotFoundException {
        String str = mAssets.getResourceEntryName(resid);
        if (str != null) return str;
        throw new NotFoundException("Unable to find resource ID #0x"
                + Integer.toHexString(resid));
    }
    
    
    /**
     * Retrieve underlying AssetManager storage for these resources.
     */
    public final AssetManager getAssets() {
        return mAssets;
    }

    /*private Resources() {
        mAssets = AssetManager.getSystem();
        // NOTE: Intentionally leaving this uninitialized (all values set
        // to zero), so that anyone who tries to do something that requires
        // metrics will get a very wrong value.
        mConfiguration.setToDefaults();
        updateConfiguration(null, null);
        mAssets.ensureStringBlocks();
    }*/
}
