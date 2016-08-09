package android.database;

import android.content.ContextImpl;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import junit.framework.TestCase;
import android.content.pm.PackageInfo;
import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PackageInfoTest extends TestCase {

    public void testPackageInfo(){
        Context myCont = new ContextImpl();
        PackageManager pm = myCont.getPackageManager();
        String packID = myCont.getPackageName();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(packID, 0);
        } catch (PackageManager.NameNotFoundException e) {
            fail("Exception during getPackageInfo: "+e.toString());
        }
        if((packID.compareTo("org.moe.libcore-tests-Test")!=0) ||
                (pi.packageName.compareTo("org.moe.libcore-tests-Test")!=0)){
            fail("result is incorrect getPackageNaem from Context "+packID +
                    ", package name from pacakgeInfo is "+ pi.packageName);
        }
    }
}
