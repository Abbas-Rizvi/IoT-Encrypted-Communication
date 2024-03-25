package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Scanner;

import crypt.Keys;
import crypt.Sign;
import network.Host;
import network.HostSerialization;
import network.KnownHosts;
import network.MsgPacket;
import network.PackRouting;

public class Start extends Thread {

    static KnownHosts db = new KnownHosts();
    static String username;
    static Keys key;
    static Host localhost;

    /////////////////////////////////////////
    // get IP Address of host
    public static InetAddress hostIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!(addr instanceof Inet4Address) || addr.isLoopbackAddress())
                        continue;

                    // This is your public IP address
                    return addr;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    /////////////////////////////////////////
    // Check if Key Pair exists
    public static boolean checkKeysExist() {

        String file1Path = "./data/id_rsa";
        String file2Path = "./data/id_rsa.pub";

        // Create an instance of File with the specified path
        File file1 = new File(file1Path);
        File file2 = new File(file2Path);

        if (file1.exists() && file2.exists())
            return true;

        // if no keys exist
        return false;

    }

    /////////////////////////////////////////
    // Run Setup Routine
    public static void setup() {

        // welcome prompt
        System.out.println("\033[0;92mStarting server setup...\033[0m");

        // get ip of host
        String localIp = hostIp().getHostAddress();
        System.out.println("\033[0;92mHost Ip: " + localIp + "\033[0m");

        if (checkKeysExist()) { // check if keys exist

            key = new Keys(); // Load keys

            KnownHosts knownHosts = new KnownHosts();

            String name = knownHosts.lookupNameByIP(localIp);
            System.out.println("\033[0;92mID: " + name + "\033[0m");

            // create host
            localhost = new Host(name, localIp, key.getPublicKeyStr());

        } else {

            // enter username if creating new keys
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter username:");
            username = scan.nextLine();
            System.out.println("\033[0;92mID: " + username + "\033[0m");

            // create keys
            key = new Keys();
            key.generateRSAKkeyPair();

            // create the local host
            localhost = new Host(username, localIp, key.getPublicKeyStr());

            // check if name taken
            if (db.insertRecord(localhost) == 1) {
                System.out.println("User " + localhost.getName() + " aleady exists!");
            }

        }

        System.out.println("\033[0;92mSetup Complete!\033[0m\n");

    }

    /////////////////////////////////////////
    // Main
    public static void main(String[] args) {

        // setup server
        setup();

        // Start a listening server thread
        Start start = new Start();
        Thread nodeListen = new Thread(start);
        nodeListen.start();

        // Program driver
        Scanner scanner = new Scanner(System.in);

        int choice;
        do {
            // delay 
            try {
                Thread.sleep(1000); // Adjust the sleep time as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("\nMain Menu:");
            System.out.println("1. Connect to server");
            System.out.println("2. Send message");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("\nConnecting to server...");

                    scanner.nextLine(); // escape the \n
                    System.out.print("Enter Server IP: ");
                    String ip = scanner.nextLine();

                    Host server = new Host("", ip);

                    System.out.println("Sending request to server for connection...");

                    HostSerialization Hserial = new HostSerialization();
                    MsgPacket conReq = new MsgPacket("SETUP", Hserial.serializeHost(localhost), "");

                    PackRouting packR = new PackRouting(server, conReq);
                    packR.start();

                    System.out.println("Connection Req Sent!");

                    break;
                case 2:
                    System.out.println("\nSending message...");

                    scanner.nextLine(); // escape the \n

                    System.out.print("Enter server name: ");
                    String serverName = scanner.nextLine();

                    System.out.print("Enter recipeint name: ");
                    String recipName = scanner.nextLine();

                    // get server host
                    KnownHosts knownHosts = new KnownHosts();
                    Host serverHost = knownHosts.getHostByIP(knownHosts.lookupIPAddressByName(serverName));

                    // send message to serv
                    System.out.print("Enter Message to send: ");
                    String msgTxt = scanner.nextLine();

                    // put msg in file so can be accessed by thread
                    String msgPath = "data/msg.txt";

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(msgPath))) {
                        writer.write(msgTxt);
                    } catch (IOException e) {
                        System.err.println("Error writing msg to temp file: " + e.getMessage());
                    }

                    ;

                    // initiate message request chain
                    MsgPacket msgForward = new MsgPacket("REQ-PUBKEY", recipName, "");

                    // sign message
                    Sign sign = new Sign();
                    sign.signMsg(msgForward);

                    PackRouting packR1 = new PackRouting(serverHost, msgForward);

                    packR1.start();

                    break;
                case 3:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                    break;
            }
        } while (choice != 3);

        scanner.close();

    }

    // Handle the running of the server
    // run on thread to not interupt main program
    @Override
    public void run() {

        // System.out.println("\033[0;92mServer listening on " + localhost.getIp() + ":"
        // +
        // localhost.getPORT() + "...\033[0m");

        localhost.startListener();

    }

}
