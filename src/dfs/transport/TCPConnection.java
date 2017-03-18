package dfs.transport;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Naman
 */
public class TCPConnection {

    private Socket socket;
    private TCPSender sender;
    private TCPReceiver receiver;
    private Thread receiverThread;

    public Thread getReceiverThread() {
        return receiverThread;
    }

    public void setReceiverThread(Thread receiverThread) {
        this.receiverThread = receiverThread;
    }

    public TCPConnection(Socket socket, Node node) {
        this.socket = socket;
        this.sender = new TCPSender(socket);
        this.receiver = new TCPReceiver(socket, node);
    }

    public void startReceiverThread() {
        receiverThread = new Thread(receiver);
        receiverThread.start();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public TCPSender getSender() {
        return sender;
    }

    public void setSender(TCPSender sender) {
        this.sender = sender;
    }

    public TCPReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(TCPReceiver receiver) {
        this.receiver = receiver;
    }

    public void closeConnection(){
        try {
            receiverThread.interrupt();
            socket.close();
        } catch (IOException ex) {
            System.err.println("ERROR : closing connection.");
        }
    }
}
