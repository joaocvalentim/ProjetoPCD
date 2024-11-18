package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/* Pagina 2 do enunciado
 *  Objetivo: Classe responsável por gerenciar o envio de pedidos de ligação entre nós na rede.
    Atributos/Métodos: Não especificado no enunciado, mas mencionada como necessária para iniciar uma conexão entre nós.
 */
public class NewConnectionRequest {
   // atributos do node

   private Socket connection;
   private ObjectInputStream input;
   private ObjectOutputStream output;

   // construtor para iniciar conexao
   /*
    * public NewConnectionRequest(Node node, String endereco, int port) {
    * try {
    * for (Node p : node.getPeers()) {
    * if (p.getAddress().equals(endereco) && p.getPort() == port) {
    * System.out.println("Connection already exists with: " + endereco);
    * return;
    * }
    * }
    * //caso ainda nao esteja connectado
    * this.connection = new Socket(endereco, port);
    * node.addPeer(node);
    * this.output = new ObjectOutputStream(connection.getOutputStream());
    * this.input = new ObjectInputStream(connection.getInputStream());
    * System.out.println("Streams created with: " +
    * connection.getInetAddress().getHostName());
    * } catch (Exception e) {
    * e.printStackTrace();
    * }
    * 
    * }
    * 
    * public void closeConnection(Node node) {
    * try{
    * if(input != null) input.close();
    * if(output != null) output.close();
    * if(connection != null && !connection.isClosed()){
    * connection.close();
    * }
    * System.out.println("Connection closed");
    * } catch (Exception e) {
    * e.printStackTrace();
    * System.err.println("Error closing connection");
    * }
    * }
    * 
    */

   // Construtor para iniciar uma conexão com outro nó (cliente)
   public NewConnectionRequest(String address, int port) throws IOException {
      this.connection = new Socket(address, port);
      initializeStreams();
   }

   // Construtor para aceitar uma conexão existente (servidor)
   public NewConnectionRequest(Socket socket) throws IOException {
      this.connection = socket;
      initializeStreams();
   }

   // Método para inicializar os streams
   private void initializeStreams() throws IOException {
      try {
         this.output = new ObjectOutputStream(connection.getOutputStream());
         this.output.flush();
         this.input = new ObjectInputStream(connection.getInputStream());
         System.out.println("Streams initialized for connection with " + connection.getInetAddress().getHostName());
      } catch (IOException e) {
         System.err.println("Error initializing streams.");
         e.printStackTrace();
         close();
      }
   }

   // Método para enviar uma mensagem
   public synchronized void sendMessage(Object message) {
      try {
          output.writeObject(message);
          output.flush();
      } catch (IOException e) {
          System.err.println("Error sending message.");
          e.printStackTrace();
          close();
      }
  }

  // Método para receber uma mensagem
  public Object receiveMessage() {
      try {
          return input.readObject();
      } catch (IOException | ClassNotFoundException e) {
          System.err.println("Error receiving message.");
          e.printStackTrace();
          close();
          return null;
      }
  }

  // Método para fechar a conexão
  public void close() {
      try {
          if (input != null) input.close();
          if (output != null) output.close();
          if (connection != null && !connection.isClosed()) connection.close();
          System.out.println("Connection closed.");
      } catch (IOException e) {
          System.err.println("Error closing connection.");
          e.printStackTrace();
      }
  }
}
