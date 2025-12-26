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
import java.util.List;
import utils.Utils;

/**
 * Created on 14/10/2025, 13:02:21
 *
 * @author manso - computer
 */
public class WalletTransaction implements Serializable{

    private final SaudeTransaction transaction;
    private final List<byte[]> proof;
    private final int blockID;

    public WalletTransaction(SaudeTransaction transaction, List<byte[]> proof, int blockID) {
        this.transaction = transaction;
        this.proof = proof;
        this.blockID = blockID;
    }

    public SaudeTransaction getTransaction() {
        return transaction;
    }

    public List<byte[]> getProof() {
        return proof;
    }

    public int getBlockID() {
        return blockID;
    }
    
    @Override
    public String toString(){
        return transaction.toString() +" Block ID[ " +blockID + "] Proof [ " + Utils.toStringList(proof, 8) +" ]";
    }
    
    
   
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510141302L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
