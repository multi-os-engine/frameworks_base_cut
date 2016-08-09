package android.database;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import android.database.sqlite.*;


public class SQLiteConnectionPoolTest extends TestCase {

    private File dbFile = null;
    private String connectionURL = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getConnectionURL();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (dbFile != null) {
            dbFile.delete();
        }
    }

    public void testSqliteConnectionPool_open() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL, SQLiteDatabase.OPEN_READWRITE);
        SQLiteConnectionPool conn = null;
        try {
            conn = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        if(conn != null)
            conn.close();

    }

    public void testSqliteConnectionPool_reconfigure() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL, SQLiteDatabase.OPEN_READWRITE);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection conn = null;
        String sqlWriteComm="INSERT INTO test (col1, col2) VALUES ('1','2')";
        String sqlCreateComm="CREATE TABLE test (col1 int, col2 int)";
        try {
            conn = connPool.acquireConnection("",
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        try {
            conn.execute(sqlCreateComm, null, null);
            conn.execute(sqlWriteComm,null, null);
        }catch (Exception e){
            fail("Error on sql query for read/write connection");
        }

        try {
            config.openFlags = SQLiteDatabase.OPEN_READONLY;
            connPool.reconfigure(config);
        } catch (Exception e){
            fail("error: reconfiguration failed: "+e.toString());
        }

        connPool.releaseConnection(conn);

        try {
            conn = connPool.acquireConnection("",
                    SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }
        try {
            conn.execute(sqlWriteComm,null, null);
            fail("No error on write operation on read only connection");
        }catch (SQLiteReadOnlyDatabaseException e){
            //ignore
        }
        connPool.releaseConnection(conn);

        if(connPool != null)
            connPool.close();

    }

    public void testSqliteConnectionPool_acquireConnectionPrimReadWrite() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL, SQLiteDatabase.OPEN_READWRITE);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection conn = null;
        try {
            for (int i=0; i< 10; i++) {
                conn = connPool.acquireConnection("",
                        SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
                if(!conn.isPrimaryConnection())
                    fail("Error: not primary connection was created");
                connPool.releaseConnection(conn);
            }
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        if(connPool != null)
            connPool.close();
    }

    public void testSqliteConnectionPool_acquireConnectionPrimReadOnly() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL,
                SQLiteDatabase.OPEN_READONLY);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection conn = null;
        try {
            for (int i=0; i< 10; i++) {
                conn = connPool.acquireConnection("SELECT",
                        SQLiteConnectionPool.CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY, null);
                if(!conn.isPrimaryConnection())
                    fail("Error: not primary connection was created");
                connPool.releaseConnection(conn);
            }
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        if(connPool != null)
            connPool.close();
    }

    public void testSqliteConnectionPool_acquireConnectionReadWrite() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL,
                SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection[] conn = new SQLiteConnection[3];
        try {
            for (int i=0; i< conn.length; i++) {
                conn[i] = connPool.acquireConnection("", 0, null);
                if(conn[i].isPrimaryConnection())
                    fail("Error: primary connection was created");}
            for (int i=0; i< conn.length; i++) {
                connPool.releaseConnection(conn[i]);
            }
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        if(connPool != null)
            connPool.close();
    }

    public void testSqliteConnectionPool_acquireConnectionReadOnly() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL,
                SQLiteDatabase.OPEN_READONLY  | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection[] conn = new SQLiteConnection[3];
        try {
            for (int i=0; i< conn.length; i++) {
                conn[i] = connPool.acquireConnection("", 0, null);
                if(conn[i].isPrimaryConnection())
                    fail("Error: primary connection was created");}
            for (int i=0; i< conn.length; i++) {
                connPool.releaseConnection(conn[i]);
            }
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        if(connPool != null)
            connPool.close();
    }

    public void testSqliteConnectionPool_acquireConnectionInteractiveReadWrite() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL,
                SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection[] conn = new SQLiteConnection[3];
        try {
            for (int i=0; i< conn.length; i++) {
                conn[i] = connPool.acquireConnection("", SQLiteConnectionPool.CONNECTION_FLAG_INTERACTIVE, null);
                if(conn[i].isPrimaryConnection())
                    fail("Error: primary connection was created");}
            for (int i=0; i< conn.length; i++) {
                connPool.releaseConnection(conn[i]);
            }
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        if(connPool != null)
            connPool.close();
    }

    public void testSqliteConnectionPool_acquireConnectionInteractiveReadOnly() throws Exception {
        SQLiteDatabaseConfiguration config = new SQLiteDatabaseConfiguration(connectionURL,
                SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING);
        SQLiteConnectionPool connPool = null;
        try {
            connPool = SQLiteConnectionPool.open(config);
        } catch (Exception e) {
            fail("error creating connection to DB: "+e.toString());
        }

        SQLiteConnection[] conn = new SQLiteConnection[3];
        try {
            for (int i=0; i< conn.length; i++) {
                conn[i] = connPool.acquireConnection("", SQLiteConnectionPool.CONNECTION_FLAG_INTERACTIVE, null);
                if(conn[i].isPrimaryConnection())
                    fail("Error: primary connection was created");}
            for (int i=0; i< conn.length; i++) {
                connPool.releaseConnection(conn[i]);
            }
        }catch (Exception e){
            fail("Error acquire connection to DB");
        }

        if(connPool != null)
            connPool.close();
    }

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
}
