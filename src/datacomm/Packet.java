package datacomm;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet
{
    public enum PacketType
    {
        INFORM_AND_UPDATE,
	QUERY_FOR_CONTENT,
	RATE_CONTENT,
	ACK,
        NACK,
	EXIT,
    }
    public static final String CRLF = System.getProperty("line.separator") + " ";

    private static final int DEFAULT_PORT = 40110;

    public static DatagramPacket buildClientPacket(PacketType type, String header, String entity)
    {
        try
        {
            InetAddress inet = InetAddress.getLocalHost();
            String message =
                    type.toString() + " " + inet.getHostName()+ " " +
                    inet.getHostAddress() + CRLF
                    + header + CRLF + entity;
            byte buffer[] = message.getBytes();
            return new DatagramPacket(buffer, buffer.length, inet, DEFAULT_PORT);
        }
        catch(Exception ex)
        {
            System.err.println(ex);
        }
        
        return null;
    }

    public static DatagramPacket buildServerPacket(PacketType type, String status, String header, String entity, InetAddress inet, int port)
    {
        try
        {
            String message = status + " " + type.toString() + CRLF + header + CRLF + CRLF + entity;
            byte buffer[] = message.getBytes();
            return new DatagramPacket(buffer, buffer.length, inet, port);
        }
        catch(Exception ex)
        {
            System.err.println(ex);
        }
        return null;
    }

}
