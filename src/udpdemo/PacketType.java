package udpdemo;

public enum PacketType {
    REQUEST ((byte) 0),
    ACK     ((byte) 1),
    DATA    ((byte) 2),
    ERROR   ((byte) 3);

    public final byte code;

    PacketType(byte code) {
        this.code = code;
    }

    public static PacketType fromCode(byte code) {
        for (PacketType type: values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown request type code: " + code);
    }
}
