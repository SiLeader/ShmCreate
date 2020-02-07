package app.shm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class Sizeof {
    public static final int UINT32_T = 4;
    public static final int SHM_QUEUE_META = UINT32_T * 4;
    public static final int SHM_IDX_QUEUE = UINT32_T * 2;
    public static final int SHM_QUEUE = SHM_QUEUE_META + SHM_IDX_QUEUE * 2;
}

class ShmIndexQueue {
    IntBuffer meta;
    IntBuffer table;
    int q_length;
    int q_mask;

    public ShmIndexQueue(IntBuffer meta, IntBuffer table, int log_q_length) {
        this.meta = meta;
        this.table = table;
        this.q_length = 1 << log_q_length;
        this.q_mask = this.q_length - 1;
    }

    final static int IDX_PROD = 0;
    final static int IDX_CONS = 1;

    public void init() {
        meta.put(IDX_PROD, 0);
        meta.put(IDX_CONS, 0);
    }

    public int enq(int v, boolean poll) {
        int prod, cons, n_filled;
        do {
            prod = meta.get(IDX_PROD);
            cons = meta.get(IDX_CONS);
            n_filled = prod - cons;
            if (n_filled < q_length)
                break;
            if (!poll)
                return -1;
        } while (true);
        table.put(prod & q_mask, v);
        meta.put(IDX_PROD, prod + 1);
        return 0;
    }

    public int enq(int v) {
        return enq(v, false);
    }

    int deq(boolean poll) {
        int prod, cons, n_filled;
        do {
            prod = meta.get(IDX_PROD);
            cons = meta.get(IDX_CONS);
            n_filled = prod - cons;
            if (n_filled > 0)
                break;
            if (!poll)
                return -1;
        } while (true);
        final int v = table.get(cons & q_mask);
        meta.put(IDX_CONS, cons + 1);
        return v;
    }

    public int deq() {
        return deq(false);
    }
}

class ShmQueueBuffer {
    IntBuffer meta;
    ShmIndexQueue avail;
    ShmIndexQueue used;
    ByteBuffer table;

    final static int IDX_LOG_OBJ_SIZE = 0;
    final static int IDX_LOG_Q_LENGTH = 1;
    final static int IDX_PROD_EXPOSE_IDX = 2;
    final static int IDX_CONS_EXPOSE_IDX = 3;

    void map_buffer(MappedByteBuffer buffer) {
        IntBuffer avail_meta, used_meta, avail_table, used_table;
        final int log_obj_size = meta.get(IDX_LOG_OBJ_SIZE);
        final int log_q_length = meta.get(IDX_LOG_Q_LENGTH);
        final int obj_size = 1 << log_obj_size;
        final int q_length = 1 << log_q_length;
        final int idx_table_size = Sizeof.UINT32_T * q_length;
        final int obj_table_size = obj_size * q_length;

        buffer.position(Sizeof.SHM_QUEUE_META);
        avail_meta = ((MappedByteBuffer)buffer.slice().limit(Sizeof.SHM_IDX_QUEUE)).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.position(buffer.position() + Sizeof.SHM_IDX_QUEUE);
        used_meta = ((MappedByteBuffer)buffer.slice().limit(Sizeof.SHM_IDX_QUEUE)).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.position(buffer.position() + Sizeof.SHM_IDX_QUEUE);
        avail_table = ((MappedByteBuffer)buffer.slice().limit(idx_table_size)).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.position(buffer.position() + idx_table_size);
        used_table = ((MappedByteBuffer)buffer.slice().limit(idx_table_size)).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.position(buffer.position() + idx_table_size);
        table = ((MappedByteBuffer)buffer.slice().limit(obj_table_size)).order(ByteOrder.nativeOrder());

        avail = new ShmIndexQueue(avail_meta, avail_table, log_q_length);
        used = new ShmIndexQueue(used_meta, used_table, log_q_length);
    }

    void update_meta(int log_obj_size, int log_q_length) {
        meta.put(IDX_LOG_OBJ_SIZE, log_obj_size);
        meta.put(IDX_LOG_Q_LENGTH, log_q_length);
        meta.put(IDX_PROD_EXPOSE_IDX, -1);
        meta.put(IDX_CONS_EXPOSE_IDX, -1);
    }

    void init() {
        final int log_q_length = meta.get(IDX_LOG_Q_LENGTH);
        final int q_length = 1 << log_q_length;
        avail.init();
        used.init();
        for (int i = 0; i < q_length; i++) {
            avail.enq(i);
        }
    }

    ByteBuffer start_op(int IDX_EXPOSE_IDX, ShmIndexQueue q, boolean poll) {
        final int expose_idx = meta.get(IDX_EXPOSE_IDX);
        if (expose_idx >= 0)
            return null;
        final int idx = q.deq(poll);
        if (idx < 0)
            return null;
        final int log_obj_size = meta.get(IDX_LOG_OBJ_SIZE);
        final int obj_size = 1 << log_obj_size;
        table.position(obj_size * idx);
        ByteBuffer b = ((ByteBuffer)table.slice().limit(obj_size)).order(ByteOrder.nativeOrder());
        meta.put(IDX_EXPOSE_IDX, idx);
        return b;
    }

