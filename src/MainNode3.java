package src;

import java.io.IOException;

public class MainNode3 {

    static final String folderPath = "node3-server";
    static final int port = 3000;
    static final String address = "localhost";

    public static void main(String[] args) throws IOException {

        new Node(port, address, folderPath);

    
    }
}
