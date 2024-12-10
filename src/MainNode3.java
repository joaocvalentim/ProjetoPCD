package src;

import java.io.IOException;

public class MainNode3 {

    static final String folderPath = "dl3";
    static final int port = 8083;
    static final String address = "localhost";

    public static void main(String[] args) throws IOException {

        new Node(port, address, folderPath);

    
    }
}
