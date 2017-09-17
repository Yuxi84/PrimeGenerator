import java.io.BufferedReader;
import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Yuxi on 2017/4/30.
 */
public class test {
    public static class connectionManager{
        boolean connected = true;
        public connectionManager(){}

    }
    public static class p implements Runnable{
        protected Thread t;
        protected connectionManager cm;
        public p(connectionManager cm){
            this.cm = cm;
        }

        @Override
        public void run() {

            BigInteger p = BigInteger.probablePrime(500, new Random());

            synchronized (cm) {
                if (cm.connected) {
                    //to simulate printback
                    System.out.println(p);
                    return;
                }else{
                    System.out.println("don't have to print");
                }

            }
        }


    };


    public static void main(String[] args){
//        String s = "34.5";
//        boolean right = s.matches("\\d+\\.\\d+");
////        System.out.print(right);
//        System.out.print((s.split("\\."))[1]);
//        String s ="3\n";
//        System.out.print(Integer.parseInt(s));

        //experienment with interrupt method

      //  String threadId = "p1";

        test.connectionManager cm = new test.connectionManager();

        Thread t = new Thread(new p(cm));
        t.start();

        //simlate blocking method here by calling
        BigInteger p = BigInteger.probablePrime(5, new Random());
        System.out.println(p);
        synchronized (cm){
              cm.connected = false;
        }
    }
}
