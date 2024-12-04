package src;

/*  Página 4 e 9 do enunciado
 *  Objetivo: Responsável pelo pedido de blocos específicos de um ficheiro.
    Atributos:
    hash: Identificador do ficheiro.
    offset: Índice do byte onde começar o bloco.
    length: Número de bytes a ler.
    Métodos: Não especificado.
 */

import java.io.Serializable;

public class FileBlockRequestMessage implements Serializable {
   // private final int BLOCK_SIZE = 1024;
   private String hash;
   private int offset;
   private int length;
   private String requestAddress;
   private int requestPort;

   public FileBlockRequestMessage(String hash, int offset, int length, String requestAddress, int requestPort) {
      this.hash = hash;
      this.offset = offset;
      this.length = length;
      this.requestAddress = requestAddress;
      this.requestPort = requestPort;
      
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

   public String getRequestAddress() {
      return requestAddress;
   }

   public int getRequestPort() {
      return requestPort;
   }
   
   @Override
   public String toString() {
      return "FileBlockRequestMessage{" + "hash='" + hash + '\'' + ", offset=" + offset + ", length=" + length + '}';
   }
}
