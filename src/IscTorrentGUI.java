package src;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/*
 * Objetivo: Esta classe gerencia a interface gráfica do usuário, encapsulando todos os elementos e funcionalidades da GUI do sistema.
 * 
 * Ecra principal:
 * 2 paineis principais:
 *      1. Painel de pesquisa
 *         - JLABEL: "Texto a procurar:"
 *         - Campo de texto para inserir a palavra-chave    
 *         - Botão "Procurar"
 *      2. Painel de resultados + botões
 *         - 2 paineis secundários:
 *              1. Painel de resultados
 *                  - JList: Lista com os resultados da pesquisa (nome do ficheiro, e nós que possuiem o ficheiro)
 *              2. Painel de botoes
 *                  - Botão "Descarregar"
 *                  - Botão "Ligar a nó"
 * 
 * Ecra secundario (após clicar em "Ligar a nó"):
 * 1 painel:
 *     - JLABEL: "Endereço:"
 *     - Campo de texto para inserir o endereço do nó
 *     - JLABEL: "Porta:"
 *     - Campo de texto para inserir a porta do nó
 *     - Botão "Cancelar"
 *     - Botão "OK"
 * 
 */
public class IscTorrentGUI {
    private Node node; // Nó ao qual a GUI está conectada

    private JFrame mainFrame; // Janela principal (dividida em paineis)
    private JTextField searchKeyword; // Palavra-chave a ser pesquisada
    private JList<FileSearchResult> resultList; // Lista de resultados da pesquisa - confirmar se é este objeto

    private JFrame connectFrame; // Janela de conexão com um nó
    private JTextField addressField; // Endereço do nó
    private JTextField portField; // Porta do nó

    public IscTorrentGUI(Node node) {

        this.node = node;
        // Inicializa a janela principal
        this.mainFrame = new JFrame(node.getFolderPath()); // nome da janela principal é o caminho para a pasta onde
                                                           // estão as músicas
        this.mainFrame.setLayout(new BorderLayout()); // border layout para dividir a janela em 2 paineis (tamanho
                                                      // variável)
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha o programa quando a janela principal é
                                                                       // fechada
        addMainFrameContent(); // Adiciona os elementos da janela principal

        // Inicializa a janela de conexão com um nó
        this.connectFrame = new JFrame("Connect");
        this.connectFrame.setLayout(new BorderLayout());
        this.connectFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addConnectFrameContent();
        this.connectFrame.pack();
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Ecrã principal
     * 
     * pesquisar palavra-chave / escolher ficheiro para descarregar / abrir ecrã
     * secundário
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    private void addMainFrameContent() {
        // Formatar a janela principal
        mainFrame.setSize(800, 400);
        mainFrame.setResizable(false);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize(); // centrar a janela
        mainFrame.setLocation(dimension.width / 2 - mainFrame.getSize().width / 2, dimension.height / 2 - mainFrame.getSize().height / 2);

        // Adiciona os elementos da janela principal
        // Painel de pesquisa
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new GridLayout(1, 3)); // grid layout para alinhar os elementos horizontalmente - mesmo
                                                     // tamanho as 3 colunas
        mainFrame.add(searchPanel, BorderLayout.NORTH);
        // Painel de resultados + botões
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout()); // border layout para dividir a lista de resultados e os botões
        mainFrame.add(resultPanel, BorderLayout.CENTER);

