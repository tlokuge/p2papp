package datacomm;

/**
 *
 * @author l3whalen
 */
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

class DirectoryListEntry
{
    private String file;
    private String address;
    private long size;
    private int rating;
    private int port;

    public DirectoryListEntry(String file, String address, long size, int rating, int port)
    {
        this.file = file;
        this.address = address;
        this.size = size;
        this.rating = rating;
        this.port = port;
    }

    public String getFile()    { return file; }
    public String getAddress() { return address; }
    public long getSize()      { return size; }
    public int getRating()     { return rating; }
    public int getPort()       { return port; }

    public String toString()
    {
        return "File Listing[" + file + "(" + size + ") - " + address + ":" + port + " - " + rating + "]";
    }
}

class DirectoryServer extends JFrame
{
    public final static int BUFSIZE = 128;
    public final static String STATUS_OK  = "200(OK)";
    public final static String STATUS_ERR = "400(ERROR)";

    public ArrayList<DirectoryListEntry> directory;

    private JTextArea list_area;

    public DirectoryServer()
    {
        super();
        directory = new ArrayList<DirectoryListEntry>();

        initComponents();
    }

    private void initComponents()
    {
        list_area = new JTextArea(100, 200);
        list_area.setEditable(false);

        add(new JScrollPane(list_area));
        setSize(800, 300);
        setTitle("Directory Server");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void buildListTextArea()
    {
        System.err.println("Building list text area");
        String files = "";
        for(DirectoryListEntry entry : directory)
            files += entry + "\n";
        list_area.setText(files);
    }

    public void printDirectory()
    {
        for(DirectoryListEntry entry : directory)
            System.out.println(entry);
        System.out.println("Number of entries:" + directory.size());
        System.out.println("-----------------------------------");
    }
    
    public void updateFileListing(DatagramPacket packet)
    {
        System.err.println("Updating directory listing");
        String splat[] = new String(packet.getData()).split(Packet.CRLF);
        String[] files = new String[splat.length-3];
        for(int i = 0; i < files.length; ++i)
            files[i] = splat[i+1];

        String address = packet.getAddress().getHostAddress();
        int port = packet.getPort();

        for(int i = 0; i < files.length; ++i)
        {
            String[] file_split = files[i].replaceAll("&%", " ").split(";"); // Convert all our &% back to spaces
            System.out.println(file_split[0] + " - " + file_split[1]);
            boolean skip = false;
            for(DirectoryListEntry entry : directory)
                if(entry.getFile().equals(file_split[0])) // Make sure file with same name does not exist on server
                    skip = true;

            if(skip)
                continue;
            DirectoryListEntry entry = new DirectoryListEntry(file_split[0], address, Long.parseLong(file_split[1]), 1, port);
            directory.add(entry);
        }
        buildListTextArea();
        
        printDirectory();
    }

    public void queryListing(DatagramPacket packet)
    {
    }

    public void rateContent(DatagramPacket packet)
    {
    }

    public void registerClientExit(DatagramPacket packet)
    {
        InetAddress inet = packet.getAddress();
        int port = packet.getPort();

        for(Iterator<DirectoryListEntry> itr = directory.iterator(); itr.hasNext(); )
        {
            DirectoryListEntry entry = itr.next();
            if(entry.getAddress().equals(inet.getHostAddress()) && entry.getPort() == port)
                itr.remove();
        }

        buildListTextArea();
        printDirectory();
    }

    public void parsePacket(DatagramPacket packet)
    {
        String data = new String(packet.getData());
        String packetType = data.split(" ")[0];
        Packet.PacketType type = Packet.PacketType.valueOf(packetType);

        System.out.println("SERVER: Parsing " + type + " PACKET");
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
                
                String str = new String(packet.getData());
                System.out.println("SERVER - RECEIVED PACKET: " + str);

                sendAck(packet);

                parsePacket(packet);
                buffer = new byte[BUFSIZE];
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
        InetAddress inet = rcvPacket.getAddress();
        DatagramPacket ack = Packet.buildServerPacket(Packet.PacketType.ACK, STATUS_OK, "", "", inet, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(ack);
        socket.close();
        System.out.println("SERVER: ACK sent to " + inet.getHostAddress() + ":" + port);
    }

    public static void main(String ar[])
    {
        System.out.println("Server initialized.");
        DirectoryServer server = new DirectoryServer();
        server.packetWaitLoop();
    }
}
