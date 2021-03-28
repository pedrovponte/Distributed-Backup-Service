import java.util.Arrays;
import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;

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
        String[] messageStr = (new String(this.message)).split(" ");

        Random r = new Random();
        int low = 0;
        int high = 400;
        int result = r.nextInt(high-low) + low;

        ConcurrentHashMap<String, Chunk> chunksStored = this.peer.getStorage().getChunksStored();
        String chunkId = messageStr[3]+"_"+messageStr[4];

        for(String key : chunksStored.keySet()) {
            Chunk chunk = chunksStored.get(key);
            if(chunk.getFileId().equals(chunkId)) {

                // <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
                String message = messageStr[0] + " CHUNK " + this.peer + " " + messageStr[4] + chunk.getChunkMessage() + " \r\n\r\n";
                
                try {
                    this.peer.getThreadExec().schedule(new ThreadSendMessages(this.peer.getMDR(), message.getBytes()), result, TimeUnit.MILLISECONDS);
                    System.out.println("SENT: " + message);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
}
