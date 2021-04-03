import java.util.Arrays;
import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class PutChunkMessageThread implements Runnable {
    private byte[] message;
    private Peer peer;
    private byte[] header;
    private byte[] body;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replication_degree;
    private String protocolVersion;

    // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    public PutChunkMessageThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
        splitHeaderAndBody();
        String[] headerStr = new String(this.header).split(" ");
        this.protocolVersion = headerStr[0];
        this.senderId = Integer.parseInt(headerStr[2]);
        this.fileId = headerStr[3];
        this.chunkNo = Integer.parseInt(headerStr[4]);
        this.replication_degree = Integer.parseInt(headerStr[5]);
        // System.out.println("SenderId: " + this.senderId);
        // System.out.println("FileId: " + this.fileId);
        // System.out.println("ChunkNo: " + this.chunkNo);
        // System.out.println("Replication: " + this.replication_degree);

    }

    @Override
    public void run() {
        // in case senderId and peerId are equal, the thread returns because a peer must never store the chunks of its own files.
        if(checkIfSelf() == 1) {
            // System.out.println("Equals");
            return;
        }
        // System.out.println("Not equals");

        System.out.println("RECEIVED: " + this.protocolVersion + " PUTCHUNK " + this.senderId + " " + this.fileId + " " + this.chunkNo + " " + this.replication_degree);

        //check if the peer already has stored this chunk
        if(this.peer.getStorage().hasChunk(this.fileId, this.chunkNo) == true) {
            System.out.println("Already has chunk");
            Random r = new Random();
            int low = 0;
            int high = 400;
            int result = r.nextInt(high-low) + low;
            // <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
            String toSend = this.peer.getProtocolVersion() + " STORED " + this.peer.getPeerId() + " " + this.fileId + " " + this.chunkNo + " " + "\r\n\r\n";
            this.peer.getThreadExec().schedule(new ThreadSendMessages(this.peer.getMC(), toSend.getBytes()), result, TimeUnit.MILLISECONDS);
            System.out.println("SENT: " + toSend);
            return;
        }

        ArrayList<FileManager> files = this.peer.getStorage().getFilesStored();

        for(int i = 0; i < files.size(); i++) {
            if(files.get(i).getFileID().equals(this.fileId)) {
                System.out.println("Initiator peer of this file (" + files.get(i).getPath() + "). Can't store chunks of this one.");
                return;
            }
        }

        if(!(this.peer.getStorage().checkIfHasSpace(this.body.length))) {
            System.out.println("Doesn't have space to store chunk " + this.chunkNo);
            return;
        }

        Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body, this.replication_degree, this.body.length);

        this.peer.getStorage().addChunk(chunk);
        System.out.println("Added chunk: " + this.peer.getStorage().hasChunk(this.fileId, this.chunkNo));

        // create the chunk file in the peer directory
        String dir = "peer_" + this.peer.getPeerId();
        String backupDir = "peer_" + this.peer.getPeerId() + "/" + "backup";
        String file = "peer_" + this.peer.getPeerId() + "/" + "backup" + "/" + this.fileId + "_" + this.chunkNo;
        File directory = new File(dir);
        File backupDirectory = new File(backupDir);
        File f = new File(file);

        try{
            if (!directory.exists()){
                // System.out.println("Not exists dir");
                directory.mkdir();
                // System.out.println("After mkdir directory");
                backupDirectory.mkdir();
                // System.out.println("After mkdir backup");
                f.createNewFile();
                // System.out.println("Created file");
            } 
            else {
                if (directory.exists()) {
                    // System.out.println("Directory already exists");
                    if(backupDirectory.exists()) {
                        // System.out.println("Backup directory already exists");
                        f.createNewFile();
                        // System.out.println("Created file");
                    }
                    else {
                        backupDirectory.mkdir();
                        // System.out.println("After mkdir backup 1");
                        f.createNewFile();
                        // System.out.println("Created file");
                    }
                } 
            }

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(body);
            fos.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        Random r = new Random();
        int low = 0;
        int high = 400;
        int result = r.nextInt(high-low) + low;
        
        // <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        String toSend = this.peer.getProtocolVersion() + " STORED " + this.peer.getPeerId() + " " + this.fileId + " " + this.chunkNo + " " + "\r\n\r\n";
        this.peer.getThreadExec().schedule(new ThreadSendMessages(this.peer.getMC(), toSend.getBytes()), result, TimeUnit.MILLISECONDS);
        System.out.println("SENT: " + toSend);
    }

    public void splitHeaderAndBody() {
        int i;
        for(i = 0; i < this.message.length; i++) {
            if(this.message[i] == 0xD && this.message[i + 1] == 0xA && this.message[i + 2] == 0xD && this.message[i + 3] == 0xA) {
                break;
            }
        }

        this.header = Arrays.copyOfRange(this.message, 0, i);
        this.body = Arrays.copyOfRange(this.message, i + 4, message.length); // i+4 because between i and i+4 are \r\n\r\n
    }

    // checks if the senderId is equal to the receiver peerId. In case it is equal, returns 1, else returns 0.
    int checkIfSelf() {
        if(this.peer.getPeerId() == this.senderId) {
            return 1;
        }
        return 0;
    }
    
}
