/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj.dfs.client;

import proj.dfs.controller.ChunkServerFreeSpace;
import proj.dfs.wireformats.ClientControllerChunkRequest;
import proj.dfs.transport.Node;
import proj.dfs.transport.TCPConnection;
import proj.dfs.util.ConsoleCommands;
import proj.dfs.util.Constants;
import proj.dfs.wireformats.ChunkServerControllerConnection;
import proj.dfs.wireformats.ClientToChunkServerStoreFile;
import proj.dfs.wireformats.ControllerToClientChunkServerInfoToWrite;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rajiv
 */
public class Client implements Node {

    /**
     * @param args the command line arguments
     */
    private byte[][] fileInChunk;
    private String controllerHost;
    private int controllerPort;
    private TCPConnection controllerConnection;

    public static void main(String[] args) {
        //args[0] = client port
        //args[1] = controller host
        //args[2] = controller port
        new Client().startClient(args);
    }

    public void startClient(String[] args) {
        boolean validSocket = false;
        int port = Integer.parseInt(args[0]);
        controllerHost = args[1];
        controllerPort = Integer.parseInt(args[2]);
        while (!validSocket) {
            try {
                //client server socket                
                validSocket = true;
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("INFO : Client is listening on : " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
                //starting console command thread
                ConsoleCommands consoleCommands = new ConsoleCommands(this);
                Thread consoleCommandThread = new Thread(consoleCommands);
                consoleCommandThread.start();
                while (true) {
                    Socket incomingConnectionSocket = serverSocket.accept();
                    TCPConnection incomingConnection = new TCPConnection(incomingConnectionSocket, this);
                    incomingConnection.startReceiverThread();
                }
            } catch (IOException ex) {
                System.err.println("ERROR : Not able to connect with controller.");
                System.err.println("ERROR : Unable to listen on given socket. please try again.");
                Scanner sc = new Scanner(System.in);
                port = sc.nextInt();
            }
        }
//        try {
//            // connect to controller
//            controllerHost = args[1];
//            controllerPort = Integer.parseInt(args[2]);
//            Socket socket;
//
//            socket = new Socket(controllerHost, controllerPort);
//            TCPConnection connection = new TCPConnection(socket, this);
//            connection.startReceiverThread();
//            connection.getSender().sendData("Naman".getBytes());
//        } catch (IOException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    public static void splitFile(File f) throws IOException {
        int partCounter = 1;//I like to name parts from 001, 002, 003, ...
        //you can change it to 0 if you want 000, 001, ...

        int sizeOfChunks = Constants.CHUNK_SIZE;// 64KB
        byte[] buffer = new byte[sizeOfChunks];
        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f))) {//try-with-resources to ensure closing stream
            String name = f.getName();

            int tmp = 0;
            while ((tmp = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                File newFile = new File(f.getParent(), name + "."
                        + String.format("%03d", partCounter++));
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, tmp);//tmp is chunk size
                }
            }
        }
    }

    public static void mergeFile(File f) throws FileNotFoundException {
        File ofile = new File(f.getName() + "_merged");
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;
        List<File> files = new ArrayList<>();
        long noOfChunks = (long) Math.ceil(f.length() * 1.0f / Constants.CHUNK_SIZE);
        System.out.println("-length-" + f.length() + "-no of chunks-" + noOfChunks);
        for (long i = 0; i < noOfChunks; i++) {
            files.add(new File(f.getName() + "." + String.format("%03d", (i + 1))));
        }
        try {
            fos = new FileOutputStream(ofile, false);
            for (File file : files) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0, (int) file.length());
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) file.length());
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;

        } catch (Exception e) {

        }
    }

    public static void getSHA1Checksum(File datafile) throws NoSuchAlgorithmException, FileNotFoundException, IOException {

        MessageDigest md = MessageDigest.getInstance("SHA1");
        FileInputStream fis = new FileInputStream(datafile);
        byte[] dataBytes = new byte[1024];

        int nread = 0;

        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        };

        byte[] mdbytes = md.digest();

        //convert the byte to hex format
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("Digest(in hex format):: " + sb.toString());
    }

    @Override
    public void processRequest(byte[] bytes, Socket socket) {
        byte type = bytes[0];
        switch (type) {
            case Constants.MESSAGES.CONTROLLER_TO_CLIENT_STORE_FILE:
                ControllerToClientChunkServerInfoToWrite writeInfo = new ControllerToClientChunkServerInfoToWrite(bytes);
                System.out.println("-write info-" + writeInfo);
                System.out.println("-request from-" + socket.getInetAddress().getHostAddress() + socket.getPort());
                int chunkSeq = writeInfo.getChunkSeq();
                String chunkServerInfo = writeInfo.getChunkServerInfo();
                String filePath = writeInfo.getFilePath();
                System.out.println("-file path-" + filePath);
                String[] splittedChunkServers = chunkServerInfo.split(Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR);
                if (splittedChunkServers != null && splittedChunkServers.length > 0) {
                    String server1 = splittedChunkServers[0];
                    if (server1 != null && !server1.isEmpty()) {
                        String otherChunks = "";
                        int chunkServersCount = splittedChunkServers.length;
                        for (int i = 1; i < chunkServersCount; i++) {
                            otherChunks += splittedChunkServers[i] + Constants.DELIMITORS.CHUNK_SERVERS_DELIMITOR;
                        }
                        otherChunks = otherChunks.substring(0, otherChunks.length() - 1);
                        String[] ipPort = server1.split(Constants.DELIMITORS.IP_PORT);
//                        for (int i = 0; i < ipPort.length; i++) {
//                            System.out.println("-chunk server1-" + ipPort[i]);
//                            
//                        }
                        if (ipPort != null && ipPort.length > 0) {
                            try {
                                String chunk1Ip = ipPort[0];
                                String chunk1Port = ipPort[1];
                                System.out.println("-chunk servere port-" + chunk1Port);
                                Socket chunkServerSocket = new Socket(chunk1Ip, Integer.parseInt(chunk1Port));
                                TCPConnection chunkServerConnection = new TCPConnection(chunkServerSocket, this);
//                                chunkServerConnection.startReceiverThread();
                                //Create response
                                ClientToChunkServerStoreFile clientToServerStore = new ClientToChunkServerStoreFile();
                                //                                clientToServerStore.s
                                RandomAccessFile raf = new RandomAccessFile(filePath, "r");
                                raf.seek((chunkSeq - 1) * Constants.CHUNK_SIZE);
                                byte[] buffer = new byte[Constants.CHUNK_SIZE];
                                raf.readFully(buffer);
                                clientToServerStore.setChunkData(buffer);
                                clientToServerStore.setChunkServerInfo(otherChunks);
                                clientToServerStore.setSequence(chunkSeq);
                                clientToServerStore.setFilename(filePath);
                                System.out.println("-chunk data to send-" + clientToServerStore);
                                chunkServerConnection.getSender().sendData(clientToServerStore.getBytes());
                            } catch (IOException ex) {
                                System.err.println("ERROR : While sending chunk from client to chunkserver.");
                                ex.printStackTrace();
                            }
                        }
                    }
                }
        }
    }

    public void storefile(String filePath) {
        //start storing files
        File f = new File(filePath);
        if (f.exists() && f.isFile() && f.canRead()) {
            long length = f.length();
            int sizeOfChunks = Constants.CHUNK_SIZE;// 64KB
//            float chunkCountFloat = length * 1.0f / Constants.CHUNK_SIZE;
//            int completeChunks = (int) chunkCountFloat;
//            int remainder = (chunkCountFloat > completeChunks) ? 1 : 0;
            int totalchunks = (int) Math.ceil(f.length() * 1.0f / Constants.CHUNK_SIZE);
            System.out.println("-chunks-" + (totalchunks));
            try {
                //input stream to read file
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
                int tmp = 0;
                int counter = 0;
                byte[] buffer = new byte[sizeOfChunks];
//                fileInChunk = new byte[totalchunks][Constants.CHUNK_SIZE];
//                while ((tmp = bis.read(buffer)) > 0) {
//                    fileInChunk[counter] = buffer.clone();
//                    ++counter;
//                }
//                for (int i = 0; i < fileInChunk.length; i++) {
//                    byte[] fileInChunk1 = fileInChunk[i];
//                    System.out.println("-file-" + new String(fileInChunk1));
//                }
                bis.close();
                if (controllerConnection == null) {
                    Socket controllerSocket = new Socket(controllerHost, controllerPort);
                    controllerConnection = new TCPConnection(controllerSocket, this);
                    controllerConnection.startReceiverThread();
                }

                for (int i = 0; i < totalchunks; i++) {
                    //ask controller for 3 chunk server replicas     
                    ClientControllerChunkRequest chunkReq = new ClientControllerChunkRequest();
                    chunkReq.setChunkSeq(i + 1);
                    chunkReq.setFilePath(filePath);
                    controllerConnection.getSender().sendData(chunkReq.getBytes());
                }
            } catch (FileNotFoundException ex) {
                System.err.println("ERROR : File not found : " + f.getAbsolutePath());
            } catch (IOException ex) {
                System.err.println("ERROR : Exception while reading : " + f.getAbsolutePath());
                ex.printStackTrace();
            }
        } else {
            System.err.println("ERROR : File not exists OR Its not a file OR File cant be read");
        }
    }

    public void readFile(String filePath) {

    }

    public void updateFile(String filePath) {

    }

}
