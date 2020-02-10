package app.shm.queue;

import java.nio.IntBuffer;

public class IndexQueue {
    private final IntBuffer mMetaBuffer;
    private final IntBuffer mTableBuffer;
    private final int mQueueLength;
    private final int mQueueMask;

    private final static int INDEX_PRODUCE = 0;
    private final static int INDEX_CONSUME = 1;

    public IndexQueue(IntBuffer meta, IntBuffer table, int logQueueLength) {
        mMetaBuffer = meta;
        mTableBuffer = table;
        mQueueLength = 1 << logQueueLength;
        mQueueMask = mQueueLength - 1;
        mMetaBuffer.put(INDEX_PRODUCE, 0);
        mMetaBuffer.put(INDEX_CONSUME, 0);
    }

    public boolean enqueue(int v, boolean waitForData) {
        do {
            final int prod = mMetaBuffer.get(INDEX_PRODUCE);
            final int cons = mMetaBuffer.get(INDEX_CONSUME);

            final int nFilled = prod - cons;
            if (nFilled < mQueueLength) {
                mTableBuffer.put(prod & mQueueMask, v);
                mMetaBuffer.put(INDEX_PRODUCE, prod + 1);
                break;
            }
            if (!waitForData)
                return false;
        } while (true);
        return true;
    }

    public boolean enqueue(int v) {
        return enqueue(v, true);
    }

    public int dequeue(boolean waitForData) {
        do {
            final int prod = mMetaBuffer.get(INDEX_PRODUCE);
            final int cons = mMetaBuffer.get(INDEX_CONSUME);
            final int nFilled = prod - cons;
            if (nFilled > 0) {
                final int v = mTableBuffer.get(cons & mQueueMask);
                mMetaBuffer.put(INDEX_CONSUME, cons + 1);
                return v;
            }
            if (!waitForData)
                return -1;
        } while (true);
    }

    public int dequeue() {
        return dequeue(true);
    }
}
