package app.shm;

import app.shm.queue.Queue;

import java.io.IOException;
import java.util.Random;

public class Sender {
    public static class Builder {
        Configuration mConfiguration;

        public Builder(Configuration configuration) {
            mConfiguration = configuration;
        }

        public Sender buildWithMake() throws IOException {
            return new Sender(mConfiguration, true);
        }

        public Sender buildWithoutMake() throws IOException {
            return new Sender(mConfiguration);
        }
    }

    private final Queue mQueue;
    private final XorDigest mDigest;
    private final Random mRandom;
    private final byte[] mSeries;

    private Sender(Configuration conf, Queue queue) {
        System.err.println("path=" + conf.getPath() +
                ", n_objs=" + conf.getObjectCount() + ", obj_size=" + conf.getObjectSize() + ", q_length=" + conf.getQueueLength());
        mQueue = queue;
        mDigest = new XorDigest(2);
        mRandom = new Random();
        mSeries = new byte[conf.getObjectSize()];
    }

    private Sender(Configuration conf) throws IOException {
        this(
                conf,
                new Queue.Builder(conf.getPath())
                        .setObjectSize(conf.getObjectSize())
                        .setQueueLength(conf.getQueueLength())
                        .buildWithTruncate()
        );
    }

    private Sender(Configuration conf, Boolean shouldMake) throws IOException {
        this(
                conf,
                new Queue.Builder(conf.getPath()).build()
        );
    }

    public XorDigest randomSending(String path, int n_objs, int obj_size, int q_length) throws Exception {
        mRandom.nextBytes(mSeries);
        for (int i = 0; i < n_objs; i++) {
            System.err.println("[" + i + "]:"+ mSeries);
            mQueue.startEnqueueWait().put(mSeries);
            mQueue.finishEnqueue();
        }
        mDigest.update(mSeries);
        return mDigest;
    }

}