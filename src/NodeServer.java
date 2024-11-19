package src;

import java.io.IOException;
import java.net.ServerSocket;

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
            System.err.println("Erro ao iniciar o servidor na porta " + node.getPort());
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void waitForConnection() throws IOException {
        while (true) {
            NewConnectionRequest newConnection = new NewConnectionRequest(serverSocket.accept());
            System.out.println("Nova conexão recebida de " + newConnection.getSocket().getInetAddress().getHostName()
                    + ":" + newConnection.getSocket().getPort());
            node.addConnection(newConnection.getSocket());
            // node.newConnection(newConnection.getSocket().getInetAddress().getHostName(),
            // newConnection.getSocket().getPort());
            System.out.println("Conexão estabelecida e registrada com sucesso!");
        }
    }
}
