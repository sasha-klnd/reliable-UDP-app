package udpdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
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
        linger();
    }
    
    private void sendData(FileInputStream fis) throws IOException {
        short maxBodySize = (short) (maxSegmentSize - 8);
        byte[] bodyBuffer = new byte[maxBodySize];
        int bytesRead;

        socket.setSoTimeout(5000);

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
            try {
                socket.receive(receiveDatagram);
                Packet response = PacketParser.parse(receiveDatagram);
    
                if (response.getConnectionId() != connectionId || response.getPacketType() != PacketType.ACK) {
                    continue;   // Discard packet and continue waiting
                }
    
                if (response.getSequenceNumber() == expectedSeqNum) {
                    expectedSeqNum = Utils.nextSequenceNumber(expectedSeqNum);
                    return;
                }

                if (response.getSequenceNumber() == Utils.previousSequenceNumber(expectedSeqNum)) {
                    // Duplicate ACK, the server should resend the last data packet
                    resend();
                    continue;
                }

                // Received a non-duplicate unexpected sequence number
                sendError("Unexpected sequence number.");
            } catch (SocketTimeoutException e) {
                System.out.println("[SESSION] ACK timeout, resending last packet");
                resend();
            }
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

    private void linger() throws IOException {
        System.out.println("[SESSION] Complete, entering linger state");
        socket.setSoTimeout(10_000);

        byte[] receiveBuffer = new byte[maxSegmentSize];
        DatagramPacket receiveDatagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        while (true) {
            try {
                socket.receive(receiveDatagram);
                Packet response = PacketParser.parse(receiveDatagram);

                if (response.getConnectionId() != connectionId) { 
                    continue; 
                }

                if (response.getPacketType() == PacketType.ACK &&
                    response.getSequenceNumber() == Utils.previousSequenceNumber(expectedSeqNum)) {
                    System.out.println("[SESSION] Duplicate ACK during linger, resending EOF");
                    resend();
                    continue;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("[SESSION] Linger timeout, closing session.");
                return;
            }
        }
    }
}
