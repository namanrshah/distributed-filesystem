/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj.dfs.controller;

import proj.dfs.util.Constants;
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
public class ControllerToClientChunkServerInfoToWrite {

    byte type;
    String chunkServerInfo;
//    int nodeId;
//    String informationString;

    public ControllerToClientChunkServerInfoToWrite() {
        this.type = Constants.MESSAGES.CONTROLLER_TO_CLIENT_STORE_FILE;
    }

    public ControllerToClientChunkServerInfoToWrite(byte[] marshalledBytes) {
        try {
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
            type = din.readByte();
            int length = din.readInt();
            byte[] info = new byte[length];
            din.read(info);
            chunkServerInfo = new String(info);
//            nodeId = din.readInt();
//            int infoLength = din.read();
////            System.out.println(NodeReportsOverlaySetupStatus.class + "-length at registry-" + infoLength);
//            byte[] infoBytes = new byte[infoLength];
//            System.out.println("");
//            din.read(infoBytes, 0, infoLength);
//            informationString = new String(infoBytes);
//            System.out.println(NodeReportsOverlaySetupStatus.class + "-info string-" + informationString);
            baInputStream.close();
            din.close();
        } catch (IOException ex) {
            System.err.println("ERROR : exception in deserializing." + this.getClass());
        }
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getChunkServerInfo() {
        return chunkServerInfo;
    }

    public void setChunkServerInfo(String chunkServerInfo) {
        this.chunkServerInfo = chunkServerInfo;
    }

//    public int getNodeId() {
//        return nodeId;
//    }
//
//    public void setNodeId(int nodeId) {
//        this.nodeId = nodeId;
//    }
//
//    public String getInformationString() {
//        return informationString;
//    }
//
//    public void setInformationString(String informationString) {
//        this.informationString = informationString;
//    }
    public byte[] getBytes() {
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
            dout.write(type);
            dout.writeInt(chunkServerInfo.length());
            dout.write(chunkServerInfo.getBytes());
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

//    @Override
//    public String toString() {
//        return "NodeReportsOverlaySetupStatus{" + "type=" + type + ", nodeId=" + nodeId + ", informationString=" + informationString + '}';
//    }
    @Override
    public String toString() {
        return "ClientControllerChunkRequest{" + "type=" + type + '}';
    }
}
