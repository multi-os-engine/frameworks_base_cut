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

public class AssetManagerTest extends TestCase {

    public void testAssetManagerOpenClose(){
        AssetManager am = new AssetManager();

        InputStream assetInputStream = null;
        try {
             assetInputStream = am.open("classlist.txt");
        } catch (IOException e) {
            fail("Exception happens on opening asset: " + e.toString());
        }

        try {
            assetInputStream.close();
        } catch (IOException e) {
            fail("Exception happens on closing asset: " + e.toString());
        }

        am.close();
    }

    public void testAssetManagerReadChar(){
        AssetManager am = new AssetManager();

        InputStream assetInputStream = null;
        try {
            assetInputStream = am.open("classlist.txt");
        } catch (IOException e) {
            System.out.println("Exception happens on opening asset: "+e.toString());
        }

        if(assetInputStream == null)
            fail("Asset was not open");

        try {
            char sym = (char)assetInputStream.read();
            if(sym != 'J') {
                assetInputStream.close();
                fail("Wrong symbol was read : " + sym + "instead of J");
            }
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: "+e1.toString());
            }
            fail("Read of char failed:"+e.toString());
        }


        try {
            assetInputStream.close();
        } catch (IOException e) {
            fail("Exception happens on closing asset: " + e.toString());
        }

        am.close();
    }

    public void testAssetManagerReadBuffer(){
        AssetManager am = new AssetManager();

        InputStream assetInputStream = null;
        try {
            assetInputStream = am.open("classlist.txt");
        } catch (IOException e) {
            System.out.println("Exception happens on opening asset: "+e.toString());
        }

        if(assetInputStream == null)
            fail("Asset was not open");

        byte[] buf = new byte[10];
        try {
            int len = assetInputStream.read(buf, 0, 5);
            try {
                assetInputStream.close();
            } catch (IOException e) {
                fail("Exception happens on closing asset: " + e.toString());
            }
            if(len != 5)
                fail("Wrong number of symbols was read : "+Integer.toString(len)+"instead of 5");
            if(buf[0]!='J' || buf[1]!='u' || buf[2]!='n'||
                    buf[3]!='i'||buf[4]!='t')
                fail("Wrong symbols were read: "+buf.toString());
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: " + e1.toString());
            }
            fail("Read of char failed:"+e.toString());
        }

        am.close();
    }

    public void testAssetManagerReadBufferWithOffset(){
        AssetManager am = new AssetManager();

        InputStream assetInputStream = null;
        try {
            assetInputStream = am.open("classlist.txt");
        } catch (IOException e) {
            System.out.println("Exception happens on opening asset: "+e.toString());
        }

        if(assetInputStream == null)
            fail("Asset was not open");

        byte[] buf = new byte[10];
        try {
            int len = assetInputStream.read(buf, 5, 3);
            if(len != 3)
                fail("Wrong number of symbols was read : "+Integer.toString(len)+"instead of 3");
            if(buf[0]!='E' || buf[1]!='x' || buf[2]!='t')
                fail("Wrong symbols were read: "+buf.toString());
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: " + e1.toString());
            }
            fail("Read of char failed:"+e.toString());
        }

        am.close();
    }

    public void testAssetManagerSkip(){
        AssetManager am = new AssetManager();

        InputStream assetInputStream = null;
        try {
            assetInputStream = am.open("classlist.txt");
        } catch (IOException e) {
            System.out.println("Exception happens on opening asset: "+e.toString());
        }

        if(assetInputStream == null)
            fail("Asset was not open");

        byte[] buf = new byte[10];
        try {
            assetInputStream.skip(5);
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: "+e1.toString());
            }
            fail("Skip failed with exception "+e.toString());
        }
        try {
            int len = assetInputStream.read(buf, 0, 3);
            try {
                assetInputStream.close();
            } catch (IOException e) {
                fail("Exception happens on closing asset: "+e.toString());
            }
            if(len != 3)
                fail("Wrong number of symbols was read : "+Integer.toString(len)+"instead of 5");
            if(buf[0]!='E' || buf[1]!='x' || buf[2]!='t')
                fail("Wrong symbols were read: "+buf.toString());
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: "+e1.toString());
            }
            fail("Read of char failed:"+e.toString());
        }

        am.close();
    }

    public void testAssetManagerLength(){
        AssetManager am = new AssetManager();

        InputStream assetInputStream = null;
        try {
            assetInputStream = am.open("classlist.txt");
        } catch (IOException e) {
            System.out.println("Exception happens on opening asset: "+e.toString());
        }

        if(assetInputStream == null)
            fail("Asset was not open");

        int len = 0;
        try {
            len = assetInputStream.available();
            if(len != 1246) {
                try {
                    assetInputStream.close();
                } catch (IOException e) {
                    fail("Exception happens on closing asset: "+e.toString());
                }
                fail("Wrong lenth was returned " + Integer.toString(len));
            }
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: "+e1.toString());
            }
            fail("Get length failed with exception "+e.toString());
        }

        try {
            assetInputStream.skip(5);
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: "+e1.toString());
            }
            fail("Skip failed with exception "+e.toString());
        }

        try {
            len = assetInputStream.available();
            if(len != 1241) {
                try {
                    assetInputStream.close();
                } catch (IOException e) {
                    fail("Exception happens on closing asset: "+e.toString());
                }
                fail("Wrong lenth was returned " + Integer.toString(len));
            }
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e1) {
                fail("Exception happens on closing asset: "+e1.toString());
            }
            fail("Get length failed with exception "+e.toString());
        }

        try {
            assetInputStream.close();
        } catch (IOException e) {
            fail("Exception happens on closing asset: "+e.toString());
        }

        am.close();
    }

    public void testAssetManagerIdentifierEntryName(){
        AssetManager am = new AssetManager();

        int resid = am.getResourceIdentifier("txt", "classlist", "");
        String name = am.getResourceEntryName(resid);
        if(name.compareTo("classlist.txt")!=0){
            fail("Wrong mapping resources to identifiers");
        }

        am.close();
    }
    
    public void testAssetManagerFindAsset(){
        String tmp = System.getenv("MOE_TMP_DIR");
        File tmpDir = new File(tmp);
        File dbFile = null;
        if (tmpDir.isDirectory()) {
            try {
                dbFile = File.createTempFile("AndroidDatabaseConnectionTest", ".db", tmpDir);
            } catch (IOException e) {
                System.err.println("error creating temporary DB file.");
            }
            dbFile.deleteOnExit();
        } else {
            System.err.println("java.io.tmpdir does not exist");
        }
        
        String correctPath = dbFile.getPath();
        String correctName = dbFile.getName();
        
        AssetManager am = new AssetManager();
        String foundPath = am.findAsset(correctName).getPath();
        am.close();
        
        if(correctPath.compareTo(foundPath)!=0)
            fail("Incorrect path was found: "+foundPath+" but should be "+correctPath);
    }
}
