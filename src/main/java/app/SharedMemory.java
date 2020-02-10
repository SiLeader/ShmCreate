package app;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SharedMemory {
    private final long EMPTY_LENGTH = 0;

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

    public double[] get(boolean waitForData) {
        while(waitForData) {
            mBuffer.position(0);
            if(mBuffer.getInt() != EMPTY_LENGTH) {
                break;
            }
        }
        mBuffer.position(0);
        long length = mBuffer.getLong();

        if(length == EMPTY_LENGTH) {
            return null;
        }

        DoubleBuffer doubleBuffer = mBuffer.asDoubleBuffer();
        double[] doubles = new double[(int)length];
        doubleBuffer.get(doubles);

        mBuffer.position(0);
        mBuffer.putLong(EMPTY_LENGTH);

        return doubles;
    }

    public double[] get() {
        return get(true);
    }

    public void set(double[] doubles, boolean waitForRead) {
        assert doubles.length <= mMaxObjectCount;

        while(waitForRead) {
            mBuffer.position(0);
            if(mBuffer.getLong() == EMPTY_LENGTH) {
                break;
            }
        }

        mBuffer.position(0);
        mBuffer.putLong(EMPTY_LENGTH);
        mBuffer.asDoubleBuffer().put(doubles);

        mBuffer.position(0);
        mBuffer.putLong(doubles.length);
    }

    public void set(double[] doubles) {
        set(doubles, true);
    }
}
