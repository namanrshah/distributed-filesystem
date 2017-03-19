/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj.dfs.controller;

/**
 *
 * @author namanrs
 */
public class ChunkServerFreeSpace {

    public String ipPort;
    public long freeSpace;

    public ChunkServerFreeSpace(String ipPort, long freeSpace) {
        this.ipPort = ipPort;
        this.freeSpace = freeSpace;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    @Override
    public String toString() {
        return "ChunkServerFreeSpace{" + "ipPort=" + ipPort + ", freeSpace=" + freeSpace + '}';
    }

}
