package network;

import java.security.PublicKey;
import java.io.Serializable;
import server.Listener;

public class Host implements Serializable{
    
    private final int PORT = 5687;

    private int id;
    private String username;
    private String ip;
    private PublicKey pubKey;

    
    
    public Host(String name, String ip, PublicKey pubKey) {
        this.username = name;
        this.ip = ip;
        this.pubKey = pubKey;
    }

    ////////////////////////////
    // Start Listner
    public void startListener() {
        
        Listener server = new Listener();   // Listener handles requests
        server.listener();

    }
    

    ////////////////////////////
    // Getters and Setters
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
        return username;
    }

    public void setName(String name) {
        this.username = name;
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
