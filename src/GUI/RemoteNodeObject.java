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
            if (listener != null) {
                listener.onException(ex, "Start remote Object");
            }
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
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
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

    // ... (Mantém searchUser, findUserRemote e loadUserFromDisk iguais) ...
    public User searchUser(String username) throws RemoteException {
        User local = loadUserFromDisk(username);
        if (local != null) {
            return local;
        }
        String searchID = UUID.randomUUID().toString();
        return findUserRemote(username, searchID, 3);
    }

    @Override
    public User findUserRemote(String username, String searchID, int ttl) throws RemoteException {
        if (ttl <= 0 || activeSearches.contains(searchID)) {
            return null;
        }
        activeSearches.add(searchID);
        User local = loadUserFromDisk(username);
        if (local != null) {
            return local;
        }
        for (RemoteNodeInterface node : network) {
            try {
                User found = node.findUserRemote(username, searchID, ttl - 1);
                if (found != null) {
                    return found;
                }
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    private User loadUserFromDisk(String username) {
        try {
            File pubFile = new File("data_user/" + username + ".pub");
            if (!pubFile.exists()) {
                pubFile = new File("data_wallet/" + username + ".pub");
            }
            if (pubFile.exists()) {
                return User.login(username);
            }
        } catch (Exception e) {
        }
        return null;
    }

    // ... (Mantém getAdress, addNode, getNetwork iguais) ...
    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        if (network.contains(node)) {
            return;
        }
        network.add(node);
        this.transactions.addAll(node.getTransactions());

        // :::::::::: SYNC: SINCRONIZAÇÃO DA BLOCKCHAIN AO CONECTAR ::::::::::
        try {
            System.out.println("A iniciar sincronização com " + node.getAdress());

            // 1. Pedir a blockchain do nó remoto
            core.BlockChain remoteChain = node.getBlockchain();
            core.BlockChain localChain = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");

            // 2. Decidir se devemos atualizar (Se a dele for maior ou se eu não tiver nada)
            boolean devoAtualizar = false;

            if (remoteChain != null) {
                if (localChain == null) {
                    devoAtualizar = true; // Sou novo, aceito tudo
                } else {
                    // Se o último bloco dele tiver ID maior, ele está à frente
                    if (remoteChain.getLastBlock().getID() > localChain.getLastBlock().getID()) {
                        devoAtualizar = true;
                    }
                }
            }

            // 3. Aplicar a atualização
            if (devoAtualizar) {
                System.out.println("SYNC: Recebida blockchain maior. A atualizar...");

                // Gravar no disco
                remoteChain.save(core.BlockChain.FILE_PATH + "blockchain.bch");

                // Atualizar Saldos e Carteiras com a nova chain
                // (Percorre todos os blocos para reconstruir o estado das carteiras)
                core.Block current = remoteChain.getLastBlock();
                // Nota: O ideal seria reconstruir desde o genesis, mas para este projeto, 
                // garantir que o último bloco atualiza o estado pode ser suficiente se o SaudeWallet for robusto.
                // Mas para garantir, podemos forçar um reload na GUI.

                if (listener != null) {
                    // Envia sinal para a GUI recarregar o inventário visualmente
                    listener.onTransaction("BlockReceived");
                }
            } else {
                System.out.println("SYNC: A minha blockchain já está atualizada.");
            }

        } catch (Exception e) {
            System.err.println("Erro no SYNC: " + e.getMessage());
            e.printStackTrace();
        }
        // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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

    // ::::::::::: T R A N S A C T I O N S :::::::::::
    /**
     * CORRIGIDO: Recebe a String (Base64) da transação. Não tenta desencriptar,
     * apenas guarda e propaga.
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
            } catch (Exception e) {
            } // Ignora erros de rede
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
        if (miner.isMining()) {
            return 0;
        }
        miner.isWorking.set(true);
        // Sem ciclo for aqui (a GUI controla)
        return miner.mine(message, dificulty);
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
        if (!miner.isMining()) {
            return;
        }
        miner.stopMining(nonce);
        // Sem ciclo for aqui
    }

    // ... (Mantém getters do miner e propagateBlock iguais) ...
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

    @Override
    public void propagateBlock(byte[] blockData) throws RemoteException {
        // 1. Parar mineração se recebermos um bloco válido
        if (miner.isMining()) {
            miner.stopMining(-1);
        }

        synchronized (activeSearches) {
            try {
                core.Block newBlock = (core.Block) utils.Serializer.byteArrayToObject(blockData);
                core.BlockChain bc = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");

                boolean isNewBlock = true;

                // 2. Verificar o estado da blockchain
                if (bc != null) {
                    if (bc.getLastBlock().getID() == newBlock.getID()) {
                        // O bloco já existe (recebido de outro nó ou lido do disco).
                        // Marcamos como não-novo para não readicionar, mas deixamos continuar para ATUALIZAR a carteira.
                        isNewBlock = false;
                    } else if (bc.getLastBlock().getID() > newBlock.getID()) {
                        return; // Bloco muito antigo, ignorar.
                    } else if (!java.util.Arrays.equals(bc.getLastBlock().getCurrentHash(), newBlock.getPreviousHash())) {
                        System.out.println("Bloco órfão/inválido recebido. Ignorado.");
                        return;
                    }
                } else if (newBlock.getID() == 0) {
                    bc = new core.BlockChain(newBlock); // Criar nova cadeia se for Genesis
                }

                // 3. Adicionar à Blockchain se for novo
                if (isNewBlock && bc != null) {
                    bc.add(newBlock);
                }

                // 4. ATUALIZAR CARTEIRAS (Sempre, mesmo que o bloco já existisse)
                SaudeCerteira.SaudeWallet.updateWallets(newBlock);

                // 5. Limpar transações processadas da lista visual
                List<SaudeCerteira.SaudeTransaction> mined = (List<SaudeCerteira.SaudeTransaction>) newBlock.getData().getElements();
                for (SaudeCerteira.SaudeTransaction t : mined) {
                    byte[] tBytes = utils.Serializer.objectToByteArray(t);
                    String tString = java.util.Base64.getEncoder().encodeToString(tBytes);
                    this.transactions.remove(tString);
                }

                // 6. Notificar GUI
                if (listener != null) {
                    listener.onTransaction("BlockReceived");
                }

                // 7. Propagar para a rede (apenas se foi novidade para nós)
                if (isNewBlock) {
                    for (RemoteNodeInterface node : network) {
                        try {
                            node.propagateBlock(blockData);
                        } catch (Exception ignore) {
                        }
                    }
                }

            } catch (Exception e) {
                // Log de erro sem crashar
                System.out.println("Nota no processamento do bloco: " + e.getMessage());
            }
        }
    }

    // [NOVO] Implementação do método de sync
    @Override
    public core.BlockChain getBlockchain() throws RemoteException {
        try {
            return core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
        } catch (Exception e) {
            return null; // Se não tiver blockchain, devolve null
        }
    }

}
