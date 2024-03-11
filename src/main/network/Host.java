package network;

import java.security.PublicKey;

public class Host {
    
    private final int PORT = 5687;

    private int id;
    private String name;
    private String ip;
    private PublicKey pubKey;

    
    
    public Host(String name, String ip, PublicKey pubKey) {
        this.name = name;
        this.ip = ip;
        this.pubKey = pubKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPORT() {
        return PORT;
    }
    


    
}
