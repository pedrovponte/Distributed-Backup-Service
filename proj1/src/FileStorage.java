import java.util.*;
import java.util.concurrent.*;

public class FileStorage implements java.io.Serializable {

    // Array to store all the files that the peer received as initiator
    private ArrayList<FileManager> filesStored;

    // key = fileId_chunkNo; value = chunk
    private ConcurrentHashMap<String, Chunk> chunksStored;

    // key = fileId_chunkNo; value = number of stored messages received
    private ConcurrentHashMap<String, Integer> storedMessagesReceived;

    public FileStorage() {
        this.filesStored = new ArrayList<FileManager>();
        this.chunksStored = new ConcurrentHashMap<String, Chunk>();
        this.storedMessagesReceived = new ConcurrentHashMap<String, Integer>();
    }

    public ArrayList<FileManager> getFilesStored() {
        return this.filesStored;
    }

    public ConcurrentHashMap<String,Chunk> getChunksStored() {
        return this.chunksStored;
    }

    public ConcurrentHashMap<String,Integer> getStoredMessagesReceived() {
        return this.storedMessagesReceived;
    }

    public Chunk getChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        Chunk chunk = this.chunksStored.get(chunkId);

        return chunk;
    }

    public void addFile(FileManager file) {
        this.filesStored.add(file);
    }

    public void addChunk(Chunk chunk) {
        String fileId = chunk.getFileId();
        int chunkNo = chunk.getChunkNo();

        this.chunksStored.put((fileId + "_" + chunkNo), chunk);
    }

    public boolean hasChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;

        if(this.chunksStored.containsKey(chunkId)) {
            return true;
        }
        else {
            return false;
        }
    }

    public synchronized void incrementStoredMessagesReceived(String fileId, int chunkNo) { // put syncronized
        String chunkId = fileId + "_" + chunkNo;

        if(this.storedMessagesReceived.containsKey(chunkId)) {
            int total = this.storedMessagesReceived.get(chunkId) + 1;
            this.storedMessagesReceived.put(chunkId, total);
            // System.out.println("Regist exists. Times: " + this.storedMessagesReceived.get(chunkId));
        }
        else {
            this.storedMessagesReceived.put(chunkId, 1);
            // System.out.println("Not exists regist");
        }

        // System.out.println("Contains: " + this.storedMessagesReceived.containsKey(chunkId));
    }

    public synchronized void createRegisterToStore(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        this.storedMessagesReceived.put(chunkId, 0);
    }

    public boolean hasRegisterStore(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        if(this.storedMessagesReceived.containsKey(chunkId)) {
            return true;
        }
        else {
            return false;
        }
    }
}

