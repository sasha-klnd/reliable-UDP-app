package protocol;

import java.nio.ByteBuffer;

public class PacketBuilder {

    public static byte[] build(int connectionID, byte sequenceNumber, PacketType packetType, byte[] body) {
        short payloadLength = (short) body.length;

        return ByteBuffer.allocate(8 + payloadLength)
            .putInt(connectionID)
            .put((byte) sequenceNumber)
            .put((byte) packetType.code)
            .putShort((short) payloadLength)
            .put(body)
            .array();
    }
}
