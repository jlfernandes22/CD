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

public class RemoteNodeObject extends UnicastRemoteObject implements RemoteNodeInterface {

    public static String REMOTE_OBJECT_NAME = "remoteNode";
    
    // Set for active searches
    Set<String> activeSearches = new CopyOnWriteArraySet<>();

    String address;
    Set<RemoteNodeInterface> network;
    Set<String> transactions; // Mantemos String para facilitar a leitura na lista
    Nodelistener listener;
    MinerDistibuted miner = new MinerDistibuted();

    public RemoteNodeObject(int port, Nodelistener listener) throws RemoteException {
        super(port);
        try {
            String host = getRealIp();
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
            this.network = new CopyOnWriteArraySet<>();
            this.transactions = new CopyOnWriteArraySet<>();
            
            this.listener = listener;
            if (listener != null) {
                listener.onStart("Object " + address + " listening");
            } else {
                System.err.println("Object " + address + " listening");
            }
        } catch (Exception ex) {
            Logger.getLogger(RemoteNodeObject.class.getName()).log(Level.SEVERE, null, ex);
            if (listener != null) listener.onException(ex, "Start remote Object");
        }
    }

    // ... (Mantém o método getRealIp igual) ...
    public static String getRealIp() {
        // ... (O teu código do getRealIp mantém-se inalterado) ...
        // Vou omitir aqui para poupar espaço, mas usa o que já tinhas feito
        String backupIp = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) return ip;
                        if (!ip.startsWith("172.")) backupIp = ip;
                    }
                }
            }
        } catch (SocketException e) { e.printStackTrace(); }
        return backupIp;
    }

    // ... (Mantém searchUser, findUserRemote e loadUserFromDisk iguais) ...
    public User searchUser(String username) throws RemoteException {
        User local = loadUserFromDisk(username);
        if (local != null) return local;
        String searchID = UUID.randomUUID().toString();
        return findUserRemote(username, searchID, 3);
    }

    @Override
    public User findUserRemote(String username, String searchID, int ttl) throws RemoteException {
        if (ttl <= 0 || activeSearches.contains(searchID)) return null;
        activeSearches.add(searchID);
        User local = loadUserFromDisk(username);
        if (local != null) return local;
        for (RemoteNodeInterface node : network) {
            try {
                User found = node.findUserRemote(username, searchID, ttl - 1);
                if (found != null) return found;
            } catch (RemoteException e) {}
        }
        return null;
    }

    private User loadUserFromDisk(String username) {
        try {
            File pubFile = new File("data_user/" + username + ".pub");
            if (!pubFile.exists()) pubFile = new File("data_wallet/" + username + ".pub");
            if (pubFile.exists()) return User.login(username);
        } catch (Exception e) {}
        return null;
    }

    // ... (Mantém getAdress, addNode, getNetwork iguais) ...
    @Override
    public String getAdress() throws RemoteException { return address; }

    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        if (network.contains(node)) return;
        network.add(node);
        this.transactions.addAll(node.getTransactions());
        node.addNode(this);
        for (RemoteNodeInterface iremoteP2P : network) iremoteP2P.addNode(node);
        if (listener != null) listener.onConect(node.getAdress());
    }

    @Override
    public List<RemoteNodeInterface> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }

    // ::::::::::: T R A N S A C T I O N S :::::::::::
    
    /**
     * CORRIGIDO: Recebe a String (Base64) da transação.
     * Não tenta desencriptar, apenas guarda e propaga.
     */
    @Override
    public void addTransaction(String dataBase64) throws RemoteException {
        // Se já tiver a transação, ignora
        if (this.transactions.contains(dataBase64)) {
            return;
        }
        
        // Guarda na lista local
        this.transactions.add(dataBase64);
        
        // Propaga para a rede
        for (RemoteNodeInterface node : network) {
            try {
                node.addTransaction(dataBase64);
            } catch(Exception e) {} // Ignora erros de rede
        }
        
        // Avisa a GUI
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

    // ... (Mantém Mine, StopMining, PropagateBlock iguais ao que corrigimos antes) ...
    
    @Override
    public int mine(String message, int dificulty) throws RemoteException {
        if (miner.isMining()) return 0;
        miner.isWorking.set(true);
        // Sem ciclo for aqui (a GUI controla)
        return miner.mine(message, dificulty);
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
        if (!miner.isMining()) return;
        miner.stopMining(nonce);
        // Sem ciclo for aqui
    }
    
    // ... (Mantém getters do miner e propagateBlock iguais) ...
    @Override public boolean isMining() throws RemoteException { return miner.isMining(); }
    @Override public boolean isWinner() throws RemoteException { return miner.isWinner(); }
    @Override public int getNonce() throws RemoteException { return miner.getNonce(); }
    @Override public String getHash() throws RemoteException { return MinerDistibuted.getHash(miner.message+miner.getNonce()); }

    @Override
    public void propagateBlock(byte[] blockData) throws RemoteException {
        // ... (Use a versão corrigida que lhe dei na resposta anterior que limpa as transações) ...
        // Vou resumir para caber:
        try {
            core.Block newBlock = (core.Block) utils.Serializer.byteArrayToObject(blockData);
            core.BlockChain bc = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
            if (bc == null && newBlock.getID() == 0) bc = new core.BlockChain(newBlock);
            if (bc != null && bc.getLastBlock().getID() >= newBlock.getID()) return;

            bc.add(newBlock);
            SaudeCerteira.SaudeWallet.updateWallets(newBlock);

            // LIMPEZA DE TRANSAÇÕES (Importante)
            List<SaudeCerteira.SaudeTransaction> txsProcessadas = (List<SaudeCerteira.SaudeTransaction>) newBlock.getData().getElements();
            for (SaudeCerteira.SaudeTransaction t : txsProcessadas) {
                byte[] tBytes = utils.Serializer.objectToByteArray(t);
                String tString = java.util.Base64.getEncoder().encodeToString(tBytes);
                this.transactions.remove(tString);
            }
            
            if (listener != null) listener.onTransaction("BlockReceived");
            
            for (RemoteNodeInterface node : network) {
                try { node.propagateBlock(blockData); } catch (Exception e) {}
            }
        } catch (Exception e) {
            System.out.println("Erro ao receber bloco: " + e.getMessage());
        }
    }
}