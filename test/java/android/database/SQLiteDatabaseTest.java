package android.database;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.database.sqlite.*;
import android.os.ParcelFileDescriptor;
import android.content.ContentValues;

public class SQLiteDatabaseTest extends TestCase {

    private File dbFile = null;
    private String connectionURL = null;
    private SQLiteConnectionPool connPool = null;

    private String getConnectionURL() {
        if (connectionURL == null) {
            String tmp = System.getProperty("java.io.tmpdir");
            File tmpDir = new File(tmp);
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

            connectionURL = dbFile.getPath();
        }

        return connectionURL;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getConnectionURL();

        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL, SQLiteDatabase.OPEN_READWRITE);

        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if(connPool != null)
            connPool.close();

        if (dbFile != null) {
            dbFile.delete();
        }
    }

    public void testSqliteDatabase_transactionSuccess() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.create(null);

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            db.beginTransaction();
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            db.setTransactionSuccessful();
            db.endTransaction();
        }catch (Exception e){
            db.close();
            fail("Error on add to table sql query " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_transactionFail() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.create(null);

        final AtomicBoolean wasRollback = new AtomicBoolean(false);
        SQLiteTransactionListener listener = new SQLiteTransactionListener() {
            @Override
            public void onBegin() {
                System.out.println("transaction started");
            }

            @Override
            public void onCommit() {
                System.out.println("transaction commited");
            }

            @Override
            public void onRollback() {
                wasRollback.set(true);
                System.out.println("transaction rollback");
            }
        };

        try {
            db.beginTransactionWithListener(listener);
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            long r = db.insert("test", null, cv);
            if(r >= 0) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
            if(!wasRollback.get()){
                db.close();
                fail("Transaction was not rollback after fail");
            }
        }catch (SQLiteException e){
            //ignore expected fail
        }

        db.close();
    }

    public void testSqliteDatabase_transactionNonExclusive() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.create(null);

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            db.beginTransactionNonExclusive();
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            db.setTransactionSuccessful();
            db.endTransaction();
        }catch (Exception e){
            db.close();
            fail("Error on add to table sql query " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_transactionWithListener() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        SQLiteTransactionListener listener = new SQLiteTransactionListener() {
            @Override
            public void onBegin() {
                System.out.println("transaction started");
            }

            @Override
            public void onCommit() {
                System.out.println("transaction commited");
            }

            @Override
            public void onRollback() {
                System.out.println("transaction rollback");
            }
        };

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            db.beginTransactionWithListener(listener);
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            db.setTransactionSuccessful();
            db.endTransaction();
        }catch (Exception e){
            db.close();
            fail("Error on add to table sql query " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_transactionWithListenerNonExclusive() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.create(null);

        SQLiteTransactionListener listener = new SQLiteTransactionListener() {
            @Override
            public void onBegin() {
                System.out.println("transaction started");
            }

            @Override
            public void onCommit() {
                System.out.println("transaction commited");
            }

            @Override
            public void onRollback() {
                System.out.println("transaction rollback");
            }
        };

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            db.beginTransactionWithListenerNonExclusive(listener);
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            db.setTransactionSuccessful();
            db.endTransaction();
        }catch (Exception e){
            db.close();
            fail("Error on add to table sql query");
        }

        db.close();
    }

    public void testSqliteDatabase_openDatabase(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(connectionURL, null, SQLiteDatabase.OPEN_READWRITE);
            db.close();
        } catch (SQLiteException e){
            db.close();
            fail("Exception on opening database happens " + e.toString());
        }
    }

    public void testSqliteDatabase_openOrCreateDatabasePath(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(connectionURL, null);
            db.close();
        } catch (SQLiteException e){
            db.close();
            fail("Exception on opening database happens " + e.toString());
        }
    }

    public void testSqliteDatabase_openOrCreateDatabaseFile(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            db.close();
        } catch (SQLiteException e){
            db.close();
            fail("Exception on opening database happens " + e.toString());
        }
    }

    public void testSqliteDatabase_reopenReadWrite(){
        SQLiteDatabase db = null;
        try {
           db = SQLiteDatabase.openDatabase(connectionURL, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e){
            db.close();
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.reopenReadWrite();
        } catch (SQLiteException e){
            db.close();
            fail("Excepton happens during reopen: "+e.toString());
        }

        db.close();

    }

    public void testSqliteDatabase_create() throws Exception {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.create(null);
            db.close();
        }catch (Exception e){
            fail("Error on creation database "+ e.toString());
        }
    }

    public void testSqliteDatabase_query(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.clear();
            cv.put("col1", "2");
            cv.put("col2", "3");
            db.insert("test", null, cv);
            cv.put("col1", "3");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.put("col1", "4");
            cv.put("col2", "3");
            db.insert("test", null, cv);
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        try {
            Cursor c = db.query(false, "test", null,
                    null, null, null,
                    null, null, null);
            c.moveToFirst();
            if(c.getColumnCount() != 2 ||
                    c.getInt(0) != 1 ||
                    c.getInt(1) != 2) {
                db.close();
                fail("Result of query wrong");
            }
            c.close();
        } catch (SQLiteException e){
            db.close();
            fail("Exception in query happens " + e.toString());
        }

        String []cols = {"col1"};
        try {
            Cursor c = db.query(true, "test", cols, "col2='2'", null, null,
                    null, "col1 DESC", null);
            c.moveToFirst();
            if(c.getColumnCount() != 1 ||
                    c.getInt(0) != 3) {
                db.close();
                fail("Result of query wrong");
            }
            c.close();
        } catch (SQLiteException e){
            db.close();
            fail("Exception in query happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_rawQuery(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.clear();
            cv.put("col1", "2");
            cv.put("col2", "3");
            db.insert("test", null, cv);
            cv.put("col1", "3");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.put("col1", "4");
            cv.put("col2", "3");
            db.insert("test", null, cv);
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        try {
            Cursor c = db.rawQuery("SELECT col1 FROM test WHERE col2='2'", null);
            c.moveToFirst();
            if(c.getColumnCount() != 1 ||
                    c.getInt(0) != 1) {
                db.close();
                fail("Result of query wrong");
            }
            c.close();
        } catch (SQLiteException e){
            db.close();
            fail("Exception in query happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_insert(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            long r = db.insert("test", null, cv);
            if(r < 0) {
                db.close();
                fail("Insert failed");
            }
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_insertOrThrow(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        ContentValues cv = null;
        try {
            cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            long r = db.insertOrThrow("test", null, cv);
            if(r < 0) {
                db.close();
                fail("Insert failed");
            }
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_replace(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            long r = db.replace("test", null, cv);
            if(r < 0) {
                db.close();
                fail("Replace failed");
            }

            r = db.replace("test", null, cv);
            if(r < 0) {
                db.close();
                fail("Replace failed");
            }
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_insertWithOnConflict(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            long r = db.insert("test", null, cv);
            if(r < 0) {
                db.close();
                fail("Replace failed");
            }

            r = db.insertWithOnConflict("test", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            if(r < 0) {
                db.close();
                fail("Replace failed");
            }
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_delete(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        try {
            ContentValues cv = new ContentValues(2);
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.clear();
            cv.put("col1", "2");
            cv.put("col2", "3");
            db.insert("test", null, cv);
            cv.put("col1", "3");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.put("col1", "4");
            cv.put("col2", "3");
            db.insert("test", null, cv);
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        try {
            int ret = db.delete("test", "col2='2'",null);
            if(ret != 2) {
                db.close();
                fail("Wrong result of delete");
            }
        } catch (SQLiteException e){
            db.close();
            fail("Exception in query happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_update(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        ContentValues cv = new ContentValues(2);
        try {
            cv.put("col1", "1");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.clear();
            cv.put("col1", "2");
            cv.put("col2", "3");
            db.insert("test", null, cv);
            cv.clear();
            cv.put("col1", "3");
            cv.put("col2", "2");
            db.insert("test", null, cv);
            cv.clear();
            cv.put("col1", "4");
            cv.put("col2", "3");
            db.insert("test", null, cv);
        } catch (SQLiteException e){
            db.close();
            fail("Exception on adding row to database happens " + e.toString());
        }

        try {
            cv.put("col1", "10");
            int ret = db.update("test", cv, "col2='2'",null);
            if(ret != 2) {
                db.close();
                fail("Wrong result of delete");
            }
        } catch (SQLiteException e){
            db.close();
            fail("Exception in query happens " + e.toString());
        }

        db.close();
    }

    public void testSqliteDatabase_execSQL(){
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (SQLiteException e){
            fail("Exception on opening database happens " + e.toString());
        }

        try {
            db.execSQL("CREATE TABLE test (col1 int, col2 int)");
        }catch (Exception e){
            db.close();
            fail("Error on create table sql query");
        }

        db.close();
    }
}
