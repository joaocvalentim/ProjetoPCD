package src;

import java.io.IOException;

public class MainNode2 {

    static final String folderPath = "node2-server";
    static final int port = 2000;
    static final String address = "localhost";
    public static WordSearchMessage[] wordSearchMessages;

    public static void main(String[] args) throws IOException {

        Node node = new Node(port, address, folderPath);

        

    }
}
