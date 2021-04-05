**Compile files**

Inside src folder: javac *.java

**Start RMI**

Inside src folder: start rmiregistry / rmiregistry &

**Run Peer**

* java Peer <protocol_version> <peer_id> <service_access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>

* (ex.) java Peer 1.0 1 Peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
* (ex.) java Peer 1.0 2 Peer2 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
* (ex.) java Peer 1.0 3 Peer3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

**Run Client**

* **Backup:**
  - java Client <peer_ap> BACKUP <file_path_name> <replication_degree>
  - (ex.) java Client Peer1 BACKUP "D:\U. Porto\3 ano\2 semestre\sdis_09\proj1\pinguim.png" 2



**Doubts**

* Because UDP is not reliable, a peer that has stored a chunk must reply with a STORED message to every PUTCHUNK message it receives -> neste caso, apenas os peers que guardaram determinado chunk dessa vez devem responder STORED ou os que anteriormente ja teriam guardado tambem devem responder?

