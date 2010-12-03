package datacomm;

/**
 *
 * @author l3whalen
 */
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

class DirectoryServer extends JFrame
{
    public final static String STATUS_OK  = "200(OK)";
    public final static String STATUS_ERR = "400(ERROR)";

    public ArrayList<DirectoryListEntry> directory;
    private JTextArea list_area;
    private JTextArea console;

    public DirectoryServer()
    {
        super();
        directory = new ArrayList<DirectoryListEntry>();

        initComponents();

        debug("Server Initialized");
        packetWaitLoop();
    }

    private void initComponents()
    {
        list_area = new JTextArea();
        list_area.setEditable(false);

        console = new JTextArea();
        console.setEditable(false);

        setLayout(new GridLayout(2, 1));
        add(new JScrollPane(list_area));
        add(new JScrollPane(console));
        setSize(800, 400);
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
        public ArrayList<Integer> received_segNums;

        private InetAddress client_inet;
        private int client_port;

        public ServerThread(DatagramPacket packet)
        {
            this.packet = packet;

            thread = new Thread(this);
            thread.start();

            listen_port = -1;

            packets = new ArrayList<DatagramPacket>();
            received_segNums = new ArrayList<Integer>();

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
                        if( socket != null)
                                socket.close();
                        byte buffer[] = new byte[Globals.BUF_SIZE];
                        socket = new DatagramSocket(listen_port);
                        debug(listen_port);
                        packet = new DatagramPacket(buffer, buffer.length);
                        debug("ServerThread: Waiting for packet");
                        socket.receive(packet);
                        socket.close();
                    }
                    catch(Exception ex)
                    {
                        if(socket != null)
                            socket.close();
                        
                        Globals.exception(ex, "S:HandlePacket() loop");
                        ex.printStackTrace();
                    }
                }
                
                String str = new String(packet.getData());
                debug("S: Packet Received:\n---\n" + str + "\n---");

