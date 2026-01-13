package SaudeCerteira;

import java.io.Serializable;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Utils;

public class SaudeTransaction implements Serializable {

    // Cabeçalho Público
    String txtSender;
    String txtReceiver;
    PublicKey sender;
    PublicKey receiver;
    byte[] signature;
    long timestamp;

    // --- SEGURANÇA (ENVELOPE DUPLO) ---
    private byte[] dadosEncriptados;    // A Receita (Trancada com AES)
    
    // As Chaves do Envelope (A mesma chave AES, trancada para duas pessoas diferentes)
    private byte[] chaveAesReceiver;    // Cópia para o Destinatário (Abrir com RSA do Destino)
    private byte[] chaveAesSender;      // [NOVO] Cópia para o Remetente (Abrir com RSA do Remetente)

    public SaudeTransaction(String senderName, String receiverName, int qtd, String nomeMedicamento) throws Exception {
        this.txtSender = senderName;
        this.txtReceiver = receiverName;
        this.timestamp = System.currentTimeMillis();

        // 1. Carregar Chaves RSA (Identidade)
        User uSender = User.login(senderName);
        User uReceiver = User.login(receiverName);
        this.sender = uSender.getPublicKey();
        this.receiver = uReceiver.getPublicKey();

        // --- PASSO 1: GERAR CHAVE ALEATÓRIA AES (Session Key) ---
        Key sessionKey = SecurityUtils.generateAESKey(256);

        // --- PASSO 2: ENCRIPTAR DADOS COM AES ---
        String segredo = qtd + ":" + nomeMedicamento;
        this.dadosEncriptados = SecurityUtils.encrypt(segredo.getBytes(), sessionKey);

        // --- PASSO 3: CRIAR OS DOIS ENVELOPES ---
        
        // Cópia 1: Para o Destinatário (Como já tinha)
        this.chaveAesReceiver = SecurityUtils.encrypt(sessionKey.getEncoded(), this.receiver);
        
        // Cópia 2: Para o Remetente (NOVO - Para conseguir ler o histórico e descontar stock)
        this.chaveAesSender = SecurityUtils.encrypt(sessionKey.getEncoded(), this.sender);
    }

    public void sign(PrivateKey privKey) throws Exception {
        // Assinamos tudo para garantir integridade
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        allData = Utils.concatenate(allData, dadosEncriptados);
        
        // Assinar ambos os envelopes
        allData = Utils.concatenate(allData, chaveAesReceiver);
        allData = Utils.concatenate(allData, chaveAesSender);
        
        this.signature = SecurityUtils.sign(allData, privKey);
    }

    /**
     * Tenta abrir o envelope.
     * Agora é inteligente: Tenta abrir como Destinatário OU como Remetente.
     */
    public String[] desencriptarConteudo(PrivateKey minhaChavePrivadaRSA) {
        byte[] aesKeyBytes = null;

        // TENTATIVA 1: Sou o Destinatário?
        try {
            aesKeyBytes = SecurityUtils.decrypt(this.chaveAesReceiver, minhaChavePrivadaRSA);
        } catch (Exception e) {
            // Se falhar, não sou o destinatário.
        }

        // TENTATIVA 2: Sou o Remetente? (Se a tentativa 1 falhou)
        if (aesKeyBytes == null) {
            try {
                aesKeyBytes = SecurityUtils.decrypt(this.chaveAesSender, minhaChavePrivadaRSA);
            } catch (Exception e) {
                // Se falhar também, não sou ninguém autorizado.
                return null;
            }
        }

        // Se chegámos aqui, temos a chave AES! Vamos ler a receita.
        try {
            Key sessionKey = SecurityUtils.getAESKey(aesKeyBytes);
            byte[] rawData = SecurityUtils.decrypt(this.dadosEncriptados, sessionKey);
            String texto = new String(rawData); // Ex: "10:Ben-u-ron"
            return texto.split(":");
        } catch (Exception e) {
            return null;
        }
    }
    
    // Getters
    public String getTxtSender() { 
        return txtSender; 
    }
    public String getTxtReceiver() {
        return txtReceiver; 
    }
    public byte[] getSignature() { return 
            signature;
    }
    
    // Getters de compatibilidade
    public String getDescription() { 
        return "AES Dual Encrypted"; 
    }
    
    public double getValue() { 
        return 0; 
    }

    @Override
    public String toString() {
        return "Receita Segura (AES+RSA) De: " + txtSender + " Para: " + txtReceiver;
    }
    
    // Alterei o SerialVersion porque a estrutura da classe mudou
    private static final long serialVersionUID = 202512129999L;
}