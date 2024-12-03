package src;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



/*  Pagina 7 e 8 do enunciado
 *  Objetivo: Gerir e coordenar as tarefas de descarregamento de blocos de ficheiros.
    Atributos/Métodos: Deve coordenar múltiplas threads de descarregamento. Gerencia a lista de blocos a descarregar, e controla o envio de novos pedidos apenas após a recepção do bloco anterior.
 */

public class DownloadTasksManager implements Serializable {

   
   private int numBlocksSent = 0; //numero de blocos enviados
   private int numBlocksReceived = 0; //numero de blocos recebidos

   private BlockingQueue<FileBlockRequestMessage> fileBlockRequestMessages; //lista onde vão ser adicionados os pedidos
   private BlockingQueue<FileBlockAnswerMessage> fileBlockAnswerMessages ; //lista onde vão ser adicionadas as respostas
   private FileSearchResult fsr;



   

   public DownloadTasksManager(FileSearchResult fsr) {
      this.fileBlockRequestMessages = new LinkedBlockingQueue<FileBlockRequestMessage>();
      this.fileBlockAnswerMessages = new LinkedBlockingQueue<FileBlockAnswerMessage>();
      this.fsr = fsr;
   }



   //adiciona um pedido (FBRM) à lista de pedidos
   /*public synchronized void putRequestMessage(FileBlockRequestMessage fbrm) throws InterruptedException{
      if (fbrm != null) {
         fileBlockRequestMessages.put(fbrm);
         numBlocksSent++;
         this.notifyAll();
      }
   }*/

   public void putRequestMessage(FileBlockRequestMessage fbrm) throws InterruptedException{
      fileBlockRequestMessages.put(fbrm);
      numBlocksSent++;
   }

   //tira a 1ª mensagem (FBRM) da lista de pedidos
   /*public synchronized FileBlockRequestMessage takeRequestMessage(){
      try {
         while(fileBlockRequestMessages.isEmpty()) {
            this.wait();
         }
         //condicao para agir -> notEmpty
         FileBlockRequestMessage fbrm = fileBlockRequestMessages.remove(0);
         //notificar fbrm retirado
         //interessados -> RequestProcessor??
         this.notifyAll();

         return fbrm;
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
      
   }*/
   public FileBlockRequestMessage takeRequestMessage() throws InterruptedException{
      FileBlockRequestMessage fbrm = fileBlockRequestMessages.take(); 
      return fbrm;
   }




   public void putAnswerMessage(FileBlockAnswerMessage fbam) throws InterruptedException{
      System.out.println("DownloadTasksManager: putAnswerMessage");
      fileBlockAnswerMessages.put(fbam);
      numBlocksReceived++;
   }
   

   public FileBlockAnswerMessage takeAnswerMessage() throws InterruptedException{
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

   public FileSearchResult getFsr() {
      return fsr;
   }

}
   