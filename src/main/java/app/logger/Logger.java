package app.logger;

import java.io.*;

public class Logger{
    private static PrintWriter mLogger = null;

    public static void destroy(){
        if(mLogger != null){
            mLogger.close();
        }
    }

    public static PrintWriter getInstance(){
        if(mLogger == null){
            try{
                mLogger = new PrintWriter("log.txt");
            }catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        return Logger.mLogger;
    }
}