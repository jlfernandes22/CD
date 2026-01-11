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

    // --- O SEU DIAGRAMA (Híbrido) ---
    private byte[] dadosEncriptados;    // A Receita (Encriptada com AES)
    private byte[] chaveAesEncriptada;  // A Chave AES (Encriptada com RSA Pública do Utente)

    public SaudeTransaction(String senderName, String receiverName, int qtd, String nomeMedicamento) throws Exception {
        this.txtSender = senderName;
        this.txtReceiver = receiverName;
        this.timestamp = System.currentTimeMillis();

        // 1. Carregar Chaves RSA (Identidade)
        User uSender = User.login(senderName);
        User uReceiver = User.login(receiverName);
        this.sender = uSender.getPublicKey();
        this.receiver = uReceiver.getPublicKey();

        // --- PASSO 1: GERAR CHAVE ALEATÓRIA AES ---
        // Criamos uma chave descartável só para esta receita
        Key sessionKey = SecurityUtils.generateAESKey(256);

        // --- PASSO 2: ENCRIPTAR DADOS COM AES ---
        String segredo = qtd + ":" + nomeMedicamento;
        this.dadosEncriptados = SecurityUtils.encrypt(segredo.getBytes(), sessionKey);

        // --- PASSO 3: ENCRIPTAR A CHAVE AES COM A RSA DO UTENTE ---
        // Isto corresponde à seta do seu desenho: "obter a chave aes que virá encriptada com a sua chave pública"
        this.chaveAesEncriptada = SecurityUtils.encrypt(sessionKey.getEncoded(), this.receiver);
    }

    public void sign(PrivateKey privKey) throws Exception {
        // Assinamos tudo para garantir integridade
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        allData = Utils.concatenate(allData, dadosEncriptados);
        allData = Utils.concatenate(allData, chaveAesEncriptada);
        
        this.signature = SecurityUtils.sign(allData, privKey);
    }

    /**
     * O Utente usa este método para ler a receita.
     * Recebe a Chave Privada RSA dele para "abrir o envelope" da chave AES.
     */
    public String[] desencriptarConteudo(PrivateKey minhaChavePrivadaRSA) {
        try {
            // 1. Destrancar a Chave AES usando a minha Private Key (RSA)
            byte[] aesKeyBytes = SecurityUtils.decrypt(this.chaveAesEncriptada, minhaChavePrivadaRSA);
            
            // Reconstruir o objeto Key AES
            Key sessionKey = SecurityUtils.getAESKey(aesKeyBytes);
            
            // 2. Destrancar a Receita usando a Chave AES recuperada
            byte[] rawData = SecurityUtils.decrypt(this.dadosEncriptados, sessionKey);
            
            String texto = new String(rawData); // Ex: "10:Ben-u-ron"
            return texto.split(":");
            
        } catch (Exception e) {
            // Se der erro, é porque a chave privada não corresponde à pública do destinatário
            // e.printStackTrace(); // Descomente para debug
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
        return "AES Encrypted"; 
    }
    
    public double getValue() { 
        return 0; 
    }

    @Override
    public String toString() {
        return "Receita Segura (AES+RSA) De: " + txtSender + " Para: " + txtReceiver;
    }
    
    private static final long serialVersionUID = 202510141147L;
}