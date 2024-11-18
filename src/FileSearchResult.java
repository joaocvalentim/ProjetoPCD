package src;

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

public class FileSearchResult {
   private WordSearchMessage wordSearchMessage;
   private String hash;
   private long tamanho;
   private String nome;
   private String endereco;
   private int porta;

   public FileSearchResult(WordSearchMessage wordSearchMessage, String hash, long tamanho, String nome, String endereco,
         int porta) {
      this.wordSearchMessage = wordSearchMessage;
      this.hash = hash;
      this.tamanho = tamanho;
      this.nome = nome;
      this.endereco = endereco;
      this.porta = porta;
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
      // TODO Auto-generated method stub
      return "Nome: " + this.nome + " | Endereço: " + this.endereco + " | Porta: " + this.porta;
   }

}