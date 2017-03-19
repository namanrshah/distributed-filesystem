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
public class ClientControllerChunkRequest {

    byte type;
    int chunkSeq;
    String filePath;
//    int nodeId;
//    String informationString;

    public ClientControllerChunkRequest() {
        this.type = Constants.MESSAGES.CLIENT_CONTROLLER_CHUNK_REQUEST;
    }

    public ClientControllerChunkRequest(byte[] marshalledBytes) {
        try {
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
            type = din.readByte();
            chunkSeq = din.readInt();
            int length = din.readInt();
            byte[] info = new byte[length];
            din.read(info);
            filePath = new String(info);
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

    public int getChunkSeq() {
        return chunkSeq;
    }

    public void setChunkSeq(int chunkSeq) {
        this.chunkSeq = chunkSeq;
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
            dout.writeInt(chunkSeq);
            dout.writeInt(filePath.length());
            dout.write(filePath.getBytes());
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "ClientControllerChunkRequest{" + "type=" + type + ", chunkSeq=" + chunkSeq + ", filePath=" + filePath + '}';
    }

    
}
