package src;


public class MainNode2 {
    
    static final String folderPath = "node2-server";
    static final int port = 4000;
    static final String address = "localhos2";
    static final int NUM_THREADS = 3;
    public static WordSearchMessage[] wordSearchMessages ;
    public static void main(String[] args) {
        
        Node node = new Node(port, address, folderPath);

        IscTorrentGUI gui = new IscTorrentGUI(node, wordSearchMessages);
        gui.open();
        
    }
}
