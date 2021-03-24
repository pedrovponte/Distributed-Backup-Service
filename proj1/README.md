**Compile files**

Inside src folder: javac *.java

**Start RMI**

Inside src folder: start rmiregistry

**Run Peer**

* java Peer <protocol_version> <peer_id> <service_access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>

* (ex.) java Peer 1.0 1 Peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003

**Run Client**

* **Backup:**
  - java Client <peer_ap> BACKUP <file_path_name> <replication_degree>
  - (ex.) java Client Peer1 BACKUP "D:\U. Porto\3 ano\2 semestre\sdis_09\proj1\pinguim.png" 2



**Doubts**

* Because UDP is not reliable, a peer that has stored a chunk must reply with a STORED message to every PUTCHUNK message it receives -> neste caso, apenas os peers que guardaram determinado chunk dessa vez devem responder STORED ou os que anteriormente ja teriam guardado tambem devem responder?

* When a peer sends a STORED message, all the others should receive and increment the counter of the chunk or only the initiator peer shoul increment?

* in the 5 attempts that the initiator peer should do in order to receive all the STORED messages in order to have the replication needed, the first time that the initiator peer sends the PUTCHUNK counts to this 5 or not?

* when a peer replies to the PUTCHUNK message for the second time, the initiator peer should increment the number of chunks or ignore this one? should we reset the counter if the peer not receives all the stored messages before we create another thread?