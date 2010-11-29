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
        System.out.println("QUERY");
        InetAddress inet = packet.getAddress();
        int port = packet.getPort();
        String searchQuery = new String(packet.getData()).split(Packet.CRLF)[1].toLowerCase();

        ArrayList<DirectoryListEntry> results = new ArrayList<DirectoryListEntry>();
        for(DirectoryListEntry entry : directory)
        {
            if(entry.getFile().toLowerCase().contains(searchQuery))
            {
                results.add(entry);
            }
        }

        String[] header = new String[results.size()];
        for(int i = 0; i < header.length; ++i)
            header[i] = results.get(i).convertToPacketData();
        
        DatagramPacket response = Packet.buildServerPacket(Packet.PacketType.QUERY_FOR_CONTENT,
                STATUS_OK, header, " ", inet, port);

        sendPacket(response);
    }

    public void rateContent(DatagramPacket packet)
    {
        String data = new String(packet.getData());
        String header = data.split(Packet.CRLF)[1];
        String filename = header.split(";")[0];
        String rate = header.split(";")[1];

        DirectoryListEntry target = null;
        for(DirectoryListEntry entry : directory)
            if(entry.getFile().toLowerCase().equals(filename.toLowerCase()))
            {
                target = entry;
                break;
            }

        target.rate(Integer.parseInt(rate));
        buildListTextArea();
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
        DatagramSocket ds = null;
        try
        {
            ds = new DatagramSocket(40110);
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
            if(ds != null)
                ds.close();
            System.err.println(ex);
        }
    }
    
    public void sendAck(DatagramPacket rcvPacket) throws UnknownHostException, SocketException, IOException
    {
        int port = rcvPacket.getPort();
        InetAddress inet = rcvPacket.getAddress();
        DatagramPacket ack = Packet.buildServerPacket(Packet.PacketType.ACK, STATUS_OK, null, "", inet, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(ack);
        socket.close();
        System.out.println("SERVER: ACK sent to " + inet.getHostAddress() + ":" + port);
    }

    public void sendPacket(DatagramPacket packet)
    {
        DatagramSocket ds = null;
        try
        {
            ds = new DatagramSocket();
            ds.send(packet);
            ds.close();
        }
        catch(Exception ex)
        {
            if(ds != null)
                ds.close();

            System.out.println("DIE");
        }
    }

    public static void main(String ar[])
    {
        System.out.println("Server initialized.");
        DirectoryServer server = new DirectoryServer();
        server.packetWaitLoop();
    }
}
