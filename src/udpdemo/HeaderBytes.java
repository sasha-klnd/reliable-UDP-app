
package udpdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class HeaderBytes {

    public static void main(String[] args) throws Exception {

        final InetAddress SRVR_IP = InetAddress.getLocalHost();
        final short SRVR_PORT = 9999;
        final short SEG_SIZE = 512;
        final String RESOURCE_NAME = "Paramore - This is Why.mp3";

        byte sequenceNum = 0;

        DatagramSocket ds = new DatagramSocket();
        byte[] receiveBuf = new byte[SEG_SIZE];

        // Get header values
        int connectionID = newConnectionID();
        byte packetType = getPacketTypeAsHeader("REQUEST");
        byte[] packetBody = RESOURCE_NAME.getBytes(StandardCharsets.UTF_8);
        short payloadLength = (short) packetBody.length; 

        // Convert header values into byte[]
        byte[] completePacket = ByteBuffer.allocate(8 + payloadLength)
            .putInt(connectionID)
            .put((byte) sequenceNum)
            .put((byte) packetType)
            .putShort((short) payloadLength)
            .put(packetBody, 0, payloadLength)
            .array();

        // sequenceNum = (byte) ((sequenceNum + 1) % 128);

        ds.close();
        
        System.out.println("[CLIENT] - Send Packet");
        System.out.println("Current Connection ID: " + connectionID);
        System.out.println("Current Sequence Number: " + sequenceNum);
        System.out.println("Requesting Resource: " + RESOURCE_NAME);
        System.out.println("Packet length: " + completePacket.length + "\n");

        int receivedCID = ByteBuffer.wrap(
            Arrays.copyOfRange(completePacket, 0, 4)
        ).getInt();

        byte receivedSeqNum = completePacket[4];
        byte receivedRequestType = completePacket[5];
        
        short receivedPayloadLength = ByteBuffer.wrap(
            Arrays.copyOfRange(completePacket, 6, 8)
        ).getShort();

        byte[] receivedPayload = Arrays.copyOfRange(completePacket, 8, completePacket.length);

        System.out.println("[SERVER] - Received Packet");
        System.out.println("Packet length: " + receivedPayloadLength);
        System.out.println("Connection ID: " + receivedCID);
        System.out.println("Sequence Number: " + receivedSeqNum);
        System.out.println("Request Type: " + receivedRequestType);
        System.out.println("Payload Length: " + completePacket.length);
        System.out.println("Payload: ");

        for (byte b : receivedPayload) {
            System.out.print(b + " ");
        }

        System.out.println("\nDecoded Payload: " + new String(receivedPayload, StandardCharsets.UTF_8));



        // 1. Define a string -> String a = "abcd"
        // 2. Encode string as a byte array with UTF 8 -> .getBytes(StandardCharsets.UTF_8)
        // 3. Read encoded byte array -> new String(bytes, StandardCharsets.UTF_8)

        /* 
            Connection ID: 4 bytes = int
            Sequence Number: 1 byte, incrementing = byte
            Request Type: 1 byte = byte
            Payload Length: 2 bytes = short
            Payload: 
        */

    }

    private static int newConnectionID() {
        Random rand = new Random();

        return (int) rand.nextInt(Integer.MAX_VALUE);
    }

    private static byte getPacketTypeAsHeader(String packetType) {
        if (packetType.equals("REQUEST")) {
            return (byte) 0;
        } else if (packetType.equals("DATA")) {
            return (byte) 1;
        } else if (packetType.equals("ACK")) {
            return (byte) 2;
        } else {
            // packetType == "ERROR", but also acts as default
            return (byte) 3;
        }
    }

}