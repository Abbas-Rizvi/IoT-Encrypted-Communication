package network;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// route packet to other node
public class PackRouting {

    MsgPacket msgPacket;
    Host targHost;

    // constructor
    public PackRouting(Host targHost, MsgPacket msgPacket) {
        this.targHost = targHost;
        this.msgPacket = msgPacket;
    }

    // send message to host
    public void run() {

        // System.out.println("Thread started for sending to " + host.getIp());

        try {
            // create socket channel
            SocketChannel socketChannel = SocketChannel.open();
            InetSocketAddress addr = new InetSocketAddress(targHost.getIp(), targHost.getPORT());

            // connect without blocking
            socketChannel.configureBlocking(false);

            socketChannel.connect(addr);

            // check if the connection is complete
            while (!socketChannel.finishConnect()) {

                Thread.sleep(100); // sleep for a short duration
            }

            // System.out.println("# Connected to " + host.getIp());

            // Send the file bytes
            ByteBuffer buffer = ByteBuffer.wrap(msgPacket.serialize());
            socketChannel.write(buffer);

            if (socketChannel != null && socketChannel.isConnectionPending())
                socketChannel.close();

        } catch (

        Exception e) {
            System.err.println("# Unable to connect to " + targHost.getIp() + "; " + e);
        }

    }

}
