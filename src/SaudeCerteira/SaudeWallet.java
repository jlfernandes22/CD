package SaudeCerteira;

import core.Block;
import core.BlockChain;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * Carteira de Saúde - Gere Inventário de Medicamentos
 * @author manso - computer
 */
public class SaudeWallet implements Serializable {
    
    // Pasta com os ficheiros das carteiras
    public static final String FILE_PATH = "data_wallet/";

    String user; // Nome do utilizador
    List<WalletTransaction> transactions; // Histórico
    
    // INVENTÁRIO: Nome do Medicamento -> Quantidade
    Map<String, Integer> drugInventory; 

    /**
     * Construtor privado 
     * @param user nome do utilizador
     */
    private SaudeWallet(String user) {
        this.user = user;
        this.transactions = new ArrayList<>();
        this.drugInventory = new HashMap<>();
    }

    /**
     * Cria uma nova carteira e as respetivas credenciais
     */
    public static SaudeWallet create(String name, String password, String dataNascimento, 
                                String identidadeCC, String numeroUtente, String sexo,
                                String paisnacionalidade, String naturalidade, String morada,
                                String NISS, String telemovel, boolean medico, String unidadeSaude) throws Exception { 
        
       return create(User.register(name, password, dataNascimento, identidadeCC, 
                                    numeroUtente, sexo, paisnacionalidade, naturalidade, morada, 
                                    NISS, telemovel, medico, unidadeSaude));
    }

    public static SaudeWallet create(User newUSer) throws Exception {
        SaudeWallet w = new SaudeWallet(newUSer.getUserName());
        w.save();
        return w;
    }

    public void save() throws Exception {
        if (!(new File(FILE_PATH).exists())) {
            new File(FILE_PATH).mkdirs();
        }
        String fileName = FILE_PATH + user + ".wlt";
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }

