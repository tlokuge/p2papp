package datacomm;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.text.Segment;

public class Packet
{
    public enum PacketType
    {
        WELCOME,
        INFORM_AND_UPDATE,
	QUERY_FOR_CONTENT,
	RATE_CONTENT,
	ACK,
        NACK,
        FIN,
	EXIT,
    }
    public static final String CRLF = System.getProperty("line.separator") + " ";

    private static final int DEFAULT_PORT = 40110;

    public ArrayList<DatagramPacket> packets;

    public Packet()
    {
        packets = new ArrayList<DatagramPacket>();
    }

    public ArrayList<DatagramPacket> getPackets()
    {
        return packets;
    }

    public void buildPacket(PacketType type, String[] header, String entity, int port)
    {
        System.out.println("Inside BuildPacket");
        try
        {
            System.out.println("In try...");
            InetAddress inet = InetAddress.getLocalHost();
            String requestLine = type.toString() + " " + inet.getHostAddress() + " " + CRLF;
            String headerLines = "";
            if(header != null)
            {
                headerLines = header[0] + CRLF;
                for(int i = 1; i < header.length; ++i)
                    headerLines += "#%" + header[i] + CRLF;
            }

            headerLines += CRLF;
            String message = headerLines + entity;
            System.out.println("Message Built...");
            byte buffer[] = message.getBytes();
            System.out.println("LENGTH OF BUFFER " + buffer.length);

            if(buffer.length + requestLine.getBytes().length < 128)
            {
                String m = "0 " + requestLine + message;
                buffer = m.getBytes();
                packets.add(new DatagramPacket(buffer, buffer.length, inet, port));
            }
            else
            {
                System.out.println("Inside segmentor...");
                int m = 0;
                int n = 0;
                int counter = 0;
                byte[] seqlength;
                Segment segment;
                //  String segment;
                while(!(n >= message.toCharArray().length))
                {
                   String seqNum = 128*counter + " " + requestLine;
                   seqlength = seqNum.getBytes();

                   m = 128 - seqlength.length;
                   
                   if((n + m) < message.length() )
                   {
                        m = 128 - seqlength.length;
                        segment = new Segment(message.toCharArray(), n, m);
                        n += m;
                   }
                   else
                   {
                        m = message.toCharArray().length - n;
                        segment = new Segment(message.toCharArray(), n, m);
                        n += m;
                   }
                   String toAdd = seqNum + segment.toString();
                   buffer = toAdd.getBytes();

                   System.out.println("Packet: " + toAdd);
                   packets.add(new DatagramPacket(buffer, buffer.length, inet, port));

                 counter++;

                }
            }
        }
        catch(Exception ex)
        {
            System.err.println("buildPacket: " + ex);
        }
    }

    public static DatagramPacket buildEmptyClientPacket(PacketType type, int port)
    {
        try
        {
            InetAddress inet = InetAddress.getLocalHost();
            String requestLine = "0 " + type.toString() + " " + inet.getHostName() + " " + inet.getHostAddress() + CRLF;
            byte buffer[] = requestLine.getBytes();

            return new DatagramPacket(buffer, buffer.length, inet, port);
        }
        catch(Exception ex)
        {
            System.out.println("buildEmptyClientPacket(): " + ex);
        }
        return null;
    }

    public static DatagramPacket buildClientWelcomePacket()
    {
        return buildEmptyClientPacket(PacketType.WELCOME, DEFAULT_PORT);
    }

    public static DatagramPacket buildClientFinPacket(int port)
    {
        return buildEmptyClientPacket(PacketType.FIN, port);
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
