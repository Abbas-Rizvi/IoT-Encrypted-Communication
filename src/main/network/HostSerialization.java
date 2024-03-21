package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HostSerialization {

    public byte[] serializeHost(Host host) {

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
            objectOutputStream.writeObject(host);
            objectOutputStream.flush();
            return byteStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Host deserializeHost(byte[] bytes) {

        try {

            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
            return (Host) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
