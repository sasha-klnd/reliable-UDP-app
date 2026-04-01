package udpdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

    public static void main(String[] args) throws Exception {

        final InetAddress IP_ADDR = InetAddress.getLocalHost();
        final int SRVR_PORT_NUM = 9999;
        
        // Create a socket for this server with SRVR_PORT_NUM
        DatagramSocket ds = new DatagramSocket(SRVR_PORT_NUM);
        byte[] receiveBuf = new byte[1024];

        System.out.println("[SERVER] Awaiting packets...");

        // Receive any packet addressed to this port
        DatagramPacket receivePacket = new DatagramPacket(
            receiveBuf, 
            receiveBuf.length
        );
        ds.receive(receivePacket);

        System.out.println("[SERVER] Received packet.");
        
        // Parse packet data and compute result
        String receiveData = new String(receivePacket.getData());
        int num = Integer.parseInt(receiveData.trim());
        int result = num*num;

        System.out.println("[SERVER] Received data: " + num);
        System.out.println("[SERVER] Result: " + result);

        byte[] sendBuf = String.valueOf(result).getBytes();

        // Build packet with IP_ADDR and port number of client, and send
        DatagramPacket sendPacket = new DatagramPacket(
            sendBuf, 
            sendBuf.length, 
            IP_ADDR, 
            receivePacket.getPort()
        );
        ds.send(sendPacket);

        System.out.println("[SERVER] Sending result to client.");
    }
    
}
