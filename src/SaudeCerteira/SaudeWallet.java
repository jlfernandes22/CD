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
 *
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
            String NISS, String telemovel, String role, String unidadeSaude) throws Exception {
        return create(User.register(name, password, dataNascimento, identidadeCC,
                numeroUtente, sexo, paisnacionalidade, naturalidade, morada,
                NISS, telemovel, role, unidadeSaude));
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
        } catch (Exception e) {
        }

        try {
            SaudeWallet receiver = load(t.getTransaction().getTxtReceiver());
            receiver.add(t);
        } catch (Exception e) {
        }
    }

    /**
     * [CORRIGIDO] toString já não tenta imprimir o inventário vazio. Mostra
     * apenas resumo técnico. O inventário real é visto na GUI.
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

    public String getUser() {
        return user;
    }

    public List<WalletTransaction> getTransactions() {
        return transactions;
    }

    // --- REMOVIDO: getDrugInventory (Obsoleto) ---
    /**
     * Gera o relatório visual (SNS24) com Stock Infinito para médicos
     */
    public String getInventarioDescodificado(PrivateKey minhaChavePrivada) {
        Map<String, Integer> inventario = new HashMap<>();
        StringBuilder relatorio = new StringBuilder();

        // 1. Carregar o Role (Papel) do utilizador a partir do disco
        String role = "Utente"; // Valor predefinido
        try {
            java.io.File userFile = new java.io.File("data_user/" + this.user + ".user");
            if (userFile.exists()) {
                try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(userFile))) {
                    User u = (User) in.readObject();
                    if (u.getRole() != null) {
                        role = u.getRole();
                    }
                }
            }
        } catch (Exception e) {
            // Se der erro, mantém-se como "Utente" por segurança
        }

        // 2. Percorrer Transações para calcular saldo
        for (WalletTransaction wt : transactions) {
            SaudeTransaction t = wt.getTransaction();

            // Tenta abrir o envelope digital com a chave privada
            String[] dados = t.desencriptarConteudo(minhaChavePrivada);

            if (dados != null && dados.length == 2) {
                try {
                    int qtd = Integer.parseInt(dados[0]);
                    String medicamento = dados[1];

                    // CASO A: RECEBI -> SOMA SEMPRE
                    // (Utente recebe do Médico) ou (Farmácia recebe do Utente)
                    if (t.getTxtReceiver().equals(this.user)) {
                        int atual = inventario.getOrDefault(medicamento, 0);
                        inventario.put(medicamento, atual + qtd);
                    }

                    if (t.getTxtSender().equals(this.user)) {

                        if ("Utente".equals(role)) {
                            int atual = inventario.getOrDefault(medicamento, 0);
                            inventario.put(medicamento, atual - qtd);
                        }
                    }

                } catch (Exception e) {
                }
            }
        }

        // 3. Construir o Relatório Personalizado
        relatorio.append("\n=== 🏥 CARTEIRA DIGITAL (").append(role.toUpperCase()).append(") ===\n");

        if ("Médico".equals(role)) {
            relatorio.append(" [MODO CLÍNICO: Emissão Ilimitada]\n");
        } else if ("Farmácia".equals(role)) {
            relatorio.append(" [MODO FARMÁCIA: Receitas Aviadas/Stock]\n");
        }

        if (inventario.isEmpty()) {
            relatorio.append(" (Sem registos)\n");
        } else {
            relatorio.append("--- Histórico de Medicamentos ---\n");
            for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
                // Se quiser mostrar mesmo quando é 0 (para provar que enviou), remova o if
                if (entry.getValue() > 0) {
                    relatorio.append(" 💊 ").append(entry.getKey())
                            .append(": ").append(entry.getValue()).append(" un.\n");
                }
            }
        }
        relatorio.append("====================================\n");
        return relatorio.toString();
    }

    /**
     * Verifica se o utilizador tem stock suficiente para realizar a transação.
     */
    public boolean possoEnviar(String medicamentoAlvo, int qtdDesejada, PrivateKey minhaChavePrivada) {
        // 1. Carregar Role
        String role = "Utente";
        try {
            java.io.File userFile = new java.io.File("data_user/" + this.user + ".user");
            if (userFile.exists()) {
                try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(userFile))) {
                    User u = (User) in.readObject();
                    if (u.getRole() != null) {
                        role = u.getRole();
                    }
                }
            }
        } catch (Exception e) {
        }

        // SE FOR MÉDICO, PODE SEMPRE (Stock Infinito)
        if ("Médico".equals(role)) {
            return true;
        }

        // 2. Calcular Stock Atual (Igual ao getInventarioDescodificado)
        int stockAtual = 0;

        for (WalletTransaction wt : transactions) {
            SaudeTransaction t = wt.getTransaction();
            String[] dados = t.desencriptarConteudo(minhaChavePrivada);

            if (dados != null && dados.length == 2) {
                try {
                    int qtd = Integer.parseInt(dados[0]);
                    String med = dados[1];

                    // Só nos interessa o medicamento que queremos enviar
                    if (med.equals(medicamentoAlvo)) {
                        // Recebi -> Soma
                        if (t.getTxtReceiver().equals(this.user)) {
                            stockAtual += qtd;
                        }
                        // Enviei -> Subtrai (Já sabemos que não sou Médico)
                        if (t.getTxtSender().equals(this.user)) {
                            stockAtual -= qtd;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        // 3. Verificar se chega
        return stockAtual >= qtdDesejada;
    }

    public static BlockChain restartSaudeCerteira() throws Exception {
        FolderUtils.cleanFolder(FILE_PATH, true);
        FolderUtils.cleanFolder("data_user/", true);
        FolderUtils.cleanFolder("data_blocks/", true);

        User.deleteAllUsers();
        BlockChain.deleteAllBlocks();

        // Criar Utilizadores
        SaudeWallet.create("Master", "123qwe", "01/01/1980", "111", "111", "M", "PT", "Tomar", "Hospital", "111", "911", "Médico", "Hospital Central");
        SaudeWallet.create("System", "123qwe", "01/01/1980", "222", "222", "M", "PT", "Server", "Cloud", "222", "922", "Farmacêutico", "System Root");
        SaudeWallet.create("aa", "aa", "01/01/2000", "000", "000", "M", "PT", "Lisboa", "Rua A", "000", "900", "Utente", "Clínica A");

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
