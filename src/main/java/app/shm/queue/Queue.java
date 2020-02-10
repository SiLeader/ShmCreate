package app.shm.queue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidParameterException;

public class Queue {
    public static class Builder {
        private final String mPath;
        private Integer mObjectSize, mQueueLength;

        public Builder(String path) {
            mPath = path;
            mObjectSize = null;
            mQueueLength = null;
        }

        public Queue.Builder setObjectSize(int objectSize) {
            mObjectSize = objectSize;
            return this;
        }

        public Queue.Builder setQueueLength(int queueLength) {
            mQueueLength = queueLength;
            return this;
        }

        private MappedByteBuffer commonBuild(RandomAccessFile file) throws IOException {
            FileChannel channel = file.getChannel();

            final int shmSize = getSize(mObjectSize, mQueueLength);
            channel.truncate(shmSize);
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, shmSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            return buffer;
        }

        public Queue buildWithTruncate() throws IOException {
            if(mObjectSize == null || mQueueLength == null) {
                throw new InvalidParameterException("object size and queue length must be set.");
            }

            RandomAccessFile file = new RandomAccessFile(resolveFullPath(mPath), "rw");
            MappedByteBuffer buffer = commonBuild(file);

            final int logObjectSize = bitCountPowerOf2(mObjectSize);
            final int logQueueLength = bitCountPowerOf2(mQueueLength);
            if (logObjectSize < 0)
                throw new InvalidParameterException("log object size >= 0");
            if (logQueueLength < 0)
                throw new InvalidParameterException("log queue length >= 0");
            QueueBuffer queue = new QueueBuffer(buffer, logObjectSize, logQueueLength);

            return new Queue(mPath, file, queue, buffer);
        }

        public Queue build() throws IOException {
            RandomAccessFile file = new RandomAccessFile(resolveFullPath(mPath), "rw");
            MappedByteBuffer buffer = commonBuild(file);

            QueueBuffer queue = new QueueBuffer(buffer);

            return new Queue(mPath, file, queue, buffer);
        }
    }

    private static String resolveFullPath(String path) {
        return "/dev/shm" + path;
    }

    private static boolean isPowerOf2(int n) {
        if (n <= 0)
            return false;
        return (n & (n - 1)) == 0;
    }

    private static int bitCountPowerOf2(int n) {
        if (!isPowerOf2(n))
            return -1;
        return Integer.bitCount(n);
    }

    private static int getSize(int objectSize, int queueLength) {
        final int sz_meta = Sizeof.SHM_QUEUE;
        final int sz_avail_q_table = Sizeof.UINT32_T * queueLength;
        final int sz_used_q_table = Sizeof.UINT32_T * queueLength;
        final int sz_obj_table = objectSize * queueLength;
        return sz_meta + sz_avail_q_table + sz_used_q_table + sz_obj_table;
    }

    private final String mPath;
    private final RandomAccessFile mFile;
    private final QueueBuffer mQueue;
    private final MappedByteBuffer mBuffer;

    private Queue(String path, RandomAccessFile file, QueueBuffer queue, MappedByteBuffer buffer) {
        mPath = path;
        mFile = file;
        mQueue = queue;
        mBuffer = buffer;
    }

    public boolean remove() throws IOException {
        mFile.close();
        return (new File(resolveFullPath(mPath))).delete();
    }

    public ByteBuffer startEnqueue() {
        return mQueue.startEnqueue();
    }

    public ByteBuffer startEnqueueWait() {
        return mQueue.startEnqueueWait();
    }

    public boolean finishEnqueue() {
        return mQueue.finishEnqueue();
    }

    public ByteBuffer startDequeue() {
        return mQueue.startDequeue();
    }

    public ByteBuffer startDequeueWait() {
        return mQueue.startDequeueWait();
    }

    public boolean finishDequeue() {
        return mQueue.finishDequeue();
    }

    public int getObjectSize() {
        return mQueue.getObjectSize();
    }

    public int getQueueLength() {
        return mQueue.getQueueLength();
    }

    ByteBuffer getBuffer() {
        return ((ByteBuffer)mBuffer.duplicate().position(0)).order(ByteOrder.nativeOrder());
    }

    byte[] dumpBuffer() {
        byte[] b = new byte[mBuffer.limit()];
        getBuffer().get(b);
        return b;
    }
}
