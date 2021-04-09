public class DeletedMessageThread implements Runnable {
    private Peer peer;
    private String protocolVersion;
    private int senderId;
    private int initiatorId;
    private String fileId;

    // <Version> DELETED <SenderId> <InitiatorId> <FileId> <CRLF><CRLF>
    public DeletedMessageThread(byte[] message, Peer peer) {
        this.peer = peer;
        String[] headerStr = new String(message).split(" ");
        this.protocolVersion = headerStr[0];
        this.senderId = Integer.parseInt(headerStr[2]);
        this.initiatorId = Integer.parseInt(headerStr[3]);
        this.fileId = headerStr[4];
    }

	@Override
	public void run() {
        if(!this.protocolVersion.equals("2.0")) {
            return;
        }

        this.peer.getStorage().deleteChunksDistribution(fileId, senderId);

		if(!(this.peer.getPeerId() == initiatorId)) {
            return;
        }
        
        System.out.println("RECEIVED: " + this.protocolVersion + " DELETED " + this.senderId + " " + this.initiatorId + " " + this.fileId);

        this.peer.getStorage().addDeletedFile(this.fileId, this.senderId);
	}
}