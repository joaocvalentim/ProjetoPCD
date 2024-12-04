package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectOutputStream;


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

    public void waitForConnection() throws IOException{
        while(true){
            Socket clientSocket = serverSocket.accept();
            System.out.println("Nova conexão recebida de " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
            HandleConnection handleConnection = new HandleConnection(clientSocket, node);
            handleConnection.start();
        }
    }

    private static class HandleConnection extends Thread{ 
        private Socket clientSocket;
        private Node node;
        private ObjectInputStream input;    
        private ObjectOutputStream output;

        public HandleConnection(Socket clientSocket, Node node) throws IOException{
            this.clientSocket = clientSocket;
            this.node = node;

            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            this.input = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("Streams iniciados com " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                            
        }

        @Override
        public void run(){
            try{
                //ObjectOutput output = new ObjectOutputStream(clientSocket.getOutputStream());
                //ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                //BufferedReader in = new BufferedReader ( new InputStreamReader (input));

                while(true){
                //while(in.readLine() != null){
                    Object message = input.readObject();
                    //Object message = in.readLine();
                    node.handleMessage(clientSocket, message);
                }
            } catch (IOException | ClassNotFoundException e){
                System.err.println("Erro ao receber mensagem de " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                e.printStackTrace();
            } finally {
                if (clientSocket != null){
                    try{
                        clientSocket.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
