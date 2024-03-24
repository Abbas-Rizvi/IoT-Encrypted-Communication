package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;

import network.Host;

public class HostTest {

    private Host host;
    private PublicKey pubKey;

    @Before
    public void setUp() {
        // Initialize Host object with sample data
        pubKey = generateSamplePublicKey();
        host = new Host("TestUser", "127.0.0.1", pubKey);
    }

    @Test
    public void testConstructor() {
        assertNotNull(host);
    }

    @Test
    public void testGettersAndSetters() {
        // Test getters
        assertEquals("TestUser", host.getName());
        assertEquals("127.0.0.1", host.getIp());
        assertEquals(pubKey, host.getPubKey());

        // Test setters
        host.setName("NewUser");
        assertEquals("NewUser", host.getName());

        host.setIp("192.168.0.1");
        assertEquals("192.168.0.1", host.getIp());

        PublicKey newPubKey = generateSamplePublicKey();
        host.setPubKey(newPubKey);
        assertEquals(newPubKey, host.getPubKey());
    }

    private PublicKey generateSamplePublicKey() {
        return null;
    }
}
