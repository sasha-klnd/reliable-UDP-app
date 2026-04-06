package udpdemo;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
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
    private byte[] lastAckSent = null;
    
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

        socket.setSoTimeout(5000);
        
        while (!endOfTransfer) {
            try {
                socket.receive(receiveDatagram);
    
                Packet receivePacket = PacketParser.parse(receiveDatagram);
    
                if (receivePacket.getPacketType() == PacketType.ERROR) {
                    String errorMessage = new String(receivePacket.getBody(), StandardCharsets.UTF_8);
                    System.err.println("Server error: " + errorMessage);
                    return;
                }
    
                if (!(receivePacket.getConnectionId() == connectionId)) {
                    continue;   // Discard packet and keep waiting
                } 
                
                if (receivePacket.getSequenceNumber() == expectedSeqNum) {
                    if (fos == null) {
                        Path outputPath = Paths.get("")
                            .toAbsolutePath()
                            .resolve("output")
                            .resolve(resourceName);
        
                        fos = new FileOutputStream(outputPath.toString());
                    }
                    
                    fos.write(receivePacket.getBody());
                    sendAck();
                    expectedSeqNum = Utils.nextSequenceNumber(expectedSeqNum);
                } else if (receivePacket.getSequenceNumber() == Utils.previousSequenceNumber(expectedSeqNum)) {
                    // Duplicate data packet, the client should resend the last ACK
                    System.out.println("[SESSION] Received duplicate packet, resending ACK");
                    resend();
                } else {
                    sendError("Unexpected sequence number");
                }
                
                if (receivePacket.getPacketType() == PacketType.EOF) {
                    sendAck();
                    endOfTransfer = true;
                    
                    System.out.println("[SESSION] Successfully received " + resourceName);
                }
            } catch (SocketTimeoutException e) {
                System.out.println("[SESSION] Timeout waiting for DATA, resending last ACK");
                resend();
            }
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

        lastAckSent = packet;
        
        System.out.println("[SESSION] Sent ACK for packet with sequence number " + expectedSeqNum);
    }

    private void resend() throws IOException {
        socket.send(new DatagramPacket(
            lastAckSent, 
            lastAckSent.length,
            serverAddress,
            serverPort
        ));
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
