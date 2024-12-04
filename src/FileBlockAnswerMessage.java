package src;

import java.io.Serializable;

/*  Página 4 do enunciado
 *  Objetivo: Classe que encapsula a resposta ao pedido de um bloco de ficheiro, contendo os dados binários do bloco.
    Atributos/Métodos: Não especificado.
 */

public class FileBlockAnswerMessage implements Serializable{

   private String hash;
   private int offset;
   private int length;
   private byte[] data;

   public FileBlockAnswerMessage(String hash, int offset, int length, byte[] data) {
      this.hash = hash;
      this.offset = offset;
      this.length = length;
      this.data = data;
   }

   @Override
   public String toString() {
      return "FileBlockAnswerMessage{" +
              "hash='" + hash + '\'' +
              ", offset=" + offset +
              ", length=" + length +
              ", data=" + data +
              '}';
   }
   public String getHash() {
      return hash;
   }
   
   public int getOffset() {
      return offset;
   }

   public int getLength() {
      return length;
   }

   public byte[] getData() {
      return data;
   }
   
}
