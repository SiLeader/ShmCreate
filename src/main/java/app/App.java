package app;

import app.shm.*;
import app.mpi.MPIConnector;

public class App{
    public static void main(String[] args) throws Exception {
        ShmConf conf = new ShmConf();
        //MPIConnector mpiConnector = new MPIConnector();
        //mpiConnector.openMPI(args);

        XorDigest md;
        //if(mpiConnector.isMaster()){
        if(args[0].equals("send")){
            ShmSender sender = new ShmSender(conf);
            md = sender.random_sending(conf.path, conf.n_objs, conf.obj_size, conf.q_length);
        }
        else{
            ShmReceiver receiver = new ShmReceiver(conf);
            md = receiver.random_receiving(conf.path, conf.n_objs);
        }

        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        System.out.println(result);
    }
}