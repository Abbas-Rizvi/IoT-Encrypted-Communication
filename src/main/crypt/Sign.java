package crypt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import network.Host;
import network.KnownHosts;
import network.MsgPacket;

public class Sign {

    PrivateKey pk;

    // load keys into object
    public Sign() {

        Keys keys = new Keys();
        pk = keys.getPrivatKey();

    }

    // sign message packet
    public MsgPacket signMsg(MsgPacket pack) {

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

    // verify signature
    public boolean verifySignature(MsgPacket pack) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");

            // db connection
            KnownHosts knownHosts = new KnownHosts();

            // check for valid signature among known hosts
            for (Host tHost : knownHosts.readAllHosts()) {
                pack.getSig();

                signature.initVerify(tHost.getPubKey());
                signature.update(pack.getMsg());

                if (signature.verify(pack.getSig())) {
                    System.out.println("\033[0;34mLOG: Received Message was verified as signed by " + tHost.getName() + "!\033[0m");
                    return true; // signature verified
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // sig not verified
        return false;
    }

}
