package test;


import static org.junit.Assert.*;

import java.security.PublicKey;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import network.Host;
import network.KnownHosts;

public class KnownHostsTest {

    private KnownHosts knownHosts;

    @Before
    public void setUp() {
        knownHosts = new KnownHosts();
        knownHosts.insertRecord("TestHost", "TestPublicKey", "TestIPAddress");
    }

    @After
    public void tearDown() {
        knownHosts.deleteRowsByName("TestHost");
    }

    @Test
    public void testInsertRecord() {
        assertEquals(1, knownHosts.insertRecord("TestHost", "TestPublicKey", "TestIPAddress"));
        assertEquals(0, knownHosts.insertRecord("NewHost", "NewPublicKey", "NewIPAddress"));
    }

    @Test
    public void testLookupPublicKeyByName() {
        assertEquals("TestPublicKey", knownHosts.lookupPublicKeyByName("TestHost"));
        assertNull(knownHosts.lookupPublicKeyByName("NonExistentHost"));
    }

    @Test
    public void testLookupIPAddressByName() {
        assertEquals("TestIPAddress", knownHosts.lookupIPAddressByName("TestHost"));
        assertNull(knownHosts.lookupIPAddressByName("NonExistentHost"));
    }

    @Test
    public void testLookupNameByPublicKey() {
        assertEquals("TestHost", knownHosts.lookupNameByPublicKey("TestPublicKey"));
        assertNull(knownHosts.lookupNameByPublicKey("NonExistentPublicKey"));
    }

    @Test
    public void testLookupPubKeyByIP() {
        assertEquals("TestPublicKey", knownHosts.lookupPubKeyByIP("TestIPAddress"));
        assertNull(knownHosts.lookupPubKeyByIP("NonExistentIPAddress"));
    }

    @Test
    public void testLookupNameByIP() {
        assertEquals("TestHost", knownHosts.lookupNameByIP("TestIPAddress"));
        assertNull(knownHosts.lookupNameByIP("NonExistentIPAddress"));
    }

    @Test
    public void testGetAllMatchingRows() {
        List<String> matchingRows = knownHosts.getAllMatchingRows("TestHost");
        assertEquals(1, matchingRows.size());
        assertTrue(matchingRows.get(0).contains("TestHost"));
    }

    @Test
    public void testReadAllHosts() {
        List<Host> allHosts = knownHosts.readAllHosts();
        assertEquals(1, allHosts.size());
        assertEquals("TestHost", allHosts.get(0).getName());
    }

    @Test
    public void testSerialize() {
        assertNotNull(knownHosts.serialize());
    }

    @Test
    public void testGetHostByIP() {
        assertNotNull(knownHosts.getHostByIP("TestIPAddress"));
        assertNull(knownHosts.getHostByIP("NonExistentIPAddress"));
    }

    @Test
    public void testGetAllPublicKeys() {
        List<PublicKey> publicKeys = knownHosts.getAllPublicKeys();
        assertEquals(1, publicKeys.size());
        assertEquals("TestPublicKey", publicKeys.get(0).toString());
    }
}
