package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.Serializable;


public class MsgPacket {

    private String type;
    private byte[] msg;

    // constructor
    // creates message
    // define message for sending packets over network
    public MsgPacket(String targetHostName, String type, byte[] msg) {
        this.type = type;
        this.msg = msg;

    }

    // read message from byte[]
    public MsgPacket deserialize(byte[] msgBytes) {

        try {
            ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(msgBytes);
            ObjectInputStream ois = new ObjectInputStream(byteArrayIn);

            // cast input to object
            Object obj = ois.readObject();

            if (obj instanceof MsgPacket) {

                this.type = ((MsgPacket)obj).type;
                this.msg = ((MsgPacket)obj).msg;
                
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
    public byte[] serialize() {

        try {

            // create output streams
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOut);

            // Write the object to the byte array stream
            oos.writeObject(this);

            // get byte array from the output stream
            byte[] byteArray = byteArrayOut.toByteArray();

            return byteArray;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getType() {
        return type;
    }

    public byte[] getMsg() {
        return msg;
    }
    

    

}
