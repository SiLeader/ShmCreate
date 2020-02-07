package app;

import app.shm.*;

// not make shm
public class SparkInstead{
    public static void main(String[] args) throws Exception {
        ShmConf conf = new ShmConf();
        XorDigest md;
        if(args[0].equals("send")){
            ShmSender sender = new ShmSender(conf,false);
            md = sender.random_sending(conf.path, conf.n_objs, conf.obj_size, conf.q_length);
        }
        else{
            ShmReceiver receiver = new ShmReceiver(conf,false);
            md = receiver.random_receiving(conf.path, conf.n_objs);
        }

        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        System.out.println(result);
    }
}
