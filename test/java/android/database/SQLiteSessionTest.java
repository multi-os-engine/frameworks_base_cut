package android.database;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import android.database.sqlite.*;
import android.os.ParcelFileDescriptor;

public class SQLiteSessionTest extends TestCase {

    private File dbFile = null;
    private String connectionURL = null;
    private SQLiteConnectionPool connPool = null;
    private SQLiteSession session = null;

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

        session =new SQLiteSession(connPool);

        SQLiteConnection conn = null;
        try {
            conn = connPool.acquireConnection("",
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        try {
            conn.execute("CREATE TABLE test (col1 int, col2 int)", null, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        connPool.releaseConnection(conn);
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

    public void testSqliteSession_transactionSuccess() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";

        try {
            session.beginTransaction(SQLiteSession.TRANSACTION_MODE_EXCLUSIVE, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            session.execute(sqlWriteComm, null, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            session.setTransactionSuccessful();
            session.endTransaction(null);
        }catch (Exception e){
            fail("Error on add to table sql query");
        }

        long ret = session.executeForLong("SELECT COUNT(*) FROM test", null,
                SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        if(ret != 1) {
            fail("Error in transaction work");
        }
    }

    public void testSqliteSession_transactionFail() throws Exception {
        String sqlWriteComm="INSERT INTO test1 (col1, col2) VALUES ('1','2')";

        try {
            session.beginTransaction(SQLiteSession.TRANSACTION_MODE_EXCLUSIVE, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            session.execute(sqlWriteComm, null, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            session.setTransactionSuccessful();
            session.endTransaction(null);
            fail("Transaction didn't fail with expected error");
        }catch (Exception e){
           //ignore expected fail
        }

        long ret = session.executeForLong("SELECT COUNT(*) FROM test", null,
                SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        if(ret != 0) {
            fail("Error in transaction work");
        }
    }

    public void testSqliteSession_execute() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";

        try {
            session.execute(sqlWriteComm, null, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteSession_prepare() throws Exception {
        String sqlWrong = "CREATE TaBALe test (col1 int, col2 int)";

        try {
            session.prepare(sqlWrong, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null, null);
            fail("No error on preparation incorrect sql query");
        }catch (SQLiteException e){
            //ignore
        }
    }

    public void testSqliteSession_executeForLong() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlGetLong="SELECT col1 FROM test";

        try {
            session.execute(sqlWriteComm, null, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            long ret = session.executeForLong(sqlGetLong, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            if(ret != 1)
                fail("Incorrect result for executeForChangedRowCount call: should be 1, but there is "
                        + Long.toString(ret));
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteSession_executeForString() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlGetString="SELECT col2 FROM test";

        try {
            session.execute(sqlWriteComm, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            String ret = session.executeForString(sqlGetString, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            if(ret.compareTo("2")!=0) {
                fail("Incorrect result for executeForChangedRowCount call: should be 2, but there is "
                        + ret);
            }
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteSession_executeForBlobFileDescriptor() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";

        String sqlGetBlob="SELECT col2 FROM test";
        try {
            session.execute(sqlWriteComm, null, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        try {
            ParcelFileDescriptor ret = session.executeForBlobFileDescriptor(sqlGetBlob, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            ret.close();
        }catch (Exception e){
            fail("Error on add to table sql query "+e.toString());
        }
    }

    public void testSqliteSession_executeForChangedRowCount() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";

        try {
            int ret = session.executeForChangedRowCount(sqlWriteComm, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            if(ret != 1){
                fail("Incorrect result for executeForChangedRowCount call: should be 1, but there is "
                        +Integer.toString(ret));
            }
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteSession_executeForLastInsertedRowId() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";

        try {
            long ret = session.executeForLastInsertedRowId(sqlWriteComm, null,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
            if(ret != 1){
                fail("Incorrect result for executeForLastInsertedRowId call: should be 1, but there is "
                        + Long.toString(ret));
            }
        }catch (Exception e){
            fail("Error on add to table sql query");
        }
    }

    public void testSqliteSession_executeForCursorWindow() throws Exception {
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlTestCursorWindow="SELECT col1 FROM test";

        try {
            session.execute(sqlWriteComm, null, SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error on create table sql query");
        }

        CursorWindow cw = new CursorWindow("test");

        try {
            long ret = session.executeForCursorWindow(sqlTestCursorWindow, null, cw, 0, 0, true,
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
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
