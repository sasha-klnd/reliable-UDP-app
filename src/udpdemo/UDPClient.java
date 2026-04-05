package udpdemo;

import java.util.HashMap;
import java.net.InetAddress;
import java.net.DatagramSocket;

public class UDPClient {

    public static void main(String[] args) throws Exception {
        // Create params hashmap 
        // String[] acceptedParams = {"srvr-ip", "srvr-port", "resource-name", "segment-size"};
        // Map<String, String> params = createParamsHashMap(args, acceptedParams);
        // final InetAddress IP_ADDR = InetAddress.getLocalHost();
        // final int SRVR_PORT_NUM = 9999;

        // Set by params
        final InetAddress SRVR_IP = InetAddress.getLocalHost();
        final short SRVR_PORT = 9999;
        final short MAX_SEG_SIZE = 512;
        final String RESOURCE_NAME = "crime-and-punishment.txt";

        DatagramSocket clientSocket = new DatagramSocket();
        
        ClientSession session = new ClientSession(
            clientSocket, 
            SRVR_IP, 
            SRVR_PORT, 
            RESOURCE_NAME, 
            MAX_SEG_SIZE
        );

        System.out.println("Created a session for " + RESOURCE_NAME);
        session.run();
        System.out.println("Successfully received " + RESOURCE_NAME);
        
        clientSocket.close();
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