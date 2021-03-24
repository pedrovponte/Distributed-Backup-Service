import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
        // System.out.println("Before while");
        // while(this.tries < 5) {
        //     System.out.println("TRIES: " + tries);
        //     ConcurrentHashMap<String, Integer> stored = this.peer.getStorage().getStoredMessagesReceived();
        //     for(String key : stored.keySet()) {
        //         String chunkId = this.fileId + "_" + this.chunkNo;
        //         if(key.equals(chunkId)) {
        //             if(stored.get(key) < this.replication) {
        //                 this.peer.getThreadExec().execute(new ThreadSendMessages(this.channel, this.message));
        //                 String[] messageArr = (new String(this.message).toString()).split(" ");
        //                 System.out.println("SENT: "+ messageArr[0] + " " + messageArr[1] + " " + messageArr[2] + " " + messageArr[3] + " " + messageArr[4]);
        //                 this.time *= 2;
        //                 System.out.println("TIME: " + this.time);
        //                 this.tries++;
        //                 this.peer.getThreadExec().schedule(this, this.time, TimeUnit.SECONDS);
        //                 System.out.println("After create thread");
        //             }
        //             else {
        //                 System.out.println("Replication completed: " + stored.get(key));
        //                 return;
        //             }
        //         }
        //     }
        // }

        ConcurrentHashMap<String, Integer> stored = this.peer.getStorage().getStoredMessagesReceived();
        String chunkId = this.fileId + "_" + this.chunkNo;
        int storedReplications = 0;

        for(String key : stored.keySet()) {
            if(key.equals(chunkId)) {
                storedReplications = stored.get(key);
                break;
            }
        }
        
        if(storedReplications < this.replication && this.tries < 5) {
            this.peer.getThreadExec().execute(new ThreadSendMessages(this.channel, this.message));
            String[] messageArr = (new String(this.message).toString()).split(" ");
            System.out.println("SENT: "+ messageArr[0] + " " + messageArr[1] + " " + messageArr[2] + " " + messageArr[3] + " " + messageArr[4]);
            this.time = this.time * 2;
            System.out.println("TIME: " + this.time);
            this.tries++;
            this.peer.getThreadExec().schedule(this, this.time, TimeUnit.SECONDS);
            System.out.println("After create thread");
        }

        if(this.tries >= 5) {
            System.out.println("Minimum replication not achieved");
            return;
        }
        else if(storedReplications >= this.replication) {
            System.out.println("Replication completed: " + stored.get(chunkId));
            return;
        }
    }
}
