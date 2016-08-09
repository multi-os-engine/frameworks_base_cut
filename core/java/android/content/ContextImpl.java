package android.content;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ContextImpl extends Context {
    private PackageManager pm;
    private AssetManager am;
    private Resources res;
    private String dir;

    private final Object mSync = new Object();
    private File mDatabasesDir;

    public ContextImpl(){
        pm = new PackageManagerImpl();
        am = new AssetManager();
        DisplayMetrics dm = new DisplayMetrics();
        Configuration conf = new Configuration();
        res = new Resources(am, dm, conf);
        dir = getTmpDir();
    }

    @Override
    public AssetManager getAssets() {
        return am;
    }

    @Override
    public Resources getResources() {
        return res;
    }

    @Override
    public PackageManager getPackageManager() {
        return pm;
    }

    private static native String nativeGetPackageName();

    @Override
    public String getPackageName() {
        return nativeGetPackageName();
    }

    private static native String getTmpDir();

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        boolean append =  (mode == MODE_APPEND);
        return new FileOutputStream(new File(dir+name), append);
    }

    @Override
    public File getFilesDir() {
        return new File(dir);
    }
    
    private File validateFilePath(String name, boolean createDirectory) {
        File dir;
        File f;
        
        if (name.charAt(0) == File.separatorChar) {
            String dirPath = name.substring(0, name.lastIndexOf(File.separatorChar));
            dir = new File(dirPath);
            name = name.substring(name.lastIndexOf(File.separatorChar));
            f = new File(dir, name);
        } else {
            dir = getDatabasesDir();
            f = makeFilename(dir, name);
        }
        
       /* if (createDirectory && !dir.isDirectory() && dir.mkdir()) {
            FileUtils.setPermissions(dir.getPath(),
                                     FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IXOTH,
                                     -1, -1);
        }*/
        
        return f;
    }
    
    /**
     * Returns the absolute path on the filesystem where a database created with
     * {@link #openOrCreateDatabase} is stored.
     *
     * @param name The name of the database for which you would like to get
     *          its path.
     *
     * @return An absolute path to the given database.
     *
     * @see #openOrCreateDatabase
     */
    public File getDatabasePath(String name){
        return validateFilePath(name, false);
    }
    
    private File getDatabasesDir() {
        String pathDoc = System.getenv("MOE_TMP_DIR");
        pathDoc = pathDoc.substring(0, pathDoc.length() - 4);
        pathDoc = pathDoc + "/Documents/";
        synchronized (mSync) {
            if (mDatabasesDir == null) {
                mDatabasesDir = new File(pathDoc, "databases");
            }
            /*if (mDatabasesDir.getPath().equals("databases")) {
                mDatabasesDir = new File("/data/system");
            }*/
            return mDatabasesDir;
        }
    }
    
    private File makeFilename(File base, String name) {
        if (name.indexOf(File.separatorChar) < 0) {
            return new File(base, name);
        }
        throw new IllegalArgumentException(
                                           "File " + name + " contains a path separator");
    }
};