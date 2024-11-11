package src;
/* Pagina 2 e 3 do enunciado
 *  Objetivo: Representa a mensagem de procura de ficheiros enviada entre os nós conectados.
    Atributos/Métodos: Não especificado no enunciado, apenas mencionada como a classe usada para efetuar a procura de ficheiros por palavra-chave.
 */

import java.awt.event.FocusAdapter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class WordSearchMessage {

   private File songFolder;
   private File[] files;
   private List<FileSearchResult> searchResults;
   private String folderPath="node1-server"; //caminho para a pasta onde estão as músicas
   private String keyword; //palavra-chave a ser pesquisada

   // return FileSearchResult - pesquisa os ficheiros em cada node
   // implementa as cenas da JList e da semana 1

   public WordSearchMessage(String keyword) {
      this.songFolder = new File(folderPath); // pasta onde estão as músicas
      this.keyword = keyword;
      this.searchResults = new ArrayList<FileSearchResult>();
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

   public List <FileSearchResult> searchFiles() {
      getFilesFromFolder(); //mete no Files todos os ficheiros da pasta
      if(files != null){
         for (File f : files) {
            if (f.getName().contains(keyword)){
               FileSearchResult fsr = new FileSearchResult(this, "hash", f.length(), f.getName(), "endereco", 1234); //descobrir como saber o hash, endereco e porta
               this.searchResults.add(fsr);
               System.out.println("Encontrado: "+fsr.toString());
            }
         }
   
         if(searchResults.isEmpty()){
            System.err.println("Não foram encontrados resultados para a pesquisa");
         }
      }
      return searchResults;
   }
}