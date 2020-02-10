package app;

import app.shm.*;
import app.logger.Logger;
import app.mpi.MPIConnector;

import java.io.PrintWriter;
import java.nio.ByteBuffer;

public class App{
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println(
                    "usage: java " + Configuration.class.getName() + " <shm_name> <n_objs> [obj_size] [q_length]");
            System.exit(1);
        }

        Configuration.Builder confBuilder = new Configuration.Builder(args[3], Integer.parseInt(args[4]));
        if(args.length > 5) {
            confBuilder.setObjectSize(Integer.parseInt(args[5]));
            if(args.length > 6) {
                confBuilder.setQueueLength(Integer.parseInt(args[6]));
            }
        }

        Configuration conf = confBuilder.build();
        PrintWriter LOG = Logger.getInstance();
        MPIConnector mpiConnector = MPIConnector.openMPI(args);

        if(mpiConnector.isMaster()){
            Receiver receiver = new Receiver.Builder(conf).buildWithMake();
            ByteBuffer[] bb = receiver.receiveFromSpark(conf.getObjectCount());
            mpiConnector.sendInt(bb.length);
            mpiConnector.sendByteBuffer(bb);
            for(ByteBuffer b: bb){
                LOG.format("" + b);
            }
        }
        else{
            Sender sender = new Sender.Builder(conf).buildWithMake();
            int arraySize = mpiConnector.receiveInt();
            ByteBuffer[] bb = mpiConnector.receiveByteBuffer(arraySize);
            for(ByteBuffer b: bb){
                LOG.format("" + b);
            }
            //md = sender.random_sending(conf.path, conf.n_objs, conf.obj_size, conf.q_length);
        }

        StringBuilder result = new StringBuilder();
        System.out.println(result);

        mpiConnector.closeMPI();
    }
}