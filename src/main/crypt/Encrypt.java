package crypt;

import java.security.*;
import javax.crypto.Cipher;

public class Encrypt{

    private PrivateKey privateKey;

    // Constructor to initialize private key
    public Encrypt() {
        Keys keys = new Keys();
        privateKey = keys.getPrivatKey();
    }

    // Encrypt message with given public key
    public byte[] encrypt(byte[] message, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Decrypt message with private key
    public byte[] decrypt(byte[] encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
