package protocol;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class PacketParser {
    public static Packet parse(DatagramPacket datagram) {
        ByteBuffer buf = ByteBuffer.wrap(datagram.getData());

        int connectionID    = buf.getInt();
        byte seqNum         = buf.get();
        PacketType type     = PacketType.fromCode(buf.get());
        short payloadLength = buf.getShort();
        byte[] body         = new byte[payloadLength];

        buf.get(body);

        return new Packet(connectionID, seqNum, type, payloadLength, body);
    }
}
