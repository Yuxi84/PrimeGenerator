import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

/**
 * Created by yzhan14 on 4/29/2017.
 */
public class Server {
    private static int PORT = 12345;

    // class for holding isConnected boolean value
    protected static class ConnectionManager{
        boolean connected;
        protected ConnectionManager(){
            this.connected = true;
        }
    }


    private static class genPrime_Runnable implements Runnable {
        private PrintWriter to;
        private ConnectionManager cm;
        private int bitLength;
        private Socket client_sock;
        public genPrime_Runnable(int bitlen, PrintWriter to, Socket client_sock, ConnectionManager cm) {
            this.to = to;
            this.cm = cm;
            this.bitLength = bitlen;
            this.client_sock = client_sock;
        }

        @Override
        public void run() {
            BigInteger p = BigInteger.probablePrime(bitLength, new Random());

            //check whether connected to decide whether print or not
            synchronized (cm){
                if (cm.connected){
                    //print back to client
                    to.println(p.toString());
                    System.out.println("prime sent");
                    try {
                        client_sock.close();
                    } catch (IOException e) {
                        System.out.println(e.toString());
                    }
                }

                //if not connected, thread terminates automatically
            }
        }

    }

    private static class server_runnable implements Runnable{

        //instance variable
        private Socket client_sock;

        //constructor that takes client socket
        private server_runnable(Socket c_socket){
            this.client_sock = c_socket;
        }
        @Override
        public void run() {

            try {

                BufferedReader from = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));

                PrintWriter to = new PrintWriter(client_sock.getOutputStream(), true);

                String bitlenLit = from.readLine();
                int bitlen = Integer.parseInt(bitlenLit);

                //create a connection manager for this server thread to link stopBt listener and genPrime thread together.
                ConnectionManager server_cm = new ConnectionManager();

                //start a separate thread to generate and print a prime for the client
                Thread t = new Thread(new genPrime_Runnable(bitlen,to, client_sock, server_cm));
                t.start();

                //listen for any stop message from client
                // use while loop for listener to make sure, even one stop msg is enough

                String msg = from.readLine();

                if (msg.equals("stop")){
                    // need to terminate genPrime thread without throwing any exception
                    synchronized (server_cm){

                        server_cm.connected = false;

                        //stop the genPrime thread
                        t.stop();
                    }

                }else{
                    //shouldn't happen
                    System.out.println("msg unexpected from the client received: " + msg);
                }


                //TODO: right place to close connection? or should closed by client?
                client_sock.close();
            }
            catch (SocketException e){
                // Server will close the connection after printing back the prime
                // client could also purposefully close the connection
                System.out.println(e.toString());

            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public static void main(String[] args) {

        ServerSocket server_sock = null;
        try {
            server_sock = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //assume listen fot clients' requests forever
        while (true) {
            try {
                System.out.println("Waiting for connection");
                Socket client_sock = server_sock.accept();

                //now we are connected to a client
                System.out.println("Connected: " + client_sock.getInetAddress());

                Thread server_thread = new Thread(new server_runnable(client_sock));
                server_thread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
