import java.util.concurrent.*;
import java.util.Random;

public class RemovedMessageThread implements Runnable {

    private byte[] message;
    private Peer peer;

    public RemovedMessageThread(byte[] message, Peer peer) {
        this.message = message;
        this.peer = peer;
    }

    @Override
    public void run() {
        // <Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        String[] messageStr = (new String(this.message)).split(" ");
        String protocolVersion = messageStr[0];
        int senderId = Integer.parseInt(messageStr[2]);
        String fileId = messageStr[3];
        int chunkNo = Integer.parseInt(messageStr[4]);

        // checks if the senderId is equal to the receiver peerId
        if(this.peer.getPeerId() == senderId) {
            return;
        }

        System.out.println("RECEIVED: " + protocolVersion + " REMOVED " + senderId + " " + fileId + " " + chunkNo);

        // verificar se este peer tem guardado o chunk que foi apagado pelo outro peer. Caso nao tenha entao retorna pois nao o pode enviar para outros replicarem;
        // provavelmente dentro do chunk vai ser preciso criar uma variavel que contenha a replicaçao atual desse chunk, e incrementar essa variavel depois nos sitios onde se recebe os chunks, 
        // tendo cuidado para nao incrementar com chunks repetidos do mesmo sender (se calhar ate sera preciso apenas recorrer a funçao increment que tem no storage)
        // dessa maneira pode-se comparar com a variavel replication que ja existe no chunk e assim saber se e preciso replica-lo novamente ou nao
        // caso a replicaçao atual seja inferior a replicaçao necessaria, entao espera-se um tempo random (como no delete) e volta-se a verificar se a replicaçao do chunk ja esta certa;
        // caso nao esteja, entao envia-se a mensagem putchunk


    }   
}
