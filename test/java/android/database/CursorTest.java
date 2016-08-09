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

public class CursorTest extends TestCase {

    public void testMatrixCursor(){
        String[] cols=new String[3];
        cols[0]="col0";
        cols[1]="col1";
        cols[2]="col2";
        MatrixCursor c =new MatrixCursor(cols, 2);
        Integer row1[] = new Integer[3];
        row1[0]=new Integer(0);
        row1[1]=new Integer(1);
        row1[2]=new Integer(2);
        c.addRow(row1);

        if(c.getCount()!=1)
            fail("Wrong number of elements in cursor");

        c.moveToFirst();
        if(c.getInt(0)!=0)
            fail("Wrong first element");

        c.close();
    }

    public void testMergeCursor(){
        String[] cols=new String[3];
        cols[0]="col0";
        cols[1]="col1";
        cols[2]="col2";
        MatrixCursor mc1 =new MatrixCursor(cols, 2);
        Integer row1[] = new Integer[3];
        row1[0]=new Integer(0);
        row1[1]=new Integer(1);
        row1[2]=new Integer(2);
        mc1.addRow(row1);

        cols[0]="col20";
        cols[1]="col21";
        cols[2]="col22";
        MatrixCursor mc2 =new MatrixCursor(cols, 2);
        row1[0]=new Integer(10);
        row1[1]=new Integer(11);
        row1[2]=new Integer(12);
        mc2.addRow(row1);

        Cursor[] c = new Cursor[2];
        c[0]=mc1; c[1]=mc2;
        MergeCursor mc =new MergeCursor(c);

        if(mc.getCount()!=2)
            fail("Wrong number of elements in cursor");

        mc.moveToFirst();
        if(mc.getInt(0)!=0)
            fail("Wrong first element");

        mc.moveToNext();
        if(mc.getInt(0)!=10)
            fail("Wrong first element");

        mc1.close();
        mc2.close();
        mc.close();
    }
}
