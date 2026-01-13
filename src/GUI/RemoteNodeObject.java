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

            // 1. Obter a chain remota
            core.BlockChain remoteChain = node.getBlockchain();
            
            // [CORREÇÃO] Carregar chain local de forma segura
            core.BlockChain localChain = null;
            try {
                localChain = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
            } catch (Exception e) {
                // Se der erro (ficheiro não existe), assumimos null para forçar o download
                localChain = null;
                System.out.println("Blockchain local não encontrada ou vazia.");
            }

            // 2. Decidir se devemos atualizar
            boolean devoAtualizar = false;

            if (remoteChain != null) {
                if (localChain == null) {
                    devoAtualizar = true; // Sou novo (ou load falhou), aceito tudo
                } else {
                    // Se o último bloco dele tiver ID maior, ele está à frente
                    if (remoteChain.getLastBlock().getID() > localChain.getLastBlock().getID()) {
                        devoAtualizar = true;
                    }
                }
            }

            // 3. Aplicar a atualização (Download ZIP)
            if (devoAtualizar) {
                System.out.println("SYNC: Recebida blockchain maior. A atualizar...");

                // Pede os dados ZIP ao nó remoto
                byte[] zipData = node.getBlockchainData(); // Certifique-se que adicionou este método à Interface e ao Objecto Remoto!

                if (zipData != null && zipData.length > 0) {
                    // Criar referência para a pasta
                    java.io.File folder = new java.io.File("data_blocks");
                    
                    // A. Limpar a pasta atual (se existir)
                    if (folder.exists()) {
                        java.io.File[] files = folder.listFiles();
                        if (files != null) {
                            for (java.io.File f : files) {
                                if (f.isFile()) f.delete();
                            }
                        }
                    } else {
                        // Se não existir, CRIA A PASTA AGORA
                        folder.mkdirs();
                    }

                    // B. Descomprimir o ZIP recebido
                    utils.Zip.unzipFolder(zipData, folder);
                    System.out.println("SYNC: Ficheiros descomprimidos com sucesso em data_blocks/");

                    // C. Recarregar a memória
                    try {
                        core.BlockChain novaChain = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");
                        
                        // D. Atualizar Saldos e Carteiras
                        if (novaChain != null) {
                            SaudeCerteira.SaudeWallet.updateWallets(novaChain.getLastBlock());
                        }
                    } catch (Exception ex) {
                        System.out.println("Erro ao carregar nova blockchain: " + ex.getMessage());
                    }

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

        // Variável para saber se devemos enviar aos vizinhos
        boolean shouldPropagate = false;

        // :::::::: FASE 1: ATUALIZAÇÃO LOCAL (COM LOCK) ::::::::
        synchronized (activeSearches) {
            try {
                core.Block newBlock = (core.Block) utils.Serializer.byteArrayToObject(blockData);
                core.BlockChain bc = core.BlockChain.load(core.BlockChain.FILE_PATH + "blockchain.bch");

                boolean isNewBlock = true;

                // Verificar o estado da blockchain
                if (bc != null) {
                    if (bc.getLastBlock().getID() == newBlock.getID()) {
                        isNewBlock = false; 
                    } else if (bc.getLastBlock().getID() > newBlock.getID()) {
                        return; // Bloco velho, sai logo
                    } else if (!java.util.Arrays.equals(bc.getLastBlock().getCurrentHash(), newBlock.getPreviousHash())) {
                        System.out.println("Bloco órfão/inválido recebido. Ignorado.");
                        return;
                    }
                } else if (newBlock.getID() == 0) {
                     bc = new core.BlockChain(newBlock);
                }

                // Adicionar à Blockchain se for novo
                if (isNewBlock && bc != null) {
                    bc.add(newBlock);
                    shouldPropagate = true; // Define flag para enviar fora do lock
                }

                // ATUALIZAR CARTEIRAS (Sempre)
                try {
                    SaudeCerteira.SaudeWallet.updateWallets(newBlock);
                } catch (Exception e) {
                    System.out.println("Aviso: Erro ao atualizar carteiras (mas o bloco é válido): " + e.getMessage());
                    // Opcional: e.printStackTrace(); para ver o erro detalhado no log
                }

                // Limpar transações
                List<SaudeCerteira.SaudeTransaction> mined = (List<SaudeCerteira.SaudeTransaction>) newBlock.getData().getElements();
                for (SaudeCerteira.SaudeTransaction t : mined) {
                    byte[] tBytes = utils.Serializer.objectToByteArray(t);
                    String tString = java.util.Base64.getEncoder().encodeToString(tBytes);
                    this.transactions.remove(tString);
                }

                // Notificar GUI
                if (listener != null) {
                    listener.onTransaction("BlockReceived");
                }

            } catch (Exception e) {
                System.out.println("Nota no processamento do bloco: " + e.getMessage());
                return; // Se deu erro, não propaga
            }
        } // <--- FIM DO SYNCHRONIZED (Lock libertado aqui)

        
        // :::::::: FASE 2: PROPAGAÇÃO REMOTA (SEM LOCK) ::::::::
        // Isto impede que o RMI congele à espera de resposta
        if (shouldPropagate) {
            new Thread(() -> {
                for (RemoteNodeInterface node : network) {
                    try {
                        // Envia e não bloqueia o sistema principal
                        node.propagateBlock(blockData);
                    } catch (Exception ignore) {
                        // Node offline, ignorar
                    }
                }
            }).start();
        }
    }
    
    // Implementação do método que o outro nó chama para sacar a blockchain
    @Override
    public byte[] getBlockchainData() throws RemoteException {
        try {
            java.io.File folder = new java.io.File("data_blocks");
            
            // Se a pasta não existir ou estiver vazia, tenta enviar apenas o ficheiro .bch se estiver na raiz
            if (!folder.exists() || folder.listFiles().length == 0) {
                // (Opcional) Lógica de fallback se quiser
                return null; 
            }
            
            System.out.println("A enviar pasta data_blocks comprimida...");
            return utils.Zip.zipFolder(folder);
            
        } catch (Exception e) {
            System.err.println("Erro ao zipar blockchain: " + e.getMessage());
            return null;
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
