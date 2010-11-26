package datacomm;

/**
 *
 * @author l3whalen
 */
import java.net.*;
import java.io.*;
import java.util.ArrayList;

class DirectoryListEntry
{
    private String file;
    private String address;
    private int rating;
    private int port;

    public DirectoryListEntry(String file, String address, int rating, int port)
    {
        this.file = file;
        this.address = address;
        this.rating = rating;
        this.port = port;
    }

    public String getFileName()
    {
        String split[] = file.split("\\");
        return split[split.length-1];
    }

    public String getFile()    { return file; }
    public String getAddress() { return address; }
    public int getRating()     { return rating; }
    public int getPort()       { return port; }

    public String toString()
    {
        return "File Listing[" + getFile() + " - " + getAddress() + ":" + getPort() + " - " + getRating() + "]\n";
    }
}

class DirectoryServer
{
    public final static int BUFSIZE = 128;
    public final static String STATUS_OK  = "200(OK)";
    public final static String STATUS_ERR = "400(ERROR)";

    public ArrayList<DirectoryListEntry> directory;

    public DirectoryServer()
    {
        directory = new ArrayList<DirectoryListEntry>();
    }

    public void updateFileListing(DatagramPacket packet)
    {
        String splat[] = new String(packet.getData()).split(" ");
        String file = splat[3];
        String address = packet.getAddress().getHostAddress();
        int port = packet.getPort();

        DirectoryListEntry entry = new DirectoryListEntry(file, address, 1, port);
        directory.add(entry);
        System.out.println("Number of entries:" + directory.size());
        System.out.println(directory);
        System.out.println("-----------------------------------");
    }

    public void queryListing(DatagramPacket packet)
    {
    }

    public void rateContent(DatagramPacket packet)
    {
    }

    public void registerClientExit(DatagramPacket packet)
    {
    }

    public void parsePacket(DatagramPacket packet)
    {
        String data = new String(packet.getData());
        String packetType = data.split(" ")[0];
        Packet.PacketType type = Packet.PacketType.valueOf(packetType);
        switch(type)
        {
            case INFORM_AND_UPDATE: updateFileListing(packet);   break;
            case QUERY_FOR_CONTENT: queryListing(packet);        break;
            case RATE_CONTENT:      rateContent(packet);         break;
            case EXIT:              registerClientExit(packet);  break;

            case ACK:
                break;

            case NACK:
                break;

        }
    }

    public void packetWaitLoop()
    {
        try
        {
            DatagramSocket ds = new DatagramSocket(40110);
            byte buffer[] = new byte[BUFSIZE];

            while(true)
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ds.receive(packet);
                sendAck(packet);
                String str = new String(packet.getData());
                System.out.println("SERVER - RECEIVED PACKET: " + str);

                parsePacket(packet);
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex);
        }
    }
    
    public void sendAck(DatagramPacket rcvPacket) throws UnknownHostException, SocketException, IOException
    {
        int port = rcvPacket.getPort();
        System.err.println("PORT IS " + port);
        InetAddress inet = rcvPacket.getAddress();
        System.out.println("Inet Host Addy: " + inet.getHostAddress());
        DatagramPacket ack = Packet.buildServerPacket(Packet.PacketType.ACK, STATUS_OK, "", "", inet, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(ack);
        socket.close();
    }

    public static void main(String ar[])
    {
        System.out.println("Initializing server...");
        DirectoryServer server = new DirectoryServer();
        server.packetWaitLoop();
    }
}
