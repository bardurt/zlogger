package com.example.zlogger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by Bardur Thomsen on 6/28/18.
 * <p>
 * bardur.thomsen@avantica.net
 */
public class Zlogger {

    public static class LogLevel{

        public static final int DEBBUG  = 0;
        public static final int INFO    = 1;
        public static final int ERROR   = 2;

    }

    private static final String TAG = Zlogger.class.getSimpleName();

    public static final int PERMISSION_STORAGE = 9001;

    private static final int LOG_LENTGH = 100;
    private static int logLevel = LogLevel.DEBBUG;
    private static boolean STARTED = false;
    private static String FOLDER_LOCATION = Environment.getExternalStorageDirectory() + "/" + "Zlogger";
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd MM yyyy HH:mm:ss");

    private File logFile;

    private static final Zlogger ourInstance = new Zlogger();

    public static Zlogger getInstance() {
        return ourInstance;
    }

    private Zlogger() { }

    /**
     * Method to start up the Zlogger. The method
     * checks if the proper permission is set. If not it request the
     * necessary permissions.
     *
     * @param context context for checking permissions
     */
    public void init(Context context){

        STARTED = true;

        // check if proper permissions are set
        if(!checkPermissionStorage(context)){
            requestPermissionStorage(context);
        }

        logFile = getActiveLogFile();
    }

    /**
     * Same as other init, but here the user is allowed to specify the name of parent folder
     * @param context
     * @param folder the name of the parent folder
     */
    public void init(Context context, String folder){

        STARTED = true;

        if(isAlpahNum(folder)) {
            FOLDER_LOCATION = Environment.getExternalStorageDirectory() + "/" + folder;
        } else {
            Log.i(TAG, "Folder name invalid. Setting default name");
        }

        // check if proper permissions are set
        if(!checkPermissionStorage(context)){
            requestPermissionStorage(context);
        }

        logFile = getActiveLogFile();
    }

    public void setLogLevel(int level){

        logLevel = level;
    }

    /**
     * Method to request permission to write to external storage
     * @param context context for requesting permission.
     */
    private void requestPermissionStorage(Context context){

        // Ask permissions to for external storage
        ActivityCompat.requestPermissions((Activity)context,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_STORAGE);

    }

    /**
     * Method to check if the application has permission to write to external storage
     * @param context context for checking permissions
     * @return true if permission is set
     */
    private boolean checkPermissionStorage(Context context){

        boolean isOkey = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                Log.i(TAG, "Permissions are set");

            } else {

                isOkey = false;

                Log.i(TAG, "Permissions WRITE.EXTERNAL.STORAGE required");
            }
        } else {

            Log.i(TAG, "Permissions are set");
        }

        return isOkey;
    }

    /**
     * Write info messages to file
     * @param tag origin of the message
     * @param message message to write
     */
    public void info(String tag, String message){

        if(logLevel <= LogLevel.INFO) {
            String log = "[ INFO ] : " + tag + " : " + message;

            writeLog(log);
        }

    }

    /**
     * Write debug messages to file
     * @param tag origin of the message
     * @param message message to write
     */
    public void debug(String tag, String message){

        if(logLevel <= LogLevel.DEBBUG) {
            String log = "[ DEBUG ] : " + tag + " : " + message;

            writeLog(log);
        }

    }

    /**
     * Write error messages to file
     * @param tag origin of the message
     * @param message message to write
     */
    public void error(String tag, String message){

        if(logLevel <= LogLevel.ERROR) {
            String log = "[ ERROR ] : " + tag + " : " + message;

            writeLog(log);
        }

    }

    /**
     * Write a message to file
     * @param message
     */
    public void writeLog(String message){

        if(!STARTED){
            Log.e(TAG, "init(Context) must be called before logging");
        } else {

            log( cleanUp(message) );
        }
    }

    /**
     * Write a a log message to file
     * @param message
     */
    private void log(final String message) {

        if(logFile == null){

            Log.e(TAG, "Could not create log file");

        } else {

            dispatch(message);

        }

    }

    /**
     * Write a message to a log file from a worker thread
     * @param message
     */
    private void dispatch(final String message){


        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {

                    Date date = new Date();

                    String fileName = FILE_NAME_FORMAT.format(date) + ".log";

                    if (!logFile.getName().equals(fileName)) {
                        logFile = getActiveLogFile();
                    }

                    String data = TIMESTAMP_FORMAT.format(new Date(System.currentTimeMillis()));

                    FileWriter workingLogFileWriter = new FileWriter(logFile, true);

                    workingLogFileWriter.write(data + ": " + message + "\n");

                    workingLogFileWriter.close();

                } catch (IOException e) {
                    Log.e(TAG, "Exception -> " + e.getMessage());
                }

            }
        };

        Thread t = new Thread(r);

        t.start();

    }

    /**
     * Method to clean up old log files
     */
    private void cleanLogs() {

        Date date = new Date();

        // Remove logs which are 3 to 10 days old
        for (int i = 2; i < 11; i++) {

            Date oldDate = new Date(date.getTime() - i * 24 * 60 * 60 * 1000L);

            String fileName = FILE_NAME_FORMAT.format(oldDate) + ".log";

            File file = new File(this.getLogFileDir(), fileName);

            if (file.exists()){
                file.delete();
            }
        }

    }

    /**
     * Method to get the current active log file. The log file
     * which is currently being written to.
     * @return
     */
    private File getActiveLogFile() {

        try {

            Date date = new Date();

            String fileName = FILE_NAME_FORMAT.format(date) + ".log";

            File fileDir = getLogFileDir();

            File file = new File(fileDir, fileName);

            Log.i(TAG, "Log file: " + file.getAbsolutePath());

            if (!file.exists()) {

                cleanLogs();

                fileDir.mkdirs();

                file.createNewFile();

            }

            return file;

        } catch (IOException e) {
            Log.e(TAG, "Could not create log file. Exception -> ", e);
        }

        return null;
    }


    /**
     * Method to get the folder where the log files are stored
     * @return
     */
    private File getLogFileDir() {

        File logFileDir = new File(FOLDER_LOCATION, "log-files");

        Log.i(TAG, "Log folder: " + logFileDir.getAbsolutePath());


        if (!logFileDir.exists()) {
            logFileDir.mkdirs();
        }

        return logFileDir;
    }

    /**
     * Helper method to check if a String is alphanumeric only
     * @param value String to check
     * @return true if value is alphanumeric
     */
    private boolean isAlpahNum(String value){
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");

        return pattern.matcher(value).matches();
    }

    /**
     * Limit the size of the log message to @LOG_LENGTH chars
     * @param value
     * @return
     */
    private String cleanUp(String value){

        Log.i(TAG, "Msg size : " + value.length() );

        String formatted = "";

        if(value.length() > LOG_LENTGH){

            formatted = value.substring(0, LOG_LENTGH);

        } else {

            formatted = value;

        }

        Log.i(TAG, "Msg size : " + formatted.length() );

        return formatted;

    }

}
