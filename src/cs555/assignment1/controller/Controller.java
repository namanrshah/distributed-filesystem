package cs555.assignment1.controller;

import cs555.assignment1.wireformats.ClientControllerChunkRequest;
import cs555.assignment1.wireformats.ChunkServerControllerConnection;
import cs555.assignment1.transport.Node;
import cs555.assignment1.transport.TCPConnection;
import cs555.assignment1.util.Constants;
import cs555.assignment1.wireformats.ControllerToClientChunkServerInfoToWrite;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rajiv
 */
public class Controller implements Node {

    /**
     * @param args the command line arguments
     */
    public static int count = 0;
    public static Controller controller = new Controller();
    TCPConnection connection;
    List<ChunkServerFreeSpace> chunkServerFreeSpace = new ArrayList<>();
    List<String> previousChunkServers = new ArrayList<>();
    List<TCPConnection> connections = new ArrayList<>();

    private Controller() {
        ++count;
    }

    public static Controller getInstance() {
        return controller;
    }

    public static void main(String[] args) throws IOException {
        //        try {
//
//            File f = new File("dummy.txt");
//            splitFile(f);
//            mergeFile(f);
//            getSHA1Checksum(f);
//            getSHA1Checksum(new File(f.getName() + "_merged"));
//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
//        }        Collections.so
        System.out.println("-count-" + Controller.count + "-" + controller.hashCode());
        controller.startController(args);
    }

