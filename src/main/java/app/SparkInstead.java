package app;

import app.shm.Configuration;
import app.eval.Sender;
import app.eval.Receiver;

// not make shm
public class SparkInstead{
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println(
                    "usage: java " + Configuration.class.getName() + " <send|recv> <sharedMemoryName> <maxQueueLength>");
            System.exit(1);
        }

        Configuration.Builder confBuilder = new Configuration.Builder(args[1], Integer.parseInt(args[2]));
        Configuration conf = confBuilder.build();

        if(args[0].equals("send")){
            double[] doubles = new double[100000];
            Sender sender = new Sender.Builder(conf).build();
            sender.writeDoubles(doubles);
        }
        else{
            Receiver receiver = new Receiver.Builder(conf).build();
            double[] doubles = receiver.readDoubles();
        }

        //StringBuilder result = new StringBuilder();
        //for (byte b : md.digest()) {
        //    result.append(String.format("%02x", b));
        //}
        //System.out.println(result);
    }
}
