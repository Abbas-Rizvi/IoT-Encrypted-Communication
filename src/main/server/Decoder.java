package server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import crypt.Encrypt;
import crypt.Keys;
import crypt.Sign;
import network.Host;
import network.HostSerialization;
import network.KnownHosts;
import network.MsgPacket;
import network.MsgSerializer;
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

        MsgSerializer mSerializer = new MsgSerializer();

        MsgPacket msg = mSerializer.deserialize(data);

        /*
         * // cast data into message object to allow understanding
         * try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
         * ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
         * 
         * Object obj = objectStream.readObject();
         * if (obj instanceof MsgPacket) {
         * msg = (MsgPacket) obj;
         * }
         * } catch (IOException | ClassNotFoundException e) {
         * e.printStackTrace();
         * }
         */

        // if message is not empty
        if (msg != null) {

            try {

                // print what was received
                System.out.println("Received " + msg.getType() + " From " + socketChannel.getRemoteAddress());

                // handle recived message
                String messageType = msg.getType().toUpperCase();
                switch (messageType) {

                    case "SETUP":
                        // client initial conenct to server
                        setupMsg(msg, ipAddress);
                        break;

                    case "SETUP-RECV-MSG":
                        // server response to inital client connect
                        setupRecvMsg(msg);
                        break;

                    case "REQ-PUBKEY":
                        // client requesting key from server
                        reqPubKey(msg, ipAddress);
                        break;

                    case "RECV-PUBKEY":
                        // server processsing client req for key
                        recvPubKey(msg, ipAddress);
                        break;

                    case "FORWARD-CRYPT":
                        // forward encrypted packet
                        forwardCrypt(msg);
                        break;

                    case "RECV-MSG":
                        // Decrypt and output msg
                        recvMsg(msg);
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
                knownHosts.lookupPubKeyByIP(ipAddress));

        // send packet back to host
        PackRouting packR = new PackRouting(sendHost, msgPack);
        packR.start();

    }

    // setup message
    // assume message contains a HOST object
    private void setupRecvMsg(MsgPacket msg) {

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

    private void reqPubKey(MsgPacket msg, String ipAddress) {

        KnownHosts knownHosts = new KnownHosts();
        HostSerialization Hserial = new HostSerialization();

        // verify integrity of signature
        Sign signer = new Sign();

        if (!signer.verifySignature(msg))
            System.out.println("unknown signature; discarding message");
        else {

            // get requested name
            // resolve using database
            String reqSearch = msg.getMsgString();

            String tarHostIp = knownHosts.lookupIPAddressByName(reqSearch);
            Host tarHost = knownHosts.getHostByIP(tarHostIp);

            Keys key = new Keys();
            Encrypt encrypt = new Encrypt();

            byte[] msgPayload = encrypt.encrypt(
                    Hserial.serializeHost(tarHost),
                    key.convertPublicKey(knownHosts.lookupPubKeyByIP(ipAddress)));

            // send back msg recoognizing connection
            MsgPacket msgPack = new MsgPacket(
                    "RECV-PUBKEY",
                    msgPayload,
                    null);

            // create host for message to be sent to
            Host sendHost = new Host("recv", ipAddress,
                    knownHosts.lookupPubKeyByIP(ipAddress));

            // sign message
            Sign sign = new Sign();
            sign.signMsg(msgPack);

            // send packet back to host
            PackRouting packR = new PackRouting(sendHost, msgPack);
            packR.start();

        }

    }

    private void recvPubKey(MsgPacket msg, String ipAddress) {

        KnownHosts knownHosts = new KnownHosts();
        HostSerialization Hserial = new HostSerialization();
        Keys key = new Keys();
        Encrypt encrypt = new Encrypt();

        // verify integrity of signature
        Sign signer = new Sign();

        if (!signer.verifySignature(msg))
            System.out.println("unknown signature; discarding message");
        else {

            // decrypt
            Host destHost = Hserial.deserializeHost(encrypt.decrypt(msg.getMsg()));

            // get server host info
            Host tarHost = knownHosts.getHostByIP(ipAddress);

            // read message stored in file
            String msgPath = "data/msg.txt";

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(msgPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                // delete file
                File delFile = new File(msgPath);
                delFile.delete();

            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
            }

            // enter payload & encrypt for destination
            byte[] destEncryption = encrypt.encrypt(content.toString().getBytes(), destHost.getPubKey());

            // send msg to server regarding packet
            MsgPacket msgPack = new MsgPacket(
                    "FORWARD-CRYPT",
                    destEncryption,
                    destHost.getName());

            // create host for message to be sent to
            Host sendHost = new Host("recv", ipAddress,
                    knownHosts.lookupPubKeyByIP(ipAddress));

            // sign message
            Sign sign = new Sign();
            sign.signMsg(msgPack);

            // send packet back to host
            PackRouting packR = new PackRouting(sendHost, msgPack);
            packR.start();

        }

    }

    // forward encrypted packet to the destination
    private void forwardCrypt(MsgPacket msg) {

        KnownHosts knownHosts = new KnownHosts();
        HostSerialization Hserial = new HostSerialization();
        Keys key = new Keys();
        Encrypt encrypt = new Encrypt();

        // verify integrity of signature
        Sign signer = new Sign();

        if (!signer.verifySignature(msg))
            System.out.println("unknown signature; discarding message");
        else {

            // get destination info using metadata
            String ipAddress = knownHosts.lookupIPAddressByName(msg.getMetadata());
            Host tarHost = knownHosts.getHostByIP(ipAddress);

            // send msg to destination
            MsgPacket msgPack = new MsgPacket(
                    "RECV-MSG",
                    msg.getMsg(),
                    tarHost.getName());

            // sign message
            Sign sign = new Sign();
            sign.signMsg(msgPack);

            // send packet back to host
            PackRouting packR = new PackRouting(tarHost, msgPack);
            packR.start();

        }

    }

    private void recvMsg(MsgPacket msg) {

        Encrypt encrypt = new Encrypt();

        // verify integrity of signature
        Sign signer = new Sign();

        if (!signer.verifySignature(msg))
            System.out.println("unknown signature; discarding message");
        else {

            // decrypt
            byte[] decrpytedMsgBytes = encrypt.decrypt(msg.getMsg());

            String outputMsg = new String(decrpytedMsgBytes , StandardCharsets.UTF_8);

            System.out.println(outputMsg);

        }

    }

}
