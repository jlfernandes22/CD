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
 * Representa a Carteira Digital (Wallet) de um utilizador no sistema de saúde.
 * <p>
 * Diferente de uma carteira de criptomoedas tradicional, esta carteira gere
 * <b>registos médicos e receitas</b>.
 * <p>
 * <b>Princípio de Funcionamento:</b>
 * <ul>
 * <li>A carteira não guarda o saldo final (ex: "Tens 10 Ben-u-ron").</li>
 * <li>A carteira guarda o <b>histórico completo</b> de transações (Entradas e
 * Saídas).</li>
 * <li>O saldo/stock é calculado dinamicamente a cada pedido, reprocessando todo
 * o histórico.</li>
 * </ul>
 *
 * @author aluno_25979, aluno_25946
 * @version 2.0
 */
public class SaudeWallet implements Serializable {

    /**
     * Diretoria onde as carteiras são persistidas (.wlt).
     */
    public static final String FILE_PATH = "data_wallet/";

    /**
     * Identificador do dono da carteira (Nome de utilizador).
     */
    String user;

    /**
     * Histórico local de transações onde este utilizador esteve envolvido.
     * Serve de base para calcular o estado atual (Inventário).
     */
    List<WalletTransaction> transactions;

    // --- NOTA ARQUITETURAL ---
    // O mapa 'drugInventory' foi removido na versão 2.0.
    // Motivo: O saldo deve ser calculado em tempo real desencriptando a blockchain 
    // com a chave privada, para garantir que o saldo visualizado é real e seguro.
    /**
     * Construtor privado para forçar o uso do padrão Factory (método create).
     *
     * @param user Nome do utilizador.
     */
    private SaudeWallet(String user) {
        this.user = user;
        this.transactions = new ArrayList<>();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GESTÃO DE PERSISTÊNCIA E CRIAÇÃO
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Cria uma nova carteira e regista o utilizador associado no sistema.
     *
     * @param name Nome do utilizador.
     * @param password Palavra-passe.
     * @param role Papel no sistema (Médico, Utente, Farmacêutico).
     * @param unidadeSaude Local de trabalho ou centro de saúde.
     * @return A nova instância de SaudeWallet.
     * @throws Exception Se houver erro no registo ou na escrita em disco.
     */
    public static SaudeWallet create(String name, String password, String dataNascimento,
            String identidadeCC, String numeroUtente, String sexo,
            String paisnacionalidade, String naturalidade, String morada,
            String NISS, String telemovel, String role, String unidadeSaude) throws Exception {
        return create(User.register(name, password, dataNascimento, identidadeCC,
                numeroUtente, sexo, paisnacionalidade, naturalidade, morada,
                NISS, telemovel, role, unidadeSaude));
    }

    /**
     * Cria uma carteira a partir de um objeto User já existente.
     *
     * @param newUSer O utilizador registado.
     * @return A carteira criada e guardada.
     */
    public static SaudeWallet create(User newUSer) throws Exception {
        SaudeWallet w = new SaudeWallet(newUSer.getUserName());
        w.save();
        return w;
    }

    /**
     * Persiste o estado atual da carteira no disco (ficheiro .wlt).
     */
    public void save() throws Exception {
        if (!(new File(FILE_PATH).exists())) {
            new File(FILE_PATH).mkdirs();
        }
        String fileName = FILE_PATH + user + ".wlt";
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }

    /**
     * Carrega a carteira do disco.
     *
     * @param user Nome do utilizador.
     * @return A instância da carteira.
     */
    public static SaudeWallet load(String user) throws Exception {
        String fileName = FILE_PATH + user + ".wlt";
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (SaudeWallet) in.readObject();
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: LÓGICA DE ATUALIZAÇÃO (SYNC BLOCKCHAIN)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Adiciona uma transação ao histórico local.
     * <p>
     * Inclui verificação de duplicados para evitar processar a mesma transação
     * duas vezes caso a blockchain seja recarregada.
     *
     * @param trans A transação a adicionar.
     */
    public void add(WalletTransaction trans) throws Exception {
        // 1. VERIFICAR DUPLICADOS (Idempotência)
        for (WalletTransaction w : this.transactions) {
            if (java.util.Arrays.equals(w.getTransaction().getSignature(), trans.getTransaction().getSignature())) {
                return; // Já existe, ignorar.
            }
        }
        // 2. GUARDAR E PERSISTIR
        this.transactions.add(trans);
        save();
    }

    /**
     * Atualiza as carteiras envolvidas num novo bloco minerado.
     * <p>
     * Este método percorre todas as transações do bloco e distribui-as para as
     * carteiras do Remetente e do Destinatário.
     *
     * @param block O bloco novo recebido da rede.
     */
    public static void updateWallets(Block block) throws Exception {
        List<SaudeTransaction> transactions = (List<SaudeTransaction>) block.getData().getElements();
        for (SaudeTransaction t : transactions) {
            // Obter Prova de Merkle (Merkle Proof) para validação futura
            List<byte[]> proof = block.getData().getProof(t);
            WalletTransaction w = new WalletTransaction(t, proof, block.getID());

            // Atualizar intervenientes
            SaudeWallet.updateWallets(w);
        }
    }

    /**
     * Atualiza especificamente o Remetente e o Destinatário de uma transação.
     *
     * @param t A transação wrapper (com metadados do bloco).
     */
    public static void updateWallets(WalletTransaction t) throws Exception {
        // Atualizar Remetente (Para descontar stock/histórico de envio)
        try {
            SaudeWallet sender = load(t.getTransaction().getTxtSender());
            sender.add(t);
        } catch (Exception e) {
            // Pode falhar se o utilizador não existir localmente (ex: nó remoto)
        }

        // Atualizar Destinatário (Para receber stock)
        try {
            SaudeWallet receiver = load(t.getTransaction().getTxtReceiver());
            receiver.add(t);
        } catch (Exception e) {
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: LÓGICA DE NEGÓCIO E APRESENTAÇÃO
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
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

    /**
     * Gera o inventário descodificado e legível (Dashboard do Utente).
     * <p>
     * <b>Mecanismo Seguro:</b>
     * Percorre todo o histórico encriptado e usa a Chave Privada do utilizador
     * para tentar abrir os envelopes digitais (Dual Envelope).
     * <p>
     * <b>Regras de Negócio por Papel (Role):</b>
     * <ul>
     * <li><b>Médico:</b> Emite receitas (Stock Infinito). Não subtrai ao
     * enviar.</li>
     * <li><b>Utente:</b> Recebe receitas. Subtrai ao enviar para a
     * Farmácia.</li>
     * <li><b>Farmacêutico:</b> Recebe receitas (avia). Subtrai se transferir
     * stock.</li>
     * </ul>
     *
     * @param minhaChavePrivada A chave RSA privada para desencriptar os dados.
     * @return String formatada com o relatório do inventário.
     */
    public String getInventarioDescodificado(PrivateKey minhaChavePrivada) {
        Map<String, Integer> inventario = new HashMap<>();
        StringBuilder relatorio = new StringBuilder();

        // 1. Carregar o Role (Papel) do utilizador a partir do disco
        String role = "Utente"; // Valor predefinido por segurança
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
            // Ignora erro de leitura de role
        }

        // 2. Percorrer Transações para reconstruir o saldo (Replay)
        for (WalletTransaction wt : transactions) {
            SaudeTransaction t = wt.getTransaction();

            // Tenta abrir o envelope digital (Dual Envelope permite ler enviados e recebidos)
            String[] dados = t.desencriptarConteudo(minhaChavePrivada);

            if (dados != null && dados.length == 2) {
                try {
                    int qtd = Integer.parseInt(dados[0]);
                    String medicamento = dados[1];

                    // --- CASO A: RECEBI (Entrada de Stock) ---
                    // Utente recebe do Médico | Farmácia recebe do Utente
                    if (t.getTxtReceiver().equals(this.user)) {
                        int atual = inventario.getOrDefault(medicamento, 0);
                        inventario.put(medicamento, atual + qtd);
                    }

                    // --- CASO B: ENVIEI (Saída de Stock) ---
                    if (t.getTxtSender().equals(this.user)) {

                        // [REGRA DE NEGÓCIO]
                        // Se for "Médico", não subtrai (Stock Infinito/Emissão).
                        // Se for "Utente" ou "Farmacêutico", subtrai do inventário.
                        if (!"Médico".equals(role)) {
                            int atual = inventario.getOrDefault(medicamento, 0);
                            inventario.put(medicamento, atual - qtd);
                        }
                    }

                } catch (Exception e) {
                    // Ignora transações malformadas ou corrompidas
                }
            }
        }

        if (!"Farmacêutico".equals(role)) {
            // 3. Construir o Relatório Visual
            relatorio.append("\n=== 🏥 CARTEIRA DIGITAL (").append(role.toUpperCase()).append(") ===\n");
        }else{
            relatorio.append("\n=== 🏥 CARTEIRA DIGITAL (").append("FARMÁCIA").append(") ===\n");
        }
        

        if ("Médico".equals(role)) {
            relatorio.append(" [MODO CLÍNICO: Emissão de Receitas]\n");
        } else if ("Farmacêutico".equals(role)) {
            relatorio.append(" [MODO FARMÁCIA: Gestão de Stock e Aviamentos]\n");
        }

        if (inventario.isEmpty()) {
            relatorio.append(" (Sem registos ativos)\n");
        } else {
            relatorio.append("--- Histórico e Stock Atual ---\n");
            for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
                // Filtro visual: Mostrar apenas o que existe em stock ou histórico positivo
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
     * Validação de segurança (Guard Clause).
     * <p>
     * Verifica se o utilizador possui stock suficiente antes de criar uma
     * transação. Impede que um Utente envie uma receita que não tem.
     *
     * @param medicamentoAlvo Nome do medicamento a enviar.
     * @param qtdDesejada Quantidade a enviar.
     * @param minhaChavePrivada Chave para ler o saldo atual.
     * @return true se tiver saldo (ou for Médico), false caso contrário.
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

        // EXCEÇÃO: MÉDICO TEM STOCK INFINITO
        if ("Médico".equals(role)) {
            return true;
        }

        // 2. Calcular Stock Atual (Lógica idêntica ao getInventario)
        int stockAtual = 0;

        for (WalletTransaction wt : transactions) {
            SaudeTransaction t = wt.getTransaction();
            String[] dados = t.desencriptarConteudo(minhaChavePrivada);

            if (dados != null && dados.length == 2) {
                try {
                    int qtd = Integer.parseInt(dados[0]);
                    String med = dados[1];

                    // Só processa se for o medicamento que estamos a validar
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

        // 3. Validação Final
        return stockAtual >= qtdDesejada;
    }

    /**
     * [UTILITY] Hard Reset do Sistema.
     * <p>
     * Apaga todas as pastas de dados (Wallet, User, Blocks) e recria os
     * utilizadores padrão para demonstração.
     * <b>CUIDADO: Irreversível.</b>
     */
    public static BlockChain restartSaudeCerteira() throws Exception {
        // Limpeza de pastas
        FolderUtils.cleanFolder(FILE_PATH, true);
        FolderUtils.cleanFolder("data_user/", true);
        FolderUtils.cleanFolder("data_blocks/", true);

        User.deleteAllUsers();
        BlockChain.deleteAllBlocks();

        // Criar Utilizadores de Teste (Bootstrap)
        SaudeWallet.create("Master", "123qwe", "01/01/1980", "111", "111", "M", "PT", "Tomar", "Hospital", "111", "911", "Médico", "Hospital Central");
        SaudeWallet.create("System", "123qwe", "01/01/1980", "222", "222", "M", "PT", "Server", "Cloud", "222", "922", "Farmacêutico", "System Root");
        SaudeWallet.create("aa", "aa", "01/01/2000", "000", "000", "M", "PT", "Lisboa", "Rua A", "000", "900", "Utente", "Clínica A");

        // Criar Bloco Genesis (Opcional, mas útil para inicializar a chain)
        ArrayList<SaudeTransaction> data = new ArrayList<>();
        SaudeTransaction t = new SaudeTransaction("System", "Master", Integer.MAX_VALUE, "Osvyrti"); // Medicamento de teste
        User uSystem = User.login("System", "123qwe");
        t.sign(uSystem.getPrivateKey());
        data.add(t);

        Block genesis = new Block(0, new byte[32], 3, data);
        genesis.mine(); // Minerar genesis (Nonce)

        BlockChain blockchain = new BlockChain(genesis);

        // Atualizar wallets iniciais
        WalletTransaction w = new WalletTransaction(t, genesis.getData().getProof(t), 0);
        SaudeWallet.updateWallets(w);

        return blockchain;
    }

    /**
     * Auxiliar para obter a chave privada do utilizador atual. Necessita da
     * password em memória ou carregada anteriormente.
     */
    public PrivateKey getPrivateKey(String nome) throws Exception {
        User user = User.login(nome);
        return user.getPrivateKey();
    }

    private static final long serialVersionUID = 202510141301L;
}
