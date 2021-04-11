import java.io.DataOutputStream;
import java.net.Socket;

public class ThreadChunkMessage implements Runnable {
    private DataOutputStream dos;
    private byte[] message;
    private String hostname;
    private int port;

    public ThreadChunkMessage(byte[] message, int port) {
        this.message = message;
        this.port = port;

        try {
            Socket socket = new Socket("localhost", this.port);
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public void run() {
        try {
            this.dos.write(this.message);
            this.dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
		
	}
    
}