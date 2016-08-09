package android.database;

import android.content.ContextImpl;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import junit.framework.TestCase;
import android.content.pm.PackageInfo;
import android.content.Context;

import java.io.*;

public class ResourcesTest extends TestCase {

    public void testOpenRawResources(){
        Context context = new ContextImpl();
        ContextWrapper contextWrapper = new ContextWrapper(context);
        AssetManager am = contextWrapper.getAssets();
        Resources res = contextWrapper.getResources();

        int resid = am.getResourceIdentifier("txt", "classlist", "");

        InputStream is = res.openRawResource(resid);

        char[] buffer=new char[5];
        Reader inread = new InputStreamReader(is);
        try {
            int len = inread.read(buffer, 0, 5);
            if(len != 5)
                fail("Wrong number of symbols read: "+len);
            if(buffer[0]!='J' || buffer[1]!='u' || buffer[2]!='n' || buffer[3]!='i' || buffer[4]!='t')
                fail("Wrong symbols read "+buffer[0]+buffer[1]+buffer[2]+buffer[3]+buffer[4]);
        } catch (IOException e) {
            fail("Exception during read of resource "+e.toString());
        }

        try {
            inread.close();
            is.close();
            am.close();
        } catch (IOException e) {
            fail("Close failed "+e.toString());
        }
    }
}
