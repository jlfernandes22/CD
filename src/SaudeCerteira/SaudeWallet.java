//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package SaudeCerteira;

import core.Block;
import core.BlockChain;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import utils.FolderUtils;

/**
 * Created on 14/10/2025, 13:01:23
 *
 * @author manso - computer
 */
public class SaudeWallet implements Serializable {
    //pasta com os ficheiros das carteiras
    public static final String FILE_PATH = "data_wallet/";

    String user; //nome do utilizador
    List<WalletTransaction> transactions; //listas das transações
    Map<String, Integer> drugInventory; // saldo (evita estar a processar as transações)

    /**
     * construtor privado 
     * @param user nome do utilizador
     */
    private SaudeWallet(String user) {
        this.user = user;
        this.transactions = new ArrayList<>();
        this.drugInventory = new HashMap<>();
    }
    /**
     * Cria uma nova carteira e as respetivas credenciais (public, privada e AES)
     * @param name
     * @param dataNascimento
     * @param identidadeCC
     * @param medico
     * @param numeroUtente
     * @param sexo
     * @param paisnacionalidade
     * @param naturalidade
     * @param morada
     * @param NISS
     * @param telemovel
     * @param password password 
     * @param unidadeSaude 
     * @return carteira criada
     * @throws Exception 
     */
    public static SaudeWallet create(String name, String password, String dataNascimento, 
                                String identidadeCC, String numeroUtente, String sexo,
                                String paisnacionalidade, String naturalidade, String morada,
                                String NISS, String telemovel, boolean medico, String unidadeSaude) throws Exception { 
    
       return create(User.register(name, password, dataNascimento, identidadeCC, 
                                    numeroUtente, sexo, paisnacionalidade, naturalidade, morada, 
                                    NISS, telemovel, medico, unidadeSaude));
    }
    /**
     * Cria uma carteira com um utilizador já existente
     * @param newUSer utilizador
     * @return
     * @throws Exception 
     */
    public static SaudeWallet create(User newUSer) throws Exception {
        SaudeWallet w = new SaudeWallet(newUSer.getUserName());
        w.save();
        return w;
    }

