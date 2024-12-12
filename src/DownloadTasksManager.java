package src;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/*  Pagina 7 e 8 do enunciado
 *  Objetivo: Gerir e coordenar as tarefas de descarregamento de blocos de ficheiros.
    Atributos/Métodos: Deve coordenar múltiplas threads de descarregamento. Gerencia a lista de blocos a descarregar, e controla o envio de novos pedidos apenas após a recepção do bloco anterior.
 */

public class DownloadTasksManager implements Serializable {

   // private int numBlocksSent = 0; //numero de blocos enviados
   // private int numBlocksReceived = 0; //numero de blocos recebidos

   private List<FileBlockRequestMessage> fileBlockRequestMessages; // lista onde vão ser adicionados os pedidos que ainda vão ser processados
   private List<FileBlockAnswerMessage> fileBlockAnswerMessages; // lista onde vão ser adicionadas as respostas 
   private Node requesterNode; // nó que iniciou o pedido de download
   private Map<String, Integer> requestCounter ; // número de pedidos feitos para cada bloco

   private Lock lock = new ReentrantLock(); // lock para garantir a sincronização entre threads
   private Condition requestNotEmpty = lock.newCondition(); // condição para agir: notEmpty - necessário usar para avisar o consumidor quando a lista não está vazia
   private Condition answerNotEmpty= lock.newCondition(); // condição para agir: notEmpty - necessário usar para avisar o consumidor quando a lista não está vazia
   
   public DownloadTasksManager(List<FileBlockRequestMessage> fileBlockRequestMessages, Node requesterNode) {
      this.fileBlockRequestMessages = new LinkedList<FileBlockRequestMessage>();
      this.fileBlockRequestMessages.addAll(fileBlockRequestMessages);
      this.fileBlockAnswerMessages = new LinkedList<FileBlockAnswerMessage>();
      this.requesterNode = requesterNode;
      requestCounter = new ConcurrentHashMap<String, Integer>();
      Downloader downloader = new Downloader(this);
      downloader.start();
   }

   public void putRequestMessage(FileBlockRequestMessage fbrm) throws InterruptedException {
      lock.lock();
      try{
         fileBlockRequestMessages.add(fbrm);
         requestNotEmpty.signalAll();
      } finally {
         lock.unlock();
      }
   }

   public FileBlockRequestMessage takeRequestMessage() throws InterruptedException {
      lock.lock();
      try{
         while(fileBlockRequestMessages.isEmpty()){
            requestNotEmpty.await();
         }
         FileBlockRequestMessage fbrm = fileBlockRequestMessages.remove(0);
         return fbrm;
      } finally {
         lock.unlock();
      }
   }

   public void putAnswerMessage(FileBlockAnswerMessage fbam) throws InterruptedException {
      lock.lock();
      try{
         System.out.println("DownloadTasksManager: putAnswerMessage");
         fileBlockAnswerMessages.add(fbam);
         answerNotEmpty.signalAll();
      } finally {
         lock.unlock();
      }
   }

   public FileBlockAnswerMessage takeAnswerMessage() throws InterruptedException {
      lock.lock();
      try{
         while(fileBlockAnswerMessages.isEmpty()){
            answerNotEmpty.await();
         }
         FileBlockAnswerMessage fbam = fileBlockAnswerMessages.remove(0);
         return fbam;
      } finally {
         lock.unlock();
      }
      
   }

   public List<FileBlockRequestMessage> getFileBlockRequestMessages() {
      return fileBlockRequestMessages;
   }

   public List<FileBlockAnswerMessage> getFileBlockAnswerMessages() {
      return fileBlockAnswerMessages;
   }

   public void addRequestCounter(String key) {
      if(requestCounter.containsKey(key)){
         requestCounter.put(key, requestCounter.get(key) + 1);
      } else {
         requestCounter.put(key, 1);
      }
   }

   private static class Downloader extends Thread { 

      private DownloadTasksManager downloadTasksManager;
      private List<FileBlockAnswerMessage> fileBlockAnswerMessages;
      private List<FileBlockRequestMessage> fileBlockRequestMessages;
      private Node node;
      private long startTime;
      private long endTime;
      
      private CountDownLatch latch;

      // classe interna
      public Downloader(DownloadTasksManager dtm) {
         this.downloadTasksManager = dtm;
         this.fileBlockAnswerMessages = new ArrayList<FileBlockAnswerMessage>();
         this.fileBlockRequestMessages = new ArrayList<FileBlockRequestMessage>();
         this.fileBlockRequestMessages.addAll(dtm.getFileBlockRequestMessages());
         this.node = dtm.requesterNode;
         this.latch = new CountDownLatch(fileBlockRequestMessages.size());
      }

      @Override
      public void run() {
         try{
            startTime = System.currentTimeMillis();
            while(latch.getCount() > 0){
               fileBlockAnswerMessages.add(downloadTasksManager.takeAnswerMessage());
               latch.countDown();
            }
            orderAnswerBlocks();
            createFile();
         }catch(InterruptedException e){
            e.printStackTrace();
         } finally {
            endTime = System.currentTimeMillis();
            node.getGUI().downloadFinished(downloadTasksManager.requestCounter, (endTime - startTime)/1000);
         }
      }

      // ordena a lista de respostas por ordem de bloco a partir do offset
      public void orderAnswerBlocks (){
         FileBlockAnswerMessage temp;
         for (int i=fileBlockAnswerMessages.size()-1;i!=0;i--){
            for (int j=0; j!=i;j++){
               if (fileBlockAnswerMessages.get(j).getOffset() > fileBlockAnswerMessages.get(j+1).getOffset()){
                  temp = fileBlockAnswerMessages.get(j);
                  fileBlockAnswerMessages.set(j, fileBlockAnswerMessages.get(j+1));
                  fileBlockAnswerMessages.set(j+1, temp);
               }
            }
         }
      }

      // cria o ficheiro final a partir dos blocos recebidos na ordem correta e guarda o ficheiro no caminho indicado
      public void createFile() {
         try {
            String nome = "";
            for (FileSearchResult fsr : node.getSearchResults()) {
               if (fsr.getHash().equals(fileBlockRequestMessages.get(0).getHash())) {
                  System.out.println("DownloadTasksManager: createFile: fsr.getNome(): " + fsr.getNome());
                  nome = fsr.getNome();
                  break;
               }
            }
            FileOutputStream file = new FileOutputStream(node.getFolderPath() + "/" + nome);
            for (FileBlockAnswerMessage fbam : fileBlockAnswerMessages) {
               file.write(fbam.getData());
            }
            file.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

}
