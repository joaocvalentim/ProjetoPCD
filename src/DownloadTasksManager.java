package src;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*  Pagina 7 e 8 do enunciado
 *  Objetivo: Gerir e coordenar as tarefas de descarregamento de blocos de ficheiros.
    Atributos/Métodos: Deve coordenar múltiplas threads de descarregamento. Gerencia a lista de blocos a descarregar, e controla o envio de novos pedidos apenas após a recepção do bloco anterior.
 */

public class DownloadTasksManager implements Serializable {

   // private int numBlocksSent = 0; //numero de blocos enviados
   // private int numBlocksReceived = 0; //numero de blocos recebidos

   private BlockingQueue<FileBlockRequestMessage> fileBlockRequestMessages; // lista onde vão ser adicionados os pedidos
   private BlockingQueue<FileBlockAnswerMessage> fileBlockAnswerMessages; // lista onde vão ser adicionadas as respostas
   private Node requesterNode;

   public DownloadTasksManager(List<FileBlockRequestMessage> fileBlockRequestMessages, Node requesterNode) {
      this.fileBlockRequestMessages = new LinkedBlockingQueue<FileBlockRequestMessage>();
      this.fileBlockRequestMessages.addAll(fileBlockRequestMessages);
      this.fileBlockAnswerMessages = new LinkedBlockingQueue<FileBlockAnswerMessage>();
      this.requesterNode = requesterNode;

      Downloader downloader = new Downloader(this);
      downloader.start();
   }

   public void putRequestMessage(FileBlockRequestMessage fbrm) throws InterruptedException {
      fileBlockRequestMessages.put(fbrm);
      // numBlocksSent++;
   }

   public FileBlockRequestMessage takeRequestMessage() throws InterruptedException {
      FileBlockRequestMessage fbrm = fileBlockRequestMessages.take();
      return fbrm;
   }

   public void putAnswerMessage(FileBlockAnswerMessage fbam) throws InterruptedException {
      System.out.println("DownloadTasksManager: putAnswerMessage");
      fileBlockAnswerMessages.put(fbam);
      // numBlocksReceived++;
   }

   public FileBlockAnswerMessage takeAnswerMessage() throws InterruptedException {
      FileBlockAnswerMessage fbam = fileBlockAnswerMessages.take();
      System.out.println("DownloadTasksManager: takeAnswerMessage");
      return fbam;
   }

   public BlockingQueue<FileBlockRequestMessage> getFileBlockRequestMessages() {
      return fileBlockRequestMessages;
   }

   public BlockingQueue<FileBlockAnswerMessage> getFileBlockAnswerMessages() {
      return fileBlockAnswerMessages;
   }

   private static class Downloader extends Thread {

      private DownloadTasksManager downloadTasksManager;
      private List<FileBlockAnswerMessage> fileBlockAnswerMessages;
      private List<FileBlockRequestMessage> fileBlockRequestMessages;
      private Node node;
      private long startTime;
      private long endTime;


      public Downloader(DownloadTasksManager dtm) {
         this.downloadTasksManager = dtm;
         this.fileBlockAnswerMessages = new ArrayList<FileBlockAnswerMessage>();
         this.fileBlockRequestMessages = new ArrayList<FileBlockRequestMessage>();
         this.fileBlockRequestMessages.addAll(dtm.getFileBlockRequestMessages());
         this.node = dtm.requesterNode;

      }

      @Override
      public void run() {
         try{
            startTime = System.currentTimeMillis();
            while(fileBlockAnswerMessages.size() < fileBlockRequestMessages.size()){
               fileBlockAnswerMessages.add(downloadTasksManager.takeAnswerMessage());
            }
            System.err.println("acabei de receber os blocos");
            System.err.println("answerList.size(): " + fileBlockAnswerMessages.size());
            System.err.println("requestList.size(): " + fileBlockRequestMessages.size());
            orderAnswerBlocks();
            createFile();
         }catch(InterruptedException e){
            e.printStackTrace();
         } finally {
            endTime = System.currentTimeMillis();
            node.getGUI().downloadFinished(node.getRequestCounter(), (endTime - startTime)/1000);
            //System.out.println("DownloadTasksManager: Acabou" + (endTime - startTime)/1000 + " s");
         }
      }

      // ordena a lista de respostas por ordem de bloco
      public void orderAnswerBlocks() {
         System.out.println("DownloadTasksManager: orderAnswerBlocks");
         fileBlockAnswerMessages.sort(Comparator.comparing(FileBlockAnswerMessage::getOffset));
      }

      // cria o ficheiro a partir dos blocos recebidos
      public synchronized void createFile() {
         System.out.println("DownloadTasksManager: createFile");
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
