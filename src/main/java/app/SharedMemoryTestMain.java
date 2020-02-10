package app;

import java.util.Arrays;

public class SharedMemoryTestMain {
    public static void main(String[] args) throws Exception {
        SharedMemory sm = new SharedMemory("data.bin", 1_000);

        if(args.length == 1) {
            double[] doubles = new double[100_000];
            for(int i = 0; i < doubles.length; ++i) {
                doubles[i] = i;
            }

            sm.set(doubles);
            System.out.printf("length: %s\n", doubles.length);
            System.out.println(Arrays.stream(doubles).reduce(Double::sum));
        }else {
            double[] doubles = sm.get();

            System.out.printf("length: %s\n", doubles.length);
            System.out.println(Arrays.stream(doubles).reduce(Double::sum));
        }
    }
}
