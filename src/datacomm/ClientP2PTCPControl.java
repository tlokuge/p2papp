package datacomm;

import java.io.*;
import java.net.*;
/**
 *
 * @author Thavisha
 */
public class ClientP2PTCPControl implements Runnable
{
    private int listen_port;

    private String send_hostname;
    private int send_port;

    Thread thread;
    
    public ClientP2PTCPControl(int listen, String send_host, int send)
    {
        thread = new Thread(this);
        thread.start();

        listen_port = listen;

        send_hostname = send_host;
        send_port = send;
    }

    public void run()
    {
        listen();
    }
    
    public void listen()
    {
        try
        {
            ServerSocket listen_socket = new ServerSocket(listen_port);
            Socket socket = listen_socket.accept();

            byte[] buffer = new byte[128];
            InputStream is = socket.getInputStream();
            is.read(buffer);

            String packet = new String(buffer);
            String split[] = packet.split(";");
            String type = packet.split(";")[0];
            if(type.equalsIgnoreCase("GET"))
            {
                String filename = split[1];
                String host = split[2];
                String port = Globals.normalize(split[3]);
                setTransmitInfo(split[2], Integer.parseInt(port));

                File f = new File(filename);
                if(f == null || f.exists())
                    transmitFile(f);
            }
            else if(type.equalsIgnoreCase("TRANSMIT"))
            {
                BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(new File("temp.jpeg")));
                while(is.read(buffer) != -1)
                    bs.write(buffer);

                is.close();
                bs.flush();
                bs.close();
            }
        }
        catch(Exception ex)
        {
            Globals.debug("Listen(): " + ex);
        }
    }

    public void requestFile(DirectoryListEntry file, String hostname, int port)
    {
        try
        {
            Socket socket = new Socket(send_hostname, send_port);

            OutputStream os = socket.getOutputStream();

            String data = "GET;" + file.getFile() + ";" + hostname + ";" + port;
            os.write(data.getBytes());

            os.flush();
            os.close();
        }
        catch(Exception ex)
        {
            Globals.exception(ex, "C:requestFile()");
        }
    }

    public void transmitFile(File f)
    {
        try
        {
            Socket socket = new Socket(send_hostname, send_port);

            OutputStream os = socket.getOutputStream();
            int length = (int)f.length();
            BufferedInputStream bs = new BufferedInputStream(new FileInputStream(f));
            byte[] buffer = new byte[128];
            int bytesRead = 0;
            os.write("TRANSMIT;\n".getBytes());
            while((bytesRead = bs.read(buffer)) != -1)
                os.write(buffer);
            
            os.flush();
            os.close();
            bs.close();
        }
        catch(Exception ex)
        {
            Globals.exception(ex, "C:transmitFile()");
        }
    }

    public void setTransmitInfo(String hostname, int port)
    {
        send_hostname = hostname;
        send_port = port;
    }
}
