package proj.dfs.chunkServer;

import java.util.List;

/**
 *
 * @author namanrs
 */
public class MetadataToStore {

    int versionNo;
    int sequenceNo;
    long timeStamp;
    String fileName;
    List<String> checksum;

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getChecksum() {
        return checksum;
    }

    public void setChecksum(List<String> checksum) {
        this.checksum = checksum;
    }

}
