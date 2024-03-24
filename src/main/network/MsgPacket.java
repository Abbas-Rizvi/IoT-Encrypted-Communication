package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class MsgPacket implements Serializable{

    private String type;
    private String metadata;
    private byte[] msg;
    private byte[] sig;

    // constructor
    // creates message
    // define message for sending packets over network
    public MsgPacket(String type, String msgString, String metadata) {
        this.type = type;
        this.metadata = metadata;

        byte[] encodedMsg = Base64.getEncoder().encode(msgString.getBytes());
        this.msg = encodedMsg;

    }

    public MsgPacket(String type, byte[] msgByte, String metadata) {
        this.type = type;
        this.metadata = metadata;
        this.msg = msgByte;

    }

    // read message from byte[]
    public MsgPacket deserialize(byte[] msgBytes) {

        try {
            ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(msgBytes);
            ObjectInputStream ois = new ObjectInputStream(byteArrayIn);

            // cast input to object
            Object obj = ois.readObject();

            if (obj instanceof MsgPacket) {

                this.type = ((MsgPacket) obj).type;
                this.msg = ((MsgPacket) obj).msg;

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

    public String getMetadata() {
        return metadata;
    }

    public String getMsgString() {
        // Decoding the Base64 encoded message
        byte[] decodedMsg = Base64.getDecoder().decode(this.msg);
        // Converting the decoded bytes to a string
        return new String(decodedMsg);
    }

    public byte[] getSig() {
        return sig;
    }

    public void setSig(byte[] signedBytes) {
        this.sig = signedBytes;
    }

    // encode string to base64 and store
    public void setMsg(String msgString) {
        byte[] encodedMsg = Base64.getEncoder().encode(msgString.getBytes());
        this.msg = encodedMsg;
    }

}
