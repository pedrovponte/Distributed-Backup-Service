public class ManageReceivedMessages implements Runnable {

    private Peer peer;
    private String[] message;

    public ManageReceivedMessages(Peer peer, byte[] message) {
        this.peer = peer;
        String received = new String(message, 0, message.length);
        String receivedTrim = received.trim(); //returns a copy of this string with leading and trailing white space removed
        this.message = receivedTrim.split(" ");
    }

    // message: <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
    public void run() {
        switch (this.message[1]){
            case "PUTCHUNK":
                break;

            case "STORED":
                break;
                
            default:
                break;
        }

    }
    
}
