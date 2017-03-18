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
public class ChunkDataPropogation {

    byte type;
    String chunkServerInfo;
    String filename;
    int sequence;
    byte[] chunkData;

    public ChunkDataPropogation() {
        this.type = Constants.MESSAGES.CHUNKDATA_PROPOGATION;
    }

    public ChunkDataPropogation(byte[] marshalledBytes) {
        try {
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
            type = din.readByte();
            int len = din.readInt();
            byte[] info = new byte[len];
            din.read(info);
            chunkServerInfo = new String(info);
            int fileNameLen = din.readInt();
            byte[] fileName = new byte[fileNameLen];
            din.read(fileName);
            filename = new String(fileName);
            sequence = din.readInt();
            int dataLenInt = din.readInt();
            chunkData = new byte[dataLenInt];
            din.read(chunkData);
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

    public byte[] getChunkData() {
        return chunkData;
    }

    public void setChunkData(byte[] chunkData) {
        this.chunkData = chunkData;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getBytes() {
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
            dout.write(type);
            dout.writeInt(chunkServerInfo.length());
            dout.write(chunkServerInfo.getBytes());
            dout.writeInt(filename.length());
            dout.write(filename.getBytes());
            dout.writeInt(sequence);
            dout.writeInt(chunkData.length);
            dout.write(chunkData);
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
        return "ClientToChunkServerStoreFile{" + "type=" + type + ", chunkServerInfo=" + chunkServerInfo + ", chunkData=" + chunkData + '}';
    }
}
