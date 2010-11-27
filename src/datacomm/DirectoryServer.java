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
    private int rating;
    private int port;

    public DirectoryListEntry(String file, String address, int rating, int port)
    {
        this.file = file;
        this.address = address;
        this.rating = rating;
        this.port = port;
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
            files += entry.getFile() + "\n";
        list_area.setText(files);
    }

    public void printDirectory()
    {
        System.out.println(directory);
        System.out.println("Number of entries:" + directory.size());
        System.out.println("-----------------------------------");
    }
    
    public void updateFileListing(DatagramPacket packet)
    {
        System.err.println("Updating directory listing");
        String splat[] = new String(packet.getData()).split(" ");
        String file = splat[3];
        String address = packet.getAddress().getHostAddress();
        int port = packet.getPort();

        for(DirectoryListEntry entry : directory)
            if(entry.getFile().equals(file)) // Make sure file with same name does not exist on server
                return;

        DirectoryListEntry entry = new DirectoryListEntry(file, address, 1, port);
        directory.add(entry);

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
        System.out.println("SERVER: Received EXIT packet");
        InetAddress inet = packet.getAddress();
        int port = packet.getPort();

        for(Iterator<DirectoryListEntry> itr = directory.iterator(); itr.hasNext(); )
        {
            DirectoryListEntry entry = itr.next();
            if(entry.getAddress().equals(inet.getHostAddress()) && entry.getPort() == port)
            {
                System.out.println("Removing directory entry: " + entry);
                itr.remove();
            }
        }

        buildListTextArea();
        printDirectory();

        System.err.println("Registered Client exit");
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
                
                String str = new String(packet.getData());
                System.out.println("SERVER - RECEIVED PACKET: " + str);

                sendAck(packet);

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