package src;

import java.io.IOException;

public class MainNode4 {
    static final String folderPath = "dl4";
    static final int port = 8084;
    static final String address = "localhost";

    public static void main(String[] args) throws IOException {

        new Node(port, address, folderPath);

    
    }
}
