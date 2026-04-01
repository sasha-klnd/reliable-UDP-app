package udpdemo;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;

public class UDPClient {

    public static void main(String[] args) throws Exception {
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
    }

}