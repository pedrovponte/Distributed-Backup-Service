import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.*;

public class ThreadCountStored implements Runnable {
    private Peer peer;
    private int replication;
    private String fileId;
    private int chunkNo;
    private ChannelController channel;
    private byte[] message;
    private int tries = 0;
    private int time = 1;

    public ThreadCountStored(Peer peer, int replication, String fileId, int chunkNo, ChannelController channel, byte[] message) {
        this.peer = peer;
        this.replication = replication;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.channel = channel;
        this.message = message;
    }


    @Override
    public void run() {
        // <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        /*ConcurrentHashMap<String, Integer> stored = this.peer.getStorage().getStoredMessagesReceived();
        String chunkId = this.fileId + "_" + this.chunkNo;
        int storedReplications = 0;

        for(String key : stored.keySet()) {
            if(key.equals(chunkId)) {
                storedReplications = stored.get(key);
                break;
            }
        }*/

        ConcurrentHashMap<Integer, ArrayList<String>> distribution = this.peer.getStorage().getChunksDistribution();
        String chunkId = this.fileId + "_" + this.chunkNo;
        int storedReplications = 0;

        for(Integer key : distribution.keySet()) {
            if(distribution.get(key).contains(chunkId)) {
                storedReplications++;
                //System.out.println("Stored replications of " + chunkId + ": " + storedReplications);
            }
        }
        
        if(storedReplications < this.replication && this.tries < 4) {
            this.peer.getThreadExec().execute(new ThreadSendMessages(this.channel, this.message));
            String[] messageArr = (new String(this.message).toString()).split(" ");
            System.out.println("SENT: "+ messageArr[0] + " " + messageArr[1] + " " + messageArr[2] + " " + messageArr[3] + " " + messageArr[4]);
            this.time = this.time * 2;
            System.out.println("TIME: " + this.time);
            this.tries++;
            this.peer.getThreadExec().schedule(this, this.time, TimeUnit.SECONDS);
            // System.out.println("After create thread");
        }

        if(this.tries >= 5) {
            System.out.println("Minimum replication not achieved");
            return;
        }
        else if(storedReplications >= this.replication) {
            System.out.println("Replication completed: " + storedReplications);
            return;
        }
    }
}
