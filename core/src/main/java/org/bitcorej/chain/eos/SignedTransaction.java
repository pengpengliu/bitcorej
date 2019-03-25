package org.bitcorej.chain.eos;

import com.google.gson.annotations.Expose;
import org.bitcorej.chain.eos.types.EosByteWriter;
import org.bitcorej.chain.eos.types.TypeChainId;
import org.bitcorej.crypto.Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnibble on 2017-09-12.
 */

public class SignedTransaction extends Transaction {

    @Expose
    private List<String> signatures = null;

    @Expose
    private List<String> context_free_data = new ArrayList<>();


    public SignedTransaction(){
        super();
    }

    public SignedTransaction( SignedTransaction anotherTxn){
        super(anotherTxn);
        this.signatures = deepCopyOnlyContainer( anotherTxn.signatures );
        this.context_free_data = context_free_data;
    }

    public List<String> getSignatures() {
        return signatures;
    }

    public void putSignatures(List<String> signatures) {
        this.signatures = signatures;
    }

    public int getCtxFreeDataCount() {
        return ( context_free_data == null ) ? 0 : context_free_data.size();
    }

    public List<String> getCtxFreeData() {
        return context_free_data;
    }


    private byte[] getDigestForSignature(TypeChainId chainId) {
        EosByteWriter writer = new EosByteWriter(255);

        // data layout to sign :
        // [ {chainId}, {Transaction( parent class )}, {hash of context_free_data only when exists ]

        writer.putBytes(chainId.getBytes());
        pack( writer);
        if (context_free_data.size() > 0 ) {
        }
        else {
            writer.putBytes( new TypeChainId().getBytes());
        }

        return Hash.sha256(writer.toBytes());
    }

    public void sign(byte[] prvKey, TypeChainId chainId) {
        if ( null == this.signatures){
            this.signatures = new ArrayList<>();
        }

        String signature = EOSSign.sign(getDigestForSignature( chainId ), prvKey);

        this.signatures.add(signature);
    }
}

