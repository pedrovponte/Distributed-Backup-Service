import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            if(args.length > 4) {
                System.out.println("SIZE: " + args.length);
                System.out.println("Usage: Client <peer_ap> <sub_protocol> [<opnd_1> [<opnd_2>]]");
                return;
            }

            String peer_ap = args[0];
            String subprotocol = args[1].toUpperCase();
            String file_path_name;
            int replication_degree;
            int maximum_disk_space;

            Registry registry = LocateRegistry.getRegistry("localhost"); // deve ser dado nos argumentos?
            RemoteInterface stub = (RemoteInterface) registry.lookup(peer_ap);

            switch (subprotocol) {
                case "BACKUP":
                    if(args.length != 4) {
                        System.out.println("Invalid invocation for BACKUP subprotocol.");
                        System.out.println("Usage: Client <peer_ap> BACKUP <file_path_name> <replication_degree>");
                        return;
                    }

                    file_path_name = args[2];
                    replication_degree = Integer.parseInt(args[3]);
                    System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("File path: " + file_path_name);
                    System.out.println("Replication: " + replication_degree);
                    stub.backup(file_path_name, replication_degree);
                    break;
                
                case "RESTORE":
                    if(args.length != 3) {
                        System.out.println("Invalid invocation for RESTORE subprotocol.");
                        System.out.println("Usage: Client <peer_ap> RESTORE <file_path_name>");
                        return;
                    }

                    file_path_name = args[2];
                    System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("File path: " + file_path_name);
                    stub.restore(file_path_name);
                    break;

                case "DELETE":
                    if(args.length != 3) {
                        System.out.println("Invalid invocation for DELETE subprotocol.");
                        System.out.println("Usage: Client <peer_ap> DELETE <file_path_name>");
                        return;
                    }

                    file_path_name = args[2];
                    System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("File path: " + file_path_name);
                    stub.restore(file_path_name);
                    break;

                case "RECLAIM":
                    if(args.length != 3) {
                        System.out.println("Invalid invocation for RECLAIM subprotocol.");
                        System.out.println("Usage: Client <peer_ap> RECLAIM <maximum_disk_space>");
                        return;
                    }

                    maximum_disk_space = Integer.parseInt(args[2]);
                    System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    System.out.println("Maximum disk: " + maximum_disk_space);
                    stub.reclaim(maximum_disk_space);
                    break;

                case "STATE":
                    if(args.length != 2) {
                        System.out.println("Invalid invocation for STATE subprotocol.");
                        System.out.println("Usage: Client <peer_ap> STATE");
                        return;
                    }

                    System.out.println("Peer_ap: " + peer_ap);
                    System.out.println("Subprotocol: " + subprotocol);
                    stub.state();
                    break;

                default:
                    break;
            }

        } catch(Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}