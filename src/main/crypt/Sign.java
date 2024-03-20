package crypt;

import java.security.PrivateKey;
import java.security.Signature;

import network.MsgPacket;

public class Sign {

    PrivateKey pk;

    // load keys into object
    public Sign() {

        Keys keys = new Keys();
        pk = keys.getPrivatKey();

    }

    // sign message packet
    public MsgPacket signMsg(MsgPacket pack){
        
        try {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(pk);
        signature.update(pack.getMsg());

        byte[] signedBytes = signature.sign();

        pack.setSig(signedBytes);

        return pack;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        
    }

}