        // Adiciona os elementos do painel de pesquisa
        JLabel searchLabel = new JLabel("  Texto a procurar:");
        searchPanel.add(searchLabel, BorderLayout.WEST);
        // Campo de texto para inserir a palavra-chave
        this.searchKeyword = new JTextField();
        searchPanel.add(searchKeyword, BorderLayout.CENTER);
        // Botão "Procurar"
        JButton searchButton = new JButton("Procurar");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // método para pesquisar a palavra-chave -ig que é isto mas not sure
                if (searchKeyword.getText().isEmpty())
                    JOptionPane.showMessageDialog(null, "Insira uma palavra-chave!", "Erro", JOptionPane.ERROR_MESSAGE);
                else {
                    node.startSearch(searchKeyword.getText()); // enviar a palavra-chave para o nó
                }
            }
        });
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Adiciona os elementos do painel de resultados + botões
        // Painel de resultados
        resultList = new JList<FileSearchResult>(); // confirmar se é este tipo de objeto

        resultPanel.add(resultList, BorderLayout.CENTER);
        // Painel de botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1)); // box layout para alinhar os botões verticalmente
        resultPanel.add(buttonPanel, BorderLayout.EAST);
        // Botão "Descarregar"
        JButton downloadButton = new JButton("Descarregar");
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resultList.isSelectionEmpty())
                    JOptionPane.showMessageDialog(null, "Não está nenhum item selecionado!", "Erro",JOptionPane.ERROR_MESSAGE);
                else {
                    List<FileSearchResult> fsr = new ArrayList<FileSearchResult>();
                    fsr.addAll(resultList.getSelectedValuesList());
                    node.startDownload(fsr);
                }
            }
        });
        buttonPanel.add(downloadButton);
        // Botão "Ligar a nó"
        JButton connectButton = new JButton("Ligar a nó");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectFrame.setVisible(true);
            }
        });
        buttonPanel.add(connectButton);
    }

    /**************************************************************************
     **************************************************************************
     ************************************************************************** 
     * 
     * Ecrã secundário
     * 
     * inserir endereço e porta do nó / ligar a nó / cancelar
     *************************************************************************
     *************************************************************************
     *************************************************************************/
    private void addConnectFrameContent() {
        connectFrame.setSize(800, 100);
        connectFrame.setResizable(false);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize(); // centrar a janela
        connectFrame.setLocation(dimension.width / 2 - connectFrame.getSize().width / 2 + 150,
                dimension.height / 2 + mainFrame.getSize().height / 2 + 15);

        // Adiciona os elementos da janela de conexão com um nó
        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new GridLayout(1, 6)); // grid layout para alinhar os elementos horizontalmente - mesmo tamanho as 4 colunas
        connectFrame.add(connectPanel, BorderLayout.CENTER);
        JLabel addressLabel = new JLabel("  Endereço:");
        connectPanel.add(addressLabel);
        this.addressField = new JTextField();
        connectPanel.add(addressField);
        JLabel portLabel = new JLabel("  Porta:");
        connectPanel.add(portLabel);
        this.portField = new JTextField();
        connectPanel.add(portField);
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressField.setText("");
                portField.setText("");
                connectFrame.setVisible(false);
            }
        });
        connectPanel.add(cancelButton);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addressField.getText().isEmpty() || portField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Insira o endereço e a porta do nó!", "Erro",JOptionPane.ERROR_MESSAGE);
                } else {
                    node.newConnection(addressField.getText(), Integer.parseInt(portField.getText())); // ligar ao nó
                    addressField.setText("");
                    portField.setText("");
                    connectFrame.setVisible(false);
                    
                }
            }
        });
        connectPanel.add(okButton);

    }


    /*
     * 
     * 
     * 
     * Error/Success Messages
     * 
     * 
     * 
     */


    public void updateSearchResults(List<FileSearchResult> searchResults) {
        resultList.setListData(searchResults.toArray(new FileSearchResult[0]));
    }

    public void downloadFinished(Map<String, Integer> downloadResults, long time) {
        String message ="Descarga completa. \n";
        for (Map.Entry<String, Integer> entry : downloadResults.entrySet()) {
            message += "Fornecedor [Node = "+entry.getKey() + " || Blocos enviados = " + entry.getValue()+" ]\n";
        }

        message += "Tempo decorrido: " + time + " segundos";
        JOptionPane.showMessageDialog(null,message, "Download Completo",JOptionPane.INFORMATION_MESSAGE);  
    }

    public void needToConnect(List<String> connectionKeyUnknow) {
        String message = "Para realizar o download necessita de se connectar a: \n";
        for (String key : connectionKeyUnknow) {
            message += "Node : "+key+"\n";
        }
        String[] options = {"Connectar ao(s) nó(s)", "Cancelar"};
        int answer = JOptionPane.showOptionDialog(null, message , "Problema ao iniciar download", 0, 3, null, options, JOptionPane.QUESTION_MESSAGE);
        
        if(answer == 0){
            for (String key : connectionKeyUnknow) {
                String address = key.split(":")[0];
                int port = Integer.parseInt(key.split(":")[1]);
                node.newConnection(address, port);   
            }
            List<FileSearchResult> fsr = new ArrayList<FileSearchResult>();
            fsr.addAll(resultList.getSelectedValuesList());
            node.startDownload(fsr);
        }
        
    }

    public boolean chooseToConnect(List <String> connectionKeyUnknow) {
        boolean stop = true;

        String message = "Prefere realizar o ficheiro apenas com os nós conhecidos ou deseja ligar aos nós? \n";
        for (String key : connectionKeyUnknow) {
            message += "Node : "+key+"\n";
        }
        String[] options = {"Apenas nós conhecidos", "Ligar ao(s) nó(s)", "Cancelar"};
        int answer = JOptionPane.showOptionDialog(null, message , "Problema ao iniciar download", 0, 3, null, options, JOptionPane.QUESTION_MESSAGE);
        if(answer == 0){
            stop = false;
        } else if(answer == 1){
            for (String key : connectionKeyUnknow) {
                String address = key.split(":")[0];
                int port = Integer.parseInt(key.split(":")[1]);
                node.newConnection(address, port);   
            }
            List<FileSearchResult> fsr = new ArrayList<FileSearchResult>();
            fsr.addAll(resultList.getSelectedValuesList());
            node.startDownload(fsr);
        } 
        return stop;
        
    }

    public void connectToSelf() {
        JOptionPane.showMessageDialog(null, "Não é possível ligar a si mesmo!", "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void failedToConnect(String address, int port) {
        JOptionPane.showMessageDialog(null, "Falha ao ligar ao nó: "+address+":"+port+" !", "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public void open() {
        mainFrame.setVisible(true);
        connectFrame.setVisible(false);
    }
}
