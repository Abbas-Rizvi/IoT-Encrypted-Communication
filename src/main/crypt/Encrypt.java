package crypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Encrypt {

    private PrivateKey privateKey;

    // Initialize private key
    public Encrypt() {
        Keys keys = new Keys();
        privateKey = keys.getPrivatKey();
    }

    // Encrypt message with RSA and AES
    public byte[] encrypt(byte[] message, PublicKey publicKey) {
        try {
            // Generate AES key
            Key symmetricKey = generateSymmetricKey();

            // Encrypt message with AES
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            byte[] encryptedMessage = aesCipher.doFinal(message);

            // Encrypt key using RSA and public key
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedSymmetricKey = rsaCipher.doFinal(symmetricKey.getEncoded());

            // Combine key and message
            byte[] merged = new byte[encryptedSymmetricKey.length + encryptedMessage.length];
            System.arraycopy(encryptedSymmetricKey, 0, merged, 0, encryptedSymmetricKey.length);
            System.arraycopy(encryptedMessage, 0, merged, encryptedSymmetricKey.length, encryptedMessage.length);

            return merged;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Decrypt message with private key
    public byte[] decrypt(byte[] encryptedMessage) {

        try {
            // Extract symmetric key and message
            int keySize = 512;

            byte[] encryptedSymmetricKey = new byte[keySize];
            byte[] encryptedData = new byte[encryptedMessage.length - keySize];
            System.arraycopy(encryptedMessage, 0, encryptedSymmetricKey, 0, keySize);
            System.arraycopy(encryptedMessage, keySize, encryptedData, 0, encryptedData.length);

            // Decrypt symmetric key using RSA
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedSymmetricKey = rsaCipher.doFinal(encryptedSymmetricKey);

            // Decrypt message using decrypted AES key
            Key symmetricKey = new SecretKeySpec(decryptedSymmetricKey, "AES");
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, symmetricKey);
            System.out.println(
                    "\033[0;34mLOG: Message has been decrypted successfully!\033[0m");

            return aesCipher.doFinal(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Generate AES key
    private Key generateSymmetricKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
