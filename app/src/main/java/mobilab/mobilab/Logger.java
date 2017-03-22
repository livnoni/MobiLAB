package mobilab.mobilab;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class Logger extends Application
{
    private static File logger;
    private static FileOutputStream fos;


    public Logger() throws IOException
    {
        logger = getOutputMediaFile();
        fos = new FileOutputStream(logger);

    }

    public static void writeLog(String message)
    {
        try {
            fos.write((System.currentTimeMillis()+":\t"+message+"\n").getBytes());
            Log.i("Logger:",message+"");
        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MobiLAB");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +"Logs.txt");

        return mediaFile;
    }

    public static void onDestroy() throws IOException
    {
        writeLog("Finished.");
       // fos.close();
    }


}
