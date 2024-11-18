package src;
/* Pagina 2 e 3 do enunciado
 *  Objetivo: Representa a mensagem de procura de ficheiros enviada entre os nós conectados.
    Atributos/Métodos: Não especificado no enunciado, apenas mencionada como a classe usada para efetuar a procura de ficheiros por palavra-chave.
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class WordSearchMessage extends Thread {
   private Node node;
   private File songFolder;
   private File[] files;
   private List<FileSearchResult> searchResults;
   private String folderPath; //caminho para a pasta onde estão as músicas
   private String keyword; //palavra-chave a ser pesquisada
   // return FileSearchResult - pesquisa os ficheiros em cada node
   // implementa as cenas da JList e da semana 1
   public WordSearchMessage(Node node) {
      this.node = node;
      this.searchResults = node.getSearchResults();
      this.songFolder = new File(node.getFolderPath()); // pasta onde estão as músicas
   }


   @Override
    public void run() {
        while (true) {
            synchronized (this) {
               try {
                  while (keyword == null) { // Aguarda uma palavra-chave
                     wait();
                  } 
                  searchFiles(); // Realiza a busca com a palavra-chave definida
                  keyword = null; // Reseta a palavra-chave após a busca
               }catch (InterruptedException e) {
                  e.printStackTrace();
                  Thread.currentThread().interrupt();
                  return;
               }
            }
        }
    }


   private void getFilesFromFolder() {
      this.files = songFolder.listFiles(new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return name.endsWith("mp3");
         }
      });
      for (File f : files) {
         System.out.println("Este node "+folderPath+" tem este ficheiro "+ f.getName());
      }
   }
   
   public void searchFiles() {
      getFilesFromFolder();
      if(files != null){
         for (File f : files) {
            if (f.getName().contains(keyword)){
               FileSearchResult fsr = new FileSearchResult(this, "hash", f.length(), f.getName(), "endereco", 1234); //descobrir como saber o hash, endereco e porta
               //this.searchResults.add(fsr);
               node.addSearchResult(fsr);
               System.out.println("Encontrado: "+fsr.toString());
            }
         }
   
         if(searchResults.isEmpty()){
            System.err.println("Não foram encontrados resultados para a pesquisa");
         }
      }
   }

   public synchronized void setKeyword(String keyword) {
      this.keyword = keyword;
      notify(); // Notifica a thread para iniciar a busca
   }

}