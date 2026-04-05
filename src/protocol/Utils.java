package protocol;

import java.util.Random;

public class Utils {
    
    public static byte nextSequenceNumber(byte current) {
        return (byte) ((current + 1) % 128);
    }

    public static boolean isInOrder(byte received, byte expected) {
        return received == expected;
    }

}
