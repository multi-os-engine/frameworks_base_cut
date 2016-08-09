package android.database;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import android.database.sqlite.*;
import android.os.ParcelFileDescriptor;

public class SQLiteConnectionTest extends TestCase {

    private File dbFile = null;
    private String connectionURL = null;
    private SQLiteConnection conn = null;
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
        try {
            conn = connPool.acquireConnection("",
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        try {
            connPool.releaseConnection(conn);
        }catch (Exception e){
            fail("Error release connection to DB");
        }

        if(connPool != null)
            connPool.close();

        if (dbFile != null) {
            dbFile.delete();
        }
    }

    public void testSqliteConnection_execute() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";

        try {
            conn.execute(sqlCreateComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            conn.execute(sqlWriteComm, null, null);
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteConnection_prepare() throws Exception {
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";
        String sqlWrong = "CREATE TaBALe test (col1 int, col2 int)";

        try {
            conn.prepare(sqlCreateComm, null);
        }catch (Exception e){
            fail("Error on preparation of correct sql query");
        }

        try {
            conn.prepare(sqlWrong, null);
            fail("No error on preparation incorrect sql query");
        }catch (SQLiteException e){
            //ignore
        }
    }

    public void testSqliteConnection_executeForLong() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";

        String sqlGetLong="SELECT col1 FROM test";

        try {
            conn.execute(sqlCreateComm, null, null);
            conn.execute(sqlWriteComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            long ret = conn.executeForLong(sqlGetLong, null, null);
            if(ret != 1)
                fail("Incorrect result for executeForChangedRowCount call: should be 1, but there is "
                        + Long.toString(ret));
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteConnection_executeForString() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 tinytext)";

        String sqlGetString="SELECT col2 FROM test";

        try {
            conn.execute(sqlCreateComm, null, null);
            conn.execute(sqlWriteComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            String ret = conn.executeForString(sqlGetString, null, null);
            if(ret.compareTo("2")!=0) {
                fail("Incorrect result for executeForChangedRowCount call: should be 2, but there is "
                        + ret);
            }
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteConnection_executeForBlobFileDescriptor() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 blob)";

        String sqlGetBlob="SELECT col2 FROM test";
        try {
            conn.execute(sqlCreateComm, null, null);
            conn.execute(sqlWriteComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            ParcelFileDescriptor ret = conn.executeForBlobFileDescriptor(sqlGetBlob, null, null);
            ret.close();
        }catch (Exception e){
            fail("Error on add to table sql query "+e.toString());
        }
    }

    public void testSqliteConnection_executeForChangedRowCount() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";

        try {
            conn.execute(sqlCreateComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            int ret = conn.executeForChangedRowCount(sqlWriteComm, null, null);
            if(ret != 1){
                fail("Incorrect result for executeForChangedRowCount call: should be 1, but there is "
                        +Integer.toString(ret));
            }
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteConnection_executeForLastInsertedRowId() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";

        try {
            conn.execute(sqlCreateComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            long ret = conn.executeForLastInsertedRowId(sqlWriteComm, null, null);
            if(ret != 1){
                fail("Incorrect result for executeForLastInsertedRowId call: should be 1, but there is "
                        + Long.toString(ret));
            }
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteConnection_executeForCursorWindow() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";

        String sqlTestCursorWindow="SELECT col1 FROM test";
        try {
            conn.execute(sqlCreateComm, null, null);
            conn.execute(sqlWriteComm, null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        CursorWindow cw = new CursorWindow("test");

        try {
            long ret = conn.executeForCursorWindow(sqlTestCursorWindow, null, cw, 0, 0, true, null);
            if(ret != 1){
                cw.close();
                fail("Incorrect result for executeForCursorWindow call: should be 1, but there is "
                        + Long.toString(ret));
            }
        }catch (Exception e){
            cw.close();
            fail("Error on add to table sql query");
        }

        cw.close();
    }
}
