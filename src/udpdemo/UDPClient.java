package udpdemo;

import java.net.InetAddress;
import java.net.DatagramSocket;

public class UDPClient {

    public static void main(String[] args) throws Exception {
        // Default values
        InetAddress serverAddress = InetAddress.getByName("localhost");
        short serverPort = 9999;
        String resourceName = "ulysses.txt";
        short maxSegmentSize = 512;

        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("--address")) {
                serverAddress = InetAddress.getByName(args[i+1]);
            }

            if (args[i].equals("--port")) {
                serverPort = Short.parseShort(args[i+1]);
            }

            if (args[i].equals("--resource-name")) {
                resourceName = args[i+1];
            }

            if (args[i].equals("--segment-size")) {
                maxSegmentSize = Short.parseShort(args[i+1]);
            } 
        }

        DatagramSocket clientSocket = new DatagramSocket();
        
        ClientSession session = new ClientSession(
            clientSocket, 
            serverAddress, 
            serverPort, 
            resourceName, 
            maxSegmentSize
        );

        System.out.println("Created a session for " + resourceName);
        session.run();
        System.out.println("Successfully received " + resourceName);
        
        clientSocket.close();
    }

}