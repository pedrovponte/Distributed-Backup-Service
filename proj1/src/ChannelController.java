import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ChannelController implements Runnable {
    private InetAddress address;
    private int port;
    private Peer peer;

    public ChannelController(String address, int port, Peer peer) {
        try{
            this.address = InetAddress.getByName(address);
            this.port = port;
            this.peer = peer;
        } catch(UnknownHostException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 
    }

    public void sendMessage(byte[] message) {
        System.out.println("Going to send");
        System.out.println("Message: " + message);
        
        try(MulticastSocket multicastSocket = new MulticastSocket(this.port)) {
            DatagramPacket datagramPacket = new DatagramPacket(message, message.length, this.address, this.port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {

        // maximum size of a chunk is 64KBytes (body)
        // header has at least 32 bytes (fileId) + version + messageType + senderId + chunkNo + replicationDegree
        // so 65KBytes should be sufficient to receive the message
        byte[] buf = new byte[65000];

        try {
            MulticastSocket multicastSocket = new MulticastSocket(port);
            multicastSocket.joinGroup(address);
            System.out.println("ChannelController: Join Multicast Group");

            // listens multicast channel
            while(true) {
                // receive a packet
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(packet);
                System.out.println("ChannelController: Received packet");

                byte[] received = Arrays.copyOf(buf, packet.getLength());
                ManageReceivedMessages manager = new ManageReceivedMessages(this.peer, received);
                
                // call a thread to execute the task
                this.peer.getThreadExec().execute(manager);
                System.out.println("ChannelController: Thread executing task");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
