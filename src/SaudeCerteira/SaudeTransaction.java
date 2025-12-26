//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package SaudeCerteira;

import java.io.Serializable;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Created on 14/12/2025, 11:47:10
 *
 * @author aluno25979, aluno25946 - computer
 */
public class SaudeTransaction implements Serializable {

    //atributos auxiliares (apagar em produção)
    String txtSender;
    String txtReceiver;

    PublicKey sender;
    PublicKey receiver;
    long timestamp;
    double value;
    byte[] signature;

    public SaudeTransaction(String senderName, String receiverName, double val, String pass) throws Exception {
        //ler as credenciais do sender e do receiver
        User uSender = User.login(senderName, pass);
        User uReceiver = User.login(receiverName);

        this.txtSender = uSender.getUserName();
        this.sender = uSender.getPublicKey();

        this.txtReceiver = uReceiver.getUserName();
        this.receiver = uReceiver.getPublicKey();

        this.value = val;

        this.timestamp = System.currentTimeMillis();

        //juntar os bytes de todos os dados
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.doubleToBytes(value));
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        //assinar
        this.signature = SecurityUtils.sign(allData, uSender.getPrivateKey());
    }

    public void validate() throws Exception {
        //juntar os bytes de todos os dados
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.doubleToBytes(value));
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        //verificar assinatura
        if (!SecurityUtils.verifySign(allData, signature, sender)) {
            throw new Exception("invalid signature ");
        }
        //verificar o valor
        if (value <= 0) {
            throw new Exception("ivalid value :" + value);
        }
        //ler a carteira
        SaudeWallet wSender = SaudeWallet.load(txtSender);
        //vertificar o saldo
        if (wSender.amount < value) {
            throw new Exception(txtSender + " not have " + value + " Tcoins Amount=" + wSender.amount);
        }
        SaudeWallet wReceiver = SaudeWallet.load(txtReceiver);

    }

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

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return txtSender + " -> " + value + " -> " + txtReceiver;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510141147L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
