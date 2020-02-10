package app.shm;

public class Configuration {
    public static class Builder {
        private String mPath;
        private int mObjectCount;
        private int mObjectSize, mQueueLength;

        public Builder(String path, int objectCount) {
            if (objectCount <= 0)
                throw new IllegalArgumentException("object count must be 1 or more.");
            mPath = path;
            mObjectCount = objectCount;
            mObjectSize = 32;
            mQueueLength = 64;
        }

        public Builder setObjectSize(int objectSize) {
            mObjectSize = objectSize;
            return this;
        }

        public Builder setQueueLength(int queueLength) {
            mQueueLength = queueLength;
            return this;
        }

        public Configuration build() {
            return new Configuration(mPath, mObjectCount, mObjectSize, mQueueLength);
        }
    }


    private final String mPath;
    private final int mObjectCount;
    private final int mObjectSize, mQueueLength;

    private Configuration(String path, int objectCount, int objectSize, int queueLength) {
        /*if (args.length < 2) {
            System.err.println(
                    "usage: java " + ShmConf.class.getName() + " </shm_name> <n_objs> [obj_size] [q_length]");
            System.exit(1);
        }*/

        mPath = path;
        mObjectCount = objectCount;
        mObjectSize = objectSize;
        mQueueLength = queueLength;
    }

    public String getPath() {
        return mPath;
    }

    public int getObjectCount() {
        return mObjectCount;
    }

    public int getObjectSize() {
        return mObjectSize;
    }

    public int getQueueLength() {
        return mQueueLength;
    }
}
