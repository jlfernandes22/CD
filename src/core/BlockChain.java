//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::      Antonio Manuel Rodrigues Manso                                     ::
//::                                                                         ::
//::      I N S T I T U T O    P O L I T E C N I C O    D E    T O M A R     ::
//::      Escola Superior de Tecnologia de Tomar                             ::
//::      e-mail: manso@ipt.pt                                               ::
//::      url   : http://orion.ipt.pt/~manso                                 ::
//::                                                                         ::
//::      This software was build with the purpose of investigate and        ::
//::      learning.                                                          ::
//::                                                                         ::
//::                                                                         ::
//::                                                                         ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.FolderUtils;

/**
 * Representa a Cadeia de Blocos (Blockchain) - O Livro Razão Distribuído.
 * <p>
 * Esta classe gerencia uma lista encadeada de blocos ({@link Block}),
 * garantindo:
 * <ul>
 * <li><b>Integridade:</b> Cada bloco deve apontar corretamente para o hash do
 * anterior.</li>
 * <li><b>Imutabilidade:</b> Valida o Proof of Work (PoW) antes de aceitar
 * alterações.</li>
 * <li><b>Persistência:</b> Guarda automaticamente o estado da cadeia no
 * disco.</li>
 * </ul>
 * Created on 08/10/2025, 15:28:52
 *
 * @author aluno_25979, aluno_25946
 */
public class BlockChain implements Serializable {

    /**
     * Diretoria onde os blocos e o ficheiro mestre são guardados.
     */
    public static final String FILE_PATH = "data_blocks/";

    /**
     * A lista em memória que contém todos os blocos ordenados.
     */
    List<Block> blocks;

    /**
     * Construtor privado. Inicializa a estrutura de pastas necessária para a
     * persistência.
     */
    private BlockChain() {
        // Garante que a diretoria existe antes de tentar gravar
        new File(FILE_PATH).mkdirs();
    }

