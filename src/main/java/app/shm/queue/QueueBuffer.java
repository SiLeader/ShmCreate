package app.shm.queue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;

public class QueueBuffer {
    private final IntBuffer mMeta;
    private final IndexQueue mAvail;
    private final IndexQueue mUsed;
    private final ByteBuffer mTable;

    final static int INDEX_LOG_OBJECT_SIZE = 0;
    final static int INDEX_LOG_QUEUE_LENGTH = 1;
    final static int INDEX_PRODUCE_EXPOSE_INDEX = 2;
    final static int INDEX_CONSUME_EXPOSE_INDEX = 3;

    public QueueBuffer(MappedByteBuffer buffer) {
        this(false, buffer, 0, 0);
    }

    public QueueBuffer(MappedByteBuffer buffer, int logObjectSize, int logQueueLength) {
        this(true, buffer, logObjectSize, logQueueLength);
    }

    private QueueBuffer(boolean initializeMappedArea, MappedByteBuffer buffer, int logObjectSize, int logQueueLength) {
        buffer.position(0);
        mMeta = ((ByteBuffer)buffer.slice().limit(Sizeof.SHM_QUEUE_META)).order(ByteOrder.nativeOrder()).asIntBuffer();

        if(initializeMappedArea) {
            mMeta.put(INDEX_LOG_OBJECT_SIZE, logObjectSize);
            mMeta.put(INDEX_LOG_QUEUE_LENGTH, logQueueLength);
            mMeta.put(INDEX_PRODUCE_EXPOSE_INDEX, -1);
            mMeta.put(INDEX_CONSUME_EXPOSE_INDEX, -1);
        }
        IntBuffer availMeta, usedMeta, availTable, usedTable;
        final int objectSize = 1 << logObjectSize;
        final int queueLength = 1 << logQueueLength;
        final int indexTableSize = Sizeof.UINT32_T * queueLength;
        final int objectTableSize = objectSize * queueLength;

        buffer.position(Sizeof.SHM_QUEUE_META);
        availMeta = ((MappedByteBuffer)buffer.slice().limit(Sizeof.SHM_IDX_QUEUE)).order(ByteOrder.nativeOrder()).asIntBuffer();

        buffer.position(buffer.position() + Sizeof.SHM_IDX_QUEUE);
        usedMeta = ((MappedByteBuffer)buffer.slice().limit(Sizeof.SHM_IDX_QUEUE)).order(ByteOrder.nativeOrder()).asIntBuffer();

        buffer.position(buffer.position() + Sizeof.SHM_IDX_QUEUE);
        availTable = ((MappedByteBuffer)buffer.slice().limit(indexTableSize)).order(ByteOrder.nativeOrder()).asIntBuffer();

        buffer.position(buffer.position() + indexTableSize);
        usedTable = ((MappedByteBuffer)buffer.slice().limit(indexTableSize)).order(ByteOrder.nativeOrder()).asIntBuffer();

        buffer.position(buffer.position() + indexTableSize);

        mTable = ((MappedByteBuffer)buffer.slice().limit(objectTableSize)).order(ByteOrder.nativeOrder());

        mAvail = new IndexQueue(availMeta, availTable, logQueueLength);
        mUsed = new IndexQueue(usedMeta, usedTable, logQueueLength);

        for (int i = 0; i < queueLength; i++) {
            mAvail.enqueue(i);
        }
    }

    private ByteBuffer startOperation(int IDX_EXPOSE_IDX, IndexQueue q, boolean waitForData) {
        final int exposeIndex = mMeta.get(IDX_EXPOSE_IDX);
        if (exposeIndex >= 0)
            return null;
        final int idx = q.dequeue(waitForData);
        if (idx < 0)
            return null;
        final int logObjectSize = mMeta.get(INDEX_LOG_OBJECT_SIZE);
        final int objectSize = 1 << logObjectSize;
        mTable.position(objectSize * idx);
        ByteBuffer b = ((ByteBuffer) mTable.slice().limit(objectSize)).order(ByteOrder.nativeOrder());
        mMeta.put(IDX_EXPOSE_IDX, idx);
        return b;
    }

    boolean finishOperation(int IDX_EXPOSE_IDX, IndexQueue q) {
        final int exposeIndex = mMeta.get(IDX_EXPOSE_IDX);
        if (exposeIndex < 0)
            return false;
        q.enqueue(exposeIndex);
        mMeta.put(IDX_EXPOSE_IDX, -1);
        return true;
    }

    public ByteBuffer startEnqueue() {
        return startOperation(INDEX_PRODUCE_EXPOSE_INDEX, mAvail, false);
    }

    public ByteBuffer startEnqueueWait() {
        return startOperation(INDEX_PRODUCE_EXPOSE_INDEX, mAvail, true);
    }

    public boolean finishEnqueue() {
        return finishOperation(INDEX_PRODUCE_EXPOSE_INDEX, mUsed);
    }

    public ByteBuffer startDequeue() {
        return startOperation(INDEX_CONSUME_EXPOSE_INDEX, mUsed, false);
    }

    public ByteBuffer startDequeueWait() {
        return startOperation(INDEX_CONSUME_EXPOSE_INDEX, mUsed, true);
    }

    public boolean finishDequeue() {
        return finishOperation(INDEX_CONSUME_EXPOSE_INDEX, mAvail);
    }

    public int getObjectSize() {
        return 1 << mMeta.get(INDEX_LOG_OBJECT_SIZE);
    }

    public int getQueueLength() {
        return 1 << mMeta.get(INDEX_LOG_QUEUE_LENGTH);
    }
}
