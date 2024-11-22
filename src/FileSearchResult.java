package src;

import java.io.Serializable;

/* Pagina 2 e 3 do enunciado
 *  Objetivo: Contém os resultados de uma pesquisa de ficheiros, detalhando quais nós têm o ficheiro solicitado.
    Atributos:
    WordSearchMessage: Instância da mensagem de procura associada.
    hash: Identificador do ficheiro para garantir a autenticidade e integridade.
    tamanho: Tamanho do ficheiro.
    nome: Nome do ficheiro.
    endereço: Endereço IP do nó que possui o ficheiro.
    porta: Porta do nó que possui o ficheiro.
    Métodos: Não especificado.
 */

public class FileSearchResult implements Serializable{
   private WordSearchMessage wordSearchMessage;
   private String hash;
   private long tamanho;
   private String nome;
   private String endereco;
   private int porta;

   //construtor normal - usado para criar um FileSearchResult a partir de uma mensagem de procura
   public FileSearchResult(WordSearchMessage wordSearchMessage, String hash, long tamanho, String nome, String endereco, int porta) {
      this.wordSearchMessage = wordSearchMessage;
      this.tamanho = tamanho;
      this.nome = nome;
      this.endereco = endereco;
      this.porta = porta;
      //Cada ficheiro ter´a um valor de hash associado. Este valor deve ser calculado pelo algoritmo SHA-256, sugerindo-se para tal a utiliza¸c˜ao da classe MessageDigest 2
      this.hash = hash;
   }

   //construtor para criar um file search result a partir da mensagem de busca (node.sendSearchRequest)
   public FileSearchResult(String searchResultString) {
      String[] parts = searchResultString.split(" ");
      this.hash = parts[0];
      this.tamanho = Long.parseLong(parts[1]);
      this.nome = parts[2];
      this.endereco = parts[3];
      this.porta = Integer.parseInt(parts[4]);
   }

   public WordSearchMessage getWordSearchMessage() {
      return wordSearchMessage;
   }

   public String getNome() {
      return nome;
   }

   public String getEndereco() {
      return endereco;
   }

   public int getPorta() {
      return porta;
   }

   public String getHash() {
      return hash;
   }

   public long getTamanho() {
      return tamanho;
   }

   @Override
   public String toString() {
      return "Nome: " + this.nome + " | Endereço: " + this.endereco + " | Porta: " + this.porta;
   }

}