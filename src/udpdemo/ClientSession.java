package udpdemo;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import java.util.Random;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import protocol.Packet;
import protocol.PacketBuilder;
import protocol.PacketParser;
import protocol.PacketType;
import protocol.Utils;

public class ClientSession {
    
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final int serverPort;
    private final int connectionId;
    private final String resourceName;
    private final short maxSegmentSize;

    private byte expectedSeqNum = 0;
    private boolean endOfTransfer = false;
    private FileOutputStream fos = null;
    
    public ClientSession(DatagramSocket socket, InetAddress serverAddress, int serverPort, String resourceName, short maxSegmentSize) {
        this.socket = socket;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connectionId = generateConnectionID();
        this.resourceName = resourceName;
        this.maxSegmentSize = maxSegmentSize;
    }

    public void run() throws IOException {
        sendRequest();
        receiveData();
    }

    private void sendRequest() throws IOException {
        String requestMessage = "mss=" + maxSegmentSize + ",resourcename=\"" + resourceName + "\"";
        byte[] body = requestMessage.getBytes(StandardCharsets.UTF_8);
        byte[] packet = PacketBuilder.build(
            connectionId, 
            expectedSeqNum,
            PacketType.REQUEST, 
            body
        );
        socket.send(new DatagramPacket(
            packet, 
            packet.length,
            serverAddress,
            serverPort
        ));
        System.out.println("[SESSION] Sent request for " + resourceName + " with sequence number " + expectedSeqNum);
    }

    private void receiveData() throws IOException {
        byte[] receiveBuffer = new byte[maxSegmentSize];
        DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        while (!endOfTransfer) {
            socket.receive(receiveDatagram);

            Packet receivePacket = PacketParser.parse(receiveDatagram);

            if (receivePacket.getPacketType() == PacketType.ERROR) {
                String errorMessage = new String(receivePacket.getBody(), StandardCharsets.UTF_8);
                System.err.println("Server error: " + errorMessage);
                return;
            }

            if (fos == null) {
                Path outputPath = Paths.get("")
                    .toAbsolutePath()
                    .resolve("output")
                    .resolve(resourceName);

                fos = new FileOutputStream(outputPath.toString());
            }

            if (!(receivePacket.getConnectionId() == connectionId)) {
                continue;   // Discard packet and keep waiting
            } else if (!Utils.isInOrder(receivePacket.getSequenceNumber(), expectedSeqNum)) {
                sendError("Received an out-of-order packet. Expected sequence number: " + expectedSeqNum);
                continue;
            } else if (receivePacket.getPacketType() == PacketType.EOF) {
                sendAck();
                endOfTransfer = true;
                
                System.out.println("[SESSION] Successfully received " + resourceName);
                continue;
            }

            fos.write(receivePacket.getBody());
            sendAck();
        }
    }
    
    private void sendAck() throws IOException {
        byte[] body = new byte[0];
        byte[] packet = PacketBuilder.build(
            connectionId, 
            expectedSeqNum,
            PacketType.ACK,
            body
        );
        socket.send(new DatagramPacket(
            packet, 
            packet.length,
            serverAddress,
            serverPort
        ));
        
        System.out.println("[SESSION] Sent ACK for packet with sequence number " + expectedSeqNum);
        expectedSeqNum = Utils.nextSequenceNumber(expectedSeqNum);
    }

    private void sendError(String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        byte[] packet = PacketBuilder.build(
            connectionId, 
            expectedSeqNum,
            PacketType.REQUEST, 
            body
        );
        socket.send(new DatagramPacket(
            packet, 
            packet.length,
            serverAddress,
            serverPort
        ));
    }

    private static int generateConnectionID() {
        Random rand = new Random();
        return (int) rand.nextInt(0, Integer.MAX_VALUE);
    }
}
