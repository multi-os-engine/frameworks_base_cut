package android.database;

import android.os.AsyncTask;
import junit.framework.TestCase;

import java.io.InputStream;


public class AsyncTaskTest extends TestCase {

    private class myAsyncTaskImpl extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            String newstr = null;
            for(int i=0; i<1000; i++){
                newstr = params + Integer.toString(i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted");
                }
            }
            return newstr;
        }
    }

    public void testAsyncTask(){
        String str = "my status string";
        AsyncTask<String, String, String> at = new myAsyncTaskImpl();
        at.execute(str);
        System.out.println("sent task and continue");
        if(at.getStatus()!= AsyncTask.Status.RUNNING)
            fail("Background task was not started");
        System.out.println("current status is " + at.getStatus().toString());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //ignore
        }
        while(at.getStatus()!= AsyncTask.Status.FINISHED){
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted");
            }
        }
        System.out.println("Background task is finished");
    }

}
