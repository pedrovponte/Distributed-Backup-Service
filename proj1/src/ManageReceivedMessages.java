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
                break;
                
            default:
                break;
        }

    }
    
}
