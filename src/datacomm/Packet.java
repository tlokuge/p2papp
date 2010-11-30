package datacomm;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Packet
{
    public enum PacketType
    {
        WELCOME,
        INFORM_AND_UPDATE,
	QUERY_FOR_CONTENT,
	RATE_CONTENT,
        FIN,
	ACK,
        NACK,
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
        try
        {
            InetAddress inet = InetAddress.getLocalHost();
            String requestLine = type.toString() + " " + inet.getHostAddress() + " " + CRLF;
            String headerLines = "";
            if(header != null)
                for(int i = 0; i < header.length; ++i)
                    headerLines += header[i] + CRLF;

            headerLines += CRLF;
            String message = requestLine + headerLines + entity;
            byte buffer[] = message.getBytes();
            if(buffer.length < 128)
                packets.add(new DatagramPacket(buffer, buffer.length, inet, port));
            else
                for(int i = 0; i < buffer.length; i += 127)
                    packets.add(new DatagramPacket(buffer, 128, inet, DEFAULT_PORT));

            for(int i = 0; i < packets.size(); ++i)
            {
                String data = new String(packets.get(i).getData());
                String splat[] = data.split(Packet.CRLF);
                String nRequestLine = data.split(Packet.CRLF)[0];
                nRequestLine = i*128 + " " + nRequestLine;
                splat[0] = nRequestLine;
                String m = "";
                for(int j = 0; j < splat.length; ++j)
                    m += splat[j] + CRLF;

                System.out.println(m);
                byte buf[] = m.getBytes();
                System.out.println("Buffer: " + buf.length);
                System.out.println("i = " + i + " packets size: " + packets.size());
                packets.set(i, new DatagramPacket(buf, buf.length, inet, DEFAULT_PORT));
            }
        }
        catch(Exception ex)
        {
            System.err.println("buildPacket: " + ex);
        }
    }

    private static DatagramPacket buildEmptyClientPacket(PacketType type)
    {
        try
        {
            InetAddress inet = InetAddress.getLocalHost();

            String request = type.toString() + " " + inet.getHostName() + " " + inet.getHostAddress() + CRLF;
            String header = "" + CRLF;

            String message = request + header;
            byte buffer[] = message.getBytes();
            return new DatagramPacket(buffer, buffer.length, inet, DEFAULT_PORT);
        }
        catch(Exception ex)
        {
            System.err.println("buildEmptyClientPacket: " + ex);
            return null;
        }
    }

    public static DatagramPacket buildClientWelcomePacket()
    {
        return buildEmptyClientPacket(PacketType.WELCOME);
    }

    public static DatagramPacket buildClientFinPacket()
    {
        return buildEmptyClientPacket(PacketType.FIN);
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
            System.err.println("buildServerPacket: " + ex);
        }
        return null;
    }

}
