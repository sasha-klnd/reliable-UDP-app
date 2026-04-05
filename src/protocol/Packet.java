package protocol;

import java.nio.charset.StandardCharsets;

public class Packet {

    private int connectionID;
    private byte sequenceNumber;
    private PacketType packetType;
    private short payloadLength;
    private byte[] body;

    public Packet(int connectionID, byte sequenceNumber, PacketType packetType, short payloadLength, byte[] body) {
        this.connectionID = connectionID;
        this.sequenceNumber = sequenceNumber;
        this.packetType = packetType;
        this.payloadLength = payloadLength;
        this.body = body;
    }

    public int getConnectionId() {
        return connectionID;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public short getPayloadLength() {
        return payloadLength;
    }

    public byte[] getBody() {
        return body;
    }

    public String toString() {
        String result = "Packet Info:";
        result += "\n  | Connection ID: " + connectionID;
        result += "\n  | Sequence Number: " + sequenceNumber;
        result += "\n  | Type: " + packetType; 
        result += "\n  | Payload Length: " + payloadLength;
        result += "\n  | Payload: " + new String(body, StandardCharsets.UTF_8);
        result += "\n";

        return result;
    }
}
