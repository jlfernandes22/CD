//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::       Antonio Manuel Rodrigues Manso                                    ::
//::                                                                         ::
//::       I N S T I T U T O   P O L I T E C N I C O   D E   T O M A R       ::
//::       Escola Superior de Tecnologia de Tomar                            ::
//::       e-mail: manso@ipt.pt                                              ::
//::       url    : http://orion.ipt.pt/~manso                               ::
//::                                                                         ::
//::       This software was build with the purpose of investigate and       ::
//::       learning.                                                         ::
//::                                                                         ::
//::                                                                         ::
//::                                                               (c)2024   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////
package GUI;

import SaudeCerteira.User;
import SaudeCerteira.SaudeWallet; // Needed for FILE_PATH
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
 * Created on 27/11/2024, 17:48:32
 *
 * @author manso - computer
 */
public class RemoteNodeObject extends UnicastRemoteObject implements RemoteNodeInterface {

    public static String REMOTE_OBJECT_NAME = "remoteNode";
    
    // Set for active searches to prevent infinite loops in P2P network
    Set<String> activeSearches = new CopyOnWriteArraySet<>();

    String address;
    Set<RemoteNodeInterface> network;
    Set<String> transactions;
    Nodelistener listener;
    MinerDistibuted miner = new MinerDistibuted();

