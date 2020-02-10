package app.shm;

public class Configuration {
    public static class Builder {
        private String mPath;
        private int mMaxQueueLength;

        public Builder(String path, int maxQueueLength) {
            assert path != null;

            if (maxQueueLength <= 0)
                throw new IllegalArgumentException("maxQueueLength must be 1 or more.");

            mPath = path;
            mMaxQueueLength = maxQueueLength;
        }

        public Configuration build() {
            return new Configuration(mPath, mMaxQueueLength);
        }
    }


    private final String mPath;
    private final int  mMaxQueueLength;

    private Configuration(String path, int maxQueueLength) {
        mPath = path;
        mMaxQueueLength = maxQueueLength;

        System.err.printf("path: %s, max queue length: %d\n", mPath, mMaxQueueLength);
    }

    public String getPath() {
        return mPath;
    }

    public int getMaxQueueLength() {
        return mMaxQueueLength;
    }
}
