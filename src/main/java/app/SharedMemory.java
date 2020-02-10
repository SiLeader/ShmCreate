package app;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SharedMemory {
    private final int EMPTY_LENGTH = 0;
    private final int LAST_LENGTH = -1;

    private RandomAccessFile mFile;
    private final MappedByteBuffer mBuffer;
    private final int mMaxObjectCount;

    public SharedMemory(String filePath, int maxObjectCount) throws IOException {
        assert maxObjectCount > 0;

        final int objectSize = Double.BYTES;

        mMaxObjectCount = maxObjectCount;

        mFile = new RandomAccessFile(filePath, "rw");
        FileChannel channel = mFile.getChannel();

        mBuffer = channel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                objectSize * maxObjectCount + 8);
        mBuffer.order(ByteOrder.nativeOrder());
    }

    public void close() throws IOException {
        if(mFile != null)
            mFile.close();
        mFile = null;
    }

    private int getImpl(double[] doubles, int offset, boolean waitForData) {
        while(waitForData) {
            mBuffer.position(0);
            if(mBuffer.getInt() != EMPTY_LENGTH) {
                break;
            }
        }
        mBuffer.position(0);
        int length = mBuffer.getInt();
        mBuffer.position(8);

        DoubleBuffer doubleBuffer = mBuffer.asDoubleBuffer();
        doubleBuffer.get(doubles, offset, length);

        mBuffer.position(0);
        mBuffer.putInt(EMPTY_LENGTH);

        return length;
    }

    public double[] get(boolean waitForData) {
        while(waitForData) {
            mBuffer.position(0);
            if(mBuffer.getInt() != EMPTY_LENGTH) {
                break;
            }
        }
        mBuffer.position(0);
        int length = mBuffer.getInt();
        int totalLength = mBuffer.getInt();

        if(length == EMPTY_LENGTH) {
            return null;
        }

        double[] doubles = new double[totalLength];
        int currentOffset = 0;
        do {
            currentOffset += getImpl(doubles, currentOffset, waitForData);
            waitForData = true;

        }while(currentOffset < totalLength);

        return doubles;
    }

    public double[] get() {
        return get(true);
    }

    private void setImpl(double[] doubles, int offset, int length, boolean waitForRead) {
        while(waitForRead) {
            mBuffer.position(0);
            if(mBuffer.getInt() == EMPTY_LENGTH) {
                break;
            }
        }

        mBuffer.position(0);
        mBuffer.putInt(EMPTY_LENGTH);
        mBuffer.putInt(doubles.length);
        mBuffer.asDoubleBuffer().put(doubles, offset, length);

        mBuffer.position(0);
        mBuffer.putInt(doubles.length);
    }

    public void set(double[] doubles, boolean waitForRead) {
        int currentOffset = 0;
        int currentLength;

        do {
            currentLength = Math.min(mMaxObjectCount, doubles.length - currentOffset);
            setImpl(doubles, currentOffset, currentLength, waitForRead);
            waitForRead = true;

            currentOffset = currentLength;
        }while(currentOffset < doubles.length);
    }

    public void set(double[] doubles) {
        set(doubles, true);
    }
}
