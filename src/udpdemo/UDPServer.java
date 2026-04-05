package udpdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import protocol.Packet;
import protocol.PacketParser;

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

        ServerSession session = new ServerSession(
            serverSocket, 
            requestDatagram.getAddress(), 
            requestDatagram.getPort(), 
            requestPacket.getConnectionId(),
            maximumSegSize,
            requestPacket.getSequenceNumber()
        );

        System.out.println("Starting session.");
        session.run(resourceName);
        System.out.println("Successfully transferred resource " + resourceName);

        serverSocket.close();
    }
}
