package core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Representa um Bloco na Blockchain (Unidade fundamental de armazenamento).
 * <p>
 * Cada bloco contém:
 * <ul>
 * <li><b>Cabeçalho (Header):</b> Metadados leves (Hash anterior, Merkle Root,
 * Nonce, Timestamp).</li>
 * <li><b>Corpo (Body):</b> Os dados reais (Transações) organizados numa Árvore
 * de Merkle.</li>
 * </ul>
 * <p>
 * O bloco implementa o protocolo <b>Proof of Work (PoW)</b> para garantir
 * segurança e consenso na rede.
 *
 * @author aluno_25979, aluno_25946
 * @version 2.0
 */
public class Block implements Serializable {

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: METADADOS DO BLOCO (HEADER)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Identificador único do bloco (Altura/Index). O Genesis é 0.
     */
    private final int ID;

    /**
     * Hash do bloco anterior. É isto que cria o elo da corrente (Chain).
     */
    private final byte[] previousHash;

    /**
     * * Raiz da Árvore de Merkle. Resume todos os dados do bloco num único
     * hash de 32 bytes. Permite validar o conteúdo sem ler todas as transações.
     */
    private final byte[] merkleRoot;

    /**
     * Estrutura de dados que armazena as transações (O conteúdo real).
     */
    private final MerkleTree data;

    /**
     * Carimbo de tempo da criação do bloco (Unix Epoch).
     */
    private final long timestamp;

    /**
     * Dificuldade da mineração (Número de zeros exigidos no início do hash).
     */
    private final int dificulty;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: PROTOCOLO DE SEGURANÇA (MINERAÇÃO)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * * "Number used ONCE". O número que o mineiro tem de adivinhar para
     * resolver o puzzle criptográfico.
     */
    private int nonce;

    /**
     * O Hash final do bloco validado (Assinatura do bloco).
     */
    private byte[] currentHash;

    /**
     * Constrói um novo bloco (ainda não minerado).
     *
     * @param ID Índice do bloco na cadeia.
     * @param previousHash Hash do bloco anterior (vínculo).
     * @param dificulty Nível de dificuldade para mineração.
     * @param data Lista de transações ou dados a incluir no bloco.
     */
    public Block(int ID, byte[] previousHash, int dificulty, List data) {
        this.ID = ID;
        this.previousHash = previousHash;
        this.dificulty = dificulty;
        this.timestamp = System.currentTimeMillis();

        // Constrói a Merkle Tree imediatamente para garantir integridade dos dados
        this.data = new MerkleTree(data);
        this.merkleRoot = this.data.getRoot();
    }

    /**
     * Prepara os dados do cabeçalho para serem minerados.
     * <p>
     * Nota: A mineração é feita apenas sobre o cabeçalho (que inclui a Merkle
     * Root), e não sobre os dados brutos. Isso torna a mineração rápida
     * independente do tamanho do bloco.
     *
     * @return Array de bytes contendo ID + Timestamp + PrevHash + MerkleRoot +
     * Dificuldade.
     */
    public byte[] getHeaderData() {
        byte[] bytes = Utils.toBytes(ID);
        bytes = Utils.concatenate(bytes, Utils.toBytes(timestamp));
        bytes = Utils.concatenate(bytes, Utils.toBytes(previousHash));
        bytes = Utils.concatenate(bytes, Utils.toBytes(merkleRoot));
        return Utils.concatenate(bytes, Utils.toBytes(dificulty));
    }

    /**
     * Executa a Mineração (Proof of Work).
     * <p>
     * Delega a tarefa pesada para a classe {@link GUI.MinerDistibuted}, que
     * pode usar múltiplas threads para encontrar o nonce.
     *
     * @throws Exception Se houver erro no processo de hashing.
     */
    public void mine() throws Exception {
        // Converter cabeçalho para Base64 para facilitar transporte/visualização
        String dataTxt = Base64.getEncoder().encodeToString(getHeaderData());

        // Instancia o mineiro distribuído (Componente visual/multi-thread)
        GUI.MinerDistibuted miner = new GUI.MinerDistibuted();

        // Fica bloqueado aqui até encontrar o nonce
        int pow = miner.mine(dataTxt, this.dificulty);

        // Define o nonce vencedor e calcula o hash final
        setNonce(pow);
    }