    ByteBuffer start_op(int IDX_EXPOSE_IDX, ShmIndexQueue q) {
        return start_op(IDX_EXPOSE_IDX, q, false);
    }

    boolean finish_op(int IDX_EXPOSE_IDX, ShmIndexQueue q) {
        final int expose_idx = meta.get(IDX_EXPOSE_IDX);
        if (expose_idx < 0)
            return false;
        q.enq(expose_idx);
        meta.put(IDX_EXPOSE_IDX, -1);
        return true;
    }

    public ShmQueueBuffer(MappedByteBuffer buffer) {
        buffer.position(0);
        meta = ((ByteBuffer)buffer.slice().limit(Sizeof.SHM_QUEUE_META)).order(ByteOrder.nativeOrder()).asIntBuffer();
        map_buffer(buffer);
    }

    public ShmQueueBuffer(MappedByteBuffer buffer, int log_obj_size, int log_q_length) {
        buffer.position(0);
        meta = ((ByteBuffer)buffer.slice().limit(Sizeof.SHM_QUEUE_META)).order(ByteOrder.nativeOrder()).asIntBuffer();
        update_meta(log_obj_size, log_q_length);
        map_buffer(buffer);
        init();
    }

    public ByteBuffer start_enq() {
        return start_op(IDX_PROD_EXPOSE_IDX, avail);
    }

    public ByteBuffer start_enq_wait() {
        return start_op(IDX_PROD_EXPOSE_IDX, avail, true);
    }

    public boolean finish_enq() {
        return finish_op(IDX_PROD_EXPOSE_IDX, used);
    }

    public ByteBuffer start_deq() {
        return start_op(IDX_CONS_EXPOSE_IDX, used);
    }

    public ByteBuffer start_deq_wait() {
        return start_op(IDX_CONS_EXPOSE_IDX, used, true);
    }

    public boolean finish_deq() {
        return finish_op(IDX_CONS_EXPOSE_IDX, avail);
    }

    public int obj_size() {
        return 1 << meta.get(IDX_LOG_OBJ_SIZE);
    }

    public int q_length() {
        return 1 << meta.get(IDX_LOG_Q_LENGTH);
    }
}

public class ShmQueue {
    static boolean is_power_of_2(int n) {
        if (n <= 0)
            return false;
        return (n & (n - 1)) == 0;
    }

    static int log_uint(int n) {
        int r = 0;
        if (!is_power_of_2(n))
            return -1;
        while (n > 1) {
            r++;
            n >>= 1;
        }
        return r;
    }

    static int get_shm_size(int obj_size, int q_length) {
        final int sz_meta = Sizeof.SHM_QUEUE;
        final int sz_avail_q_table = Sizeof.UINT32_T * q_length;
        final int sz_used_q_table = Sizeof.UINT32_T * q_length;
        final int sz_obj_table = obj_size * q_length;
        return sz_meta + sz_avail_q_table + sz_used_q_table + sz_obj_table;
    }

    String path;
    RandomAccessFile file;
    ShmQueueBuffer queue;
    MappedByteBuffer buffer;

    String fullpath() {
        return "/dev/shm" + path;
    }

    public ShmQueue(String path, int obj_size, int q_length) throws IOException {
        final int log_obj_size = log_uint(obj_size);
        final int log_q_length = log_uint(q_length);
        if (log_obj_size < 0)
            throw new IllegalArgumentException("log_obj_size");
        if (log_q_length < 0)
            throw new IllegalArgumentException("log_q_length");
        this.path = path;

        this.file = new RandomAccessFile(fullpath(), "rw");
        FileChannel channel = this.file.getChannel();
        final int sz_shm = get_shm_size(obj_size, q_length);
        channel.truncate(sz_shm);
        buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, sz_shm);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.queue = new ShmQueueBuffer(buffer, log_obj_size, log_q_length);
    }

    public ShmQueue(String path) throws IOException {
        this.path = path;
        this.file = new RandomAccessFile(fullpath(), "rw");
        FileChannel channel = this.file.getChannel();
        final int sz_shm = (int) channel.size();
        buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, sz_shm);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.queue = new ShmQueueBuffer(buffer);
    }

    public void remove() throws IOException {
        file.close();
        (new File(fullpath())).delete();
    }

    public ByteBuffer start_enqueue() {
        return queue.start_enq();
    }

    public ByteBuffer start_enqueue_wait() {
        return queue.start_enq_wait();
    }

    public boolean finish_enqueue() {
        return queue.finish_enq();
    }

    public ByteBuffer start_dequeue() {
        return queue.start_deq();
    }

    public ByteBuffer start_dequeue_wait() {
        return queue.start_deq_wait();
    }

    public boolean finish_dequeue() {
        return queue.finish_deq();
    }

    public int obj_size() {
        return queue.obj_size();
    }

    public int q_length() {
        return queue.q_length();
    }

    ByteBuffer get_buffer() {
        return ((ByteBuffer)buffer.duplicate().position(0)).order(ByteOrder.nativeOrder());
    }

    byte[] dump_buffer() {
        byte[] b = new byte[buffer.limit()];
        get_buffer().get(b);
        return b;
    }
}
