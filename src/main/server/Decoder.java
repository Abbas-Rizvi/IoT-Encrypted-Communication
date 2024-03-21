package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Base64;

import crypt.Keys;
import crypt.Sign;
import network.Host;
import network.HostSerialization;
import network.KnownHosts;
import network.MsgPacket;
import network.PackRouting;

public class Decoder {

    // get IP Address of host
    public static InetAddress hostIp() {

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {

            e.printStackTrace();
        }

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Decode a message
    public void decode(byte[] data, String ipAddress, SocketChannel socketChannel) {

        MsgPacket msg = null;

        // cast data into message object to allow understanding
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

            Object obj = objectStream.readObject();
            if (obj instanceof MsgPacket) {
                msg = (MsgPacket) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // if message is not empty
        if (msg != null) {

            try {

                // print what was received
                System.out.println("Received " + msg.getType() + " From " + socketChannel.getRemoteAddress());

                // handle recived message
                String messageType = msg.getType().toUpperCase();
                switch (messageType) {

                    case "SETUP":
                        setupMsg(msg, ipAddress);
                        break;

                    case "SETUP-RECV-MSG":
                        setupRecvMsg(msg);
                        break;

                    case "REQ-PUBKEY":
                        KnownHosts knownHosts = new KnownHosts();
                        byte[] decodedMsg = Base64.getDecoder().decode(msg.getMsg());
                        String decoded = new String(decodedMsg);
                        String pubK = knownHosts.lookupPublicKeyByName(decoded);
                        MsgPacket msgPack = new MsgPacket("RECV-PUBKEY", data, null);
                        break;

                    case "RECV-PUBKEY":
                        // send a new message signed with pub key
                        break;

                    case "FORWARD-CRYPT":
                        // forward encrypted packet
                        break;

                    case "RECV-MSG":
                        // Decrypt and output msg
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // setup message
    // assume message contains a HOST object
    public void setupMsg(MsgPacket msg, String ipAddress) {

        KnownHosts knownHosts = new KnownHosts();
        HostSerialization Hserial = new HostSerialization();

        // unpack host & Store key
        try {
            Host recvHost = Hserial.deserializeHost(msg.getMsg());

            if (knownHosts.insertRecord(recvHost) == 1)
                System.out.println("Error, host already exists");
            else
                System.out.println("Host added!");

        } catch (Exception e) {
            e.getStackTrace();
        }


        Keys key = new Keys();
        
        // find record for local host
        String localIp = hostIp().getHostAddress();
        Host localHost = knownHosts.getHostByIP(localIp);

        // send back msg recoognizing connection
        MsgPacket msgPack = new MsgPacket(
                "SETUP-RECV-MSG",
                Hserial.serializeHost(localHost),
                "FROM:" + localHost.getName()
                        + ";TO:" + knownHosts.lookupNameByIP(ipAddress));

        // sign message
        Sign sign = new Sign();
        sign.signMsg(msgPack);

        // create host for message to be sent to
        Host sendHost = new Host("recv", ipAddress,
                key.convertPublicKey(knownHosts.lookupPubKeyByIP(ipAddress)));

        // send packet back to host
        new PackRouting(sendHost, msgPack);

    }

    private void setupRecvMsg(MsgPacket msg) {
        
        Sign signer = new Sign();

        KnownHosts knownHosts = new KnownHosts();
        HostSerialization Hserial = new HostSerialization();

        // unpack host & Store key
        try {
            Host recvHost = Hserial.deserializeHost(msg.getMsg());

            if (knownHosts.insertRecord(recvHost) == 1)
                System.out.println("Error, host already exists");
            else
                System.out.println("Host added!");

        } catch (Exception e) {
            e.getStackTrace();
        }


    }

}
