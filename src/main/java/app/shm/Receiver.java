package app.shm;

import app.shm.queue.Queue;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Receiver {
    public static class Builder {
        Configuration mConfiguration;

        public Builder(Configuration configuration) {
            mConfiguration = configuration;
        }

        public Receiver buildWithMake() throws IOException {
            return new Receiver(mConfiguration, true);
        }

        public Receiver buildWithoutMake() throws IOException {
            return new Receiver(mConfiguration);
        }
    }
    
    private final Queue mQueue;
    private final XorDigest mDigest;

    private Receiver(Configuration conf, Queue queue) {
        System.err.println("path=" + conf.getPath() +
                ", n_objs=" + conf.getObjectCount() + ", obj_size=" + conf.getObjectSize() + ", q_length=" + conf.getQueueLength());
        mQueue = queue;
        mDigest = new XorDigest(2);
    }

    private Receiver(Configuration conf) throws IOException {
        this(
                conf,
                new Queue.Builder(conf.getPath())
                        .setObjectSize(conf.getObjectSize())
                        .setQueueLength(conf.getQueueLength())
                        .buildWithTruncate()
        );
    }

    private Receiver(Configuration conf,Boolean shouldMake) throws IOException {
        this(conf, new Queue.Builder(conf.getPath()).build());
    }

    public XorDigest randomReceiving(String path, int n_objs)
            throws Exception {
        int progress = -1;
        ByteBuffer b = null;
        for (int i = 0; i < n_objs; i++) {
            b = mQueue.startDequeueWait();
            mQueue.finishDequeue();
            if (progress != i * 100 / n_objs) {
                progress = i * 100 / n_objs;
                System.err.println("progress: " + progress + "%");
            }
        }
        mDigest.update(b);
        return mDigest;
    }

    public ByteBuffer[] receiveFromSpark(int n_objs) throws Exception {
        int progress = -1;
        ByteBuffer[] b = new ByteBuffer[n_objs];
        for (int i = 0; i < n_objs; i++) {
            b[i] = mQueue.startDequeueWait();
            mQueue.finishDequeue();
            if (progress != i * 100 / n_objs) {
                progress = i * 100 / n_objs;
                System.err.println("progress: " + progress + "%");
            }
        }
        return b;
    }

}

