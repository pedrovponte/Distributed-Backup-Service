public class ChunkTCPMessageThread implements Runnable {
    private byte[] message;
    private Peer peer;

    public ChunkTCPMessageThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

	@Override
	public void run() {
		String[] messageStr = new String(this.message).split(" ");
        String protocolVersion = messageStr[0];
        int senderId = Integer.parseInt(messageStr[2]);
        String fileId = messageStr[3];
        int chunkNo = Integer.parseInt(messageStr[4]);

        System.out.println("RECEIVED: " + protocolVersion + " CHUNKTCP " + senderId + " " + fileId + " " + chunkNo);

        String chunkId = fileId + "_" + chunkNo;
        this.peer.incrementReceivedChunkMessagesNumber(chunkId);
	}
    
}