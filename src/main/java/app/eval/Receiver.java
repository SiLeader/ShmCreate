package app.eval;

import app.SharedMemory;
import app.shm.Configuration;
import app.shm.XorDigest;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Receiver {
    public static class Builder {
        Configuration mConfiguration;

        public Builder(Configuration configuration) {
            mConfiguration = configuration;
        }

        public app.eval.Receiver build() throws IOException {
            return new app.eval.Receiver(mConfiguration);
        }
    }

    private final SharedMemory mSm;
    private final int maxQueueLength;

    private Receiver(Configuration conf, SharedMemory sm) {
        System.err.println("path=" + conf.getPath() +
                ", maxQueueLength=" + conf.getMaxQueueLength());
        mSm = sm;
        maxQueueLength = conf.getMaxQueueLength();
    }

    private Receiver(Configuration conf) throws IOException {
        this(
                conf,
                new SharedMemory(conf.getPath(), conf.getMaxQueueLength())
        );
    }


    public double[] readDoubles(){
       return mSm.get();
    }


//    public XorDigest randomReceiving(String path, int n_objs)
//            throws Exception {
//        int progress = -1;
//        ByteBuffer b = null;
//        for (int i = 0; i < n_objs; i++) {
//            b = mQueue.startDequeueWait();
//            mQueue.finishDequeue();
//            System.err.println("[" + i + "]:" + b);
//            if (progress != i * 100 / n_objs) {
//                progress = i * 100 / n_objs;
//                System.err.println("progress: " + progress + "%");
//            }
//        }
//        mDigest.update(b);
//        return mDigest;
//    }
//
//    public ByteBuffer[] receiveFromSpark(int n_objs) throws Exception {
//        int progress = -1;
//        ByteBuffer[] b = new ByteBuffer[n_objs];
//        for (int i = 0; i < n_objs; i++) {
//            b[i] = mQueue.startDequeueWait();
//            mQueue.finishDequeue();
//            if (progress != i * 100 / n_objs) {
//                progress = i * 100 / n_objs;
//                System.err.println("progress: " + progress + "%");
//            }
//        }
//        return b;
//    }
}
