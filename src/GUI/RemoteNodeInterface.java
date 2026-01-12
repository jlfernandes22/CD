package GUI;

import SaudeCerteira.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteNodeInterface extends Remote {

    //:::: N E T W O R K :::::::::::
    public String getAdress() throws RemoteException;
    
    // Pesquisa P2P
    public User findUserRemote(String username, String searchID, int ttl) throws RemoteException;
    
    // Propagação de Blocos
    public core.BlockChain getBlockchain() throws RemoteException;
    
    public void propagateBlock(byte[] blockData) throws RemoteException;
    
    public void addNode(RemoteNodeInterface node) throws RemoteException;

    public List<RemoteNodeInterface> getNetwork() throws RemoteException;

    //::::::::::: T R A N S A C T I O N S :::::::::::
    
    // A transação entra como String (Base64) para ser igual em todos os nós
    public void addTransaction(String dataBase64) throws RemoteException;

    public List<String> getTransactions() throws RemoteException;

    //::::::::::: M I N E R :::::::::::
    public int mine(String message, int dificulty) throws RemoteException;
    public void stopMining(int nonce) throws RemoteException;
    public boolean isMining() throws RemoteException;
    public boolean isWinner() throws RemoteException;
    public int getNonce() throws RemoteException;
    public String getHash() throws RemoteException;
}