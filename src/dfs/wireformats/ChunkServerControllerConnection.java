/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dfs.wireformats;

import dfs.util.Constants;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author namanrs
 */
public class ChunkServerControllerConnection {

    byte type;
    int listeningPort;
    long freeSpace;
//    int nodeId;
//    String informationString;

    public ChunkServerControllerConnection() {
        this.type = Constants.MESSAGES.CHUNK_SERVER_CONTROLLER_CONNECTION;
    }

    public ChunkServerControllerConnection(byte[] marshalledBytes) {
        try {
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
            type = din.readByte();
            listeningPort = din.readInt();
            freeSpace = din.readLong();
            baInputStream.close();
            din.close();
        } catch (IOException ex) {
            System.err.println("ERROR : exception in deserializing." + this.getClass());
        }
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }
    

    public byte[] getBytes() {
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
            dout.write(type);
            dout.writeInt(listeningPort);
            dout.writeLong(freeSpace);
//            dout.writeInt(nodeId);
//            byte[] infoBytes = informationString.getBytes();
////            System.out.println(NodeReportsOverlaySetupStatus.class + "-bytes length-" + infoBytes.length);
//            dout.write(infoBytes.length);
//            dout.write(infoBytes);
            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();
            baOutputStream.close();
            dout.close();
            return marshalledBytes;
        } catch (IOException ex) {
            Logger.getLogger("ERROR : error in marshalling" + this.getClass());
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClientControllerChunkRequest{" + "type=" + type + ", freespace=" + freeSpace + '}';
    }

}
