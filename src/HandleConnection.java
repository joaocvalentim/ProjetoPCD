package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import src.Node.DtmHandler;


public class HandleConnection extends Thread {
    private Socket connection; // é a conexao com o outro node
    private int port; // é a porta do outro node
    private String address; // é o endereço do outro node

    private Node node;
    private ObjectInputStream input; 

    public HandleConnection(Socket connection,  ObjectInputStream in, String address, int port, Node node) {
        this.connection = connection; 
        this.port = port;
        this.address = address;

        this.node = node;
        this.input = in;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                Object message = input.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void handleMessage(Object message) throws IOException {
        if (message instanceof WordSearchMessage) {
            WordSearchMessage searchMessage = (WordSearchMessage) message;
            System.out.println("Mensagem de busca recebida (" + searchMessage.getKeyword() + ")");
            node.searchFiles(searchMessage);
            for (FileSearchResult result : node.getSearchResults()) {
                node.sendMessage(connection, result);
                System.out.println("Resultado de busca enviado: " + result.getNome());
            }
        } else if (message instanceof FileSearchResult) {
            FileSearchResult result = (FileSearchResult) message;
            System.out.println("Resultado de busca recebido: " + result.getNome()+ " - a atualizar a gui");
            node.updateSearchResults(result);
            
        }else if (message instanceof DownloadTasksManager) {
            System.out.println("DownloadTasksManager recebido.");
            DownloadTasksManager dtm = (DownloadTasksManager) message;
            node.setDtm (dtm);

            System.out.println("ESTOU A INICIAR DOWNLOAD MADJE.");
            node.addDtmByFsr(dtm.getFsr(), dtm);
            DtmHandler dtmHandler = new DtmHandler(node, dtm, null);
            dtmHandler.start();
        }else
            System.err.println("ESTOU A RECEBER UMA MERDA ATOA"); 
    }

    public Socket getConnection() {
        return connection;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

}
