package src;

import java.io.IOException;

public class MainNode1 {

    static final String folderPath = "dl1";
    static final int port = 8081;
    static final String address = "localhost";

    public static void main(String[] args) throws IOException {
        
        new Node(port, address, folderPath);


    }
}
