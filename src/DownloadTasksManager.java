package src;

import java.util.ArrayList;
import java.util.List;

/*  Pagina 7 e 8 do enunciado
 *  Objetivo: Gerir e coordenar as tarefas de descarregamento de blocos de ficheiros.
    Atributos/Métodos: Deve coordenar múltiplas threads de descarregamento. Gerencia a lista de blocos a descarregar, e controla o envio de novos pedidos apenas após a recepção do bloco anterior.
 */

public class DownloadTasksManager {

   private final int BLOCK_SIZE = 1024;
   private List<FileBlockRequestMessage> blockRequest;
   private int numBlocks;
   private int numBlocksReceived=0;

   public DownloadTasksManager(FileSearchResult fsr){
      blockRequest = new ArrayList<FileBlockRequestMessage>();
      for (int i=0; i<fsr.getTamanho(); i+=BLOCK_SIZE){
         blockRequest.add(new FileBlockRequestMessage(fsr.getHash(), i, (int)Math.min(i+BLOCK_SIZE,fsr.getTamanho())-i));
         numBlocks++;
      }
      System.out.println("DownloadTaskManager created with "+numBlocks+" blocks to download.");
      synchronized(this){
         this.notifyAll();
      }
   }
    
}
