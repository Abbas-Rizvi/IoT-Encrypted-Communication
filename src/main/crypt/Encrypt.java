package crypt;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Encrypt {

    private PrivateKey privateKey;

    // initialize private key
    public Encrypt() {
        Keys keys = new Keys();
        privateKey = keys.getPrivatKey();
    }

    // encrypt message with RSA and AES
    public byte[] encrypt(byte[] message, PublicKey publicKey) {

        try {
            // generate AES key
            Key symmetricKey = generateSymmetricKey();

            // encrypt message with AES 
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            byte[] encryptedMessage = aesCipher.doFinal(message);

            // encrypt key using RSA and public key
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedSymmetricKey = rsaCipher.doFinal(symmetricKey.getEncoded());

            // combine key and message
            byte[] merged = new byte[encryptedSymmetricKey.length + encryptedMessage.length];
            System.arraycopy(encryptedSymmetricKey, 0, merged, 0, encryptedSymmetricKey.length);
            System.arraycopy(encryptedMessage, 0, merged, encryptedSymmetricKey.length, encryptedMessage.length);

            return merged;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // decrypt message with private key
    public byte[] decrypt(byte[] encryptedMessage) {
        try {
            // extract symmetric key and message
            int keySize = 256 / 8; 

            byte[] encryptedSymmetricKey = new byte[keySize];
            byte[] encryptedData = new byte[encryptedMessage.length - keySize];
            System.arraycopy(encryptedMessage, 0, encryptedSymmetricKey, 0, keySize);
            System.arraycopy(encryptedMessage, keySize, encryptedData, 0, encryptedData.length);

            // decrypt symmetric key using RSA
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedSymmetricKey = rsaCipher.doFinal(encryptedSymmetricKey);

            // decrypt message using decrypted AES key
            Key symmetricKey = new SecretKeySpec(decryptedSymmetricKey, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, symmetricKey);

            return aesCipher.doFinal(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // generate AES key
    private Key generateSymmetricKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // AES key size is 256 bits
            return keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
