import java.net.*;
import java.nio.channels.Channel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class Peer implements RemoteInterface {
    private ChannelController MC;
    private ChannelController MDB;
    private ChannelController MDR;
    private String protocolVersion;
    private static int peerId;
    private ScheduledThreadPoolExecutor threadExec;
    private static FileStorage storage;

    public Peer(String protocolVersion, int id) {
        this.protocolVersion = protocolVersion;
        peerId = id;
        System.out.println("ID: " + peerId);
        this.threadExec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(300);
        System.out.println("--- Created Threads ---");

        if(new File("peer_" + peerId + "/storage.ser").exists()) {
            System.out.println("Exists");
            deserialization();
        }
        else {
            storage = new FileStorage();
        }

        ConcurrentHashMap<String, Integer> stored = this.getStorage().getStoredMessagesReceived();
        System.out.println("-------STORED-------");
        for(String key : stored.keySet()) {
            System.out.println("Key: " + key);
            System.out.println("Value: " + stored.get(key));
        }

        ArrayList<FileManager> files = this.getStorage().getFilesStored();
        System.out.println("-------FILES-------");
        for(int i = 0; i < files.size(); i++) {
            System.out.println("Key: " + i);
            System.out.println("Value: " + files.get(i).getPath());
        }

        ConcurrentHashMap<String, Chunk> chunks = this.getStorage().getChunksStored();
        System.out.println("-------CHUNKS-------");
        for(String key : chunks.keySet()) {
            System.out.println("Key: " + key);
            System.out.println("Value: " + chunks.get(key));
        }
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
            System.out.println("--- Running RMI Registry ---");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }

        peer.createChannels(mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
        System.out.println("--- Channels Created ---");

        peer.execChannels();
        System.out.println("--- Running Channels ---");

        Runtime.getRuntime().addShutdownHook(new Thread(Peer::serialization));
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

    public static int getPeerId() {
        return peerId;
    }

    public ScheduledThreadPoolExecutor getThreadExec() {
        return this.threadExec;
    }

    public FileStorage getStorage() {
        return storage;
    }

    public void createChannels(String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress,
            int mdrPort) {
        this.MC = new ChannelController(mcAddress, mcPort, this);
        this.MDB = new ChannelController(mdbAddress, mdbPort, this);
        this.MDR = new ChannelController(mdrAddress, mdrPort, this);
    }

    public void execChannels() {
        this.threadExec.execute(this.MC);
        this.threadExec.execute(this.MDB);
        this.threadExec.execute(this.MDR);
    }

    @Override
    public void backup(String path, int replication) {
        File backupFile = new File(path);

        if(!backupFile.exists()) {
            System.out.println("The file - " + path + " - doesn't exist.");
            return;
        }

        FileManager fileManager = new FileManager(path, replication);
        storage.addFile(fileManager);

        ArrayList<Chunk> fileChunks = fileManager.getFileChunks();

        for(int i = 0; i < fileChunks.size(); i++) {
            // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
            String header = this.protocolVersion + " PUTCHUNK " + peerId + " " + fileManager.getFileID() + " " + fileChunks.get(i).getChunkNo() + " " + fileChunks.get(i).getReplication() + " \r\n\r\n";
            
            try {
                byte[] headerBytes = header.getBytes(StandardCharsets.US_ASCII);
                byte[] body = fileChunks.get(i).getChunkMessage();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(headerBytes);
                outputStream.write(body);
                byte[] message = outputStream.toByteArray();
                
                if(!(storage.hasRegisterStore(fileManager.getFileID(), fileChunks.get(i).getChunkNo()))) {
                    storage.createRegisterToStore(fileManager.getFileID(), fileChunks.get(i).getChunkNo());
                }

                // send threads
                this.threadExec.execute(new ThreadSendMessages(this.MDB, message));
                this.threadExec.schedule(new ThreadCountStored(this, replication, fileManager.getFileID(), i, this.MDB, message), 1, TimeUnit.SECONDS);

                System.out.println("SENT: "+ header);
            } catch(UnsupportedEncodingException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            } catch(IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void restore(String path) {

    }

    @Override
    public void delete(String path) {
        // <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
        System.out.println("Inside delete");
        File backupFile = new File(path);

        if(!backupFile.exists()) {
            System.out.println("The file - " + path + " - doesn't exist.");
            return;
        }
        
        ArrayList<FileManager> files = this.getStorage().getFilesStored();

        for(int i = 0; i < files.size(); i++) {
            if(files.get(i).getPath().equals(path)) {
                // This message does not elicit any response message. An implementation may send this message as many times as it is deemed necessary
                for(int j = 0; j < 5; j++) {
                    String message = this.protocolVersion + " DELETE " + peerId + " " + files.get(i).getFileID() + " \r\n\r\n";
                    try {
                        this.threadExec.execute(new ThreadSendMessages(this.MC, message.getBytes(StandardCharsets.US_ASCII)));

                        System.out.println("SENT: " + message);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                }

                ConcurrentHashMap<String,Integer> storedMessages = this.getStorage().getStoredMessagesReceived();
                for(String key : storedMessages.keySet()) {
                    for(int k = 0; k < files.get(i).getChunkNo(); k++) {
                        String chunkId = files.get(i).getFileID() + "_" + k;
                        if(key.equals(chunkId)) {
                            this.getStorage().deleteStoreMessage(chunkId);
                        }
                    }
                }
                this.getStorage().deleteFile(files.get(i));
                break;
            }
        }
    }

    @Override
    public void reclaim(int maximum_disk_space) {

    }

    @Override
    public void state() {

    }

    // https://www.tutorialspoint.com/java/java_serialization.htm
    public void deserialization() {
        System.out.println("Deserializing data");
        try {
            String fileName = "peer_" + peerId + "/storage.ser";
            

            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            storage = (FileStorage) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Storage class not found");
            c.printStackTrace();
            return;
        }
    }

    // https://www.tutorialspoint.com/java/java_serialization.htm
    private static void serialization() {
        System.out.println("Serializing data");
        try {
            String fileName = "peer_" + getPeerId() + "/storage.ser";
            File directory = new File("peer_" + getPeerId());

            if (!directory.exists()){
                // System.out.println("Not exists dir");
                directory.mkdir();
                (new File(fileName)).createNewFile();
            } 
            else if(directory.exists()) {
                if(!(new File(fileName).exists())) {
                    (new File(fileName)).createNewFile();
                }
            }

            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(storage);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
