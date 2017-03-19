/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj.dfs.chunkServer;

import proj.dfs.wireformats.ChunkServerControllerConnection;
import proj.dfs.transport.Node;
import proj.dfs.transport.TCPConnection;
import proj.dfs.util.Constants;
import proj.dfs.wireformats.ClientToChunkServerStoreFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rajiv
 */
public class ChunkServer implements Node {

    /**
     * @param args the command line arguments
     */
    Socket controllerConnectionSocket;
    public Object metaDataWriteLock = new Object();
    public List<String> newlyCreatedChunks = new ArrayList<>();
//    Map<String, TCPConnection> connectionCache = new HashMap<>();

    public static void main(String[] args) {
        // TODO code application logic here
        new ChunkServer().startServer(args);
    }

    public void startServer(String args[]) {
        Integer listeningPort = null;
        try {
            //args[0] = listening socket
            //args[1] = controller hostname
            //args[2] = controller port
            PollingThread pollingThread = new PollingThread(this);
            Thread t = new Thread(pollingThread);
            t.start();
            listeningPort = Integer.parseInt(args[0]);
            ServerSocket listeningSocket = new ServerSocket(listeningPort);
            System.out.println("INFO : Listening on : " + InetAddress.getLocalHost().getHostAddress() + Constants.DELIMITORS.IP_PORT + listeningPort);
            int controllerPort = Integer.parseInt(args[2]);
            controllerConnectionSocket = new Socket(args[1], controllerPort);
            TCPConnection controllerConnectionObj = new TCPConnection(controllerConnectionSocket, this);
//            synchronized (connectionCache) {
//                connectionCache.put(controllerConnectionSocket.getInetAddress().getHostAddress() + ":" + controllerConnectionSocket.getPort(), controllerConnectionObj);
//            }
            //Not started receiver thread
            ChunkServerControllerConnection chunkToControllerConnection = new ChunkServerControllerConnection();
            File tmpFile = new File(Constants.CHUNK_STORE_DESTINATION);
            chunkToControllerConnection.setListeningPort(listeningPort);
            chunkToControllerConnection.setFreeSpace(tmpFile.getUsableSpace());
            controllerConnectionObj.getSender().sendData(chunkToControllerConnection.getBytes());
//        starting polling thread            
            while (true) {
                Socket incomingConnectionSocket = listeningSocket.accept();
                TCPConnection connection = new TCPConnection(incomingConnectionSocket, this);
                connection.startReceiverThread();
            }
        } catch (IOException ex) {
            System.err.println("ERROR : Not able to listen on : " + listeningPort);
        }
    }

    @Override
    public void processRequest(byte[] bytes, Socket socket) {
        byte type = bytes[0];
        switch (type) {
            case Constants.MESSAGES.CLIENT_TO_CHUNK_SERVER_STORE_FILE:
                ClientToChunkServerStoreFile clientToChunkServerStoreFile = new ClientToChunkServerStoreFile(bytes);
//                    System.out.println("-Received on-" + InetAddress.getLocalHost().getHostAddress() + "-" + clientToChunkServerStoreFile);
                byte[] chunkData = clientToChunkServerStoreFile.getChunkData();
                String chunkServerInfo = clientToChunkServerStoreFile.getChunkServerInfo();
                int sequence = clientToChunkServerStoreFile.getSequence();
                String filename = clientToChunkServerStoreFile.getFilename();
                if (chunkServerInfo != null && !chunkServerInfo.isEmpty() && !chunkServerInfo.equals(Constants.PROPOGATION_TERMINATING_STRING)) {
                    String[] otherChunks = chunkServerInfo.split(Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR);
                    if (otherChunks.length >= 1) {
                        //Store chunkData & chunk metadata(new Thread)
                        StoreChunkDataAndMetadata storeFile = new StoreChunkDataAndMetadata(chunkData, filename, sequence, this);
                        Thread t = new Thread(storeFile);
                        t.start();
                        String completefileName = Constants.CHUNK_STORE_DESTINATION + filename + "_chunk" + sequence;
                        String folders = completefileName.substring(0, completefileName.lastIndexOf("/"));
//        String actFile = filename.substring(filename.lastIndexOf("/") + 1);
                        File foldersObj = new File(folders);
                        if (!foldersObj.exists()) {
                            System.out.println("-creating folders-");
                            foldersObj.mkdirs();
                        }
                        File f = new File(completefileName);
                        try {
                            try (FileOutputStream fOut = new FileOutputStream(f)) {
                                fOut.write(chunkData);
                                fOut.close();
                            }
                        } catch (FileNotFoundException ex) {
                            System.err.println("ERROR : File not found.");
                        } catch (IOException ex) {
                            System.err.println("ERROR : Writing data to file.");
                        }
                        //Forward chunk data
                        String[] propagatedChunk = otherChunks[0].split(Constants.DELIMITORS.IP_PORT);
                        String ip = propagatedChunk[0];
                        int port = Integer.parseInt(propagatedChunk[1]);
                        Socket propChunkSocket = null;
                        try {
                            propChunkSocket = new Socket(ip, port);
                        } catch (IOException ex) {
                            System.err.println("ERROR : Connecting to propagated chunk server.");
                        }
                        TCPConnection connection = new TCPConnection(propChunkSocket, this);
                        ClientToChunkServerStoreFile chunkDataPropogation = new ClientToChunkServerStoreFile();
                        chunkDataPropogation.setChunkData(chunkData);
                        String propogationChunkDetails = "";
                        if (otherChunks.length <= 1) {
                            propogationChunkDetails = Constants.PROPOGATION_TERMINATING_STRING;
                        } else {
                            for (int i = 1; i < otherChunks.length; i++) {
                                propogationChunkDetails += otherChunks[i] + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                            }
                            propogationChunkDetails = propogationChunkDetails.substring(0, propogationChunkDetails.length() - 1);
                        }
//                        System.out.println("-Propogation details-" + propogationChunkDetails);
                        chunkDataPropogation.setChunkServerInfo(propogationChunkDetails);
                        chunkDataPropogation.setSequence(sequence);
                        chunkDataPropogation.setFilename(filename);
                        try {
                            connection.getSender().sendData(chunkDataPropogation.getBytes());
                        } catch (IOException ex) {
                            System.err.println("ERROR : Sending data");
                        } finally {
                            try {
//                                connection.getSocket().close();
                                socket.close();
                            } catch (IOException ex) {
                                System.err.println("ERROR : closing socket.");
                            }
                        }
                    }
                } else {
                    //No propogation
                    //Only store chunk data and chunk metadata
                    StoreChunkDataAndMetadata storeFile = new StoreChunkDataAndMetadata(chunkData, filename, sequence, this);
                    Thread t = new Thread(storeFile);
                    t.start();
                    try {
//                                connection.getSocket().close();
                        socket.close();
                    } catch (IOException ex) {
                        System.err.println("ERROR : closing socket.");
                    }
                }
                break;
        }
    }

    public void sendMinorHeartBeat() {
        //read list and fetch chunks metadata and send
    }

    public void sendMajorHeartbeat() {
        //read metadata folder and send
    }
}
