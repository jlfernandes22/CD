package SaudeCerteira;

import java.io.Serializable;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Representa uma transação segura no sistema de saúde (Ex: Receita Médica ou
 * Aviamento).
 * <p>
 * Esta classe implementa um esquema de <b>Criptografia Híbrida (AES + RSA)</b>
 * com uma arquitetura de <i>Dual Envelope</i> (Envelope Duplo).
 * <p>
 * <b>Mecanismo de Segurança:</b>
 * <ol>
 * <li>Os dados sensíveis (medicamento e quantidade) são cifrados com uma chave
 * aleatória simétrica (AES).</li>
 * <li>A chave AES é cifrada com a Chave Pública do <b>Destinatário</b> (para
 * ele poder ler).</li>
 * <li>A chave AES é também cifrada com a Chave Pública do <b>Remetente</b>
 * (para manter histórico legível).</li>
 * </ol>
 *
 * @author aluno_25979, aluno_25946
 * @version 2.0 (Dual Envelope Implementation)
 */
public class SaudeTransaction implements Serializable {

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: CABEÇALHO PÚBLICO (METADADOS)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Nome do remetente (quem assina a transação).
     */
    String txtSender;

    /**
     * Nome do destinatário (para quem é a receita/medicamento).
     */
    String txtReceiver;

    /**
     * Chave Pública do remetente (usada para verificar a assinatura).
     */
    PublicKey sender;

    /**
     * Chave Pública do destinatário.
     */
    PublicKey receiver;

    /**
     * Assinatura digital que garante integridade e não-repúdio.
     */
    byte[] signature;

    /**
     * Carimbo de data/hora da criação da transação.
     */
    long timestamp;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: PAYLOAD SEGURO (CRIPTOGRAFIA)
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * * Conteúdo da transação (Qt + Medicamento) cifrado com algoritmo
     * simétrico AES. Ninguém consegue ler isto sem a chave de sessão.
     */
    private byte[] dadosEncriptados;

    /**
     * * Envelope 1: A chave AES cifrada com a RSA Pública do Destinatário.
     * Permite que o destinatário abra a transação.
     */
    private byte[] chaveAesReceiver;

    /**
     * * Envelope 2: A chave AES cifrada com a RSA Pública do Remetente.
     * Permite que o remetente reabra a transação para consultar histórico e
     * calcular stock.
     */
    private byte[] chaveAesSender;

    /**
     * Constrói uma nova transação segura.
     * <p>
     * Este método gera automaticamente uma chave de sessão AES, cifra os dados
     * e cria os envelopes digitais para o remetente e destinatário.
     *
     * @param senderName Nome do utilizador que envia.
     * @param receiverName Nome do utilizador que recebe.
     * @param qtd Quantidade do medicamento.
     * @param nomeMedicamento Nome do medicamento.
     * @throws Exception Se houver erro no carregamento de chaves ou na
     * cifragem.
     */
    public SaudeTransaction(String senderName, String receiverName, int qtd, String nomeMedicamento) throws Exception {
        this.txtSender = senderName;
        this.txtReceiver = receiverName;
        this.timestamp = System.currentTimeMillis();

        // 1. Carregar Identidades e Chaves RSA do sistema de ficheiros
        User uSender = User.login(senderName);
        User uReceiver = User.login(receiverName);
        this.sender = uSender.getPublicKey();
        this.receiver = uReceiver.getPublicKey();

        // --- PASSO 1: GERAR CHAVE ALEATÓRIA AES (Session Key) ---
        // Criamos uma chave descartável de 256 bits apenas para esta transação.
        Key sessionKey = SecurityUtils.generateAESKey(256);

        // --- PASSO 2: ENCRIPTAR DADOS COM AES ---
        // O formato interno é "QUANTIDADE:NOME_MEDICAMENTO"
        String segredo = qtd + ":" + nomeMedicamento;
        this.dadosEncriptados = SecurityUtils.encrypt(segredo.getBytes(), sessionKey);

        // --- PASSO 3: CRIAR OS DOIS ENVELOPES (Dual Encryption) ---
        // Cópia 1: Para o Destinatário (Abre com PrivateKey do Destino)
        this.chaveAesReceiver = SecurityUtils.encrypt(sessionKey.getEncoded(), this.receiver);

        // Cópia 2: Para o Remetente (Abre com PrivateKey do Remetente)
        // Isto é crucial para o remetente conseguir abater stock visualmente no seu histórico.
        this.chaveAesSender = SecurityUtils.encrypt(sessionKey.getEncoded(), this.sender);
    }

