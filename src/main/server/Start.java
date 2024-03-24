package server;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import crypt.Keys;
import network.Host;
import network.KnownHosts;

public class Start extends Thread {

    static KnownHosts db = new KnownHosts();
    static String username;
    static Keys key;
    static Host localhost;

    /////////////////////////////////////////
    // get IP Address of host
    public static InetAddress hostIp() {

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {

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
        System.out.println("Starting server setup...");

        // get ip of host
        String localIp = hostIp().getHostAddress();
        System.out.println("Host Ip: " + localIp);

        if (checkKeysExist()) { // check if keys exist

            key = new Keys(); // Load keys

            KnownHosts knownHosts = new KnownHosts();

            // create host
            localhost = new Host(username, localIp, key.getPublicKey());
            
            String name = knownHosts.lookupNameByIP(localIp);

       
            System.out.println("ID: " + name);

        } else {

            // enter username if creating new keys
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter username:");
            username = scan.nextLine();
            System.out.println("ID: " + username);
            

            // create keys
            key = new Keys();
            key.generateRSAKkeyPair();

            // create the local host
            localhost = new Host(username, localIp, key.getPublicKey());
            
            // check if name taken
            if (db.insertRecord(localhost) == 1) {
                System.out.println("User " + localhost.getName() + " aleady exists!");
            }
            
        }

        System.out.println("Setup Complete!");

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

        // Delay for a short period to allow setup to complete
        try {
            Thread.sleep(1000); // Adjust the sleep time as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // Handle the running of the server
    // run on thread to not interupt main program
    @Override
    public void run() {

        System.out.println("Server listening on " + localhost.getIp() + ":" +
                localhost.getPORT() + "...");

        localhost.startListener();

    }

}
