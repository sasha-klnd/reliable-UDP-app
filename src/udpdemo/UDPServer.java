package udpdemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import protocol.Packet;
import protocol.PacketBuilder;
import protocol.PacketParser;
import protocol.PacketType;

public class UDPServer {

    public static void main(String[] args) throws Exception {

        final int SERVER_PORT = 9999;
        DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT);

        byte[] requestBuffer = new byte[512];
        DatagramPacket requestDatagram = new DatagramPacket(requestBuffer, requestBuffer.length);
        System.out.println("Awaiting request from client.");
        serverSocket.receive(requestDatagram);


        Packet requestPacket = PacketParser.parse(requestDatagram);
        String requestBody = new String(requestPacket.getBody(), StandardCharsets.UTF_8);
        
        short maximumSegSize = Short.parseShort(requestBody.substring(
            requestBody.indexOf("=") + 1,
            requestBody.indexOf(",")
        ));

        String resourceName = requestBody.substring(
            requestBody.indexOf("\"") + 1,
            requestBody.lastIndexOf("\"")
        );

        System.out.println("[SERVER] Received request for " + resourceName);
        
        FileInputStream fis = null;
        Path resourcePath = Paths.get("")
            .toAbsolutePath()
            .resolve("resources")
            .resolve(resourceName);

        try {   
            fis = new FileInputStream(resourcePath.toString());
            System.out.println("[SERVER] Successfully located the resource.");
        } catch (FileNotFoundException e) {
            System.out.println("[SERVER] Requested resource could not be found.");
            
            byte[] errorPacket = PacketBuilder.build(
                requestPacket.getConnectionId(), 
                requestPacket.getSequenceNumber(), 
                PacketType.ERROR, 
                "The requested resource could not be found.".getBytes(StandardCharsets.UTF_8)
            );

            serverSocket.send(new DatagramPacket(
                errorPacket,
                errorPacket.length,
                requestDatagram.getAddress(),
                requestDatagram.getPort()
            ));

            serverSocket.close();
            return;
        }

        ServerSession session = new ServerSession(
            serverSocket, 
            requestDatagram.getAddress(), 
            requestDatagram.getPort(), 
            requestPacket.getConnectionId(),
            maximumSegSize,
            requestPacket.getSequenceNumber()
        );

        System.out.println("Starting session.");
        session.run(fis);
        System.out.println("Successfully transferred resource " + resourceName);

        fis.close();
        serverSocket.close();
    }
}
