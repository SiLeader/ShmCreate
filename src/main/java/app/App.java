package app;

import app.shm.*;
import app.mpi.MPIConnector;

import java.nio.ByteBuffer;

public class App{
    public static void main(String[] args) throws Exception {
        ShmConf conf = new ShmConf();
        MPIConnector mpiConnector = new MPIConnector();
        mpiConnector.openMPI(args);

        ByteBuffer[] bb;
        if(mpiConnector.isMaster()){
            ShmReceiver receiver = new ShmReceiver(conf);
            bb = receiver.recvFromSpark(conf.n_objs);
            for(ByteBuffer b: bb){
                System.out.println(b);
            }
        }
        else{
            ShmSender sender = new ShmSender(conf);
            //md = sender.random_sending(conf.path, conf.n_objs, conf.obj_size, conf.q_length);
        }

        StringBuilder result = new StringBuilder();
        System.out.println(result);

        mpiConnector.closeMPI();
    }
}