package src;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

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
   private List<String> endereco = new ArrayList<String>();  //Endereço IP do nó que possui o ficheiro
   private List<Integer> porta = new ArrayList<Integer>();   //Porta do nó que possui o ficheiro

   public FileSearchResult(WordSearchMessage wordSearchMessage, long tamanho, String nome, String endereco, int porta) {
      this.wordSearchMessage = wordSearchMessage;
      this.tamanho = tamanho;
      this.nome = nome;
      this.endereco.add(endereco);
      this.porta.add(porta);

      //Cada ficheiro ter´a um valor de hash associado. Este valor deve ser calculado pelo algoritmo SHA-256, sugerindo-se para tal a utiliza¸c˜ao da classe MessageDigest 2
      createHash(nome, tamanho);
   }

   private void createHash(String nome, long tamanho) {
      try{
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         String input = nome + tamanho;
         byte[] hashInput = md.digest(input.getBytes());
         this.hash = bytesToHex(hashInput);
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      }
   }

   private String bytesToHex(byte[] hashInput) {
      Formatter formatter = new Formatter();
      for (byte b : hashInput) {
        formatter.format("%02x", b);
      }
      String hexString = formatter.toString();
      formatter.close();
      return hexString;
   }

   public WordSearchMessage getWordSearchMessage() {
      return wordSearchMessage;
   }

   public String getNome() {
      return nome;
   }

   public List<String> getEndereco() {
      return endereco;
   }

   public List<Integer> getPorta() {
      return porta;
   }

   public String getHash() {
      return hash;
   }

   public long getTamanho() {
      return tamanho;
   }

   public void addNode(String endereco, int porta) {
      for (int i = 0; i < this.endereco.size(); i++) {
         if (this.endereco.get(i).equals(endereco) && this.porta.get(i) == porta) {
            return;
         }
      }
      this.endereco.add(endereco);
      this.porta.add(porta);
   }

   @Override
   public String toString() {
      return "Nome: " + this.nome + " | nº de nós: " + this.endereco.size();
   }

   public void setWordSearchMessage(WordSearchMessage wordSearchMessage) {
      this.wordSearchMessage = wordSearchMessage;
   }

}