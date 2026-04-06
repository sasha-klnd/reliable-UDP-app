package protocol;

public class Utils {
    
    public static byte nextSequenceNumber(byte current) {
        return (byte) ((current + 1) % 128);
    }

    public static byte previousSequenceNumber(byte current) {
        return (byte) ((current - 1 + 128) % 128);
    }

}
