package udpdemo;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.net.DatagramPacket;

public class UDPClient {

    public static void main(String[] args) throws Exception {
        // Create params hashmap 
        String[] acceptedParams = {"srvr-ip", "srvr-port", "resource-name", "segment-size"};
        Map<String, String> params = createParamsHashMap(args, acceptedParams);

        final InetAddress IP_ADDR = InetAddress.getLocalHost();
        final int SRVR_PORT_NUM = 9999;
        
        DatagramSocket ds = new DatagramSocket();
        byte[] receiveBuf = new byte[1024];

        // Data
        int i = 8;
        byte[] b = String.valueOf(i).getBytes();

        // Build packet wth IP_ADDR and PORT_NUM, and send
        DatagramPacket sendPacket = new DatagramPacket(b, b.length, IP_ADDR, SRVR_PORT_NUM);
        ds.send(sendPacket);

        // Receive any packets sent to this port
        DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        ds.receive(receivePacket); 

        // Read data and print
        String receiveData = new String(receivePacket.getData());
        System.out.println("result: " + receiveData);

        ds.close();
    }

    private static HashMap<String, String> createParamsHashMap(String[] args, String[] acceptedParams) throws IllegalArgumentException {
        if (args.length % 2 != 0) throw new IllegalArgumentException("All parameters must be passed with the format \'--parameter-name value\'.");

        HashMap<String, String> returnMap = new HashMap<>();

        for (int i = 0; i < args.length; i += 2) {
            if (!args[i].startsWith("--")) {
                continue;
            }

            // Remove leading "--"
            String paramName = args[i].substring(2);
            for (String a : acceptedParams) {
                if (a.equals(paramName)) {
                    String paramValue = args[i+1];
                    returnMap.put(paramName, paramValue);
                }
            }
        }

        return returnMap;

    }

}