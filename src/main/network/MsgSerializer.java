package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MsgSerializer {

    // read message from byte[]
    public MsgPacket deserialize(byte[] msgBytes) {

        try {
            ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(msgBytes);
            ObjectInputStream ois = new ObjectInputStream(byteArrayIn);

            // cast input to object
            Object obj = ois.readObject();

            if (obj instanceof MsgPacket) {

                return (MsgPacket) obj;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // convert message to a byte[]
    public byte[] serialize(MsgPacket msg) {

        try {

            // create output streams
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOut);

            // Write the object to the byte array stream
            oos.writeObject(msg);

            // get byte array from the output stream
            byte[] byteArray = byteArrayOut.toByteArray();

            return byteArray;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
