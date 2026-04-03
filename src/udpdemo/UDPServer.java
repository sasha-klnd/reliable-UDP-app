package udpdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UDPServer {

    public static void main(String[] args) throws Exception {

        final InetAddress SRVR_IP = InetAddress.getLocalHost();
        final short SRVR_PORT = 9999;
        final short MAX_SEG_SIZE = 512;
        final byte PACKET_HEADER_SIZE = 8;
        final short PACKET_BODY_SIZE = MAX_SEG_SIZE - PACKET_HEADER_SIZE;
        byte seqNum = 0;
        
        DatagramSocket serverSocket = new DatagramSocket(SRVR_PORT);
        byte[] receiveBuffer = new byte[MAX_SEG_SIZE];

        System.out.println("[SERVER] Awaiting packets...");

        // Receive any packet addressed to this port
        DatagramPacket receivePacket = new DatagramPacket(
            receiveBuffer,
            receiveBuffer.length
        );
        serverSocket.receive(receivePacket);

        System.out.println("[SERVER] Received packet.");
        
        // Decode packet data
        byte[] receivePacketBytes= receivePacket.getData();
        
        int receivedConnectionID = ByteBuffer.wrap(
            Arrays.copyOfRange(receivePacketBytes, 0, 4)
        ).getInt();
        byte receivedSequenceNumber = receivePacketBytes[4];
        PacketType receivedType = PacketType.fromCode(
            receivePacketBytes[5]
        );
        short receivedPayloadLength = ByteBuffer.wrap(
            Arrays.copyOfRange(receivePacketBytes, 6, 8)
        ).getShort();
        byte[] receivedPayload = Arrays.copyOfRange(receivePacketBytes, 8, receivePacketBytes.length);

        // Retrieve requested resource
        String requestedResource = new String(receivedPayload, StandardCharsets.UTF_8);
        FileInputStream fis;

        try {
            fis = getInputStream(requestedResource);
            sendDataPacket();
        } catch (FileNotFoundException e) {
            sendErrorPacket();
        }


        System.out.println("Packet Info:");
        System.out.println(" - Connection ID: " + receivedConnectionID);
        System.out.println(" - Sequence Number: " + receivedSequenceNumber);
        System.out.println(" - Type: " + receivedType);
        System.out.println(" - Payload Length: " + receivedPayloadLength);
        System.out.println("\nDecoded Packet:");
        System.out.println();

        // byte[] sendBuf = String.valueOf(result).getBytes();

        // // Build packet with IP_ADDR and port number of client, and send
        // DatagramPacket sendPacket = new DatagramPacket(
        //     sendBuf, 
        //     sendBuf.length, 
        //     IP_ADDR, 
        //     receivePacket.getPort()
        // );
        // ds.send(sendPacket);

        // System.out.println("[SERVER] Sending result to client.");

        serverSocket.close();
    }
    
    private static FileInputStream getInputStream(String resourceName) throws FileNotFoundException {
        Path pathToResource = Paths.get("")
            .toAbsolutePath()
            .resolve("resources")
            .resolve(resourceName);

        if (!Files.exists(pathToResource)) {
            throw new FileNotFoundException();
        }

        return new FileInputStream(pathToResource.toString());
    }

}
