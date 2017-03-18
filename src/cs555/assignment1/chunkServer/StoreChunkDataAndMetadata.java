/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs555.assignment1.chunkServer;

import cs555.assignment1.util.Constants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author namanrs
 */
public class StoreChunkDataAndMetadata implements Runnable {

    byte[] data;
    String filename;
    int sequence;
    ChunkServer chunkServer;

    public StoreChunkDataAndMetadata(byte[] data, String filename, int sequence, ChunkServer chunkServer) {
        this.data = data;
        this.filename = filename;
        this.sequence = sequence;
        this.chunkServer = chunkServer;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public void run() {
        System.out.println("-storing-" + filename);
        String completeChunkName = Constants.CHUNK_STORE_DESTINATION + filename + "_chunk" + sequence;
        String folders = completeChunkName.substring(0, completeChunkName.lastIndexOf("/"));
//        String actFile = filename.substring(filename.lastIndexOf("/") + 1);
        File foldersObj = new File(folders);
        if (!foldersObj.exists()) {
            System.out.println("-creating folders-");
            foldersObj.mkdirs();
        }
        File f = new File(completeChunkName);
        try {
            try (FileOutputStream fOut = new FileOutputStream(f)) {
                fOut.write(data);
                fOut.close();
                //Store metadata                
                MetadataToStore metaData = new MetadataToStore();
                metaData.setSequenceNo(sequence);
                metaData.setVersionNo(1);
                int versionNo = 1;
                metaData.setTimeStamp(Calendar.getInstance().getTimeInMillis());
                metaData.setFileName(completeChunkName);
                MessageDigest md = MessageDigest.getInstance("SHA1");
                int slices = (int) Math.ceil(data.length * 1.0 / Constants.SLICE_SIZE);
                System.out.println("-slices-" + slices);
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < slices; i++) {
                    if (i < slices - 1) {
                        md.update(data, i * Constants.SLICE_SIZE, Constants.SLICE_SIZE);
                    } else {
                        md.update(data, i * Constants.SLICE_SIZE, data.length - ((i) * Constants.SLICE_SIZE));
                    }
                    byte[] mdbytes = md.digest();
                    for (int j = 0; j < mdbytes.length; j++) {
                        sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
                    }
                    sb.append("\n");
                }
                //store metadata
                String chunkMetadataFileName = Constants.METADATA_LOCATION + filename + "_" + sequence + ".metadata";
                synchronized (chunkServer.newlyCreatedChunks) {
                    chunkServer.newlyCreatedChunks.add(chunkMetadataFileName);
                }
                String metaDataFolder = chunkMetadataFileName.substring(0, chunkMetadataFileName.lastIndexOf("/"));
                File metaDataFolderObj = new File(metaDataFolder);
                if (!metaDataFolderObj.exists()) {
                    metaDataFolderObj.mkdirs();
                }
                File metadataFile = new File(chunkMetadataFileName);
                FileOutputStream fOutM = new FileOutputStream(metadataFile);
                fOutM.write((filename + "\n" + versionNo + "\n" + sequence + "\n" + Calendar.getInstance().getTimeInMillis() + "\n" + sb).getBytes());
                fOutM.close();
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(StoreChunkDataAndMetadata.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR : File not found.");
        } catch (IOException ex) {
            System.err.println("ERROR : Writing data to file.");
        }
    }
}
