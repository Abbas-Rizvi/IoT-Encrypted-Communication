package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Iterator;

import crypt.Keys;
import crypt.Sign;
import network.Host;
import network.KnownHosts;
import network.MsgPacket;
import network.PackRouting;

public class Listener implements Serializable {

    private static final long serialVersionUID = 123456789L;

    // constant for port
    private final int PORT = 5687;

    private PublicKey pubKey;
    private String pubKeyStr;

    Selector selector;

    // get the public key
    public PublicKey getPublicKey() {

        // decode public key
        Keys KeyDecode = new Keys();
        pubKey = KeyDecode.convertPublicKey(pubKeyStr);

        return pubKey;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // listen for connections, handle in non-blocking manner
    public void listener() {

        try {

            // create selector for connecting hosts
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            // OP_ACCEPT is for when the server accepts connection from the client
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // System.out.println("Server started on port " + PORT);

            // receive connections
            while (true) {

                // ignore empty selector
                if (selector.select() == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                // queue for incoming requests
                while (keyIterator.hasNext()) {
                    // remove from the queue
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        // handle connect request
                        handleAccept(key, selector);
                    } else if (key.isReadable()) {
                        // handle read request
                        handleRead(key);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // handle accept of new connections
    // pass the currently stored blockchain
    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("### Connection accepted from: " + socketChannel.getRemoteAddress());

    }

    // handle send requests from connection
    // parse message type
    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int bufferSize = 1024 * 1024 * 1024; // 1 KB, adjust as needed
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        // Track the total number of bytes read
        int totalBytesRead = 0;

        while (totalBytesRead < bufferSize) {
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead == -1) {
                // Connection closed by the client
                System.out.println("Connection closed by: " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
            } else if (bytesRead == 0) {
                // No more data to read
                break;
            }
        }

        // Reset the position and limit to read the entire buffer
        buffer.flip();

        // Check if any data was received
        if (buffer.remaining() > 0) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            // System.out.println("Received message from " +
            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            String ipAddress = remoteAddress.getAddress().getHostAddress();

            // send to decoder for decoding of message
            Decoder decoder = new Decoder();
            decoder.decode(data, ipAddress, socketChannel);
        }
    }
}
