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

public class ContextWrapperTest extends TestCase {

    public void testOpenFileOutput(){
        Context context = new ContextImpl();
        ContextWrapper contextWrapper = new ContextWrapper(context);

        OutputStream os = null;
        try {
            os = contextWrapper.openFileOutput("myTempFile.txt", 0);
        } catch (FileNotFoundException e) {
            fail("Creating temp file failed "+e.toString());
        }

        byte []buf= {1,2,3,4,5};
        try {
            os.write(buf);
            os.close();
        } catch (IOException e) {
            fail("Can't write in created file "+e.toString());
        }

        File f = contextWrapper.getFilesDir();
        String absPath = f.getAbsolutePath() + "/myTempFile.txt";

        InputStream is = null;
        try {
            byte[] bufCheck = new byte[5];
            is = new FileInputStream(absPath);
            is.read(bufCheck);
            is.close();
            for(int i=0;i< 5; i++)
                if(buf[i]!=bufCheck[i])
                    fail("Wrong message was read, should be "+buf[i]+", there is "+bufCheck[i]);
        } catch (FileNotFoundException e) {
            fail("Reading file failed "+e.toString());
        } catch (IOException e) {
            fail("Reading file failed "+e.toString());
        }
    }
}
