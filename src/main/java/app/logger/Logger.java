package app.logger;

import java.io.*;

public class Logger{
    private static Logger Instance = null;
    public PrintWriter logger = null;

    private Logger(){
        try{
            logger = new PrintWriter("log.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void destroy(){
        if(Instance.logger != null){
            Instance.logger.close();
        }
    }

    public static Logger getInstance(){
        if(Instance == null){
            Instance = new Logger();
        }
        return Logger.Instance;
    }
}