package GUI;

import SaudeCerteira.User;
import SaudeCerteira.SaudeWallet;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.RMI;

/**
 * Implementação do Nó Remoto P2P (Servidor RMI).
 * <p>
 * Esta classe é o "cérebro" da conectividade. Ela implementa a interface {@link RemoteNodeInterface}
 * e expõe os métodos que outros computadores podem chamar.
 * <p>
 * Responsabilidades:
 * <ul>
 * <li><b>Rede:</b> Gerir lista de vizinhos e conexões.</li>
 * <li><b>Blockchain:</b> Sincronizar a cadeia mais longa (Consenso) e validar blocos.</li>
 * <li><b>Mineração:</b> Controlar a thread de mineração local.</li>
 * <li><b>Dados:</b> Propagar transações e buscar utilizadores.</li>
 * </ul>
 */
public class RemoteNodeObject extends UnicastRemoteObject implements RemoteNodeInterface {

    public static String REMOTE_OBJECT_NAME = "remoteNode";

    // Conjunto para evitar ciclos infinitos nas pesquisas (Cache de IDs de pesquisa)
    Set<String> activeSearches = new CopyOnWriteArraySet<>();

    String address;
    
    /** Lista de nós vizinhos conectados (Thread-Safe). */
    Set<RemoteNodeInterface> network;
    
    /** Mempool: Transações pendentes à espera de serem mineradas. */
    Set<String> transactions; 
    
    /** Bridge para enviar eventos para a GUI. */
    Nodelistener listener;
    
    /** Motor de mineração local. */
    MinerDistibuted miner = new MinerDistibuted();

