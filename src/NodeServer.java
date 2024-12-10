package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer extends Thread {
    private ServerSocket serverSocket;
    private Node node;

    public NodeServer(Node node) throws IOException {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(node.getPort());
            System.out.println("Servidor iniciado na porta " + node.getPort());
            waitForConnection();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor na porta" + node.getPort());
            e.printStackTrace();
            stopServer();
        } 
    }

    public void waitForConnection() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Nova conexão recebida de " + clientSocket.getInetAddress().getHostName() + ":"
                    + clientSocket.getPort());
            HandleConnection handleConnection = new HandleConnection(clientSocket, node);
            handleConnection.start();
        }
    }

    public void stopServer() {
        try {
            node.stop();
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("NodeServer - Servidor encerrado.");
        } catch (IOException e) {
            System.err.println("Erro ao encerrar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class HandleConnection extends Thread {
        private Socket clientSocket;
        private Node node;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        public HandleConnection(Socket clientSocket, Node node) throws IOException {
            this.clientSocket = clientSocket;
            this.node = node;

            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            this.input = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("Streams iniciados com " + clientSocket.getInetAddress().getHostName() + ":"
                    + clientSocket.getPort());

        }

        @Override
        synchronized public void run(){
            try {
                while(!clientSocket.isClosed()){
                    Object message = input.readObject();
                    node.handleMessage(clientSocket, message);
                }    
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                closeConnection();

                    
            } 
        }

        public void closeConnection() {
            try {
                node.removeConnection(clientSocket);
                input.close();
                output.close();
                // node.removeNode(clientSocket.getInetAddress().getHostName(),
                // clientSocket.getPort());
                System.out.println("Conexão com " + clientSocket.getInetAddress().getHostName() + ":"
                        + clientSocket.getPort() + " encerrada.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