    public void startController(String[] args) {
        //args[0] = listening port
        boolean validSocket = false;
        int port = Integer.parseInt(args[0]);
        while (!validSocket) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                validSocket = true;
                System.out
                        .println("INFO : Controller is listening on "
                                + InetAddress.getLocalHost().getHostName()
                                + ":" + port);
                while (true) {
                    Socket connectionSocket = serverSocket.accept();
                    connection = new TCPConnection(connectionSocket, this);
                    connection.startReceiverThread();
                    connections.add(connection);
                }

            } catch (IOException ex) {
                System.err.println("ERROR : unable to listen on given socket. please try again.");
                Scanner sc = new Scanner(System.in);
                port = sc.nextInt();
            }
        }
    }

    @Override
    public void processRequest(byte[] bytes, Socket socket) {
        try {
            byte type = bytes[0];
            switch (type) {
                case Constants.MESSAGES.CHUNK_SERVER_CONTROLLER_CONNECTION:
                    ChunkServerControllerConnection chunkConnectionReq = new ChunkServerControllerConnection(bytes);
                    synchronized (chunkServerFreeSpace) {
                        chunkServerFreeSpace.add(new ChunkServerFreeSpace(socket.getInetAddress().getHostAddress() + Constants.DELIMITORS.IP_PORT + chunkConnectionReq.getListeningPort(), chunkConnectionReq.getFreeSpace()));
                        System.out.println("-Free space-" + chunkServerFreeSpace);
                    }
//                    System.out.println("-request from-" + socket.getInetAddress().getHostAddress() + socket.getPort());                    
                    break;
                case Constants.MESSAGES.CLIENT_CONTROLLER_CHUNK_REQUEST:
                    ClientControllerChunkRequest chunkReq = new ClientControllerChunkRequest(bytes);
                    int chunkSeq = chunkReq.getChunkSeq();
                    String filePath = chunkReq.getFilePath();
                    //Find 3 chunkservers
                    String returnString = "";
                    synchronized (chunkServerFreeSpace) {
                        Collections.sort(chunkServerFreeSpace, new Comparator<ChunkServerFreeSpace>() {
                            @Override
                            public int compare(ChunkServerFreeSpace o1, ChunkServerFreeSpace o2) {
                                return (int) (o2.getFreeSpace() - o1.getFreeSpace());
                            }
                        });
                        int chunkServerCount = chunkServerFreeSpace.size();
                        if (chunkServerCount < Constants.REPLICATION_COUNT) {
                            System.out.println("WARNING : Chunk server count is less than minimum replication count.");
                            for (int i = 0; i < chunkServerCount; i++) {
                                returnString += chunkServerFreeSpace.get(i).getIpPort() + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                                //Remove 64*1024 from stored value
                                chunkServerFreeSpace.get(i).setFreeSpace(chunkServerFreeSpace.get(i).getFreeSpace() - (64 * 1024));
                            }
                        } else if (chunkServerCount < 2 * Constants.REPLICATION_COUNT) {
                            if (previousChunkServers != null && !previousChunkServers.isEmpty()) {
                                //Compare with previous chunk's locations
                                List<Integer> indexNotUsed = new ArrayList<>();
                                int count = 0;
                                for (int i = 0; i < chunkServerCount; i++) {
                                    if (!previousChunkServers.contains(chunkServerFreeSpace.get(i).getIpPort())) {
                                        returnString += chunkServerFreeSpace.get(i).getIpPort() + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                                        //Remove 64*1024 from stored value
                                        chunkServerFreeSpace.get(i).setFreeSpace(chunkServerFreeSpace.get(i).getFreeSpace() - (64 * 1024));                                        
                                        ++count;
                                    } else {
                                        indexNotUsed.add(i);
                                    }
                                }
                                if (count < Constants.REPLICATION_COUNT) {
                                    for (int i = 0; i < Constants.REPLICATION_COUNT - count; i++) {
                                        returnString += chunkServerFreeSpace.get(indexNotUsed.get(i)).getIpPort() + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                                        //Remove 64*1024 from stored value
                                        chunkServerFreeSpace.get(i).setFreeSpace(chunkServerFreeSpace.get(i).getFreeSpace() - (64 * 1024));                                        
                                    }
                                }
                            } else {
                                //choose first Constants.REPLICATION_COUNT
                                for (int i = 0; i < Constants.REPLICATION_COUNT; i++) {
                                    returnString += chunkServerFreeSpace.get(i).getIpPort() + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                                    //Remove 64*1024 from stored value
                                    chunkServerFreeSpace.get(i).setFreeSpace(chunkServerFreeSpace.get(i).getFreeSpace() - (64 * 1024));                                    
                                }
                            }
                        } else {
                            int j = 0;
                            if (previousChunkServers != null && !previousChunkServers.isEmpty()) {
                                //Compare with previous
                                for (int i = 0; i < chunkServerCount; i++) {
                                    if (!previousChunkServers.contains(chunkServerFreeSpace.get(i).getIpPort())) {
                                        returnString += chunkServerFreeSpace.get(i).getIpPort() + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                                        //Remove 64*1024 from stored value
                                        chunkServerFreeSpace.get(i).setFreeSpace(chunkServerFreeSpace.get(i).getFreeSpace() - (64 * 1024));                                        
                                        ++j;
                                        if (j == Constants.REPLICATION_COUNT) {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                //choose first Constants.REPLICATION_COUNT
                                for (int i = 0; i < Constants.REPLICATION_COUNT; i++) {
                                    returnString += chunkServerFreeSpace.get(i).getIpPort() + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                                    //Remove 64*1024 from stored value
                                    chunkServerFreeSpace.get(i).setFreeSpace(chunkServerFreeSpace.get(i).getFreeSpace() - (64 * 1024));
                                }                                
                            }
                        }
                        returnString = returnString.substring(0, returnString.length() - 1);
                        previousChunkServers = Arrays.asList(returnString.split(Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR));
                    }
                    System.out.println("-return string-" + returnString);
                    //send response to client
                    ControllerToClientChunkServerInfoToWrite infoToWrite = new ControllerToClientChunkServerInfoToWrite();
                    infoToWrite.setChunkSeq(chunkSeq);
                    infoToWrite.setChunkServerInfo(returnString);
                    infoToWrite.setFilePath(filePath);
                    for (int i = 0; i < connections.size(); i++) {
                        if (connections.get(i).getSocket().equals(socket)) {
                            connections.get(i).getSender().sendData(infoToWrite.getBytes());
                            break;
                        }
                    }
                    break;

                default:
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
