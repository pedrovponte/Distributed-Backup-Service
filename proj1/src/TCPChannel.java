import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.*;
import java.net.*;

public class TCPChannel implements Runnable {
    private InetAddress address;
    private int port;
    private Peer peer;

    public TCPChannel(String address, int port, Peer peer) {
        try{
            this.address = InetAddress.getByName(address);
            this.port = port;
            this.peer = peer;
        } catch(UnknownHostException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 
    }

    public void run() {

        try (Socket socket = new Socket(this.address, port)) {

            while(true) {
                // receive a packet
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String message = reader.readLine();
                byte[] byteArray = message.getBytes();

                ManageReceivedMessages manager = new ManageReceivedMessages(this.peer, byteArray);
                
                // call a thread to execute the task
                this.peer.getThreadExec().execute(manager);
            }

            

        } catch (UnknownHostException ex) {

            System.out.println("Host not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
    
}
