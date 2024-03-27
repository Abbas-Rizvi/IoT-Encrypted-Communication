
package test;

import org.junit.Before;
import org.junit.Test;

import crypt.Encrypt;
import crypt.Keys;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class EncryptTest {

    private Encrypt encrypt;
    Keys keys;

    // create RSA keys
    @Before
    public void setUp() {
        encrypt = new Encrypt();
        keys = new Keys();
    }

    // test encrypt function
    @Test
    public void testEncrypt() {

        String testStr = "This is a test message";

        // generate encrypted text
        byte[] encryptedBytes = encrypt.encrypt(testStr.getBytes(), keys.getPublicKey());
        String encryptedStr = new String(encryptedBytes, StandardCharsets.UTF_8); 
        
        // verify text cannot be read as message
        assertNotEquals(testStr, encryptedStr);

        // decrypt text
        byte[] decrytpedBytes = encrypt.decrypt(encryptedBytes);
        String decryptedStr = new String(decrytpedBytes, StandardCharsets.UTF_8); 

        // verify text is decrytped
        assertEquals(testStr, decryptedStr);
        
        // output debug string for user verification
        System.out.println("///////////////////");
        System.out.println("Original String: " + testStr);
        System.out.println("///////////////////");
        System.out.println("Encrypted String: " + encryptedStr);
        System.out.println("///////////////////");
        System.out.println("Decrypted String: " + decryptedStr);
        System.out.println("///////////////////");


    }

    @Test
    public void testEncryptDecrypt() {
        String message = "Hello, world!";
        byte[] originalMessage = message.getBytes();
        byte[] encryptedMessage = encrypt.encrypt(originalMessage, keys.getPublicKey());
        byte[] decryptedMessage = encrypt.decrypt(encryptedMessage);
        assertArrayEquals(originalMessage, decryptedMessage);
    }

}