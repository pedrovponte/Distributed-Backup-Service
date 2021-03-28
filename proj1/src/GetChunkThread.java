import java.util.concurrent.*;

public class GetChunkThread implements Runnable{
    
    private byte[] message;
    private Peer peer;

    public GetChunkThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }
    
    @Override
    public void run(){
        String[] messageStr = (new String(this.message)).split(" ");
        // <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>

        
    }
}
