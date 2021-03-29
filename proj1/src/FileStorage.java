import java.util.*;
import java.util.concurrent.*;

public class FileStorage implements java.io.Serializable {

    // Array to store all the files that the peer received as initiator
    private ArrayList<FileManager> filesStored;

    // key = fileId_chunkNo; value = chunk
    private ConcurrentHashMap<String, Chunk> chunksStored;

    // key = fileId_chunkNo; value = number of stored messages received
    private ConcurrentHashMap<String, Integer> storedMessagesReceived;

    // key = sendId; value = fileId_chunkNo 
    private ConcurrentHashMap<Integer, ArrayList<String>> chunksDistribution;

    // Array to store all the files that the peer restored as initiator
    private ArrayList<FileManager> filesRestored;

    // key = fileId_chunkNo; value = chunk_content
    private ConcurrentHashMap<String, byte[]> chunksRestored;

    public FileStorage() {
        this.filesStored = new ArrayList<FileManager>();
        this.chunksStored = new ConcurrentHashMap<String, Chunk>();
        this.storedMessagesReceived = new ConcurrentHashMap<String, Integer>();
        this.chunksDistribution = new ConcurrentHashMap<Integer, ArrayList<String>>();
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

    public ArrayList<FileManager> getFilesRestored() {
        return this.filesRestored;
    }

    public ConcurrentHashMap<String,byte[]> getChunksRestored() {
        return this.chunksRestored;
    }

    public Chunk getChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        Chunk chunk = this.chunksStored.get(chunkId);

        return chunk;
    }

    public ConcurrentHashMap<Integer,ArrayList<String>> getChunksDistribution() {
        return this.chunksDistribution;
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

    public synchronized void incrementStoredMessagesReceived(int senderId, String fileId, int chunkNo) {
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

        if(this.chunksDistribution.containsKey(senderId)) {
            ArrayList<String> f = this.chunksDistribution.get(senderId);
            if(!(f.contains(chunkId))) {
                //System.out.println("Add regist to sender");
                this.chunksDistribution.get(senderId).add(chunkId);
            }
            /*else {
                System.out.println("registed");
            }*/
        }
        else {
            ArrayList<String> app = new ArrayList<String>();
            app.add(chunkId);
            this.chunksDistribution.put(senderId, app);
            //System.out.println("Add sender and regist");
        }

        /*System.out.println("-----REGISTS-------------");
        for(Integer key : this.chunksDistribution.keySet()) {
            System.out.println(key + ": " + this.chunksDistribution.get(key));  
        }
        System.out.println("--------------------------");*/
        // System.out.println("Contains: " + this.storedMessagesReceived.containsKey(chunkId));
    }

    public void deleteChunk(String chunkId) {
        this.chunksStored.remove(chunkId);
    }

    public void deleteFile(FileManager file) {
        for(int i = 0; i < filesStored.size(); i++) {
            if(filesStored.get(i).getFileID().equals(file.getFileID())) {
                this.filesStored.remove(i);
                return;
            }
        }
    }

    public void deleteStoreMessage(String chunkId) {
        this.storedMessagesReceived.remove(chunkId);
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

    public void addChunkToRestore(String chunkId, byte[] data) {
        this.chunksRestored.put(chunkId, data);
    }

    public void addFileToRestore(String fileId) {
        this.filesRestored.put(fileId);
    }

    public boolean hasRegisterToRestore(String chunkId) {
        if(this.chunksRestored.containsKey(chunkId)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasFileToRestore(String fileId) {
        if(this.filesRestored.contains(fileId)) {
            return true;
        }
        else {
            return false;
        }
    }
}

