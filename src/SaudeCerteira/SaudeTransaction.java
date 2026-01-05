package SaudeCerteira;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Atualizado para transações de Medicamentos (Receitas)
 * @author Manso - computer
 */
public class SaudeTransaction implements Serializable {

    // Atributos auxiliares (nomes mantidos conforme pedido)
    String txtSender;    // Nome do Médico
    String txtReceiver;  // Nome do Paciente
    
    // Variáveis específicas da saúde
    String medicamento;  // Nome do medicamento
    int quantidade;      // Quantidade a receitar
    
    
 
    // Atributos de segurança
    PublicKey sender;    // Chave pública do médico
    PublicKey receiver;  // Chave pública do paciente
    long timestamp;      // Carimbo de tempo
    double value;        // Mantido para compatibilidade (guarda a quantidade)
    byte[] signature;    // Assinatura digital

    /**
     * Construtor da Transação
     * @param senderName Nome do Médico
     * @param receiverName Nome do Paciente
     * @param qtd Quantidade do medicamento
     * @param nomeMedicamento Nome do medicamento
     */
    public SaudeTransaction(String senderName, String receiverName, int qtd, String nomeMedicamento) throws Exception {
        // 1. Configurar dados básicos
        this.txtSender = senderName;
        this.txtReceiver = receiverName;
        this.quantidade = qtd;
        this.medicamento = nomeMedicamento;
        
        // Mantemos 'value' igual à quantidade para compatibilidade com outros métodos se necessário
        this.value = (double) qtd; 
        
        this.timestamp = System.currentTimeMillis();

        // 2. Carregar chaves públicas (sem precisar de password aqui)
        User uSender = User.login(senderName);
        User uReceiver = User.login(receiverName);

        this.sender = uSender.getPublicKey();
        this.receiver = uReceiver.getPublicKey();
    }

    /**
     * Assina a transação com a Chave Privada do Médico
     * Deve ser chamado antes de enviar para a rede.
     */
    public void sign(PrivateKey privKey) throws Exception {
        // Juntar todos os dados importantes para assinar
        // (Chaves + Quantidade + Timestamp + MEDICAMENTO)
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.doubleToBytes(value));
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        // IMPORTANTE: Incluir o nome do medicamento na assinatura para evitar adulteração
        allData = Utils.concatenate(allData, medicamento.getBytes()); 
        
        // Assinar
        this.signature = SecurityUtils.sign(allData, privKey);
    }

    /**
     * Verifica se a transação é válida e se o Médico tem stock.
     */
    public void validate() throws Exception {
        // 1. Reconstruir os dados para verificação
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.doubleToBytes(value));
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        allData = Utils.concatenate(allData, medicamento.getBytes());

        // 2. Verificar a Assinatura Digital
        if (!SecurityUtils.verifySign(allData, signature, sender)) {
            throw new Exception("Assinatura Inválida - Transação Adulterada!");
        }

        // 3. Verificar se a quantidade é válida
        if (quantidade <= 0) {
            throw new Exception("Quantidade inválida: " + quantidade);
        }

        // 4. Verificar Stock (Inventário do Médico)
        // Carregar a carteira do remetente
        SaudeWallet wSender = SaudeWallet.load(txtSender);
        
        // Verificar se o médico tem stock suficiente deste medicamento
        int stockAtual = wSender.getDrugInventory().getOrDefault(medicamento, 0);

        if (stockAtual < quantidade) {
            throw new Exception("Stock Insuficiente! " + txtSender + " tem apenas " + stockAtual + " de " + medicamento);
        }
    }

    // --- Getters ---

    public String getTxtSender() {
        return txtSender;
    }

    public String getTxtReceiver() {
        return txtReceiver;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public double getValue() {
        return value;
    }
    
    // Getter específico para a quantidade inteira
    public int getQuantidade() {
        return quantidade;
    }

    public byte[] getSignature() {
        return signature;
    }
    
    // Método necessário para a SaudeWallet funcionar corretamente
    public String getDescription() {
        return medicamento; 
    }

@Override
public String toString() {
    StringBuilder txt = new StringBuilder();
    txt.append("/////GUIA DE TRATAMENTO AO UTENTE/////\n")
            .append("Médico: "+ txtSender+"\n")
            .append("Paciente: "+txtReceiver+"\n")
            .append("Medicamentos: "+quantidade+" "+medicamento+"\n");
    return txt.toString();
}

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510141147L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::
}