                String splat[] = str.split(" ");
                String type = splat[1];
                String segNum = splat[0];
                if(type.equalsIgnoreCase(Packet.PacketType.WELCOME.toString()))
                    sendWelcomeReply();
                else if(type.equalsIgnoreCase(Packet.PacketType.FIN.toString()))
                {
                    packet = Packet.assemblePackets(packets);
                    parsePacket();
                    packets.clear();
                    received_segNums.clear();
                }
                else
                {
                    if(!received_segNums.contains(Integer.parseInt(segNum)))
                        packets.add(packet);
                    else
                        debug("S: Received duplicate packet: " + segNum);

                    sendAck(packet);
                }
            }
        }

        public void sendWelcomeReply()
        {
            listen_port = 16000 + new Random().nextInt(1000);
            DatagramSocket socket = null;
            try
            {
                socket = new DatagramSocket(listen_port);
                socket.send(Packet.buildEmptyServerPacket(Packet.PacketType.WELCOME, STATUS_OK, packet.getAddress(), packet.getPort()));
                socket.close();

                client_port = packet.getPort();
                client_inet = packet.getAddress();
            }
            catch(Exception ex)
            {
                if(socket != null)
                    socket.close();

                Globals.exception(ex, "S: sendWelcomeReply()");
            }
        }

        public void updateFileListing(DatagramPacket packet)
        {
            if(packet == null)
                Globals.error("updateFileListing(): Received NULL pointer. Aborting!", true);
            
            String splat[] = new String(packet.getData()).split(Globals.CRLF);
            String[] files = new String[splat.length];
            for(int i = 0; i < files.length; ++i)
                files[i] = splat[i];

            String address = client_inet.getHostAddress();

            for(int i = 1; i < files.length; ++i)
            {
                String[] file_split = files[i].replaceAll("&%", " ").split(";"); // Convert all our &% back to spaces
                if(file_split.length < 2)
                {
                    Globals.error("S: Attempt to add invalid files to server directory. Aborted.");
                    continue;
                }
                debug(file_split[0] + " - " + file_split[1]);
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
            String searchQuery = new String(packet.getData()).split(Globals.CRLF)[1].toLowerCase();

            String query = "";
            for(int i = 0; i < searchQuery.length(); ++i)
                if(!Character.isWhitespace(searchQuery.charAt(i)))
                    query += searchQuery.charAt(i);
            
            ArrayList<DirectoryListEntry> results = new ArrayList<DirectoryListEntry>();
            for(DirectoryListEntry entry : directory)
            {
                if(entry.getFile().toLowerCase().contains(query))
                    results.add(entry);
            }

            String[] header = new String[results.size()];
            for(int i = 0; i < header.length; ++i)
                header[i] = results.get(i).convertToPacketData();

            Packet response = new Packet();
            response.buildServerPacket(Packet.PacketType.QUERY_FOR_CONTENT, STATUS_OK, header, "", client_inet, client_port);
            sendPacket(response);
        }

        public void rateContent(DatagramPacket packet)
        {
            String data = new String(packet.getData());
            String header = data.split(Globals.CRLF)[1];
            String filename = header.split(";")[0];
            String rate = header.split(";")[1];

            DirectoryListEntry target = null;
            for(DirectoryListEntry entry : directory)
                if(entry.getFile().toLowerCase().equals(filename.toLowerCase()))
                {
                    target = entry;
                    break;
                }

            String r = "";
            for(int i = 0; i < rate.length(); ++i)
                if(Character.isDigit(rate.charAt(i)))
                    r += rate.charAt(i);
            target.rate(Integer.parseInt(r));
            buildListTextArea();
        }

        public void registerClientExit(DatagramPacket packet)
        {
            for(Iterator<DirectoryListEntry> itr = directory.iterator(); itr.hasNext(); )
            {
                DirectoryListEntry entry = itr.next();
                if(entry.getAddress().equals(client_inet.getHostAddress()) && entry.getPort() == client_port)
                    itr.remove();
            }

            buildListTextArea();
            printDirectory();
        }

        public void parsePacket()
        {
            String data = new String(packet.getData());
            debug("S: Parsing Packet. Contains data:\n---\n" + data + "\n----");
            String packetType = data.split(" ")[1];
            Packet.PacketType type = Packet.PacketType.valueOf(packetType);

            debug("S: Parsing " + type + " packet");
            switch(type)
            {
                case INFORM_AND_UPDATE: updateFileListing(packet);   break;
                case QUERY_FOR_CONTENT: queryListing(packet);        break;
                case RATE_CONTENT:      rateContent(packet);         break;
                case EXIT:              registerClientExit(packet);  break;

                case ACK:
                    break;
            }
        }

        public void sendAck(DatagramPacket rcvPacket)
        {
            DatagramSocket socket = null;
            try
            {
                String header = new String(rcvPacket.getData()).split(" ")[0];
                received_segNums.add(Integer.parseInt(header));
                DatagramPacket ack = Packet.buildEmptyServerPacket(Packet.PacketType.ACK, header, client_inet, client_port);
                socket = new DatagramSocket();
                socket.send(ack);
                socket.close();
                debug("S: ACK sent to " + client_inet.getHostAddress() + ":" + client_port);
            }
            catch(Exception ex)
            {
                if(socket != null)
                    socket.close();
                
                Globals.exception(ex, "S:SendAck()");
            }
        }

        public void waitForAck()
        {
            DatagramSocket ds = null;
            try
            {
                ds = new DatagramSocket(listen_port);
                byte buffer[] = new byte[Globals.BUF_SIZE];

                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                ds.setSoTimeout(5000);
                ds.receive(p);
                ds.close();

                String str = new String(p.getData());
                debug("S: Received packet:\n---\n" + str + "\n---");
            }
            catch(SocketTimeoutException ex)
            {
                if(ds != null)
                    ds.close();
                debug("S: ACK timed out");
            }
            catch(Exception ex)
            {
                if(ds != null)
                    ds.close();

                Globals.exception(ex, "S:waitForAck()");
            }
        }

        public void sendPacket(Packet sendPacket)
        {
            DatagramSocket ds = null;
            try
            {
                for(DatagramPacket p : sendPacket.getPackets())
                {
                    ds = new DatagramSocket();

                    debug("S: Transmitting packet:\n---\n" + new String(p.getData()) + "\n---");
                    ds.send(p);
                    ds.close();

                    waitForAck();
                }

                ds = new DatagramSocket();
                ds.send(Packet.buildEmptyServerPacket(Packet.PacketType.FIN, STATUS_OK, client_inet, client_port));
                ds.close();

            }
            catch(Exception ex)
            {
                if(ds != null)
                    ds.close();

                Globals.exception(ex, "S:sendPacket()");
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

    private synchronized void printDirectory()
    {
        debug("S: PRINTING DIRECTORY");
        for(DirectoryListEntry entry : directory)
            debug(entry);
    }

    private synchronized void debug(Object obj)
    {
        Globals.debug(obj);
        
        console.append(obj + "\n");
    }

    public void packetWaitLoop()
    {
        DatagramSocket ds = null;
        try
        {
            ds = new DatagramSocket(Globals.SERVER_WELCOME_PORT);
            byte buffer[] = new byte[Globals.BUF_SIZE];

            while(true)
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ds.receive(packet);
                new ServerThread(packet);
                buffer = new byte[Globals.BUF_SIZE];
            }
        }
        catch (Exception ex)
        {
            if(ds != null)
                ds.close();
            Globals.exception(ex, "S:packetWaitLoop()");
        }


        if(ds != null)
            ds.close();
    }
}
