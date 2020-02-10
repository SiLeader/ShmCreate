package app;

import app.shm.*;

// not make shm
public class SparkInstead{
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println(
                    "usage: java " + Configuration.class.getName() + " <send|recv> <shm_name> <n_objs> [obj_size] [q_length]");
            System.exit(1);
        }

        Configuration.Builder confBuilder = new Configuration.Builder(args[1], Integer.parseInt(args[2]));
        if(args.length > 3) {
            confBuilder.setObjectSize(Integer.parseInt(args[3]));
            if(args.length > 4) {
                confBuilder.setQueueLength(Integer.parseInt(args[4]));
            }
        }

        Configuration conf = confBuilder.build();
        XorDigest md;
        if(args[0].equals("send")){
            Sender sender = new Sender.Builder(conf).buildWithoutMake();
            md = sender.randomSending(conf.getPath(), conf.getObjectCount(), conf.getObjectSize(), conf.getQueueLength());
        }
        else{
            Receiver receiver = new Receiver.Builder(conf).buildWithoutMake();
            md = receiver.randomReceiving(conf.getPath(), conf.getObjectCount());
        }

        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        System.out.println(result);
    }
}
