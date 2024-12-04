package src;

import java.io.IOException;

public class MainNode1 {

    static final String folderPath = "node1-server";
    static final int port = 1000;
    static final String address = "localhost";

    public static void main(String[] args) throws IOException {
        
        Node node = new Node(port, address, folderPath);
        // IscTorrentGUI gui = new IscTorrentGUI(node);


    }
}
