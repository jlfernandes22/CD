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

import blockchain2.core.Block;
import blockchain2.core.BlockChain;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    double amount; // saldo (evita estar a processar as transações)

    /**
     * construtor privado 
     * @param user nome do utilizador
     */
    private SaudeWallet(String user) {
        this.user = user;
        this.transactions = new ArrayList<>();
        this.amount = 0.0;
    }
    /**
     * Cria uma nova carteira e as respetivas credenciais (public, privada e AES)
     * @param user nome do utilizador
     * @param password password 
     * @return carteira criada
     * @throws Exception 
     */
    public static SaudeWallet create(String user, String password) throws Exception { 
       return create(User.register(user, password));
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
        if (trans.getTransaction().getTxtReceiver().equalsIgnoreCase(this.user)) {
            amount += trans.getTransaction().getValue();
        } else if (trans.getTransaction().getTxtSender().equalsIgnoreCase(this.user)) {
            amount -= trans.getTransaction().getValue();
        } else {
            throw new Exception("Wrong wallet");
        }
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
        txt.append("\nAmount : " + this.amount);
        txt.append("\nTRANSACTIONS\n");

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

    public double getAmount() {
        return amount;
    }


    /**
     * reinicia a templar coin
     * @return blockchain com o bloco genesis
     * @throws Exception 
     */
    public static BlockChain restartTemplarCoin() throws Exception {
        //apagar as pastas com os ficheiros
        //####################################
        //apagar as carteiros
        FolderUtils.cleanFolder("FILE_PATH", true);
        //apagar os utilizadores
        User.deleteAllUsers();
        //apagar a blockchain
        BlockChain.deleteAllBlocks();
        //criar as carteiras do master e do systema
        SaudeWallet.create("Master", "123qwe");
        SaudeWallet.create("System", "123qwe");
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


///////////////////////////////////////////////////////////////////////////
}
