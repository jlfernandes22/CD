//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::      Antonio Manuel Rodrigues Manso                                     ::
//::                                                                         ::
//::      I N S T I T U T O    P O L I T E C N I C O    D E    T O M A R     ::
//::      Escola Superior de Tecnologia de Tomar                             ::
//::      e-mail: manso@ipt.pt                                               ::
//::      url    : http://orion.ipt.pt/~manso                                ::
//::                                                                         ::
//::      This software was build with the purpose of investigate and        ::
//::      learning.                                                          ::
//::                                                                         ::
//::                                                                         ::
//::                                                               (c)2024   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////
package GUI;

import SaudeCerteira.User;
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
     * [NEW HELPER] Static method to find the real IP. 
     * We make it static so NodeP2PGui can also use it to set the RMI hostname property.
     */
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
                        // We save it just in case we find nothing else, but we keep looking.
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

    // 1. Procurar no meu disco local
    User local = loadUserFromDisk(username);
    if (local != null) {
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
        // O caminho completo seria data_user/username.pub
        File pubFile = new File(User.FILE_PATH + username + ".pub");
        
        if (pubFile.exists()) {
            // Utilizamos o seu método login(name) que carrega a chave pública do disco
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
        for (RemoteNodeInterface node : network) {
            node.mine(message, dificulty);
        }
        return miner.mine(message, dificulty);
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
        //se não estiver a minar
        if (!miner.isMining()) {
            return ; //nao faz nada
        }
        miner.stopMining(nonce);
        for (RemoteNodeInterface node : network) {
            node.stopMining(nonce);
        }          
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
}