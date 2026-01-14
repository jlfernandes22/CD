package GUI;

import SaudeCerteira.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface Remota (RMI) para comunicação entre Nós da Rede P2P.
 * <p>
 * Define os métodos que podem ser invocados remotamente por outros nós na rede.
 * Abrange funcionalidades de:
 * <ul>
 * <li><b>Descoberta de Rede:</b> Conectar e listar nós vizinhos.</li>
 * <li><b>Sincronização:</b> Partilhar a Blockchain e Transações.</li>
 * <li><b>Busca Distribuída:</b> Encontrar utilizadores (Flood Fill).</li>
 * <li><b>Consenso:</b> Coordenar a mineração distribuída.</li>
 * </ul>
 * <p>
 * Todos os métodos lançam {@link java.rmi.RemoteException} 
 */
public interface RemoteNodeInterface extends Remote {

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GESTÃO DE REDE (NETWORK)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Obtém o endereço RMI deste nó.
     * @return String no formato "//IP:PORTA/NomeServico".
     * @throws RemoteException Erro de rede.
     */
    public String getAdress() throws RemoteException;
    
    /**
     * Adiciona um novo nó à lista de vizinhos conhecidos.
     * @param node O stub do nó remoto a conectar.
     * @throws RemoteException Erro de rede.
     */
    public void addNode(RemoteNodeInterface node) throws RemoteException;

    /**
     * Retorna a lista de todos os nós conectados a este par.
     * Útil para propagação e visualização da topologia.
     * @return Lista de interfaces remotas.
     * @throws RemoteException Erro de rede.
     */
    public List<RemoteNodeInterface> getNetwork() throws RemoteException;
    
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: BUSCA DISTRIBUÍDA (FLOOD FILL)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Procura um utilizador na rede utilizando um algoritmo de inundação (Flood Fill).
     * <p>
     * Se este nó não tiver o utilizador, repassa o pedido aos seus vizinhos,
     * decrementando o TTL para evitar loops infinitos.
     * @param username Nome do utilizador a procurar.
     * @param searchID Identificador único da pesquisa (para evitar ciclos).
     * @param ttl Time To Live (saltos máximos permitidos).
     * @return O objeto User se encontrado, ou null.
     * @throws RemoteException Erro de rede.
     */
    public User findUserRemote(String username, String searchID, int ttl) throws RemoteException;
    
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GESTÃO DA BLOCKCHAIN (CONSENSUS DATA)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Obtém a Blockchain completa deste nó.
     * Cuidado: Pode ser um objeto muito pesado.
     * @return A instância da Blockchain.
     * @throws RemoteException Erro de rede.
     */
    public core.BlockChain getBlockchain() throws RemoteException;
    
    /**
     * Obtém a Blockchain em formato binário (byte array).
     * Mais eficiente para transmissão pela rede.
     * @return Bytes serializados da Blockchain.
     * @throws RemoteException Erro de rede.
     */
    public byte[] getBlockchainData() throws RemoteException;
    
    /**
     * Recebe um novo bloco minerado e tenta adicioná-lo à cadeia local.
     * Se o bloco for válido, este nó irá parar a sua mineração e propagar aos vizinhos.
     * @param blockData Bytes serializados do objeto Block.
     * @throws RemoteException Erro de rede.
     */
    public void propagateBlock(byte[] blockData) throws RemoteException;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GESTÃO DE TRANSAÇÕES (MEMPOOL)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    /**
     * Recebe uma nova transação pendente.
     * A transação é validada e adicionada à "Mempool" (lista de espera para mineração).
     * @param dataBase64 String Base64 representando o objeto SaudeTransaction assinado.
     * @throws RemoteException Erro de rede.
     */
    public void addTransaction(String dataBase64) throws RemoteException;

    /**
     * Obtém a lista de transações pendentes neste nó.
     * @return Lista de Strings (Base64) das transações.
     * @throws RemoteException Erro de rede.
     */
    public List<String> getTransactions() throws RemoteException;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: CONTROLO DE MINERAÇÃO (MINER)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    /**
     * Comanda o nó remoto a iniciar a mineração de um bloco.
     * @param message O cabeçalho do bloco a minerar.
     * @param dificulty A dificuldade (número de zeros) exigida.
     * @return O nonce encontrado (se esperar pelo fim) ou 0 se for assíncrono.
     * @throws RemoteException Erro de rede.
     */
    public int mine(String message, int dificulty) throws RemoteException;
    
    /**
     * Comanda o nó remoto a parar a mineração.
     * Geralmente invocado quando o consenso foi atingido (alguém já ganhou).
     * @param nonce O nonce vencedor (para validação local).
     * @throws RemoteException Erro de rede.
     */
    public void stopMining(int nonce) throws RemoteException;
    
    /** *  @return true se o nó estiver atualmente a gastar CPU a minerar.
     * @throws java.rmi.RemoteException */
    public boolean isMining() throws RemoteException;
    
    /** *  @return true se este nó foi o vencedor da última ronda de mineração.
     * @throws java.rmi.RemoteException */
    public boolean isWinner() throws RemoteException;
    
    /** *  @return O último nonce encontrado.
     * @throws java.rmi.RemoteException */
    public int getNonce() throws RemoteException;
    
    /** *  @return O hash do último bloco minerado.
     * @throws java.rmi.RemoteException */
    public String getHash() throws RemoteException;
}