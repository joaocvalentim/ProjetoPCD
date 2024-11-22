package src;
/* Pagina 2 e 3 do enunciado
 *  Objetivo: Representa a mensagem de procura de ficheiros enviada entre os nós conectados.
    Atributos/Métodos: Não especificado no enunciado, apenas mencionada como a classe usada para efetuar a procura de ficheiros por palavra-chave.
 */

import java.io.Serializable;

public class WordSearchMessage implements Serializable{
   private String keyword; // palavra-chave a ser pesquisada
   private String originAddress; // endereço IP do nó que enviou a mensagem
   private int originPort; // porta do nó que enviou a mensagem

   public WordSearchMessage(String keyword, String originAddress, int originPort) {
      this.keyword = keyword;
      this.originAddress = originAddress;
      this.originPort = originPort;
   }


   public String getKeyword() {
      return keyword;
   }

   public String getOriginAddress() {
      return originAddress;
   }

   public int getOriginPort() {
      return originPort;
   }

}