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
import utils.FolderUtils;

/**
 * Carteira de Saúde - Gere Histórico e Inventário Seguro
 * @author manso - computer
 */
public class SaudeWallet implements Serializable {
    
    public static final String FILE_PATH = "data_wallet/";

    String user; // Nome do utilizador
    List<WalletTransaction> transactions; // Histórico

    // --- REMOVIDO: drugInventory (Obsoleto com encriptação) ---
    // Map<String, Integer> drugInventory; 

    private SaudeWallet(String user) {
        this.user = user;
        this.transactions = new ArrayList<>();
        // this.drugInventory = new HashMap<>(); // Removido
    }

    // ... (Métodos create, save, load mantêm-se IGUAIS) ...
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
        if (!(new File(FILE_PATH).exists())) new File(FILE_PATH).mkdirs();
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
     * Adiciona transação (Apenas guarda histórico, não atualiza stock aqui)
     */
    public void add(WalletTransaction trans) throws Exception {
        // 1. VERIFICAR DUPLICADOS
        for (WalletTransaction w : this.transactions) {
            if (java.util.Arrays.equals(w.getTransaction().getSignature(), trans.getTransaction().getSignature())) {
                return; 
            }
        }
        // 2. GUARDAR
        this.transactions.add(trans);
        save();
    }

    // ... (updateWallets(Block) e updateWallets(WalletTransaction) mantêm-se IGUAIS) ...
    public static void updateWallets(Block block) throws Exception {
        List<SaudeTransaction> transactions = (List<SaudeTransaction>) block.getData().getElements();
        for (SaudeTransaction t : transactions) {
            List<byte[]> proof = block.getData().getProof(t);
            WalletTransaction w = new WalletTransaction(t, proof, block.getID());
            SaudeWallet.updateWallets(w);
        }
    }

    public static void updateWallets(WalletTransaction t) throws Exception {
        try {
            SaudeWallet sender = load(t.getTransaction().getTxtSender());
            sender.add(t);
        } catch (Exception e) {}

        try {
            SaudeWallet receiver = load(t.getTransaction().getTxtReceiver());
            receiver.add(t);
        } catch (Exception e) {}
    }

    /**
     * [CORRIGIDO] toString já não tenta imprimir o inventário vazio.
     * Mostra apenas resumo técnico. O inventário real é visto na GUI.
     */
    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(this.user);
        txt.append("\n=== HISTÓRICO DE BLOCOS (ENCRIPTADO) ===\n");
        for (WalletTransaction wt : transactions) {
            txt.append(wt.getTransaction().toString())
               .append(" [Block ").append(wt.getBlockID()).append("]\n");
        }
        return txt.toString().trim();
    }

    public String getUser() { return user; }
    public List<WalletTransaction> getTransactions() { return transactions; }

    // --- REMOVIDO: getDrugInventory (Obsoleto) ---

    /**
     * Gera o relatório visual (SNS24) com Stock Infinito para médicos
     */
    public String getInventarioDescodificado(PrivateKey minhaChavePrivada) {
        Map<String, Integer> inventario = new HashMap<>();
        StringBuilder relatorio = new StringBuilder();

        for (WalletTransaction wt : transactions) {
            SaudeTransaction t = wt.getTransaction();
            
            // Tenta abrir o envelope digital
            String[] dados = t.desencriptarConteudo(minhaChavePrivada);
            
            if (dados != null && dados.length == 2) {
                try {
                    int qtd = Integer.parseInt(dados[0]);
                    String medicamento = dados[1];

                    // STOCK INFINITO: Só quem recebe é que acumula.
                    // Médicos emitem sem gastar.
                    if (t.getTxtReceiver().equals(this.user)) {
                        int atual = inventario.getOrDefault(medicamento, 0);
                        inventario.put(medicamento, atual + qtd);
                    }
                    
                } catch (Exception e) {}
            }
        }

        relatorio.append("\n=== 🏥 CARTEIRA DE SAÚDE (SNS24) ===\n");
        if (inventario.isEmpty()) {
            relatorio.append(" (Sem medicamentos disponíveis)\n");
        } else {
            relatorio.append("--- Receitas Disponíveis ---\n");
            for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
                relatorio.append(" 💊 ").append(entry.getKey())
                         .append(": ").append(entry.getValue()).append(" un.\n");
            }
        }
        relatorio.append("====================================\n");
        return relatorio.toString();
    }

    public static BlockChain restartSaudeCerteira() throws Exception {
        FolderUtils.cleanFolder(FILE_PATH, true);           
        FolderUtils.cleanFolder("data_user/", true);        
        FolderUtils.cleanFolder("data_blocks/", true);      

        User.deleteAllUsers();
        BlockChain.deleteAllBlocks();
        
        // Criar Utilizadores
        SaudeWallet.create("Master", "123qwe", "01/01/1980", "111", "111", "M", "PT", "Tomar", "Hospital", "111", "911", true, "Hospital Central");
        SaudeWallet.create("System", "123qwe", "01/01/1980", "222", "222", "M", "PT", "Server", "Cloud", "222", "922", true, "System Root");
        SaudeWallet.create("aa", "aa", "01/01/2000", "000", "000", "M", "PT", "Lisboa", "Rua A", "000", "900", false, "Clínica A");
        
        // Criar Bloco Genesis
        // Nota: Com stock infinito, esta transação é opcional, mas serve para testar o sistema.
        ArrayList<SaudeTransaction> data = new ArrayList<>();
        SaudeTransaction t = new SaudeTransaction("System", "Master", Integer.MAX_VALUE, "Osvyrti");
        User uSystem = User.login("System", "123qwe");
        t.sign(uSystem.getPrivateKey());
        data.add(t);
        
        Block genesis = new Block(0, new byte[32], 3, data); 
        genesis.mine();
        
        BlockChain blockchain = new BlockChain(genesis);
        WalletTransaction w = new WalletTransaction(t, genesis.getData().getProof(t), 0);
        SaudeWallet.updateWallets(w);
        
        return blockchain;
    }
    
    public PrivateKey getPrivateKey(String nome) throws Exception {
        User user = User.login(nome);
        return user.getPrivateKey();
    }

    private static final long serialVersionUID = 202510141301L;
}