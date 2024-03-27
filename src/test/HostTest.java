package test;

import org.junit.Before;
import org.junit.Test;

import network.Host;

import static org.junit.Assert.*;

public class HostTest {

    private Host host;

    @Before
    public void setUp() {
        host = new Host("TestUser", "127.0.0.1");
    }

    @Test
    public void constructorWithPublicKeyString() {
        String publicKeyString = "dummyPublicKeyString";
        Host hostWithPublicKey = new Host("TestUser", "127.0.0.1", publicKeyString);
        assertEquals(publicKeyString, hostWithPublicKey.getPubKeyStr());
    }

    @Test
    public void getAndSetId() {
        host.setId(1);
        assertEquals(1, host.getId());
    }

    @Test
    public void getAndSetName() {
        host.setName("NewTestUser");
        assertEquals("NewTestUser", host.getName());
    }

    @Test
    public void getAndSetIp() {
        host.setIp("192.168.1.1");
        assertEquals("192.168.1.1", host.getIp());
    }

    @Test
    public void getPORT() {
        assertEquals(5687, host.getPORT());
    }

}