    /**
     * Assina digitalmente a transação para garantir integridade e
     * autenticidade.
     * <p>
     * A assinatura cobre as chaves públicas, timestamp, dados cifrados e ambos
     * os envelopes. Qualquer alteração num bit invalida a assinatura.
     *
     * @param privKey A chave privada RSA do remetente.
     * @throws Exception Se ocorrer erro no algoritmo de assinatura.
     */
    public void sign(PrivateKey privKey) throws Exception {
        // Concatenar todos os campos críticos num único array de bytes
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        allData = Utils.concatenate(allData, dadosEncriptados);

        // Incluir ambos os envelopes na assinatura para evitar manipulação
        allData = Utils.concatenate(allData, chaveAesReceiver);
        allData = Utils.concatenate(allData, chaveAesSender);

        // Gerar assinatura criptográfica
        this.signature = SecurityUtils.sign(allData, privKey);
    }

    /**
     * Tenta desencriptar o conteúdo da transação.
     * <p>
     * Este método é "inteligente": detecta automaticamente se a chave privada
     * fornecida pertence ao Destinatário ou ao Remetente e tenta abrir o
     * envelope correspondente.
     *
     * @param minhaChavePrivadaRSA A chave privada do utilizador atual.
     * @return Um array de String [Quantidade, Medicamento] ou null se a chave
     * não tiver permissão.
     */
    public String[] desencriptarConteudo(PrivateKey minhaChavePrivadaRSA) {
        byte[] aesKeyBytes = null;

        // TENTATIVA 1: Sou o Destinatário? (Tenta abrir envelope 1)
        try {
            aesKeyBytes = SecurityUtils.decrypt(this.chaveAesReceiver, minhaChavePrivadaRSA);
        } catch (Exception e) {
            // Se falhar, a chave privada não corresponde à Public Key do Receiver. Ignorar.
        }

        // TENTATIVA 2: Sou o Remetente? (Tenta abrir envelope 2)
        // Só tenta se a primeira falhou.
        if (aesKeyBytes == null) {
            try {
                aesKeyBytes = SecurityUtils.decrypt(this.chaveAesSender, minhaChavePrivadaRSA);
            } catch (Exception e) {
                // Se falhar também, o utilizador não é nem o remetente nem o destinatário.
                // Acesso Negado.
                return null;
            }
        }

        // Se chegámos aqui, recuperámos a chave AES com sucesso!
        try {
            // Reconstruir a chave AES e decifrar o conteúdo real
            Key sessionKey = SecurityUtils.getAESKey(aesKeyBytes);
            byte[] rawData = SecurityUtils.decrypt(this.dadosEncriptados, sessionKey);

            // Converter bytes para texto e separar campos
            String texto = new String(rawData); // Ex: "10:Ben-u-ron"
            return texto.split(":");

        } catch (Exception e) {
            return null;
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :: GETTERS E MÉTODOS AUXILIARES
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * @return O nome do remetente.
     */
    public String getTxtSender() {
        return txtSender;
    }

    /**
     * @return O nome do destinatário.
     */
    public String getTxtReceiver() {
        return txtReceiver;
    }

    /**
     * @return A assinatura digital em bytes.
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * @return Descrição do tipo de segurança (para logs).
     */
    public String getDescription() {
        return "AES Dual Encrypted";
    }

    /**
     * @return Valor monetário (não utilizado neste modelo, retorna 0).
     */
    public double getValue() {
        return 0;
    }

    @Override
    public String toString() {
        return "Receita Segura (AES+RSA) De: " + txtSender + " Para: " + txtReceiver;
    }

    private static final long serialVersionUID = 202512129999L;
}
