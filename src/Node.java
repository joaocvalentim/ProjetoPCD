package src;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Node{
    private int port;
    private String address;
    private String folderPath;

    public List<FileSearchResult> searchResults = new ArrayList<FileSearchResult>();

    private List<Socket> connections; // lista de sockets connectado
    private NodeServer server; // server dedicado ao nó

    private Map<Socket, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>();
    private Map<String, Socket> connectionId = new ConcurrentHashMap<>();

    public Node(int port, String address, String folderPath) throws IOException {
        this.port = port;
        this.address = address;
        this.folderPath = folderPath;
        this.connections = new ArrayList<Socket>();
        this.searchResults = new ArrayList<FileSearchResult>();

        this.server = new NodeServer(this);
        this.server.start();
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     *                  CRIAR E ACEITAR CONEXÕES
     * 
     *               newConnection // addConnection
     *************************************************************************
     *************************************************************************
     *************************************************************************/

    public void newConnection(String address1, int port1) throws IOException {
        System.out.println("Node - "+folderPath+" - A tentar connectar ao nó " + address1 + ":" + port1);
        Socket connection = new Socket(address1, port1);
        ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        outputStreams.put(connection, output);
        ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
        connections.add(connection);
        System.out.println("CARALHO" +address1 + ":" + port1);
        connectionId.put(address1 + ":" + port1, connection);
        System.out.println("Node - "+folderPath+" - Conexão estabelecida com "+address1+":"+port1);
        sendMessage(connection, new NewConnectionRequest(this.address, this.port));  
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     *                      TROCA MENSAGENS
     * 
     *               sendMessage // handleMessage
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    public void sendMessage(Socket connection, Object message) {
        try {
            ObjectOutputStream output = outputStreams.get(connection);
            if (output == null) {
                output = new ObjectOutputStream(connection.getOutputStream());
                outputStreams.put(connection, output);
            }
            output.writeObject(message);
            output.flush();
            System.out.println("Node - " + folderPath + " - Mensagem enviada para o nó " + connection.getInetAddress().getHostName() + ":" + connection.getPort());
        } catch (IOException e) {
            System.err.println("Node - " + folderPath + " - Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleMessage(Socket clientSocket, Object message) throws IOException{
        if(message instanceof NewConnectionRequest){
            NewConnectionRequest newConnectionRequest = (NewConnectionRequest) message;
            System.out.println("Node - "+folderPath+" - Pedido de conexão recebido de " + newConnectionRequest.getAddress() + ":" + newConnectionRequest.getPort());
            connections.add(clientSocket);

            /*TODO Nao funciona pq o address e a port que apanha sao os reais e nao os que criamos no inicio 

            if(connectionId.get(newConnectionRequest.getAddress() + ":" + newConnectionRequest.getPort()) == null){
                connectionId.put(newConnectionRequest.getAddress() + ":" + newConnectionRequest.getPort(), clientSocket);
                newConnection(this.address, this.port);
            } */

        }else if(message instanceof WordSearchMessage){
            WordSearchMessage searchMessage = (WordSearchMessage) message;
            System.out.println("Node - " + folderPath+" - Mensagem de busca recebida ("+searchMessage.getKeyword()+")");
            searchFiles(searchMessage);
            for (FileSearchResult result : searchResults){
                sendMessage(clientSocket, result);
            }
        }else if(message instanceof FileSearchResult){
            FileSearchResult result = (FileSearchResult) message;
            System.out.println("Node - "+folderPath+" - Resultado de busca recebido: "+result.getNome());
            updateSearchResults(result);
        }
    }


    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     *                    Pesquisa de Ficheiros
     * 
     *               startSearch // searchFiles
     *************************************************************************
     *************************************************************************
     *************************************************************************/

    synchronized public void startSearch(String keyword){
        searchResults.clear();

        WordSearchMessage message = new WordSearchMessage(keyword, this.address, this.port);
        for (Socket connection : connections){
            sendMessage(connection, message);
        }
        System.out.println("Node - "+folderPath+" - Mensagem de busca enviada (keyword: " + keyword + ")");
    }

    public void searchFiles(WordSearchMessage message) {
        // criar uma lista para guardar os resultados da pesquisa
        List <FileSearchResult> results = new ArrayList<FileSearchResult>();
        // criar um array de ficheiros (File[]) com os ficheiros da pasta do node (filtrados por .mp3 e que contenham a keyword)
        File [] files = new File(folderPath).listFiles((new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("mp3") && name.contains(message.getKeyword());
            }
        }));
        // adicionar os resultados à lista de resultados - já com o formato FileSearchResult
        if (files!=null){
            for (File f : files){
                System.out.println("Node - "+folderPath+" - Ficheiro encontrado: "+f.getName());
                results.add(new FileSearchResult(message, "hash", f.length(), f.getName(), this.address, this.port));
                //this.searchResults.add(new FileSearchResult(message, "hash", f.length(), f.getName(), this.address, this.port));
            }
        }
        System.out.println("Node - "+folderPath+" - Pesquisa de ficheiros concluída.");

        System.out.println(message.getOriginAddress() + ":" + message.getOriginPort());
        Socket connection = connectionId.get(message.getOriginAddress() + ":" + message.getOriginPort());
        for (FileSearchResult result : results){
            sendMessage(connection, result);
        }
        System.out.println("Node - "+folderPath+" - Resultados enviados para o nó " + connection.getInetAddress().getHostName() + ":" + connection.getPort());
    }

    public void updateSearchResults(FileSearchResult result){
        if(!searchResults.contains(result)){
            searchResults.add(result);
        }
        for (FileSearchResult r : searchResults){
            System.out.println("Node - "+folderPath+" - Resultado de busca: "+r.getNome());
        }
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     *            Download de Ficheiros - por fazer
     * 
     *                  sendDownloadRequest
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    public void sendDownloadRequest(FileSearchResult result) {
        DownloadTasksManager downloadManager = new DownloadTasksManager(result);
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     *                      Getters e Setters
     * 
     *      getSearchResults // getPort // getAddress // getFolderPath
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    public List<FileSearchResult> getSearchResults() {
        return searchResults;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public List<Socket> getConnections() {
        return connections;
    }

}
