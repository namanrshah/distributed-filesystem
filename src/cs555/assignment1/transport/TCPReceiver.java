package cs555.assignment1.transport;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 
 * @author Naman
 */
public class TCPReceiver implements Runnable {

	Socket socket;
	DataInputStream din;
	Node node;        
	boolean completeExecution;

	public TCPReceiver(Socket socket, Node node) {
		completeExecution = false;
		this.socket = socket;
		this.node = node;
		try {
			this.din = new DataInputStream(socket.getInputStream());
		} catch (IOException ex) {
			System.err.println("ERROR : exception in output stream");
		}
	}

	@Override
	public void run() {

		while (!completeExecution) {
			int dataLength;
			try {
//                                System.out.println("-before-");
				if (socket != null) {
//                                    System.out.println("-in if-");
					dataLength = din.readInt();
					byte[] data = new byte[dataLength];
					din.readFully(data, 0, dataLength);
					node.processRequest(data, this.socket);
				}
			} catch (IOException se) {
				completeExecution = true;
				System.err.println("ERROR : Receiving data." + Thread.currentThread().getName());
                                se.printStackTrace();
			}
		}
		System.out.println("INFO : exiting from receiver thread.");
	}

	public void completeExecution() {
		completeExecution = true;
		try {
			System.out.println("INFO : closing receiver stream. ");
			din.close();
		} catch (IOException ex) {
			System.err.println("ERROR : closing receiver stream-");
		}
	}
}
