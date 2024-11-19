package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/* Pagina 2 do enunciado
 *  Objetivo: Classe responsável por gerenciar o envio de pedidos de ligação entre nós na rede.
    Atributos/Métodos: Não especificado no enunciado, mas mencionada como necessária para iniciar uma conexão entre nós.
 */
public class NewConnectionRequest {
    // atributos do node

    private Socket connection;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    // Construtor para iniciar uma nova conexão
    public NewConnectionRequest(String address, int port) throws UnknownHostException, IOException {
        this.connection = new Socket(InetAddress.getByName(address), port);
        initializeStreams();
        System.out.println("Connection established with " + connection.getInetAddress().getHostName() + ":"
                + connection.getPort());
    }

    // Construtor para aceitar uma nova conexão
    public NewConnectionRequest(Socket nodeSocket) throws IOException {
        this.connection = nodeSocket;
        initializeStreams();
        System.out.println("Connection established with " + connection.getInetAddress().getHostName() + ":"
                + connection.getPort());
    }

    // Método para inicializar os streams
    private void initializeStreams() throws IOException {
        try {
            this.output = new ObjectOutputStream(connection.getOutputStream());
            this.output.flush();
            this.input = new ObjectInputStream(connection.getInputStream());
            System.out.println("Streams initialized for connection with " + connection.getInetAddress().getHostName()
                    + ":" + connection.getPort());
        } catch (IOException e) {
            System.err.println("Error initializing streams.");
            e.printStackTrace();
            close();
        }
    }

    /*
     * // Método para enviar uma mensagem
     * public synchronized void sendMessage(Object message) {
     * try {
     * output.writeObject(message);
     * output.flush();
     * } catch (IOException e) {
     * System.err.println("Error sending message.");
     * e.printStackTrace();
     * close();
     * }
     * }
     * 
     * // Método para receber uma mensagem
     * public Object receiveMessage() {
     * try {
     * return input.readObject();
     * } catch (IOException | ClassNotFoundException e) {
     * System.err.println("Error receiving message.");
     * e.printStackTrace();
     * close();
     * return null;
     * }
     * }
     */
    // Método para fechar a conexão
    public void close() {
        try {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            if (connection != null && !connection.isClosed())
                connection.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection.");
            e.printStackTrace();
        }
    }

    // Método para obter o socket da conexão
    public Socket getSocket() {
        return connection;
    }
}
