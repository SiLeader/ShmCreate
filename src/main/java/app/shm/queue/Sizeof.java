package app.shm.queue;

public class Sizeof {
    public static final int UINT32_T = 4;
    public static final int SHM_QUEUE_META = UINT32_T * 4;
    public static final int SHM_IDX_QUEUE = UINT32_T * 2;
    public static final int SHM_QUEUE = SHM_QUEUE_META + SHM_IDX_QUEUE * 2;
}
