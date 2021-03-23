import java.util.*;
import java.util.concurrent.*;

public class FileStorage {

    // Array to store all the files that the peer received as initiator
    private ArrayList<FileManager> filesStored;

    // key = fileId_chunkNo; value = chunk
    private ConcurrentHashMap<String, Chunk> chunksStored;

    public FileStorage() {
        this.filesStored = new ArrayList<FileManager>();
        this.chunksStored = new ConcurrentHashMap<String, Chunk>();
    }

    public ArrayList<FileManager> getFilesStored() {
        return this.filesStored;
    }

    public ConcurrentHashMap<String,Chunk> getChunksStored() {
        return this.chunksStored;
    }

    public void addFile(FileManager file) {
        this.filesStored.add(file);
    }

    public void addChunk(Chunk chunk) {
        String fileId = chunk.getFileId();
        int chunkNo = chunk.getChunkNo();

        this.chunksStored.put((fileId + "_" + chunkNo), chunk);
    }

    public Chunk getChunk(String fileId, int chunkNo) {
        String chunkId = fileId + "_" + chunkNo;
        Chunk chunk = this.chunksStored.get(chunkId);

        return chunk;
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
}

