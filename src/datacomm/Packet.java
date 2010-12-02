package datacomm;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        Globals.debug("Inside BuildPacket");
        try
        {
            Globals.debug("In try...");
            InetAddress inet = InetAddress.getLocalHost();
            String requestLine = type.toString() + " " + inet.getHostAddress() + " " + CRLF;
            String headerLines = "";
            if(header != null)
            {
                headerLines = header[0] + CRLF;
                for(int i = 1; i < header.length; ++i)
                    headerLines += Globals.HEADER_APPEND + header[i] + CRLF;
            }

            if(entity == null)
                entity = "";

            headerLines += CRLF;
            String message = headerLines + entity;
            Globals.debug("Message Built...");
            byte buffer[] = message.getBytes();
            Globals.debug("LENGTH OF BUFFER " + buffer.length);

            if(buffer.length + requestLine.getBytes().length < Globals.BUF_SIZE)
            {
                String m = "0 " + requestLine + message;
                buffer = m.getBytes();
                packets.add(new DatagramPacket(buffer, buffer.length, inet, port));
            }
            else
            {
                Globals.debug("Inside segmentor...");
                int m = 0;
                int n = 0;
                int counter = 0;
                byte[] seqlength;
                Segment segment;
                //  String segment;
                while(!(n >= message.toCharArray().length))
                {
                   String seqNum = Globals.BUF_SIZE * counter + " " + requestLine;
                   seqlength = seqNum.getBytes();

                   m = Globals.BUF_SIZE - seqlength.length;
                   
                   if((n + m) < message.length() )
                   {
                        m = Globals.BUF_SIZE - seqlength.length;
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

                   Globals.debug("Packet: " + toAdd);
                   packets.add(new DatagramPacket(buffer, buffer.length, inet, port));

                 counter++;

                }
            }
        }
        catch(Exception ex)
        {
            Globals.debug("buildPacket: " + ex);
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
            Globals.debug("buildEmptyClientPacket(): " + ex);
        }
        return null;
    }

    public static DatagramPacket buildClientWelcomePacket()
    {
        return buildEmptyClientPacket(PacketType.WELCOME, Globals.SERVER_WELCOME_PORT);
    }

    public static DatagramPacket buildClientFinPacket(int port)
    {
        return buildEmptyClientPacket(PacketType.FIN, port);
    }
    
    public static DatagramPacket buildEmptyServerPacket(PacketType type, String status, InetAddress inet, int port)
    {
        try
        {
            String requestLine = "0 " + status + " " + type.toString() + CRLF;
            byte buffer[] = requestLine.getBytes();

            return new DatagramPacket(buffer, buffer.length, inet, port);
        }
        catch(Exception ex)
        {
            Globals.debug("buildEmptyPacket: " + ex);
        }

        return null;
    }

    public void buildServerPacket(PacketType type, String status, String[] header, String entity, InetAddress inet, int port)
    {
        try
        {
            packets.clear();
            String requestLine = status + " " + type.toString() + CRLF;
            String headerLines = "";
            if(header != null)
            {
                headerLines = header[0] + CRLF;
                for(int i = 1; i < header.length; ++i)
                    headerLines += Globals.HEADER_APPEND + header[i] + CRLF;
            }
            
            headerLines += CRLF;

            String message =  headerLines + entity;

            byte buffer[] = message.getBytes();

            if(buffer.length + requestLine.getBytes().length < Globals.BUF_SIZE)
            {
                String m = "0 " + requestLine + message;
                buffer = m.getBytes();
                packets.add(new DatagramPacket(buffer, buffer.length, inet, port));
            }
            else
            {
                Globals.debug("Inside segmentor...");
                int m = 0;
                int n = 0;
                int counter = 0;
                byte[] seqlength;
                Segment segment;
                //  String segment;
                while(!(n >= message.toCharArray().length))
                {
                   String seqNum = Globals.BUF_SIZE*counter + " " + requestLine;
                   seqlength = seqNum.getBytes();

                   m = Globals.BUF_SIZE - seqlength.length;

                   if((n + m) < message.length() )
                   {
                        m = Globals.BUF_SIZE - seqlength.length;
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

                   Globals.debug("Packet: " + toAdd);
                   packets.add(new DatagramPacket(buffer, buffer.length, inet, port));

                 counter++;

                }
            }

           // return new DatagramPacket(buffer, buffer.length, inet, port);
        }
        catch(Exception ex)
        {
            Globals.debug("buildServerPacket(): " + ex);
        }
        //return null;
    }

    static class PacketSorter implements Comparator
    {
        public PacketSorter() {}
        // a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
        public int compare(Object o1, Object o2)
        {
            if(!(o1 instanceof DatagramPacket) || !(o2 instanceof DatagramPacket))
                return -1;

            int seq1 = Integer.parseInt(new String(((DatagramPacket)o1).getData()).split(" ")[0]);
            int seq2 = Integer.parseInt(new String(((DatagramPacket)o2).getData()).split(" ")[0]);

            if(seq1 > seq2)
                return 1;
            else if(seq1 == seq2)
                return 0;

            return -1;
        }
    }

    public static DatagramPacket assemblePackets(ArrayList<DatagramPacket> packets)
    {
        if(packets.isEmpty())
            return null;

        if(packets.size() == 1)
            return packets.get(0);
        
        String message = "";

        Globals.debug("Before sort:");
        for(DatagramPacket p : packets)
            Globals.debug(Integer.parseInt(new String(p.getData()).split(" ")[0]));

        Collections.sort(packets, new PacketSorter());

        Globals.debug("After sort:");
        for(DatagramPacket p : packets)
            Globals.debug(Integer.parseInt(new String(p.getData()).split(" ")[0]));

        for(DatagramPacket p : packets)
        {
            String data = new String(p.getData());
            String splat[] = data.split(Packet.CRLF);
            for(int i = 1; i < splat.length; ++i)
                message += splat[i];
        }

        message = message.replaceAll(Globals.HEADER_APPEND, Packet.CRLF);

        String requestLine = new String(packets.get(0).getData()).split(Packet.CRLF)[0] + Packet.CRLF;
        message = requestLine + message;
        byte buffer[] = message.getBytes();

        packets.clear();
        
        return new DatagramPacket(buffer, buffer.length);
    }
}
