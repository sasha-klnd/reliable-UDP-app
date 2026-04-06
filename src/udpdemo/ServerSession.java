package udpdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import protocol.Packet;
import protocol.PacketBuilder;
import protocol.PacketParser;
import protocol.PacketType;
import protocol.Utils;

public class ServerSession {
    private final DatagramSocket socket;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final int connectionId;
    private final short maxSegmentSize;
    private byte expectedSeqNum;
    
    private byte[] lastPacketSent = null;

    public ServerSession(DatagramSocket socket, InetAddress clientAddress, int clientPort,
            int connectionId, short maxSegmentSize, byte expectedSeqNum) {
        this.socket = socket;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.connectionId = connectionId;
        this.maxSegmentSize = maxSegmentSize;
        this.expectedSeqNum = expectedSeqNum;
    }

    public void run(FileInputStream fis) throws IOException {
        sendData(fis);
        System.out.println("[SESSION] Closing session");
    }
    
    private void sendData(FileInputStream fis) throws IOException {
        short maxBodySize = (short) (maxSegmentSize - 8);
        byte[] bodyBuffer = new byte[maxBodySize];
        int bytesRead;

        while ((bytesRead = fis.read(bodyBuffer)) != -1) {
            byte[] body = Arrays.copyOf(bodyBuffer, bytesRead);
            lastPacketSent = PacketBuilder.build(
                connectionId, 
                expectedSeqNum, 
                PacketType.DATA,
                body
            );

            socket.send(new DatagramPacket(
                lastPacketSent, 
                lastPacketSent.length,
                clientAddress,
                clientPort
            ));

            System.out.println("[SESSION] Sent data with sequence number " + expectedSeqNum);

            waitForAck();
        }

        System.out.println("[SESSION] Reached end of file");

        lastPacketSent = PacketBuilder.build(
            connectionId,
            expectedSeqNum,
            PacketType.EOF,
            new byte[0]
        );

        socket.send(new DatagramPacket(
            lastPacketSent, 
            lastPacketSent.length,
            clientAddress,
            clientPort
        ));

        System.out.println("[SESSION] Sent EOF packet with sequence number " + expectedSeqNum);
        waitForAck();
    }

    private void waitForAck() throws IOException {
        byte[] receiveBuffer = new byte[maxSegmentSize];
        DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        
        while (true) {
            socket.receive(receiveDatagram);
            Packet response = PacketParser.parse(receiveDatagram);

            if (response.getConnectionId() != connectionId) {
                continue;   // Discard packet and continue listening
            }

            if (response.getPacketType() != PacketType.ACK) {
                sendError("Expected ACK");
                continue;
            }

            if (response.getSequenceNumber() != expectedSeqNum) {
                sendError("Incorrect sequence number");
                resend();
                continue;
            }

            expectedSeqNum = Utils.nextSequenceNumber(expectedSeqNum);
            return;
        }
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
            clientAddress,
            clientPort
        ));
        
        expectedSeqNum = Utils.nextSequenceNumber(expectedSeqNum);
    }

    private void resend() throws IOException {
        socket.send(new DatagramPacket(
            lastPacketSent,
            lastPacketSent.length,
            clientAddress,
            clientPort
        ));
    }
}
