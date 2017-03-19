
package proj.dfs.transport;

import java.net.Socket;

/**
 *
 * @author Rajiv
 */
public interface Node {

    public void processRequest(byte[] bytes, Socket socket);
}