    /**
     * Define o Nonce vencedor e calcula o Hash final do bloco.
     *
     * @param nonce O número encontrado que resolve o puzzle.
     * @throws Exception Se o algoritmo SHA-256 não estiver disponível.
     */
    public void setNonce(int nonce) throws Exception {
        this.nonce = nonce;
        String hash = Base64.getEncoder().encodeToString(getHeaderData());

        // Calcula o hash final: SHA-256( Header + Nonce )
        this.currentHash = SecurityUtils.calculateHash(
                (hash + nonce).getBytes(), "SHA-256");
    }

    /**
     * Gera uma representação textual do cabeçalho (para logs e debug).
     */
    public String toStringHeader() {
        StringBuilder txt = new StringBuilder();
        txt.append("ID ").append(ID);
        txt.append("\nHash ").append(Base64.getEncoder().encodeToString(currentHash));
        txt.append("\ntimestamp ").append(new Date(timestamp));
        txt.append("\npreviousHash ").append(Base64.getEncoder().encodeToString(previousHash));
        txt.append("\nmerkleRoot ").append(Base64.getEncoder().encodeToString(merkleRoot));
        txt.append("\ndificulty ").append(dificulty);
        txt.append("\nnonce ").append(nonce);
        return txt.toString();
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(toStringHeader());
        txt.append("\n-------- data--------\n");
        for (Object element : data.getElements()) {
            txt.append(element).append("\n");
        }
        txt.append("-------- data--------");
        return txt.toString();
    }

    /**
     * Valida a integridade do bloco (Proof of Work).
     * <p>
     * Verifica dois critérios:
     * <ol>
     * <li><b>Dificuldade:</b> O hash atual começa com o número correto de
     * zeros?</li>
     * <li><b>Integridade:</b> O hash calculado (Header + Nonce) bate certo com
     * o hash armazenado?</li>
     * </ol>
     *
     * @return true se o bloco for válido e seguro.
     */
    public boolean isValid() {
        try {
            // 1. Verificar Dificuldade (Zeros no início do Hash em Base64)
            String txtHash = Base64.getEncoder().encodeToString(currentHash);
            String hashZeros = txtHash.substring(0, this.dificulty);
            String allZeros = String.format("%0" + dificulty + "d", 0);

            if (!hashZeros.equals(allZeros)) {
                return false; // Falhou no teste de dificuldade
            }

            // 2. Verificar Integridade Matemática (Recalcular hash)
            String headerTxt = Base64.getEncoder().encodeToString(getHeaderData()) + nonce;
            byte[] myHash = SecurityUtils.calculateHash(headerTxt.getBytes(), "SHA-256");

            return Arrays.equals(myHash, currentHash);

        } catch (Exception ex) {
            return false;
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: PERSISTÊNCIA (GRAVAÇÃO EM DISCO)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Guarda o bloco num ficheiro individual. Formato: "caminho/ID.blk"
     *
     * @param path Diretoria de destino.
     * @throws IOException Erro de escrita.
     */
    public void save(String path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(path + ID + ".blk"))) {
            out.writeObject(this);
        }
    }

    /**
     * Carrega um bloco específico do disco.
     *
     * @param path Diretoria onde estão os blocos.
     * @param ID O ID do bloco a carregar.
     * @return O objeto Block ou null se não existir/erro.
     */
    public static Block load(String path, int ID) {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(path + ID + ".blk"))) {
            return (Block) in.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    public int getID() {
        return ID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public MerkleTree getData() {
        return data;
    }

    public int getDificulty() {
        return dificulty;
    }

    public int getNonce() {
        return nonce;
    }

    public byte[] getCurrentHash() {
        return currentHash;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081445L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::
}
