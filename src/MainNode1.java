package src;


public class MainNode1 {
    
    static final String folderPath = "node1-server";
    static final int port = 5000;
    static final String address = "localhost";
    static final int NUM_THREADS = 3;
    public static WordSearchMessage[] wordSearchMessages ;
    public static void main(String[] args) {
        
        Node node = new Node(port, address, folderPath);

        //IscTorrentGUI gui = new IscTorrentGUI(node);
        IscTorrentGUI gui = new IscTorrentGUI(node, wordSearchMessages);
        gui.open();
        
    }
}
