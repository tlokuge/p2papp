package datacomm;

/**
 *
 * @author l3whalen
 */
import java.net.*;
import java.util.*;
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

    private class ServerThread implements Runnable
    {
        private Thread thread;
        private DatagramPacket packet;
        private int listen_port;

        private ArrayList<DatagramPacket> packets;

        private InetAddress client_inet;
        private int client_port;

        public ServerThread(DatagramPacket packet)
        {
            this.packet = packet;

            thread = new Thread(this);
            thread.start();

            listen_port = -1;

            packets = new ArrayList<DatagramPacket>();

            client_inet = null;
            client_port = -1;
        }

        public void run()
        {
            handlePacket();
        }

        public void handlePacket()
        {
            while(true)
            {
                if(listen_port > 0)
                {
                    DatagramSocket socket = null;
                    try
                    {
                        byte buffer[] = new byte[128];
                        socket = new DatagramSocket(listen_port);
                        packet = new DatagramPacket(buffer, buffer.length);
                        System.out.println("ServerThread: Waiting for packet");
                        socket.receive(packet);
                        socket.close();
                    }
                    catch(Exception ex)
                    {
                        if(socket != null)
                            socket.close();
                        
                        System.out.println("HandlePacket() loop: " + ex);
                    }
                }
                
                String str = new String(packet.getData());
                System.out.println("SERVER - RECEIVED PACKET: " + str);

                String type = str.split(" ")[1];
                if(type.equalsIgnoreCase(Packet.PacketType.WELCOME.toString()))
                    sendWelcomeReply();
                else if(type.equalsIgnoreCase(Packet.PacketType.FIN.toString()))
                {
                    assemblePackets();
                    parsePacket();
                }
                else
                {
                    packets.add(packet);
                    sendAck(packet);
                }
            }
        }

        public void sendWelcomeReply()
        {
            System.out.println("sendWelcomeReply()");
            listen_port = 16000 + new Random().nextInt(1000);
            System.out.println("listen_port = " + listen_port);
            DatagramSocket socket = null;
            try
            {
                socket = new DatagramSocket(listen_port);
                DatagramPacket p = Packet.buildServerPacket(Packet.PacketType.WELCOME, STATUS_OK, null, "", packet.getAddress(), packet.getPort());
                socket.send(p);
                socket.close();

                client_port = packet.getPort();
                client_inet = packet.getAddress();
            }
            catch(Exception ex)
            {
                if(socket != null)
                    socket.close();

                System.out.println("sendWelcomeReply(): " + ex);
            }
        }

        class PacketSorter implements Comparator
        {
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
        
        public boolean isLarger(DatagramPacket one, DatagramPacket two)
        {
            int seq1 = Integer.parseInt(new String(one.getData()).split(" ")[0]);
            int seq2 = Integer.parseInt(new String(two.getData()).split(" ")[0]);

            if(seq1 > seq2)
                return true;

            return false;
        }

        public void assemblePackets()
        {
            String message = "";

            System.out.println("Before sort:");
            for(DatagramPacket p : packets)
                System.out.println(Integer.parseInt(new String(p.getData()).split(" ")[0]));

            Collections.sort(packets, new PacketSorter());

            System.out.println("After sort:");
            for(DatagramPacket p : packets)
                System.out.println(Integer.parseInt(new String(p.getData()).split(" ")[0]));

            for(DatagramPacket p : packets)
            {
                String data = new String(p.getData());
                String splat[] = data.split(Packet.CRLF);
                for(int i = 1; i < splat.length; ++i)
                    message += splat[i];
            }

            message = message.replaceAll("#%", Packet.CRLF);

            String requestLine = new String(packets.get(0).getData()).split(Packet.CRLF)[0] + Packet.CRLF;
            message = requestLine + message;
            byte buffer[] = message.getBytes();

            packet = new DatagramPacket(buffer, buffer.length);            
        }

        public void updateFileListing(DatagramPacket packet)
        {
            String splat[] = new String(packet.getData()).split(Packet.CRLF);
            String[] files = new String[splat.length-3];
            for(int i = 0; i < files.length; ++i)
                files[i] = splat[i+1];

            String address = client_inet.getHostAddress();

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
                DirectoryListEntry entry = new DirectoryListEntry(file_split[0], address, file_split[1], 1, client_port);
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

        public void parsePacket()
        {
            String data = new String(packet.getData());
            System.out.println("data: " + data);
            String packetType = data.split(" ")[1];
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

        public void sendAck(DatagramPacket rcvPacket)
        {
            DatagramSocket socket = null;
            try
            {
                String header[] = {new String(rcvPacket.getData()).split(" ")[0]};
                DatagramPacket ack = Packet.buildServerPacket(Packet.PacketType.ACK, STATUS_OK, header, "", client_inet, client_port);
                socket = new DatagramSocket();
                socket.send(ack);
                socket.close();
                System.out.println("SERVER: ACK sent to " + client_inet.getHostAddress() + ":" + client_port);
            }
            catch(Exception ex)
            {
                if(socket != null)
                    socket.close();
                
                System.out.println("S: SendAck(): " + ex);
            }
        }

        public void sendPacket(DatagramPacket packet)
        {
            DatagramSocket ds = null;
            try
            {
                if(listen_port > 0)
                    ds = new DatagramSocket(listen_port);
                else
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
    }
    
    private synchronized void buildListTextArea()
    {
        String files = "";
        for(DirectoryListEntry entry : directory)
            files += entry + "\n";
        list_area.setText(files);
    }

    public synchronized void printDirectory()
    {
        for(DirectoryListEntry entry : directory)
            System.out.println(entry);
        System.out.println("Number of entries:" + directory.size());
        System.out.println("-----------------------------------");
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
                new ServerThread(packet);
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
    
    public static void main(String ar[])
    {
        System.out.println("Server initialized.");
        DirectoryServer server = new DirectoryServer();
        server.packetWaitLoop();
    }
}
