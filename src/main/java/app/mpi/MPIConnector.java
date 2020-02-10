package app.mpi;

import mpi.*;
import app.logger.Logger;

import java.io.PrintWriter;
import java.nio.ByteBuffer;

public class MPIConnector{
    // MPI  variable
    private final int mMpiRank;
    private final int mMpiSize;
    private final PrintWriter mLog;
    private final Intracomm mComm;

    private MPIConnector(Intracomm world, PrintWriter log) {
        mComm = world;
        mMpiRank = world.Rank();
        mMpiSize = world.Size();
        mLog = log;
        log.format("Rank:%d Size:%d\n", mMpiRank, mMpiSize);
    }

    public static MPIConnector openMPI(String[] args){
        MPI.Init(args);
        PrintWriter log = Logger.getInstance();

        return new MPIConnector(MPI.COMM_WORLD, log);
    }

    public void closeMPI(){
        MPI.Finalize();
    }

    public boolean isMaster(){
        return (mMpiRank == 0);
    }

    public void sendByteBuffer(ByteBuffer[] array){
        mComm.Bcast(array, 0, array.length, MPI.OBJECT, 0);
    }

    public ByteBuffer[] receiveByteBuffer(int arraySize){
        ByteBuffer[] recvBuf = new ByteBuffer[arraySize];
        mComm.Bcast(recvBuf, 0, arraySize, MPI.OBJECT, 0);
        return recvBuf;
    }

    public void sendInt(int value){
        int[] sendBuf = new int[1];
        sendBuf[0] = value;
        mComm.Bcast(sendBuf, 0, 1, MPI.INT, 0);
    }

    public int receiveInt(){
        int[] recvBuf= new int[1];
        mComm.Bcast(recvBuf, 0, 1, MPI.INT, 0);
        return recvBuf[0];
    }

    public void broadcastSendArray(Double[] array, int arraySize){
        for(int i = 0; i < arraySize ; i++){
            mLog.format("Send array[%d] is %f\n", i, array[i]);
        }
        mComm.Bcast(array, 0, arraySize, MPI.OBJECT, 0);
    }
    
    public double[] broadcastReceiveArray(int size){
        double[] recvBuf = new double[size];
        mComm.Bcast(recvBuf, 0, size, MPI.OBJECT, 0);
        mLog.format("array.size: %d array.top: %f array.bottom: %f\n", size, recvBuf[0],recvBuf[size-1]);
        return recvBuf;
    }
}