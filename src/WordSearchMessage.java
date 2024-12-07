package src;
/* Pagina 2 e 3 do enunciado
 *  Objetivo: Representa a mensagem de procura de ficheiros enviada entre os nós conectados.
    Atributos/Métodos: Não especificado no enunciado, apenas mencionada como a classe usada para efetuar a procura de ficheiros por palavra-chave.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WordSearchMessage implements Serializable{
   private String keyword; // palavra-chave a ser pesquisada
   //private String originAddress; // endereço IP do nó que enviou a mensagem
   //private int originPort; // porta do nó que enviou a mensagem
   private List <String> visitedNodes; // lista de nós visitados através de connectionKey (para comparar com )
   private String senderKey ; // chave de conexão

   public WordSearchMessage(String keyword, String senderKey) {
      this.keyword = keyword;
      //this.originAddress = originAddress;
      //this.originPort = originPort;
      this.visitedNodes = new ArrayList<String>();
      this.senderKey = senderKey;
   }


   public String getKeyword() {
      return keyword;
   }

   /*public String getOriginAddress() {
      return originAddress;
   }

   public int getOriginPort() {
      return originPort;
   }*/
  public String getSenderKey() {
      return senderKey;
   }

   public void setSenderKey(String senderKey) {
      this.senderKey = senderKey;
   }  


   public List<String> getVisitedNodes() {
      return visitedNodes;
   }

   public void addVisitedNode(String connectionKey) {
      visitedNodes.add(connectionKey);
   }

   /*public void setOriginAddress(String originAddress) {
      this.originAddress = originAddress;
   }

   public void setOriginPort(int originPort) {
      this.originPort = originPort;
   }*/

}