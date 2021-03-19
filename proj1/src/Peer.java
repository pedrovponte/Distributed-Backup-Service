import java.net.*;
import java.nio.channels.Channel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Peer implements RemoteInterface {
    private ChannelController MC;
    private ChannelController MDB;
    private ChannelController MDR;
    private String protocolVersion;
    private int peerId;
    private ScheduledThreadPoolExecutor threadExec;

    public Peer(String protocolVersion, int peerId) {
        this.protocolVersion = protocolVersion;
        this.peerId = peerId;
        this.threadExec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(300);
        System.out.println("--- Created Threads ---");
    }

    public static void main(String[] args) {

        if (args.length != 9) {
            System.out.println(
                    "Usage: Peer <protocol_version> <peer_id> <service_access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>");
            return;
        }

        String protocolVersion = args[0];

        int peerId = Integer.parseInt(args[1]);
        String serviceAccessPoint = args[2];
        String mcAddress = args[3];
        int mcPort = Integer.parseInt(args[4]);
        String mdbAddress = args[5];
        int mdbPort = Integer.parseInt(args[6]);
        String mdrAddress = args[7];
        int mdrPort = Integer.parseInt(args[8]);

        System.out.println("Protocol version: " + protocolVersion);
        System.out.println("Peer Id: " + peerId);
        System.out.println("Service Access Point: " + serviceAccessPoint);
        System.out.println("Mc address: " + mcAddress);
        System.out.println("Mc port: " + mcPort);
        System.out.println("MDB address: " + mdbAddress);
        System.out.println("MDB port: " + mdbPort);
        System.out.println("MDR address: " + mdrAddress);
        System.out.println("MDR port: " + mdrPort);

        Peer peer = new Peer(protocolVersion, peerId);

        try {
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(serviceAccessPoint, stub);
            System.out.println("--- Running RMI Resgistry ---");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }

        peer.createChannels(mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
        System.out.println("--- Channels Created ---");

        peer.execChannels();
        System.out.println("--- Running Channels ---");
    }

    public ChannelController getMC() {
        return this.MC;
    }

    public ChannelController getMDB() {
        return this.MDB;
    }

    public ChannelController getMDR() {
        return this.MDR;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public int getPeerId() {
        return this.peerId;
    }

    public ScheduledThreadPoolExecutor getThreadExec() {
        return this.threadExec;
    }

    public void createChannels(String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress,
            int mdrPort) {
        this.MC = new ChannelController(mcAddress, mcPort, this);
        this.MDB = new ChannelController(mdbAddress, mdbPort, this);
        this.MDR = new ChannelController(mdrAddress, mdrPort, this);
    }

    public void execChannels() {
        this.threadExec.execute(this.MC);
        this.threadExec.execute(    this.MDB);
        this.threadExec.execute(this.MDR);
    }

    @Override
    public void backup(String path, int replication) {
        System.out.println("before files");
        FileManager fileManager = new FileManager(path, replication);
        System.out.println("after files");

        ArrayList<Chunk> fileChunks = fileManager.getFileChunks();
        System.out.println(fileChunks);

        for(int i = 0; i < fileChunks.size(); i++) {
            System.out.println("inside for");
            // generate PUTCHUNK message
            String header = this.protocolVersion + " PUTCHUNK " + this.peerId + " " + fileChunks.get(i).getChunkNo() + " " + fileChunks.get(i).getReplication() + "\r\n\r\n";
            try {
                byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
                byte[] body = fileChunks.get(i).getChunkMessage();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(headerBytes);
                outputStream.write(body);
                byte[] message = outputStream.toByteArray( );

                // send threads
                this.threadExec.execute(new ThreadSendChunks(this.MDB, message));
            } catch(UnsupportedEncodingException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            } catch(IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            
            
            
            
            // receiver stored messages
        }

        

        // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
        // <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

    }

    @Override
    public void restore(String path) {

    }

    @Override
    public void delete(String path) {

    }

    @Override
    public void reclaim(int maximum_disk_space) {

    }

    @Override
    public void state() {

    }

}
