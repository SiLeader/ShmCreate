package app;

import app.shm.*;
import app.logger.Logger;
import app.mpi.MPIConnector;

import java.nio.ByteBuffer;

public class App{
    public static void main(String[] args) throws Exception {
        ShmConf conf = new ShmConf();
        Logger LOG = Logger.getInstance();
        MPIConnector mpiConnector = new MPIConnector();
        mpiConnector.openMPI(args);

        ByteBuffer[] bb;
        if(mpiConnector.isMaster()){
            ShmReceiver receiver = new ShmReceiver(conf);
            bb = receiver.recvFromSpark(conf.n_objs);
            mpiConnector.sendInt(bb.length);
            mpiConnector.sendByteBuffer(bb);
            for(ByteBuffer b: bb){
                LOG.logger.format("" + b);
            }
        }
        else{
            ShmSender sender = new ShmSender(conf);
            int arraySize = mpiConnector.recvInt();
            ByteBuffer[] bb = mpiConnector.recvByteBuffer(arraySize);
            for(ByteBuffer b: bb){
                LOG.logger.format("" + b);
            }
            //md = sender.random_sending(conf.path, conf.n_objs, conf.obj_size, conf.q_length);
        }

        StringBuilder result = new StringBuilder();
        System.out.println(result);

        mpiConnector.closeMPI();
    }
}