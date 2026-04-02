package udpdemo;

import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import java.util.Random;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UDPClient {

    public static void main(String[] args) throws Exception {
        // Create params hashmap 
        // String[] acceptedParams = {"srvr-ip", "srvr-port", "resource-name", "segment-size"};
        // Map<String, String> params = createParamsHashMap(args, acceptedParams);
        // final InetAddress IP_ADDR = InetAddress.getLocalHost();
        // final int SRVR_PORT_NUM = 9999;

        final InetAddress SRVR_IP = InetAddress.getLocalHost();
        final short SRVR_PORT = 9999;
        final short MAX_SEG_SIZE = 512;
        final String RESOURCE_NAME = "Paramore - This is Why.mp3";
        byte seqNum = 0;
        
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] receiveBuffer = new byte[MAX_SEG_SIZE];

        // BUILD PACKET
        byte[] packetBody = RESOURCE_NAME.getBytes(StandardCharsets.UTF_8);
        short payloadLength = (short) packetBody.length;

        int connectionID = generateConnectionID();

        byte[] sendPacketBytes = ByteBuffer.allocate(8 + payloadLength)
            .putInt((int) connectionID)
            .put((byte) seqNum)
            .put(PacketType.REQUEST.code)
            .putShort((short) payloadLength)
            .put(packetBody, 0, payloadLength)
            .array();

        DatagramPacket sendPacket = new DatagramPacket(
            sendPacketBytes, 
            sendPacketBytes.length, 
            SRVR_IP, 
            SRVR_PORT
        );
        clientSocket.send(sendPacket);

        System.out.println("[CLIENT] Sent packet.");
        System.out.println("[CLIENT] Packet Info:");
        System.out.println(" - Connection ID: " + connectionID);
        System.out.println(" - Sequence Number: " + seqNum);
        System.out.println(" - Type: " + PacketType.REQUEST);
        System.out.println(" - Payload Length: " + payloadLength);
        System.out.println("\nPacket Body:");
        System.out.println(RESOURCE_NAME);


        // DECODE RECEIVED PACKET
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        clientSocket.receive(receivePacket); 

        byte[] receivePacketBytes = receivePacket.getData();


        // System.out.println("result: " + receiveData);

        clientSocket.close();
    }

    private static HashMap<String, String> createParamsHashMap(String[] args, String[] acceptedParams) throws IllegalArgumentException {
        if (args.length % 2 != 0) throw new IllegalArgumentException("All parameters must be passed with the format \'--parameter-name value\'.");

        HashMap<String, String> returnMap = new HashMap<>();

        for (int i = 0; i < args.length; i += 2) {
            if (!args[i].startsWith("--")) {
                continue;
            }

            // Remove leading "--"
            String paramName = args[i].substring(2);
            for (String a : acceptedParams) {
                if (a.equals(paramName)) {
                    String paramValue = args[i+1];
                    returnMap.put(paramName, paramValue);
                }
            }
        }

        return returnMap;

    }

    private static int generateConnectionID() {
        Random rand = new Random();
        return (int) rand.nextInt(0, Integer.MAX_VALUE);
    }

}