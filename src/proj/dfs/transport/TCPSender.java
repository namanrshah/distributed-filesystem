package proj.dfs.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Naman
 */
public class TCPSender {

    Socket socket;
    DataOutputStream dout;

    public TCPSender(Socket socket) {
        this.socket = socket;
        try {
            this.dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("--exception in output stream--");
        }
    }

    public void sendData(byte[] data) throws IOException {
        int dataLength = data.length;
//        System.out.println("-send length-" + dataLength);        
            dout.writeInt(dataLength);
            dout.write(data, 0, dataLength);
            dout.flush();            
       
    }

}
