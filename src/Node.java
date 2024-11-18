package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private int port;
    private String address;
    private String folderPath;

    private List <WordSearchMessage> searchThreads = new ArrayList<WordSearchMessage>();
    private List <FileSearchResult> searchResults = new ArrayList<FileSearchResult>();
    private int numThreads = 3;

    private List <Socket> connections; //lista de sockets connectado
    private ServerSocket serverSocket; //socket para aceitar conexões

    public Node(int port, String address, String folderPath) {
        this.port = port;
        this.address = address;
        this.folderPath = folderPath;
        this.connections = new ArrayList<Socket>();
        this.searchResults = new ArrayList<FileSearchResult>();

        for (int i = 0; i < numThreads; i++) {
            WordSearchMessage thread = new WordSearchMessage(this);
            searchThreads.add(thread);
            thread.start();
        }
    }

    //
    //
    // Métodos para criar e aceitar conexões
    //
    //
/* 
    public void startServer() throws IOException {
        NewConnectionRequest newConnection = new NewConnectionRequest(ServerSocket);
    }

    public void newConnection(String address, int port) throws IOException {
        NewConnectionRequest newConnection = new NewConnectionRequest(address, port);
        connections.add(newConnection.getSocket());
    }

*/


    //
    //
    // Métodos para pesquisar ficheiros - isto vai sair daqui e ir para downloadtasksmanager ig
    //
    //

    //ao clicar no botão "Procurar", a GUI chama este método
    //no futuro este método será chamado por mensagens
   synchronized public void sendSearchRequest(String keyword) {        
        for (WordSearchMessage thread : searchThreads) {
            thread.setKeyword(keyword);
        }
    }

    synchronized public void addSearchResult(FileSearchResult result) {
        for (FileSearchResult searchResult : searchResults) {
            if (searchResult.getNome().equals(result.getNome()) && searchResult.getEndereco().equals(result.getEndereco()) && searchResult.getPorta() == result.getPorta()) {
                System.err.println("Duplicate search result");
                return;
            }
        }
        searchResults.add(result);        
    }

    public void resetSearchResults() {
        searchResults.clear();
    }


    //
    //
    // Métodos para download de ficheiros
    //
    //
    public void sendDownloadRequest(FileSearchResult result) {
        DownloadTasksManager downloadManager = new DownloadTasksManager(result);
    }









    //getter methods
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
