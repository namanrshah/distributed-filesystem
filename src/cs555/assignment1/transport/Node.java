
package cs555.assignment1.transport;

import java.net.Socket;

/**
 *
 * @author Rajiv
 */
public interface Node {

    public void processRequest(byte[] bytes, Socket socket);
}
