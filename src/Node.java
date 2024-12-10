package src;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {
    private int port; // porta do nó
    private String address; // endereço do nó
    private String folderPath; // pasta com os ficheiros do nó
    private IscTorrentGUI GUI; // interface gráfica do nó
    // cenas para conexao entre nós
    private List<Socket> connections; // lista de sockets connectado
    private NodeServer server; // server dedicado ao nó - sempre À espera de receber conexões
    private Map<Socket, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>(); // qual o outputstream associado a cada socket/conexao
    private Map<String, Socket> connectionId = new ConcurrentHashMap<>(); // qual o socket associado a cada endereço:porta
    // Cenas para a pesquisa
    public List<FileSearchResult> searchResults = new ArrayList<FileSearchResult>(); // lista com os resultados dapesquisa (FSR cujo nome contém akeyword)
    public List<FileSearchResult> myFiles = new ArrayList<FileSearchResult>(); // lista com os ficheiros do nó

    // cenas para download
    private static final int BLOCK_SIZE = 10240; // tamanho max dum FBRM - Transformar FSR em FBRM
    // download de ficheiros
    private ExecutorService downloadThreadPool = Executors.newFixedThreadPool(5); // pool de threads para download
    private Map<String, DownloadTasksManager> dtmByFsrHash = new ConcurrentHashMap<String, DownloadTasksManager>(); // mapa
                                                                                                                    // de
                                                                                                                    // DownloadTasksManager
                                                                                                                    // por
                                                                                                                    // hash

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * CONSTRUTOR
     * 
     * Node
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    public Node(int port, String address, String folderPath) throws IOException {
        this.port = port;
        this.address = address;
        this.folderPath = folderPath;
        this.connections = new ArrayList<Socket>();
        this.searchResults = new ArrayList<FileSearchResult>();

        this.GUI = new IscTorrentGUI(this);
        GUI.open();

        this.server = new NodeServer(this);
        this.server.start();

    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * ENVIAR PEDIDO DE CONEXÃO // TERMINAR CONEXÃO
     * 
     * newConnection // stop // removeConnection
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    public void newConnection(String address1, int port1)  {
        if (address1.equals(this.address) && port1 == this.port) {
            GUI.connectToSelf();
            System.err.println("Node - " + folderPath + " - Não posso conectar-me a mim mesmo.");
            return;
        }
        try {
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
        } catch (IOException e) {
            GUI.failedToConnect(address1, port1);
            System.err.println("Node - " + folderPath + " - Erro ao conectar ao nó " + address1 + ":" + port1);
            e.printStackTrace();
        }
    }

    public void stop() {
        for (Socket connection : connections) {
            removeConnection(connection);
        }
    }

    public void removeConnection(Socket connection) {
        try {
            for (String key : connectionId.keySet()) {
                if (connectionId.get(key).equals(connection)) {
                    connectionId.remove(key);
                    // connectionId.remove(connection);
                    break;
                }
            }
            if (outputStreams.containsKey(connection)) {
                outputStreams.get(connection).close();
                outputStreams.remove(connection);
            }
            connections.remove(connection);
            if (!connection.isClosed()) {
                connection.close();
            }
            System.out.println("Node - " + folderPath + " - Conexão encerrada com node");
        } catch (IOException e) {
            System.err.println("Node - " + folderPath + " - Erro ao fechar a conexão com "
                    + connection.getInetAddress().getHostName() + ":" + connection.getPort());
            e.printStackTrace();
        }
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

            if (!connectionId.containsKey(connectionKey)) {
                System.err.println("ainda nao havia conexao");
                connections.add(clientSocket);
                connectionId.put(connectionKey, clientSocket);
                outputStreams.put(clientSocket, new ObjectOutputStream(clientSocket.getOutputStream()));
                newConnection(newConnectionRequest.getAddress(), newConnectionRequest.getPort());
            }
            System.err.println("Conexão já estabelecida com " + connectionKey);

        } else if (message instanceof WordSearchMessage) {
            WordSearchMessage searchMessage = (WordSearchMessage) message;
            System.out.println("Node - " + folderPath + " - Mensagem de busca recebida (" + searchMessage.getKeyword() + ")");

            // procurar no proprio node
            searchFiles(searchMessage);
            for (FileSearchResult result : searchResults) {
                sendMessage(clientSocket, result);
                System.out.println("Node - " + folderPath + " - Resultado de busca enviado: " + result.getNome());
            }

            // enviar para os nodes conectados -extra
            searchMessage.setSenderKey(this.address + ":" + this.port);

            List<String> connectionKey = searchMessage.getVisitedNodes();
            List<Socket> nodesToSend = new ArrayList<Socket>();
            for (String key : connectionId.keySet()) {
                if (!connectionKey.contains(key)) {
                    nodesToSend.add(connectionId.get(key));
                    searchMessage.addVisitedNode(key);
                }
            }
            for (Socket connection : nodesToSend) {
                System.out.println("Node - " + folderPath + " - mensagem de reencaminhamento de busca enviada ");
                sendMessage(connection, searchMessage);
            }

        } else if (message instanceof FileSearchResult) {
            FileSearchResult result = (FileSearchResult) message;
            System.out.println("Node - " + folderPath + " - Resultado de busca recebido: " + result.getNome()
                    + " - a atualizar a gui");

            WordSearchMessage search = result.getWordSearchMessage();
            // se o node for o que inicio a busca -> atualizar a gui
            if (search.getVisitedNodes().get(0).equals(this.address + ":" + this.port)) {
                updateSearchResults(result);
                // se nao for o node que inicio a busca -> tentar reencaminhar para o node que
                // iniciou a busca
                // se nao conhecer o node que iniciou a busca -> reencaminhar para o primeiro
                // node da lista de visitedNodes que conhecer
            } else {

                for (String key : search.getVisitedNodes()) {
                    if (connectionId.containsKey(key)) {
                        System.out.println(
                                "Node - " + folderPath + " - Reencaminhando o resultado de busca para o nó " + key);
                        Socket connection = connectionId.get(key);
                        sendMessage(connection, result);
                        return;
                    }

                }
            }
        } else if (message instanceof FileBlockRequestMessage) {
            FileBlockRequestMessage request = (FileBlockRequestMessage) message;
            System.out.println("Node - " + folderPath + " - Pedido de bloco de ficheiro recebido.");
            this.downloadThreadPool.submit(new AnwserSender(request, this));
        } else if (message instanceof FileBlockAnswerMessage) {
            System.out.println("Node - " + folderPath + " - Resposta de bloco de ficheiro recebida.");
            try {
                FileBlockAnswerMessage answer = (FileBlockAnswerMessage) message;
                String key = answer.getHash();
                dtmByFsrHash.get(key).putAnswerMessage(answer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (message instanceof String && message.equals("STOP")) {
            System.out.println("Node - " + folderPath + " - Pedido de encerramento de conexão recebido.");
            removeConnection(clientSocket);
        } else
            System.err.println("Recebi algo nao esperado");

    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Pesquisa de Ficheiros
     * 
     * startSearch // getfiles // searchFiles // updateSearchResults
     *************************************************************************
     *************************************************************************
     *************************************************************************/

    public void startSearch(String keyword) {
        searchResults.clear();
        
        String connectionKey = this.address + ":" + this.port;
        WordSearchMessage message = new WordSearchMessage(keyword, connectionKey);
        message.addVisitedNode(this.address + ":" + this.port);
        for (String key : connectionId.keySet()) {
            message.addVisitedNode(key);
        }
        for (Socket connection : connections) {
            sendMessage(connection, message);
        }
    }


    public void getFiles() {
        File[] files = new File(folderPath).listFiles((new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("mp3");
            }
        }));

        if (files != null) {
            for (File f : files) {
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

        for (FileSearchResult fsr : myFiles) {
            if (fsr.getNome().contains(message.getKeyword())) {
                fsr.setWordSearchMessage(message);
                System.out.println("Node - " + folderPath + " - Ficheiro encontrado: " + fsr.getNome());
                results.add(fsr);
            }
        }

        Socket connection = connectionId.get(message.getSenderKey());

        for (FileSearchResult result : results) {
            sendMessage(connection, result);
        }
        System.out.println("Node - " + folderPath + " - Resultados de busca enviados para o nó "+ connection.getInetAddress().getHostName() + ":" + connection.getPort());
    }

    public void updateSearchResults(FileSearchResult result) {
        if (searchResults.isEmpty()) {
            searchResults.add(result);
            System.out.println("Node - " + folderPath + " - Resultado de busca: " + result.getNome());
        } else {
            for (FileSearchResult fsr : searchResults) {
                if (fsr.getHash().equals(result.getHash())) {
                    fsr.addNode(result.getEndereco().get(0), result.getPorta().get(0));
                    return;
                }
            }
            searchResults.add(result);
            System.out.println("Node - " + folderPath + " - Resultado de busca: " + result.getNome());
        }
        GUI.updateSearchResults(searchResults);
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Download de Ficheiros
     * 
     * startDownload // RequestSender // AnwserSender
     *************************************************************************
     *************************************************************************
     *************************************************************************/

    public void startDownload(List<FileSearchResult> resultsList) {
        List<String> connectionKeys = new ArrayList<String>(); // todas as keys que ele conhece
        List<String> connectionKeysUnknow = new ArrayList<String>(); // todas as keys que ele nao conhece

        System.out.println("quero iniciar download madje");
        for (FileSearchResult fsr : resultsList) {
            for (int x = 0; x != fsr.getPorta().size(); x++) {
                if (connectionId.get(fsr.getEndereco().get(x) + ":" + fsr.getPorta().get(x)) != null) {
                    connectionKeys.add(fsr.getEndereco().get(x) + ":" + fsr.getPorta().get(x));
                } else {
                    connectionKeysUnknow.add(fsr.getEndereco().get(x) + ":" + fsr.getPorta().get(x));
                }
            }

            if (connectionKeys.size() > 0) { // se conhecer algum dos nodes
                if (connectionKeysUnknow.size() == 0) { //se conhecer todos os nodes

                    List<FileBlockRequestMessage> requestMessages = new ArrayList<FileBlockRequestMessage>();
                    for (int i = 0; i < fsr.getTamanho(); i += BLOCK_SIZE) {
                        requestMessages.add(new FileBlockRequestMessage(fsr.getHash(), i,
                                (int) Math.min(BLOCK_SIZE, fsr.getTamanho() - i), this.address, this.port));
                    }

                    dtmByFsrHash.put(fsr.getHash(), new DownloadTasksManager(requestMessages, this));

                    Thread[] RequestSenders = new Thread[fsr.getEndereco().size()];

                    for (int i = 0; i < fsr.getEndereco().size(); i++) {
                        RequestSenders[i] = new RequestSender(fsr.getEndereco().get(i), fsr.getPorta().get(i),
                                dtmByFsrHash.get(fsr.getHash()), this);
                        RequestSenders[i].start();
                    }
                } else { //se nao conhecer todos os nodes
                    boolean stop = GUI.chooseToConnect(connectionKeysUnknow);
                    if(!stop){
                        List<FileBlockRequestMessage> requestMessages = new ArrayList<FileBlockRequestMessage>();
                        for (int i = 0; i < fsr.getTamanho(); i += BLOCK_SIZE) {
                            requestMessages.add(new FileBlockRequestMessage(fsr.getHash(), i,(int) Math.min(BLOCK_SIZE, fsr.getTamanho() - i), this.address, this.port));
                        }
                        dtmByFsrHash.put(fsr.getHash(), new DownloadTasksManager(requestMessages, this));
                        Thread[] RequestSenders = new Thread[connectionKeys.size()];
                        for (int i = 0; i < connectionKeys.size(); i++) {
                            String address = connectionKeys.get(i).split(":")[0];
                            int port = Integer.parseInt(connectionKeys.get(i).split(":")[1]);
                            RequestSenders[i] = new RequestSender(address, port, dtmByFsrHash.get(fsr.getHash()), this);
                            RequestSenders[i].start();
                        }
                    }

                }
            } else { //se nao conhecer nenhum dos nodes
                GUI.needToConnect(connectionKeysUnknow); 
            }

        }

    }

    private static class RequestSender extends Thread {
        private String address;
        private int port;
        private DownloadTasksManager dtm;
        private Node node;
        private CountDownLatch latch;

        public RequestSender(String address, int port, DownloadTasksManager dtm, Node node) {
            this.address = address;
            this.port = port;
            this.dtm = dtm;
            this.node = node;
            this.latch = new CountDownLatch(dtm.getFileBlockRequestMessages().size());
        }

        @Override
        public synchronized void run() {
            while (latch.getCount() > 0) {
                try {
                    Socket connection = node.connectionId.get(address + ":" + port);
                    FileBlockRequestMessage request = dtm.takeRequestMessage();
                    dtm.addRequestCounter(address + ":" + port);
                    node.sendMessage(connection, request);
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            System.err.println("acabei de enviar esta merda toda");
        }
    }

    private static class AnwserSender extends Thread {

        private FileBlockRequestMessage request;
        private Node node;

        public AnwserSender(FileBlockRequestMessage request, Node node) {
            this.request = request;
            this.node = node;
        }

        @Override
        public synchronized void run() {
            FileBlockAnswerMessage answer = fbrmToFbam(request);
            Socket connection = node.connectionId.get(request.getRequestAddress() + ":" + request.getRequestPort());
            node.sendMessage(connection, answer);
        }

        public FileBlockAnswerMessage fbrmToFbam(FileBlockRequestMessage fbrm) {
            try {
                // em principio esta merda consegue arranjar o data do ficheiro
                byte[] data = new byte[fbrm.getLength()];

                for (FileSearchResult fsr : node.myFiles) {
                    if (fsr.getHash().equals(fbrm.getHash())) {
                        RandomAccessFile originFile = new RandomAccessFile(node.folderPath + "/" + fsr.getNome(), "r");
                        // System.out.println("Node - " + node.folderPath + " - tou a fazer um
                        // answerBlock de : " + fsr.getNome());
                        originFile.seek(fbrm.getOffset());
                        originFile.read(data, 0, fbrm.getLength());
                        originFile.close();
                        FileBlockAnswerMessage fbam = new FileBlockAnswerMessage(fbrm.getHash(), fbrm.getOffset(),
                                fbrm.getLength(), data);
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
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Getters
     * 
     * 
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

    public IscTorrentGUI getGUI() {
        return GUI;
    }

}