    /**
     * Guarda a carteira num ficheiro
     * @throws Exception 
     */
    public void save() throws Exception {
        //cruar a path caso nao exista
        if (!(new File(FILE_PATH).exists())) {
            new File(FILE_PATH).mkdirs();
        }
        //guardar os dados
        String fileName = FILE_PATH + user + ".wlt";
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }
    /**
     * ler a carteira
     * @param user nome do utilizador
     * @return
     * @throws Exception 
     */
    public static SaudeWallet load(String user) throws Exception {
        String fileName = FILE_PATH + user + ".wlt";
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (SaudeWallet) in.readObject();
        }
    }

    /**
     * adiciona uma transação e atualiza o saldo
     * @param trans transação
     * @throws Exception 
     */
    public void add(WalletTransaction trans) throws Exception {
        // 1. Get Transaction Details
        // We assume 'description' is the Drug Name and 'value' is the Quantity
        String drugName = trans.getTransaction().getDescription(); 
        int quantity = (int) trans.getTransaction().getValue();

        // 2. Logic for Receiver (GAINS Drugs)
        if (trans.getTransaction().getTxtReceiver().equalsIgnoreCase(this.user)) {
            // Add quantity to current stock
            int currentStock = drugInventory.getOrDefault(drugName, 0);
            drugInventory.put(drugName, currentStock + quantity);
        } 
        // 3. Logic for Sender (LOSES Drugs)
        else if (trans.getTransaction().getTxtSender().equalsIgnoreCase(this.user)) {
            // Subtract quantity from current stock
            int currentStock = drugInventory.getOrDefault(drugName, 0);
            int newStock = currentStock - quantity;
            
            // Prevent negative stock (optional safety check, though validation should happen before mining)
            if (newStock < 0) newStock = 0; 
            
            drugInventory.put(drugName, newStock);
        } 
        
        // 4. Save Transaction History
        transactions.add(trans);
        save();
    }

    /**
     * atulaliza as carteiras que fazem parte das transações do bloco
     * @param block bloco
     * @throws Exception 
     */
    public static void updateWallets(Block block) throws Exception {
        //lista das transações do bloco (Merkle tree)
        List<SaudeTransaction> transactions = (List<SaudeTransaction>) block.getData().getElements();
        //update wallets
        for (SaudeTransaction t : transactions) {
            //obter a prova 
            List<byte[]> proof = block.getData().getProof(t);
            // fazer uma transacao de carte
            WalletTransaction w = new WalletTransaction(t, proof, block.getID());
            //atualizar as carteira
            SaudeWallet.updateWallets(w);
        }
    }
    /**
     * atualiza as carteira
     * @param t transação de carteira
     * @throws Exception 
     */
    public static void updateWallets(WalletTransaction t) throws Exception {
        try {
            SaudeWallet sender = load(t.getTransaction().txtSender);
            sender.add(t);
        } catch (Exception e) {
            System.getLogger(SaudeWallet.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            JOptionPane.showMessageDialog(null, "wallet Sender error: " + t.getTransaction().txtSender, "update Wallet", JOptionPane.WARNING_MESSAGE);
        }

        try {
            SaudeWallet receiver = load(t.getTransaction().txtReceiver);
            receiver.add(t);
        } catch (Exception e) {
            System.getLogger(SaudeWallet.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            JOptionPane.showMessageDialog(null, "wallet receiver error: " + t.getTransaction().txtReceiver, "update Wallet", JOptionPane.WARNING_MESSAGE);
        }

    }
    
    

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(this.user);
        
        // Display Inventory instead of Balance
        txt.append("\n=== INVENTÁRIO DE MEDICAMENTOS ===\n");
        if(drugInventory.isEmpty()){
             txt.append(" (Vazio)\n");
        } else {
            for (Map.Entry<String, Integer> entry : drugInventory.entrySet()) {
                // Example: Paracetamol : 50
                txt.append(" - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        txt.append("==================================\n");
        txt.append("Histórico de Transações:\n");
        for (WalletTransaction transaction : transactions) {
            txt.append(transaction.toString()).append("\n");
        }
        return txt.toString().trim();
    }

    public String getUser() {
        return user;
    }

    public List<WalletTransaction> getTransactions() {
        return transactions;
    }

   public Map<String, Integer> getDrugInventory() {
        // Se por acaso estiver null (de wallets antigas), cria um novo para não dar erro
        if (drugInventory == null) {
            drugInventory = new HashMap<>();
        }
        return drugInventory;
    }


    /**
     * reinicia a templar coin
     * @return blockchain com o bloco genesis
     * @throws Exception 
     */
    public static BlockChain restartSaudeCerteira() throws Exception {
        //apagar as pastas com os ficheiros
        //####################################
        //apagar as carteiros
        FolderUtils.cleanFolder("FILE_PATH", true);
        //apagar os utilizadores
        User.deleteAllUsers();
        //apagar a blockchain
        BlockChain.deleteAllBlocks();
        //criar as carteiras do master e do systema
        SaudeWallet.create("Master", "123qwe",null,null,null,null,null,null,null,null,null,true,null);
        SaudeWallet.create("System", "123qwe",null,null,null,null,null,null,null,null,null,true,null);
        //criar a transação que transfere 1000 moedas do system para o master
        ArrayList<SaudeTransaction> data = new ArrayList<>();
        SaudeTransaction t = new SaudeTransaction("System", "Master", 1000, "123qwe");
        data.add(t);
        //criar o bloco genesis com a transação
        Block genesis = new Block(0, new byte[]{0, 0, 0, 0}, 3, data);
        //minar a transaçao
        genesis.mine();
        //criar uma blockchain com a transação
        BlockChain blockchain = new BlockChain(genesis);
        WalletTransaction w = new WalletTransaction(t, genesis.getData().getProof(t), 0);
        //atualizar as carteiras do master e do system
        SaudeWallet.updateWallets(w);
        return blockchain;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510141301L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::

    public PrivateKey getPrivateKey(String nome) throws Exception {
        User user = User.login(nome);
        return user.getPrivateKey();
    }



///////////////////////////////////////////////////////////////////////////
}
