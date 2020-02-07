package app.shm;

import app.shm.ShmQueue;
import java.nio.ByteBuffer;
import java.util.Random;

public class ShmReceiver {
  ShmQueue q;
  XorDigest md;

  public ShmReceiver(ShmConf conf) throws Exception{
    System.err.println("path=" + conf.path +
            ", n_objs=" + conf.n_objs + ", obj_size=" + conf.obj_size + ", q_length=" + conf.q_length);
    q = new ShmQueue(conf.path, conf.obj_size, conf.q_length);
    md = new XorDigest(2);
  }

  public ShmReceiver(ShmConf conf,Boolean shouldMakeShm) throws Exception{
    System.err.println("path=" + conf.path +
            ", n_objs=" + conf.n_objs + ", obj_size=" + conf.obj_size + ", q_length=" + conf.q_length);
    q = new ShmQueue(conf.path);
    md = new XorDigest(2);
  }

  public XorDigest random_receiving(String path, int n_objs)
    throws Exception {
    int progress = -1;
    ByteBuffer b = null;
    for (int i = 0; i < n_objs; i++) {
      b = q.start_dequeue_wait();
      q.finish_dequeue();
      if (progress != i * 100 / n_objs) {
        progress = i * 100 / n_objs;
        System.err.println("progress: " + progress + "%");
      }
    }
    md.update(b);
    return md;
  }
}

