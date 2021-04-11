import java.util.Arrays;
import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;
import java.net.*;

public class GetChunkMessageThread implements Runnable {
    private byte[] message;
    private Peer peer;

    public GetChunkMessageThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        // <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        // <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <Port> <CRLF><CRLF>
        String[] messageStr = (new String(this.message)).split(" ");
        String protocolVersion = messageStr[0];
        int senderId = Integer.parseInt(messageStr[2]);
        String fileId = messageStr[3];
        int chunkNo = Integer.parseInt(messageStr[4]);
        int port = 0;

        if(protocolVersion.equals("2.0")) {
            port = Integer.parseInt(messageStr[5]);
        }   

        // checks if the senderId is equal to the receiver peerId
        if(this.peer.getPeerId() == senderId) {
            return;
        }

        System.out.println("RECEIVED: " + protocolVersion + " GETCHUNK " + senderId + " " + fileId + " " + chunkNo);

        ConcurrentHashMap<String, Chunk> chunksStored = this.peer.getStorage().getChunksStored();
        String chunkId = fileId + "_" + chunkNo;

        if(!(chunksStored.containsKey(chunkId))){
            System.out.println("Don't have chunk " + chunkNo + " stored");
            return;
        }

        // To avoid flooding the host with CHUNK messages, each peer shall wait for a random time uniformly distributed 
        // between 0 and 400 ms, before sending the CHUNK message. If it receives a CHUNK message before that time expires, 
        // it will not send the CHUNK message.
        Random r = new Random();
        int low = 0;
        int high = 400;
        int result = r.nextInt(high-low) + low;

        // initial chunk messages received
        Integer initialNumber = this.peer.getReceivedChunkMessages().get(chunkId);

        try {
            Thread.sleep(result);
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        
        Integer finalNumber = this.peer.getReceivedChunkMessages().get(chunkId);

        if(initialNumber != finalNumber) {
            System.out.println("A peer already has sent chunk " + chunkNo);
            return;
        }

        // <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        String header = protocolVersion + " CHUNK " + this.peer.getPeerId() + " " + fileId + " " + chunkNo + " \r\n\r\n";

        try {
            byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
            byte[] body = chunksStored.get(chunkId).getChunkMessage();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(headerBytes);
            outputStream.write(body);
            byte[] message = outputStream.toByteArray();

            if(protocolVersion.equals("1.0")) {
                this.peer.getThreadExec().execute(new ThreadSendMessages(this.peer.getMDR(), message));
            }

            else {
                this.peer.getThreadExec().execute(new ThreadChunkMessage(message, port));
            }
            System.out.println("SENT: " + header);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }     
        
        //else if (protocolVersion == "2.0")
        //{
            /*try (Socket socket = new Socket("hostname", 6868)) {
     
                Socket socket = serverSocket.accept();
                System.out.println("CONNECTED");

                byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
                byte[] body = chunksStored.get(chunkId).getChunkMessage();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(headerBytes);
                outputStream.write(body);
    
                PrintWriter writer = new PrintWriter(outputStream); 

                writer.println("SENT: " + header);
    
                socket.close();
     
            } catch (IOException ex) {
                System.out.println("Exception: " + ex.getMessage());
                ex.printStackTrace();
            }*/


        //}
        
    }
}
