public class ManageReceivedMessages implements Runnable {

    private Peer peer;
    private byte[] message;

    public ManageReceivedMessages(Peer peer, byte[] message) {
        this.peer = peer; //returns a copy of this string with leading and trailing white space removed
        this.message = message;
    }

    // message: <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
    public void run() {
        String[] messageStr = new String(this.message).split(" ");
        // System.out.println("Manager message: " + messageStr);
        switch (messageStr[1]){
            case "PUTCHUNK":
                this.peer.getThreadExec().execute(new PutChunkMessageThread(this.message, this.peer));
                break;

            case "STORED":
                this.peer.getThreadExec().execute(new StoredMessageThread(this.message, this.peer));
                break;

            case "DELETE":
                this.peer.getThreadExec().execute(new DeleteMessageThread(this.message, this.peer));
                break;

            case "GETCHUNK":
                this.peer.getThreadExec().execute(new GetChunkMessageThread(this.message, this.peer));
                break;

            case "CHUNK":
                this.peer.getThreadExec().execute(new ChunkMessageThread(this.message, this.peer));
                break;

            case "REMOVED":
                this.peer.getThreadExec().execute(new RemovedMessageThread(this.message, this.peer));
                break;
                
            default:
                break;
        }

    }
    
}
