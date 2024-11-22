package src;

import java.io.Serializable;


/* Pagina 2 do enunciado
 *  Objetivo: Classe responsável por gerenciar o envio de pedidos de ligação entre nós na rede.
    Atributos/Métodos: Não especificado no enunciado, mas mencionada como necessária para iniciar uma conexão entre nós.
 */
public class NewConnectionRequest implements Serializable {
    // atributos do node

    private String address;
    private int port;

    public NewConnectionRequest(String address, int port) {
        this.address = address;
        this.port = port;
    }

    
    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
