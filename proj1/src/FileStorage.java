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
    private ArrayList<String> filesRestored;

    // key = fileId_chunkNo; value = chunk_content
    private ConcurrentHashMap<String, byte[]> chunksRestored;

    // key = fileId; value = peerId
    private ConcurrentHashMap<String, ArrayList<Integer>> filesDeleted;

    private int capacity;

    private static final long serialVersionUID = 4066270093854086490L;

    public FileStorage() {
        this.filesStored = new ArrayList<FileManager>();
        this.chunksStored = new ConcurrentHashMap<String, Chunk>();
        this.storedMessagesReceived = new ConcurrentHashMap<String, Integer>();
        this.chunksDistribution = new ConcurrentHashMap<Integer, ArrayList<String>>();
        this.filesRestored = new ArrayList<String>();
        this.chunksRestored = new ConcurrentHashMap<String, byte[]>();
        this.filesDeleted = new ConcurrentHashMap<String, ArrayList<Integer>>();
        this.capacity = 1 * 1000 * 1000 * 1000; // 1B * 1000 (1KB) * 1000 (1MB) * 1000 (1GB)
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

    public ArrayList<String> getFilesRestored() {
        return this.filesRestored;
    }

    public ConcurrentHashMap<String,byte[]> getChunksRestored() {
        return this.chunksRestored;
    }

    public ConcurrentHashMap<String, ArrayList<Integer>> getFilesDeleted() {
        return this.filesDeleted;
    }

    public Chunk getChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        Chunk chunk = this.chunksStored.get(chunkId);

        return chunk;
    }

    public ConcurrentHashMap<Integer,ArrayList<String>> getChunksDistribution() {
        return this.chunksDistribution;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
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
    }

    public synchronized void addChunksDistribution(int senderId, String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;

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
    
        // System.out.println("-----REGISTS ADD CHUNKS DISTRIBUTION-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
        // System.out.println("Contains: " + this.storedMessagesReceived.containsKey(chunkId));
    }

    public void deleteChunksDistribution(String fileId) {
        for(Integer key : this.chunksDistribution.keySet()) {
            ArrayList<String> f = this.chunksDistribution.get(key);
            if(f.size() > 0) {
                for(int i = 0; i < f.size(); i++) {
                    String chunkId = f.get(i);
                    String fileIdStored = chunkId.split("_")[0];
                    if(fileIdStored.equals(fileId)) {
                        this.chunksDistribution.get(key).remove(chunkId);
                    }
                }
            }
        }
        // System.out.println("-----REGISTS DELETE CHUNKS DISTRIBUTION-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }

    public void deleteChunksDistribution(String fileId, int peerId) {
        ArrayList<String> chunks = this.chunksDistribution.get(peerId);
       
        if(chunks.size() > 0) {
            for(int i = 0; i < chunks.size(); i++) {
                String chunkId = chunks.get(i);
                String fileIdStored = chunkId.split("_")[0];
                if(fileIdStored.equals(fileId)) {
                    this.chunksDistribution.get(peerId).remove(chunkId);
                }
            }
        }
        // System.out.println("-----REGISTS DELETE CHUNKS DISTRIBUTION-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
    }

    public void deleteSpecificChunksDistribution(String fileId, int chunkNo, int peerId) {
        String chunkId = fileId + "_" + chunkNo;
        if(this.chunksDistribution.size() > 0) {
            if(this.chunksDistribution.get(peerId).size() > 0) {
                this.chunksDistribution.get(peerId).remove(chunkId);
            }
        }
        
        // System.out.println("-----REGISTS DELETE SPECIFIC CHUNK-------------");
        // for(Integer key : this.chunksDistribution.keySet()) {
        //     System.out.println(key + ": " + this.chunksDistribution.get(key));  
        // }
        // System.out.println("--------------------------");
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
        this.filesRestored.add(fileId);
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

    public void deleteFileRestored(String file) {
        this.filesRestored.remove(file);
    }

    public void deleteChunksRestored(String fileId) {
        for(String key : this.chunksRestored.keySet()) {
            if((key.split("_")[0]).equals(fileId)) {
                this.chunksRestored.remove(key);
            }
        }
    }

    public int getPeerOccupiedSpace() {
        int total = 0;
        for(String key : this.chunksStored.keySet()) {
            total += this.chunksStored.get(key).getSize();
        }

        return total;
    }

    public int getOccupiedSpace() {
        int occupiedSpace = 0; 
        for(String key : this.chunksStored.keySet()) {
            occupiedSpace += this.chunksStored.get(key).getSize();
        }
        return occupiedSpace;
    }

    public boolean checkIfHasSpace(int chunkSize) {
        int occupiedSpace = getOccupiedSpace();
        int finalOccupied = occupiedSpace + chunkSize;

        return finalOccupied < this.capacity;
    }

    public int getPerceivedReplication(String chunkId) {
        int replication = 0;
        for(Integer key : this.chunksDistribution.keySet()) {
            if(this.chunksDistribution.get(key).contains(chunkId)) {
                replication++;
            }
        }
        return replication;
    }

    public void addDeletedFile(String fileId, int peerId) {
        if(!this.filesDeleted.containsKey(fileId)) {
            this.filesDeleted.put(fileId, new ArrayList<Integer>());
        }

        this.filesDeleted.get(fileId).add(peerId);
    }

    public ArrayList<String> getFilesToDelete(int peerId) {
        ArrayList<String> filesToDelete = new ArrayList<String>();

        if(this.chunksDistribution.containsKey(peerId)) {
            ArrayList<String> chunks = this.chunksDistribution.get(peerId);
            ArrayList<String> files = new ArrayList<String>();
            for(int i = 0; i < chunks.size(); i++) {
                String file = chunks.get(i).split("_")[0];
                if(!files.contains(file)) {
                    files.add(file);
                }
            }

            for(String key : this.filesDeleted.keySet()) {
                if(files.contains(key) && !this.filesDeleted.get(key).contains(peerId)) {
                    filesToDelete.add(key);
                }
            }
        }
        return filesToDelete;
    }

    public boolean hasDeletedFile(String fileId) {
        if(this.filesDeleted.containsKey(fileId)) {
            return true;
        }
        return false;
    }

    public void removeDeletedFile(String fileId) {
        this.filesDeleted.remove(fileId);
    }
}