    public static SaudeWallet load(String user) throws Exception {
        String fileName = FILE_PATH + user + ".wlt";
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (SaudeWallet) in.readObject();
        }
    }

    /**
     * Adiciona uma transação e atualiza o inventário
     */
    public void add(WalletTransaction trans) throws Exception {
        // 1. Obter Detalhes
        String drugName = trans.getTransaction().getDescription(); 
        int quantity = (int) trans.getTransaction().getValue();

        // 2. Lógica para quem RECEBE (Ganha Medicamentos)
        if (trans.getTransaction().getTxtReceiver().equalsIgnoreCase(this.user)) {
            int currentStock = drugInventory.getOrDefault(drugName, 0);
            drugInventory.put(drugName, currentStock + quantity);
        } 
        // 3. Lógica para quem ENVIA (Perde Medicamentos)
        else if (trans.getTransaction().getTxtSender().equalsIgnoreCase(this.user)) {
            int currentStock = drugInventory.getOrDefault(drugName, 0);
            int newStock = currentStock - quantity;
            
            if (newStock < 0) newStock = 0; 
            
            drugInventory.put(drugName, newStock);
        } 
        
        // 4. Guardar
        transactions.add(trans);
        save();
    }

    public static void updateWallets(Block block) throws Exception {
        List<SaudeTransaction> transactions = (List<SaudeTransaction>) block.getData().getElements();
        for (SaudeTransaction t : transactions) {
            List<byte[]> proof = block.getData().getProof(t);
            WalletTransaction w = new WalletTransaction(t, proof, block.getID());
            SaudeWallet.updateWallets(w);
        }
    }

    /**
     * Atualiza as carteiras envolvidas na transação.
     * Importante: Usa try-catch individuais para suportar Mineração P2P.
     * (Se eu for um mineiro e não tiver a carteira do médico X, ignoro o erro e continuo).
     */
    public static void updateWallets(WalletTransaction t) throws Exception {
        // Atualizar Remetente (Médico)
        try {
            SaudeWallet sender = load(t.getTransaction().getTxtSender()); // Usar getter corrigido
            sender.add(t);
        } catch (Exception e) {
            // Ignorar se não tivermos a carteira localmente (normal em P2P)
            // System.out.println("Info: Carteira sender não encontrada localmente.");
        }

        // Atualizar Destinatário (Paciente)
        try {
            SaudeWallet receiver = load(t.getTransaction().getTxtReceiver()); // Usar getter corrigido
            receiver.add(t);
        } catch (Exception e) {
            // Ignorar se não tivermos a carteira localmente
        }
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(this.user);
        
        txt.append("\n=== INVENTÁRIO DE MEDICAMENTOS ===\n");
        if(drugInventory.isEmpty()){
             txt.append(" (Vazio)\n");
        } else {
            for (Map.Entry<String, Integer> entry : drugInventory.entrySet()) {
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

    public String getUser() { return user; }
    public List<WalletTransaction> getTransactions() { return transactions; }

    public Map<String, Integer> getDrugInventory() {
        if (drugInventory == null) {
            drugInventory = new HashMap<>();
        }
        return drugInventory;
    }

    /**
     * Reinicia todo o sistema e cria o Bloco Genesis com Stock Inicial
     */
    public static BlockChain restartSaudeCerteira() throws Exception {
        // 1. Limpar TODAS as pastas de dados
        FolderUtils.cleanFolder(FILE_PATH, true);           // data_wallet/
        // Certifique-se que estas pastas correspondem às usadas no User.java e BlockChain.java
        FolderUtils.cleanFolder("data_user/", true);        
        FolderUtils.cleanFolder("data_blocks/", true);      

        User.deleteAllUsers();
        BlockChain.deleteAllBlocks();
        
        // 2. Criar Utilizadores Fundadores
        // Master (Médico Principal)
        SaudeWallet.create("Master", "123qwe", "01/01/1980", "111", "111", "M", "PT", "Tomar", "Hospital", "111", "911", true, "Hospital Central");
        
        // System (Entidade que gera o stock inicial)
        SaudeWallet.create("System", "123qwe", "01/01/1980", "222", "222", "M", "PT", "Server", "Cloud", "222", "922", true, "System Root");
        
        // --- NOVO: Utilizador de Teste "aa" (Paciente) ---
        // Pass "aa", Medico = false
        SaudeWallet.create("aa", "aa", "01/01/2000", "000", "000", "M", "PT", "Lisboa", "Rua A", "000", "900", false, "Clínica A");
        
        ArrayList<SaudeTransaction> data = new ArrayList<>();
        
        // 3. Transação Genesis: System envia 100 'Osvyrti' para o Master
        // Nota: O nome "Osvyrti" deve ser igual ao que está no CSV para aparecer na ComboBox
        SaudeTransaction t = new SaudeTransaction("System", "Master", 100, "Osvyrti");
        
        // Assinar a transação com a chave do System
        User uSystem = User.login("System", "123qwe");
        t.sign(uSystem.getPrivateKey());
        
        data.add(t);
        
        // 4. Minar Bloco Genesis
        // O hash anterior é zero (byte[32])
        Block genesis = new Block(0, new byte[32], 3, data); 
        genesis.mine();
        
        // 5. Guardar Blockchain e Atualizar Carteira do Master
        BlockChain blockchain = new BlockChain(genesis);
        
        // Converter transação normal para transação de carteira (com prova Merkle)
        WalletTransaction w = new WalletTransaction(t, genesis.getData().getProof(t), 0);
        
        // Isto vai colocar os 100 Osvyrti na carteira do Master
        SaudeWallet.updateWallets(w);
        
        return blockchain;
    }
    
    // Método auxiliar para obter chave privada (usado na GUI)
    public PrivateKey getPrivateKey(String nome) throws Exception {
        User user = User.login(nome);
        return user.getPrivateKey();
    }

    private static final long serialVersionUID = 202510141301L;
}