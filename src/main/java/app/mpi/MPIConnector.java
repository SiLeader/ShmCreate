package app.mpi;

import mpi.*;
import app.logger.Logger;

import java.nio.ByteBuffer;

public class MPIConnector{
    // MPI  variable
    int mpiRank = -1;
    int mpiSize = -1;
    Logger LOG = null;

    public void openMPI(String[] args){
        MPI.Init(args);    
        mpiRank = MPI.COMM_WORLD.Rank();
        mpiSize = MPI.COMM_WORLD.Size();
        LOG = Logger.getInstance();
        LOG.logger.format("Rank:%d Size:%d\n", mpiRank, mpiSize);
    }

    public void closeMPI(){
        MPI.Finalize();
    }

    public boolean isMaster(){
        return (mpiRank == 0);
    }

    public void sendByteBuffer(ByteBuffer[] array){
        MPI.COMM_WORLD.Bcast(array, 0, array.length, MPI.OBJECT, 0);
    }

    public ByteBuffer[] recvByteBuffer(int arraySize){
        ByteBuffer[] recvBuf = new ByteBuffer[arraySize];
        MPI.COMM_WORLD.Bcast(recvBuf, 0, arraySize, MPI.OBJECT, 0);
        return recvBuf;
    }

    public void sendInt(int value){
        int[] sendBuf = new int[1];
        sendBuf[0] = value;
        MPI.COMM_WORLD.Bcast(sendBuf, 0, 1, MPI.INT, 0);
    }

    public int recvInt(){
        int[] recvBuf= new int[1];
        MPI.COMM_WORLD.Bcast(recvBuf, 0, 1, MPI.INT, 0);
        return recvBuf[0];
    }

    public void BcastSendArray(Double[] array,int arraySize){
        for(int i = 0; i < arraySize ; i++){
            LOG.logger.format("Send array[%d] is %f\n", i, array[i]);
        }
        MPI.COMM_WORLD.Bcast(array, 0, arraySize, MPI.OBJECT, 0);
    }
    
    public Double[] BcastRecvArray(int size){
        Double[] recvBuf = new Double[size];
        MPI.COMM_WORLD.Bcast(recvBuf, 0, size, MPI.OBJECT, 0);
        LOG.logger.format("array.size: %d array.top: %f array.bottom: %f\n", size, recvBuf[0],recvBuf[size-1]);
        return recvBuf;
    }
}