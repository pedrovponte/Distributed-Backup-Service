public class Chunk {
    // Each chunk is identified by the pair (fileId, chunkNo)
    private String fileId;
    private int chunkNo;
    private byte[] chunkMessage;
    private int replication;


    public Chunk(String fileId, int chunkNo, byte[] chunkMessage, int replication) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkMessage = chunkMessage;
        this.replication = replication;
    }


    public String getFileId() {
        return this.fileId;
    }

    public int getChunkNo() {
        return this.chunkNo;
    }

    public byte[] getChunkMessage() {
        return this.chunkMessage;
    }

    public int getReplication() {
        return this.replication;
    }

}
