package app.eval;

import app.SharedMemory;
import app.shm.Configuration;
import app.shm.XorDigest;
import app.shm.queue.Queue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Sender {
    public static class Builder {
        Configuration mConfiguration;

        public Builder(Configuration configuration) {
            mConfiguration = configuration;
        }

        public app.eval.Sender build() throws IOException {
            return new app.eval.Sender(mConfiguration);
        }
    }

    private final SharedMemory mSm;
    private final int maxQueueLength;

    private Sender(Configuration conf, SharedMemory sm) {
        System.err.println("path=" + conf.getPath() +
                ", maxQueueLength=" + conf.getMaxQueueLength());
        mSm = sm;
        maxQueueLength = conf.getMaxQueueLength();
    }

    private Sender(Configuration conf) throws IOException {
        this(
                conf,
                new SharedMemory(conf.getPath(), conf.getMaxQueueLength())
        );
    }

   public void writeDoubles(double[] doubles) {
        mSm.set(doubles);
   }
}
