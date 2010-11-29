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

    public static DatagramPacket buildClientPacket(PacketType type, String[] header, String entity)
    {
        try
        {
            InetAddress inet = InetAddress.getLocalHost();
            String requestLine = type.toString() + " " + inet.getHostName() + " " + inet.getHostAddress() + CRLF;
            String headerLines = "";
            if(header != null)
                for(int i = 0; i < header.length; ++i)
                    headerLines += header[i] + CRLF;

            headerLines += CRLF;
            String message = requestLine + headerLines + entity;
            byte buffer[] = message.getBytes();

            System.out.println("BUILDING CLIENT PACKET");
            System.out.println("REQUESTLINE: " + requestLine);
            System.out.println("HEADERLINES: " + headerLines);
            System.out.println("ENTITY: " + entity);

            return new DatagramPacket(buffer, buffer.length, inet, DEFAULT_PORT);
        }
        catch(Exception ex)
        {
            System.err.println(ex);
        }
        
        return null;
    }

    public static DatagramPacket buildServerPacket(PacketType type, String status, String[] header, String entity, InetAddress inet, int port)
    {
        try
        {
            String requestLine = status + " " + type.toString() + CRLF;
            String headerLines = "";
            if(header != null)
                for(int i = 0; i < header.length; ++i)
                    headerLines += header[i] + CRLF;
            
            headerLines += CRLF;

            String message = requestLine + headerLines + entity;

            System.out.println("BUILDING SERVER PACKET");
            System.out.println("REQUESTLINE: " + requestLine);
            System.out.println("HEADERLINES: " + headerLines);
            System.out.println("ENTITY: " + entity);
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
