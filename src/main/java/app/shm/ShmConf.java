package app.shm;

public class ShmConf{
    public final String path;
    public final int n_objs;
    public int obj_size, q_length;

    public ShmConf(String[] args){
        if (args.length < 2) {
            System.err.println(
                    "usage: java " + ShmConf.class.getName() + " </shm_name> <n_objs> [obj_size] [q_length]");
            System.exit(1);
        }

        path = args[0];
        n_objs = Integer.parseInt(args[1]);
        if (args.length >= 3) {
            obj_size = Integer.parseInt(args[2]);
        } else {
            obj_size = 32;
        }
        if (args.length >= 4) {
            q_length = Integer.parseInt(args[3]);
        } else {
            q_length = 64;
        }
        if (n_objs <= 0)
            throw new IllegalArgumentException("n_objs");
    }

    public ShmConf(){
        path="/hogehoge";
        n_objs=1024;
        obj_size=32; 
        q_length=64;
    }
}
