package cs555.assignment1.chunkServer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rajiv
 */
public class PollingThread implements Runnable {

    ChunkServer chunkServer;

    public PollingThread(ChunkServer ChunkServer) {
        this.chunkServer = ChunkServer;
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(30000);
//                ++i;
                if (i == 9) {
                    chunkServer.sendMajorHeartbeat();
                    i = 0;
                } else {
                    chunkServer.sendMinorHeartBeat();
                    ++i;
                }
            } catch (InterruptedException ex) {
                System.out.println("ERROR : Error in polling thread.");
            }
        }
    }

}
