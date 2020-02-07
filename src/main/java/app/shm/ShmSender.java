package app.shm;

import java.util.Random;

public class ShmSender {
    ShmQueue q;
    XorDigest md;
    Random rnd;
    byte[] series;

    public ShmSender(ShmConf conf) throws Exception{
        System.err.println("path=" + conf.path + 
                ", n_objs=" + conf.n_objs + ", obj_size=" + conf.obj_size + ", q_length=" + conf.q_length);
        q = new ShmQueue(conf.path, conf.obj_size, conf.q_length);
        md = new XorDigest(2);
        rnd = new Random();
        series = new byte[conf.obj_size];
    }

    public XorDigest random_sending(String path, int n_objs, int obj_size, int q_length) throws Exception {
        rnd.nextBytes(series);
        for (int i = 0; i < n_objs; i++) {
            q.start_enqueue_wait().put(series);
            q.finish_enqueue();
        }
        md.update(series);
        return md;
    }

}