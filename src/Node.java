package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private int port; // porta do nó
    private String address; // endereço do nó
    private String folderPath; // pasta com os ficheiros do nó

    //cenas para conexao entre nós
    private List<Socket> connections; // lista de sockets connectado
    private NodeServer server; // server dedicado ao nó - sempre À espera de receber conexões
    private Map<Socket, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>(); //qual o outputstream associado a cada socket/conexao
    private Map<String, Socket> connectionId = new ConcurrentHashMap<>(); //qual o socket associado a cada endereço:porta


    // Cenas para a pesquisa
    public List<FileSearchResult> searchResults = new ArrayList<FileSearchResult>(); //lista com os resultados da pesquisa (FSR cujo nome contém a keyword)
    public List<FileSearchResult> myFiles = new ArrayList<FileSearchResult>(); //lista com os ficheiros do nó

    // cenas para download
    private static final int BLOCK_SIZE = 1024; //tamanho max dum FBRM - Transformar FSR em FBRM
    private DownloadTasksManager dtm; //objeto partilhado entre nodes para download de ficheiros

   
    private Map<FileSearchResult, DownloadTasksManager> dtmByFsrHash = new ConcurrentHashMap<>(); //TODO PASSAR FILESEARCHRESULT PARA HASH
    

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * CONSTRUTOR
     * 
     * Node // Node
     *************************************************************************
     *************************************************************************
     *************************************************************************/
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
     * CRIAR E ACEITAR CONEXÕES
     * 
     * newConnection // addConnection
     *************************************************************************
     *************************************************************************
     *************************************************************************/

    /*public void newConnection(String address1, int port1) throws IOException {
        System.out.println("Node - " + folderPath + " - A tentar connectar ao nó " + address1 + ":" + port1);
        Socket connection = new Socket(address1, port1);
        ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        
        
        outputStreams.put(connection, output);
        // ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
        connections.add(connection);
        System.out.println("CARALHO" + address1 + ":" + port1);
        connectionId.put(address1 + ":" + port1, connection);
        System.out.println("Node - " + folderPath + " - Conexão estabelecida com " + address1 + ":" + port1);
        
        
        sendMessage(connection, new NewConnectionRequest(this.address, this.port));
    }*/
    public void newConnection(String address1, int port1) throws IOException {

        System.out.println("Node - " + folderPath + " - A tentar connectar ao nó " + address1 + ":" + port1);
        
        Socket connection = new Socket(address1, port1);
        
        ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        outputStreams.put(connection, output);
        
        connections.add(connection);
        System.out.println("CARALHO" + address1 + ":" + port1);

        connectionId.put(address1 + ":" + port1, connection);
        System.out.println("Node - " + folderPath + " - Conexão estabelecida com " + address1 + ":" + port1);
        
        sendMessage(connection, new NewConnectionRequest(this.address, this.port));
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * TROCA MENSAGENS
     * 
     * sendMessage // handleMessage
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    public synchronized void sendMessage(Socket connection, Object message) {
        try {
            ObjectOutputStream output = outputStreams.get(connection);
            if (output == null) {
                output = new ObjectOutputStream(connection.getOutputStream());
                output.flush();
                outputStreams.put(connection, output);
            }
            output.writeObject(message);
            output.flush();
            System.out.println("Node - " + folderPath + " - Mensagem enviada para o nó "
                    + connection.getInetAddress().getHostName() + ":" + connection.getPort());
        } catch (IOException e) {
            System.err.println("Node - " + folderPath + " - Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleMessage(Socket clientSocket, Object message) throws IOException {
        if (message instanceof NewConnectionRequest) {

            NewConnectionRequest newConnectionRequest = (NewConnectionRequest) message;
            System.out.println("Node - " + folderPath + " - Pedido de conexão recebido de "+ newConnectionRequest.getAddress() + ":" + newConnectionRequest.getPort());

            String connectionKey = newConnectionRequest.getAddress() + ":" + newConnectionRequest.getPort();

            if(!connectionId.containsKey(connectionKey)){ 
                System.err.println("ainda nao havia conexao");
                connections.add(clientSocket);
                connectionId.put(connectionKey, clientSocket);
                outputStreams.put(clientSocket, new ObjectOutputStream(clientSocket.getOutputStream()));
                newConnection(newConnectionRequest.getAddress(), newConnectionRequest.getPort());

            }
            System.err.println("Conexão já estabelecida com " +connectionKey);


        } else if (message instanceof WordSearchMessage) {
            WordSearchMessage searchMessage = (WordSearchMessage) message;
            System.out.println(
                    "Node - " + folderPath + " - Mensagem de busca recebida (" + searchMessage.getKeyword() + ")");
            searchFiles(searchMessage);
            for (FileSearchResult result : searchResults) {
                sendMessage(clientSocket, result);
                System.out.println("Node - " + folderPath + " - Resultado de busca enviado: " + result.getNome());
            }
        } else if (message instanceof FileSearchResult) {
            FileSearchResult result = (FileSearchResult) message;
            System.out.println("Node - " + folderPath + " - Resultado de busca recebido: " + result.getNome()+ " - a atualizar a gui");
            updateSearchResults(result);
            
        }else if (message instanceof DownloadTasksManager) {
            System.out.println("Node - " + folderPath + " - DownloadTasksManager recebido.");
            this.dtm = (DownloadTasksManager) message;

            System.out.println("ESTOU A INICIAR DOWNLOAD MADJE.");
            dtmByFsrHash.put(dtm.getFsr(), dtm);
            DtmHandler dtmHandler = new DtmHandler(this, dtm, null);
            dtmHandler.start();
        }else
            System.err.println("ESTOU A RECEBER UMA MERDA ATOA");
            
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Pesquisa de Ficheiros
     * 
     * startSearch // searchFiles // updateSearchResults
     *************************************************************************
     *************************************************************************
     *************************************************************************/

    synchronized public void startSearch(String keyword) {
        searchResults.clear();

        WordSearchMessage message = new WordSearchMessage(keyword, this.address, this.port);
        for (Socket connection : connections) {
            sendMessage(connection, message);
        }
        System.out.println("Node - " + folderPath + " - Mensagem de busca enviada (keyword: " + keyword + ")");
    }

    public void getFiles(){
        File[] files = new File(folderPath).listFiles((new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("mp3");
            }
        }));

        if( files != null){
            for (File f : files){
                System.out.println("Node - " + folderPath + " - Tenho este ficheiro: " + f.getName());
                FileSearchResult result = new FileSearchResult(null, f.length(), f.getName(), this.address, this.port);
                myFiles.add(result);
            }
        }
    }
    public void searchFiles(WordSearchMessage message) {
        // criar uma lista para guardar os resultados da pesquisa
        List<FileSearchResult> results = new ArrayList<FileSearchResult>();
        // criar um array de ficheiros (File[]) com os ficheiros da pasta do node
        // (filtrados por .mp3 e que contenham a keyword)
        getFiles();

        for (FileSearchResult fsr : myFiles){
            if(fsr.getNome().contains(message.getKeyword())){
                fsr.setWordSearchMessage(message);
                System.out.println("Node - " + folderPath + " - Ficheiro encontrado: " + fsr.getNome());
                results.add(fsr);
            }
        }

        System.out.println("Node - " + folderPath + " - Pesquisa de ficheiros concluída.");
        Socket connection = connectionId.get(message.getOriginAddress() + ":" + message.getOriginPort());
        
        for (FileSearchResult result : results) {
            sendMessage(connection, result);
        }
        System.out.println("Node - " + folderPath + " - Resultados de busca enviados para o nó "+ connection.getInetAddress().getHostName() + ":" + connection.getPort());
    }

    public void updateSearchResults(FileSearchResult result) {
        if(searchResults.isEmpty()){
            searchResults.add(result);
            System.out.println("Node - " + folderPath + " - Resultado de busca: " + result.getNome());
            return;
        }else{
            for (FileSearchResult fsr : searchResults) {
                if(fsr.getHash().equals(result.getHash())){
                    return;
                }
            }
            searchResults.add(result);
            System.out.println("Node - " + folderPath + " - Resultado de busca: " + result.getNome());

        }

    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Download de Ficheiros - por fazer
     * 
     * sendDownloadRequest
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    /*
     * public void sendDownloadRequest(FileSearchResult result) {
     * DownloadTasksManager downloadManager = new DownloadTasksManager(result);
     * }
     */
    public void startDownload(FileSearchResult fsr) {
        System.out.println("quero iniciar download madje");
        this.dtm = new DownloadTasksManager(fsr);
        dtmByFsrHash.put(fsr, dtm);

        List<FileBlockRequestMessage> requestMessages = fsrToFbrm(fsr);
        /*for (Socket connection : connections) {
            sendMessage(connection, dtm);
        }*/
        DtmHandler dtmHandler = new DtmHandler(this, dtm, requestMessages);
        dtmHandler.start();

    }

    // esta funcão divide o fileSearchResult em blocos de tamanho BLOCK_SIZE e cria
    // um FileBlockRequestMessage para cada bloco
    public synchronized List<FileBlockRequestMessage> fsrToFbrm(FileSearchResult fsr) {
        List<FileBlockRequestMessage> requestMessages = new ArrayList<FileBlockRequestMessage>();
        for (int i = 0; i < fsr.getTamanho(); i += BLOCK_SIZE) {
            // requestMessages.add(new FileBlockRequestMessage(this.fsr.getHash(), i, (int)
            // Math.min(i + BLOCK_SIZE, this.fsr.getTamanho()) - i, this.fsr));
            requestMessages.add(new FileBlockRequestMessage(fsr.getHash(), i,(int) Math.min(BLOCK_SIZE, fsr.getTamanho() - i)));
        }
        return requestMessages;
    }

    
    public static class DtmHandler extends Thread {
        private Node node;
        private DownloadTasksManager dtm;
        private int numBlocksReceived = 0;
        private int numBlocksSent = 0;
        private List<FileBlockRequestMessage> requestList = new ArrayList<FileBlockRequestMessage>();
        private List<FileBlockAnswerMessage> answerList = new ArrayList<FileBlockAnswerMessage>();

        public DtmHandler(Node node, DownloadTasksManager dtm, List<FileBlockRequestMessage> requestList) {
            this.node = node;
            this.dtm = dtm;
            
            if (requestList != null)
                this.requestList.addAll(requestList);

        }

        @Override
        public void run() {
            try {
                System.out.println("Node - " + node.folderPath + " - DtmHandler iniciado.");
                while (true) {
                    // se for iniciado como request
                    if (!this.requestList.isEmpty()) {
                        // adiciona todos os pedidos à lista de pedidos do dtm
                        for (FileBlockRequestMessage fbrm : requestList) {
                            System.out.println("Node - " + node.folderPath + " - Pedido de bloco de ficheiro adicionado ao dtm.");
                            dtm.putRequestMessage(fbrm);
                            this.numBlocksSent++;
                        }
                        //envia o dtm para todos os nós conectados
                        for (Socket connection : node.connections) {
                            node.sendMessage(connection, dtm);
                        }

                        while (numBlocksReceived < numBlocksSent) {
                            System.out.println("Node - " + node.folderPath + " - Resposta de bloco de ficheiro recebida.");
                            FileBlockAnswerMessage fbam = dtm.takeAnswerMessage();
                            answerList.add(fbam);
                            this.numBlocksReceived++;   
                        }

                        // ordenar a lista
                        System.out.println("Node - " + node.folderPath + " - A ordenar a lista de respostas.");
                        orderMessages(answerList);
                        // criar ficheiro
                        createFile();

                        answerList.clear();
                        requestList.clear();

                        // se for iniciado como answer
                    } else if (this.requestList.isEmpty()) {
                        // vai percorrer todos os request do dtm
                        for (int i= 0; i!=dtm.getFileBlockRequestMessages().size(); i++) {
                            // transforma o request em answer
                            FileBlockRequestMessage request = dtm.takeRequestMessage();
                            FileBlockAnswerMessage fbam = fbrmToFbam(request);
                            // adiciona o answer à lista de respostas do dtm
                            if (fbam != null) {
                                dtm.putAnswerMessage(fbam);
                            }
                        }
                        System.out.println("Node - " + node.folderPath + " - Respostas de bloco de ficheiro enviadas. TODAS.");
                        
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        public FileBlockAnswerMessage fbrmToFbam(FileBlockRequestMessage fbrm) {
            try {
            // em principio esta merda consegue arranjar o data do ficheiro
                byte[] data = new byte[fbrm.getLength()];
            
                for (FileSearchResult fsr : node.myFiles){
                    if(fsr.getHash().equals(fbrm.getHash())){
                        RandomAccessFile originFile = new RandomAccessFile(node.folderPath + "/" + fsr.getNome(), "r");
                        System.out.println("Node - " + node.folderPath + " - tou a fazer um answerBlock de : " + fsr.getNome());
                        originFile.seek(fbrm.getOffset());
                        originFile.read(data, 0, fbrm.getLength());
                        originFile.close();
                        FileBlockAnswerMessage fbam = new FileBlockAnswerMessage(fbrm.getHash(), fbrm.getOffset(), fbrm.getLength(),data);
                        return fbam;
                    }
                }
                System.err.println("Node - " + node.folderPath + " - Não foi possível encontrar o ficheiro.");
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // ordena a lista de respostas por ordem de bloco
        public void orderMessages(List<FileBlockAnswerMessage> answerList) {
            answerList.sort(Comparator.comparing(FileBlockAnswerMessage::getOffset));
        }

        // cria o ficheiro a partir dos blocos recebidos
        public synchronized void createFile() {
            try {
                FileOutputStream file = new FileOutputStream(node.getFolderPath() + "/" + dtm.getFsr().getNome());
                for (FileBlockAnswerMessage fbam : answerList) {
                    file.write(fbam.getData());
                }
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Getters e Setters
     * 
     * getSearchResults // getPort // getAddress // getFolderPath
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

    public Map<Socket, ObjectOutputStream> getOutputStreams() {
        return outputStreams;
    }

    public Map<String, Socket> getConnectionId() {
        return connectionId;
    }


}