    /**
     * Inicializa uma nova Blockchain a partir de um Bloco Genesis.
     * <p>
     * Este construtor é usado apenas quando a rede é iniciada pela primeira
     * vez.
     *
     * @param genesis O primeiro bloco da cadeia (ID=0).
     * @throws Exception Se houver erro de gravação.
     */
    public BlockChain(Block genesis) throws Exception {
        this();
        blocks = new ArrayList<>();
        // Adicionar bloco à lista em memória
        blocks.add(genesis);
        // Persistir o bloco individualmente (.blk)
        genesis.save(FILE_PATH);
        // Persistir o estado da cadeia (.bch)
        save(FILE_PATH + "blockchain.bch");
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: MÉTODOS DE MINERAÇÃO E ADIÇÃO LOCAL
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Wrapper para adicionar um array de objetos (transações) num novo bloco.
     *
     * @param elements Array de dados a minerar.
     */
    public void add(Object[] elements) throws Exception {
        add(Arrays.asList(elements));
    }

    /**
     * Cria, Minera e Adiciona um novo bloco com os dados fornecidos.
     * <p>
     * 1. Obtém o último bloco para referência. 2. Cria o novo bloco com o Hash
     * anterior correto. 3. Executa o Proof of Work (Mineração). 4. Adiciona à
     * cadeia.
     *
     * @param data Lista de transações a incluir no bloco.
     */
    public void add(List data) throws Exception {
        // 1. Obter referência do topo da cadeia
        Block lastBlock = blocks.get(blocks.size() - 1);

        // 2. Construir o bloco candidato
        Block newBlock = new Block(
                lastBlock.getID() + 1, // ID sequencial
                lastBlock.getCurrentHash(), // Elo criptográfico (Link)
                lastBlock.getDificulty(), // Mantém a dificuldade atual
                data);                          // Dados (Payload)

        // 3. Minerar (Encontrar o Nonce)
        // Nota: Esta operação é bloqueante e pode demorar.
        newBlock.mine();

        // 4. Validar e Persistir
        add(newBlock);
    }

    /**
     * Adiciona um bloco já minerado à Blockchain.
     * <p>
     * Este é o método CRÍTICO de segurança. Valida todas as regras de consenso
     * antes de aceitar o bloco na cadeia local.
     *
     * @param newBlock O bloco candidato a entrar na cadeia.
     * @throws Exception Se o bloco for inválido, adulterado ou fora de
     * sequência.
     */
    public void add(Block newBlock) throws Exception {
        Block last = getLastBlock();

        // VALIDAÇÃO 1: Encadeamento (Chain Link)
        // O hash anterior do novo bloco DEVE ser igual ao hash atual do último bloco.
        if (!Arrays.equals(last.getCurrentHash(), newBlock.getPreviousHash())) {
            throw new Exception("Security Alert: Block does not match - Previous hash incorrect.");
        }

        // VALIDAÇÃO 2: Proof of Work (Integridade)
        // O bloco deve ter o número correto de zeros e o hash deve bater certo com os dados.
        if (!newBlock.isValid()) {
            throw new Exception("Security Alert: Invalid Block (PoW failed or Data corrupted).");
        }

        // VALIDAÇÃO 3: Sequência (Chronology)
        // O ID do novo bloco deve ser exatamente o próximo número da sequência.
        if (blocks.size() != newBlock.getID()) {
            throw new Exception("Sync Error: Incorrect Block ID sequence.");
        }

        // ::::::: SUCESSO ::::::::
        // Se passou em todas as validações, aceitamos o bloco.
        blocks.add(newBlock);

        // Persistência
        newBlock.save(FILE_PATH); // Guarda o ficheiro 00X.blk
        save(FILE_PATH + "blockchain.bch"); // Atualiza o ficheiro mestre da cadeia
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS E UTILITÁRIOS
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * @return O bloco mais recente da cadeia (Topo).
     */
    public Block getLastBlock() {
        return blocks.get(blocks.size() - 1);
    }

    /**
     * @return Um bloco específico pelo seu ID (Altura).
     */
    public Block getBlockID(int id) {
        return blocks.get(id);
    }

    /**
     * @return A lista completa de blocos.
     */
    public List<Block> getBlocks() {
        return blocks;
    }

    @Override
    public String toString() {
        return "BlockChain Size: " + blocks.size();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: PERSISTÊNCIA (SERIALIZAÇÃO)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Guarda o estado atual da Blockchain em disco.
     *
     * @param fileName Caminho do ficheiro (ex: blockchain.bch).
     */
    public void save(String fileName) throws Exception {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }

    /**
     * Carrega a Blockchain do disco.
     *
     * @param fileName Caminho do ficheiro.
     * @return A instância da Blockchain carregada.
     */
    public static BlockChain load(String fileName) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(fileName))) {
            return (BlockChain) in.readObject();
        }
    }

    /**
     * Apaga todos os dados da Blockchain (Hard Reset). Cuidado: Esta ação é
     * irreversível.
     */
    public static void deleteAllBlocks() throws IOException {
        FolderUtils.cleanFolder(FILE_PATH, true);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: VERIFICAÇÃO DE DUPLICIDADE (REPLAY ATTACK PROTECTION)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Verifica se uma transação já existe na história da Blockchain.
     * <p>
     * Isto é fundamental para impedir "Replay Attacks" (Gastar a mesma
     * receita/moeda duas vezes). Compara a assinatura digital única da
     * transação com todas as existentes.
     *
     * @param signature A assinatura digital da transação a verificar.
     * @return true se a transação já foi processada anteriormente.
     */
    public boolean existsTransaction(byte[] signature) {
        // Percorrer todos os blocos da história (Do Genesis ao atual)
        for (Block b : blocks) {
            // Percorrer todas as transações dentro de cada bloco
            List<Object> transacoesDoBloco = b.getData().getElements();

            for (Object o : transacoesDoBloco) {
                // Verificar se é uma transação do nosso sistema de saúde
                if (o instanceof SaudeCerteira.SaudeTransaction) {
                    SaudeCerteira.SaudeTransaction t = (SaudeCerteira.SaudeTransaction) o;

                    // Comparar as assinaturas byte-a-byte
                    if (java.util.Arrays.equals(t.getSignature(), signature)) {
                        return true; // ENCONTREI! Transação duplicada.
                    }
                }
            }
        }
        return false; // Não existe, é uma transação nova e válida.
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081528L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::
}
