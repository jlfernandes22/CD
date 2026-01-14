package SaudeCerteira;

import java.io.Serializable;
import java.util.List;
import utils.Utils;

/**
 * Representa uma transação confirmada e armazenada na Carteira (Wallet).
 * <p>
 * Enquanto a {@link SaudeTransaction} contém apenas os dados do negócio (quem
 * enviou, quantidade, medicamento), esta classe <b>WalletTransaction</b>
 * contextualiza essa transação dentro da Blockchain.
 * <p>
 * Serve para provar que a transação foi efetivamente minerada e validada pela
 * rede.
 *
 * @author aluno_25979, aluno_25946
 * @version 2.0
 */
public class WalletTransaction implements Serializable {

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: CAMPOS IMUTÁVEIS (FINAL)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * A transação original com os dados de saúde e envelopes de segurança
     * (AES+RSA).
     */
    private final SaudeTransaction transaction;

    /**
     * * A Prova de Merkle (Merkle Proof).
     * <p>
     * É uma lista de hashes que permite provar matematicamente que esta
     * transação pertence à Merkle Root do bloco, sem necessidade de ter o bloco
     * inteiro.
     */
    private final List<byte[]> proof;

    /**
     * O ID do Bloco onde esta transação foi minerada.
     */
    private final int blockID;

    /**
     * Constrói um registo de transação confirmada.
     *
     * @param transaction O objeto da transação original.
     * @param proof A lista de hashes (caminho da árvore de Merkle) que valida a
     * transação.
     * @param blockID O número do bloco onde a transação ficou registada.
     */
    public WalletTransaction(SaudeTransaction transaction, List<byte[]> proof, int blockID) {
        this.transaction = transaction;
        this.proof = proof;
        this.blockID = blockID;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: MÉTODOS DE ACESSO (GETTERS)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Obtém a transação de saúde original.
     *
     * @return Objeto SaudeTransaction.
     */
    public SaudeTransaction getTransaction() {
        return transaction;
    }

    /**
     * Obtém a Prova de Merkle. Útil para validações SPV (Simplified Payment
     * Verification) ou auditoria.
     *
     * @return Lista de arrays de bytes (hashes).
     */
    public List<byte[]> getProof() {
        return proof;
    }

    /**
     * Obtém o índice do bloco na blockchain.
     *
     * @return Inteiro representando a altura do bloco.
     */
    public int getBlockID() {
        return blockID;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: MÉTODOS AUXILIARES
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Representação textual do registo para logs e depuração. Mostra os dados
     * da transação, o ID do bloco e uma versão resumida da prova.
     */
    @Override
    public String toString() {
        return transaction.toString()
                + " Block ID[ " + blockID + "]"
                + " Proof [ " + Utils.toStringList(proof, 8) + " ]";
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510141302L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::
}
