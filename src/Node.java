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
    private List <Socket> connections;
    private List <WordSearchMessage> searchThreads = new ArrayList<WordSearchMessage>();
    private List <FileSearchResult> searchResults = new ArrayList<FileSearchResult>();
    private int numThreads = 3;

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

    public void acceptConnections(ServerSocket socket) throws IOException {
        while (true) {
            Socket connection = socket.accept();
            connections.add(connection);
            System.out.println("Connection received from: " + connection.getInetAddress().getHostName());
        }
    }

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
