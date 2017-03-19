/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj.dfs.util;

/**
 *
 * @author Rajiv
 */
public class Constants {

    public static final int REPLICATION_COUNT = 3;
    public static final int CHUNK_SIZE = 64 * 1024;
    public static final int SLICE_SIZE = 8 * 1024;
    public static final String CHUNK_STORE_DESTINATION = "/tmp/cs555_namanrs";
    public static final String PROPOGATION_TERMINATING_STRING = "%^&";
    public static final String METADATA_LOCATION = "/tmp/cs555_namanrs/.metadata";

    public static class CLIENT_COMMANDS {

        public static final String READ = "read";
        public static final String STORE = "store";
        public static final String UPDATE = "update";
    }

    public static class MESSAGES {

        public static final byte CHUNK_SERVER_CONTROLLER_CONNECTION = 1;
        public static final byte CLIENT_CONTROLLER_CHUNK_REQUEST = 2;
        public static final byte CONTROLLER_TO_CLIENT_STORE_FILE = 3;
        public static final byte CLIENT_TO_CHUNK_SERVER_STORE_FILE = 4;
        public static final byte CHUNKDATA_PROPOGATION = 5;
    }

    public static class DELIMITORS {

        public static final String IP_PORT = ":";
        public static final String CHUNK_SERVERS_DELIMITOR = "&";
    }
}
