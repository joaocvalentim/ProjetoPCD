package src;

import java.io.IOException;

public class MainNode3 {

    static final String folderPath = "node3-server";
    static final int port = 3000;
    static final String address = "localhost";
    static final int NUM_THREADS = 3;
    public static WordSearchMessage[] wordSearchMessages;

    public static void main(String[] args) throws IOException {

        Node node = new Node(port, address, folderPath);

        IscTorrentGUI gui = new IscTorrentGUI(node, wordSearchMessages);
        gui.open();

    }
}