    /**
     * Construtor do Objeto Remoto.
     * Inicia o serviço RMI na porta especificada.
     * @param port Porta de escuta (ex: 10010).
     * @param listener Interface para comunicar com a GUI.
     * @throws RemoteException Se a porta estiver ocupada ou erro de rede.
     */
    public RemoteNodeObject(int port, Nodelistener listener) throws RemoteException {
        super(port);
        try {
            // Determina o IP real da máquina na rede local (ex: 192.168.x.x)
            String host = getRealIp();
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
            
            // Inicializa coleções Thread-Safe para concorrência
            this.network = new CopyOnWriteArraySet<>();
            this.transactions = new CopyOnWriteArraySet<>();

            this.listener = listener;
            
            // Notificar início
            if (listener != null) {
                listener.onStart("Object " + address + " listening");
            } else {
                System.err.println("Object " + address + " listening");
            }
        } catch (Exception ex) {
            Logger.getLogger(RemoteNodeObject.class.getName()).log(Level.SEVERE, null, ex);
            if (listener != null) {
                listener.onException(ex, "Start remote Object");
            }
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: UTILITÁRIOS DE REDE
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Obtém o endereço IP real da máquina, ignorando interfaces de Loopback (127.0.0.1)
     * e interfaces virtuais (Docker/VMs) se possível.
     * Essencial para que nós em computadores diferentes se consigam ver.
     * @return Endereço IP
     */
    public static String getRealIp() {
        String backupIp = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        // Prioriza IPs de rede local comuns
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                        if (!ip.startsWith("172.")) { // Evita docker default range
                            backupIp = ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
        }
        return backupIp;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: BUSCA DE UTILIZADORES (FLOOD FILL)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Inicia uma busca por um utilizador.
     * Primeiro verifica a cache local, depois pergunta à rede.
     * @param username
     * @return 
     * @throws java.rmi.RemoteException
     */
    public User searchUser(String username) throws RemoteException {
        // 1. Tentar Cache Local
        User local = loadUserFromDisk(username);
        if (local != null) {
            return local;
        }
        // 2. Iniciar Flood Fill na Rede
        String searchID = UUID.randomUUID().toString();
        return findUserRemote(username, searchID, 3); // TTL = 3 saltos
    }

    @Override
    public User findUserRemote(String username, String searchID, int ttl) throws RemoteException {
        // Critério de paragem: TTL expirou ou já processei este pedido
        if (ttl <= 0 || activeSearches.contains(searchID)) {
            return null;
        }
        activeSearches.add(searchID);
        
        // Verifica se tenho o utilizador aqui
        User local = loadUserFromDisk(username);
        if (local != null) {
            return local;
        }
        
        // Propaga aos vizinhos (Recursão Distribuída)
        for (RemoteNodeInterface node : network) {
            try {
                User found = node.findUserRemote(username, searchID, ttl - 1);
                if (found != null) {
                    return found;
                }
            } catch (RemoteException e) {
                // Nó indisponível, ignora
            }
        }
        return null;
    }

    /**
     * Tenta carregar a chave pública de um utilizador do disco.
     * Procura nas pastas 'data_user' e 'data_wallet'.
     */
    private User loadUserFromDisk(String username) {
        try {
            File pubFile = new File("data_user/" + username + ".pub");
            if (!pubFile.exists()) {
                pubFile = new File("data_wallet/" + username + ".pub");
            }
            if (pubFile.exists()) {
                return User.login(username); // Login público (sem chave privada)
            }
        } catch (Exception e) {
        }
        return null;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GESTÃO DE CONEXÕES E SINCRONIZAÇÃO (CORE LOGIC)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    /**
     * Adiciona um novo nó à rede e inicia a SINCRONIZAÇÃO DA BLOCKCHAIN.
     * <p>
     * Este método implementa a regra da "Cadeia Mais Longa".
     * Se o nó remoto tiver uma blockchain maior, este nó faz download de tudo e substitui a sua.
     */
    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        if (network.contains(node)) {
            return;
        }
        network.add(node);
        
        // Sincronizar transações pendentes
        this.transactions.addAll(node.getTransactions());

        // :::::::::: SYNC: DOWNLOAD DA BLOCKCHAIN ::::::::::
        try {
            System.out.println("A iniciar sincronização com " + node.getAdress());

            // 1. Comparar tamanhos
            core.BlockChain remoteChain = node.getBlockchain();
            core.BlockChain localChain = null;
            try {
                localChain = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
            } catch (Exception e) {
                localChain = null; // Assumimos vazio se der erro
            }

            boolean devoAtualizar = false;

            if (remoteChain != null) {
                if (localChain == null) {
                    devoAtualizar = true; // Sou novo na rede
                } else {
                    // A cadeia remota é maior? (Regra de Ouro)
                    if (remoteChain.getLastBlock().getID() > localChain.getLastBlock().getID()) {
                        devoAtualizar = true;
                    }
                }
            }

            // 2. Executar Atualização (Download de ZIP)
            if (devoAtualizar) {
                System.out.println("SYNC: Recebida blockchain maior. A atualizar...");

                // Download dos dados brutos
                byte[] zipData = node.getBlockchainData(); 

                if (zipData != null && zipData.length > 0) {
                    java.io.File folder = new java.io.File("data_blocks");
                    
                    // A. Limpar estado atual (Reset)
                    if (folder.exists()) {
                        java.io.File[] files = folder.listFiles();
                        if (files != null) {
                            for (java.io.File f : files) {
                                if (f.isFile()) f.delete();
                            }
                        }
                    } else {
                        folder.mkdirs();
                    }

                    // B. Instalar novos dados
                    utils.Zip.unzipFolder(zipData, folder);
                    System.out.println("SYNC: Ficheiros descomprimidos com sucesso.");

                    // C. Atualizar Memória e Carteiras
                    try {
                        core.BlockChain novaChain = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
                        
                        // Recalcular saldos com base no novo histórico
                        if (novaChain != null) {
                            SaudeCerteira.SaudeWallet.updateWallets(novaChain.getLastBlock());
                        }
                    } catch (Exception ex) {
                        System.out.println("Erro ao carregar nova blockchain: " + ex.getMessage());
                    }

                    // Notificar GUI para atualizar
                    if (listener != null) {
                        listener.onTransaction("BlockReceived");
                    }
                }
            } else {
                System.out.println("SYNC: A minha blockchain já está atualizada.");
            }

        } catch (Exception e) {
            System.err.println("Erro no SYNC: " + e.getMessage());
            e.printStackTrace();
        }
        // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

        // Conexão Bidirecional
        node.addNode(this);
        for (RemoteNodeInterface iremoteP2P : network) {
            try {
                iremoteP2P.addNode(node);
            } catch (Exception e) {
            }
        }
        if (listener != null) {
            listener.onConect(node.getAdress());
        }
    }

    @Override
    public List<RemoteNodeInterface> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GESTÃO DE TRANSAÇÕES (MEMPOOL)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    @Override
    public void addTransaction(String dataBase64) throws RemoteException {
        // Evitar duplicados e loops infinitos
        if (this.transactions.contains(dataBase64)) {
            return;
        }

        // 1. Guardar Localmente
        this.transactions.add(dataBase64);

        // 2. Propagar para Vizinhos (Flooding)
        for (RemoteNodeInterface node : network) {
            try {
                node.addTransaction(dataBase64);
            } catch (Exception e) {
            } 
        }

        // 3. Atualizar GUI
        if (listener != null) {
            listener.onTransaction(dataBase64);
        } else {
            System.out.println("Transaction received");
        }
    }

    @Override
    public List<String> getTransactions() throws RemoteException {
        return new ArrayList<>(transactions);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: CONTROLO DE MINERAÇÃO
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Override
    public int mine(String message, int dificulty) throws RemoteException {
        if (miner.isMining()) {
            return 0; // Já estou ocupado
        }
        miner.isWorking.set(true);
        return miner.mine(message, dificulty);
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
        if (!miner.isMining()) {
            return;
        }
        miner.stopMining(nonce);
    }

    @Override
    public boolean isMining() throws RemoteException {
        return miner.isMining();
    }

    @Override
    public boolean isWinner() throws RemoteException {
        return miner.isWinner();
    }

    @Override
    public int getNonce() throws RemoteException {
        return miner.getNonce();
    }

    @Override
    public String getHash() throws RemoteException {
        return MinerDistibuted.getHash(miner.message + miner.getNonce());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: PROPAGAÇÃO DE BLOCOS (CONSENSO)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    /**
     * Recebe um bloco minerado da rede, valida-o e adiciona-o.
     * É aqui que acontece a "magia" do consenso.
     */
    @Override
    public void propagateBlock(byte[] blockData) throws RemoteException {
        // 1. Se eu estiver a minerar, PARAR IMEDIATAMENTE (alguém ganhou)
        if (miner.isMining()) {
            miner.stopMining(-1);
        }

        boolean shouldPropagate = false;

        // :::::::: FASE 1: ATUALIZAÇÃO LOCAL (Atomicidade) ::::::::
        synchronized (activeSearches) { // Usa um objeto lock para evitar race conditions
            try {
                core.Block newBlock = (core.Block) utils.Serializer.byteArrayToObject(blockData);
                core.BlockChain bc = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");

                boolean isNewBlock = true;

                // Validação Lógica
                if (bc != null) {
                    if (bc.getLastBlock().getID() == newBlock.getID()) {
                        isNewBlock = false; // Já tenho este bloco
                    } else if (bc.getLastBlock().getID() > newBlock.getID()) {
                        return; // Bloco antigo, ignorar
                    } else if (!java.util.Arrays.equals(bc.getLastBlock().getCurrentHash(), newBlock.getPreviousHash())) {
                        System.out.println("Bloco órfão/inválido recebido (Hash mismatch). Ignorado.");
                        return;
                    }
                } else if (newBlock.getID() == 0) {
                     bc = new core.BlockChain(newBlock); // Genesis
                }

                // Adicionar à Blockchain
                if (isNewBlock && bc != null) {
                    bc.add(newBlock); // O método add() verifica PoW internamente
                    shouldPropagate = true; // Definir flag para propagar
                }

                // Atualizar Carteiras (Para aparecer nas "Minhas Receitas")
                try {
                    SaudeCerteira.SaudeWallet.updateWallets(newBlock);
                } catch (Exception e) {
                    System.out.println("Aviso: Erro ao atualizar carteiras: " + e.getMessage());
                }

                // Limpar da Mempool as transações que foram incluídas no bloco
                List<SaudeCerteira.SaudeTransaction> mined = (List<SaudeCerteira.SaudeTransaction>) newBlock.getData().getElements();
                for (SaudeCerteira.SaudeTransaction t : mined) {
                    byte[] tBytes = utils.Serializer.objectToByteArray(t);
                    String tString = java.util.Base64.getEncoder().encodeToString(tBytes);
                    this.transactions.remove(tString);
                }

                // Avisar GUI
                if (listener != null) {
                    listener.onTransaction("BlockReceived");
                }

            } catch (Exception e) {
                System.out.println("Nota no processamento do bloco: " + e.getMessage());
                return; // Se o bloco for inválido, o processo morre aqui
            }
        } 

        
        // :::::::: FASE 2: PROPAGAÇÃO REMOTA (Assíncrona) ::::::::
        // Enviamos o bloco para os vizinhos numa thread separada para não bloquear o RMI
        if (shouldPropagate) {
            new Thread(() -> {
                for (RemoteNodeInterface node : network) {
                    try {
                        node.propagateBlock(blockData);
                    } catch (Exception ignore) {
                        // Nó offline, continuar
                    }
                }
            }).start();
        }
    }
    
    /**
     * Prepara os dados da Blockchain para envio (ZIP).
     * Usado pelos novos nós para fazerem download da cadeia completa.
     */
    @Override
    public byte[] getBlockchainData() throws RemoteException {
        try {
            java.io.File folder = new java.io.File("data_blocks");
            
            if (!folder.exists() || folder.listFiles().length == 0) {
                return null; 
            }
            
            System.out.println("A enviar pasta data_blocks comprimida...");
            return utils.Zip.zipFolder(folder);
            
        } catch (Exception e) {
            System.err.println("Erro ao zipar blockchain: " + e.getMessage());
            return null;
        }
    }

    @Override
    public core.BlockChain getBlockchain() throws RemoteException {
        try {
            return core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
        } catch (Exception e) {
            return null;
        }
    }
}