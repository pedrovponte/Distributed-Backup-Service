import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.*;
import java.net.*;

public class TCPChannel implements Runnable {
    private String address;
    private int port;
    private Peer peer;
    private ServerSocket serverSocket;
    private DataInputStream dis;

    public TCPChannel(String address, int port, Peer peer) {
        this.address = address;
        this.port = port;
        this.peer = peer;
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public void run() {

        /*try (Socket socket = new Socket(this.address, this.port)) {

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
        }*/

        try {
            Socket socket = this.serverSocket.accept();
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] message = new byte[65000];
        
        try {
            this.dis.read(message);
        } catch(Exception e) {
            e.printStackTrace();
        }

        this.peer.getThreadExec().execute(new ManageReceivedMessages(this.peer, message));
    }
    
}
