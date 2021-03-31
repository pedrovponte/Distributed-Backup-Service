import java.util.concurrent.*;
import java.util.Random;

public class RemovedMessageThread implements Runnable {

    private byte[] message;
    private Peer peer;

    public RemovedMessageThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        // <Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        String[] messageStr = (new String(this.message)).split(" ");
        String protocolVersion = messageStr[0];
        int senderId = Integer.parseInt(messageStr[2]);
        String fileId = messageStr[3];
        int chunkNo = Integer.parseInt(messageStr[4]);

        // checks if the senderId is equal to the receiver peerId
        if(this.peer.getPeerId() == senderId) {
            return;
        }

        System.out.println("RECEIVED: " + protocolVersion + " REMOVED " + senderId + " " + fileId + " " + chunkNo);

        ConcurrentHashMap<String, Chunk> chunksStored = this.peer.getStorage().getChunksStored();
        String chunkId = fileId + "_" + chunkNo;

        if((chunksStored.containsKey(chunkId))){
            System.out.println("Already has chunk " + chunkNo + " stored");
            // Needs to update local count of chunk

            // If lower than replication
            //if (          < chunksStored.get(chunkId).getReplication()){
                
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

                //this.peer.backup(path, replication);
            //}

            return;
        }


    }   
}
