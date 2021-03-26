import java.util.concurrent.*;
import java.io.File;

public class DeleteMessageThread implements Runnable {
    private byte[] message;
    private Peer peer;


    public DeleteMessageThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        String[] messageStr = (new String(this.message)).split(" ");
        
        // <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>

        int senderId = Integer.parseInt(messageStr[2]);
        String fileId = messageStr[3];

        ConcurrentHashMap<String, Chunk> chunks = this.peer.getStorage().getChunksStored();

        for(String key : chunks.keySet()) {
            Chunk chunk = chunks.get(key);
            if(chunk.getFileId().equals(fileId)) {
                this.peer.getStorage().deleteChunk(key);
                File filename = new File("peer_" + this.peer.getPeerId() + "/backup/" + key);
                filename.delete();
                System.out.println("RECEIVED: " + new String(this.message));
            }
        }
    }
}