    public RemoteNodeObject(int port, Nodelistener listener) throws RemoteException {
        super(port);
        try {
            // [FIX] Use the helper method to get the REAL LAN IP
            String host = getRealIp();
            
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
            this.network = new CopyOnWriteArraySet<>();
            this.transactions = new CopyOnWriteArraySet<>();
            // addNode(this);
            this.listener = listener;
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

    /**
     * [UPDATED HELPER] Finds the real LAN IP, ignoring Docker/VM interfaces.
     */
    public static String getRealIp() {
        String backupIp = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Skip loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        
                        // PRIORITIZE: Home/Office networks usually start with 192.168 or 10.
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                        
                        // IGNORE: Docker/WSL usually start with 172.
                        if (!ip.startsWith("172.")) {
                             backupIp = ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return backupIp;
    }
 
    /**
     * Helper method to start a search from this node
     */
    public User searchUser(String username) throws RemoteException {
        // 1. Tentar localmente primeiro
        User local = loadUserFromDisk(username);
        if (local != null) {
            return local;
        }

        // 2. Se não encontrou, inicia propagação P2P para os vizinhos
        String searchID = UUID.randomUUID().toString();
        return findUserRemote(username, searchID, 3); // TTL de 3 saltos
    }

    @Override
    public User findUserRemote(String username, String searchID, int ttl) throws RemoteException {
        // Proteção contra loops e limite de profundidade (TTL)
        if (ttl <= 0 || activeSearches.contains(searchID)) {
            return null;
        }

        // Marcar esta busca como processada por este nó
        activeSearches.add(searchID);
        System.out.println("Searching for user: " + username + " (ID: " + searchID + ")");

        // 1. Procurar no meu disco local
        User local = loadUserFromDisk(username);
        if (local != null) {
            System.out.println("User FOUND locally: " + username);
            return local;
        }

        // 2. Se eu não tenho, pergunto a quem está na minha rede (P2P Propagation)
        for (RemoteNodeInterface node : network) {
            try {
                User found = node.findUserRemote(username, searchID, ttl - 1);
                if (found != null) {
                    return found; // Retorna o utilizador encontrado na rede
                }
            } catch (RemoteException e) {
                // Se um nó estiver offline, continuamos a pesquisar nos outros
            }
        }

        return null;
    }

    private User loadUserFromDisk(String username) {
        try {
            // O caminho completo: data_wallet/username.pub (Using SaudeWallet.FILE_PATH)
            // Note: Verify if your User class uses data_user/ or data_wallet/ and adjust FILE_PATH accordingly.
            // Assuming User.java manages its own path logic or shares SaudeWallet path.
            
            // Check if file exists before trying to load
            File pubFile = new File("data_user/" + username + ".pub"); // Hardcoded path based on your previous messages
            if (!pubFile.exists()) {
                 pubFile = new File("data_wallet/" + username + ".pub"); // Try alternate path
            }
            
            if (pubFile.exists()) {
                // Utilizamos o método User.login(name) que carrega a chave pública do disco
                return User.login(username);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar utilizador local: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        //se já tiver o nó  -  não faz nada
        if (network.contains(node)) {
            return;
        }
        //adicionar o no
        network.add(node);
        //Adicionar as transacoes
        this.transactions.addAll(node.getTransactions());
        //adicionar o this ao remoto
        node.addNode(this);

        //propagar o no na rede
        for (RemoteNodeInterface iremoteP2P : network) {
            iremoteP2P.addNode(node);

        }
        if (listener != null) {
            listener.onConect(node.getAdress());
        } else {
            System.out.println("Connected to node.getAdress()");
        }
        //::::::::: DEBUG  ::::::::::::::::
        System.out.println("Rede p2p");
        for (RemoteNodeInterface iremoteP2P : network) {
            System.out.println(iremoteP2P.getAdress());

        }

    }

    @Override
    public List<RemoteNodeInterface> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }
//::::::::::: T R A NS A C T IO N S  :::::::::::
    @Override
    public void addTransaction(String data) throws RemoteException {
      

        if (this.transactions.contains(data)) {
            return;
        }
        this.transactions.add(data);
        for (RemoteNodeInterface node : network) {
            node.addTransaction(data);
        }
        if (listener != null) {
            listener.onTransaction(data);
        } else {
            System.out.println("Transaction from  " + getRemoteHost());
        }
        for (String t : transactions) {
            System.out.println(t);
        }
     
    }

    @Override
    public List<String> getTransactions() throws RemoteException {
        return new ArrayList<>(transactions);
    }

    private String getRemoteHost() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException ex) {
            return "unknown";
        }
    }

    //::::::::::: M I N E R  :::::::::::
    @Override
    public int mine(String message, int dificulty) throws RemoteException {
//        se estiver a minar
        if (miner.isMining()) {
            return 0; // não faz nada
        }
        miner.isWorking.set(true);
        
        
        return miner.mine(message, dificulty);
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
        //se não estiver a minar
        if (!miner.isMining()) {
            return ; //nao faz nada
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
       public String getHash() throws RemoteException{
           return MinerDistibuted.getHash(miner.message+miner.getNonce());
       }

    @Override
    public void propagateBlock(byte[] blockData) throws RemoteException {
        try {
            // 1. Converter e Carregar (Código que já tem)
            core.Block newBlock = (core.Block) utils.Serializer.byteArrayToObject(blockData);
            core.BlockChain bc = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");

            if (bc == null && newBlock.getID() == 0) bc = new core.BlockChain(newBlock);
            
            // Verificar se já tenho o bloco
            if (bc != null && bc.getLastBlock().getID() >= newBlock.getID()) return;

            // 2. Adicionar e Atualizar Carteiras
            System.out.println("Recebi novo bloco: " + newBlock.getID());
            bc.add(newBlock);
            SaudeCerteira.SaudeWallet.updateWallets(newBlock);

            // =================================================================
            // === [CORREÇÃO] LIMPAR AS TRANSAÇÕES PROCESSADAS DA LISTA ========
            // =================================================================
            
            // 1. Obter as transações que vieram dentro deste bloco
            List<SaudeCerteira.SaudeTransaction> txsProcessadas = (List<SaudeCerteira.SaudeTransaction>) newBlock.getData().getElements();
            
            // 2. Percorrer e remover da lista de pendentes ('this.transactions')
            for (SaudeCerteira.SaudeTransaction t : txsProcessadas) {
                // Temos de converter de volta para String Base64 para encontrar na lista e remover
                byte[] tBytes = utils.Serializer.objectToByteArray(t);
                String tString = java.util.Base64.getEncoder().encodeToString(tBytes);
                
                // Remove da lista de pendentes
                this.transactions.remove(tString);
            }
            
            // 3. Atualizar a Interface Gráfica (limpar a caixa de texto)
            if (listener != null) {
                // Enviamos uma mensagem especial ou simplesmente atualizamos
                // Como limpámos o 'transactions', ao chamar getTransactions() a GUI vai receber vazio
                listener.onTransaction("BlockReceived"); 
            }

            // 4. Propagar para os vizinhos
            for (RemoteNodeInterface node : network) {
                try { node.propagateBlock(blockData); } catch (Exception e) {}
            }

        } catch (Exception e) {
            System.out.println("Erro ao receber bloco: " + e.getMessage());
            // throw new RemoteException("Bloco inválido", e); // Comentei para não poluir o log se for duplicado
        }
    }
    
    // Precisamos de uma referência para a GUI para atualizar a lista visual
    // Adicione esta variável e método na classe RemoteNodeObject:
    private Nodelistener gui;
    public void setGUI(Nodelistener gui){
        this.gui = gui;
    }
}