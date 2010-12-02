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

            BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(new File("temp.jpeg")));
            while(is.read(buffer) != -1)
                bs.write(buffer);

            is.close();
            bs.flush();
            bs.close();
        }
        catch(Exception ex)
        {
            Globals.debug("Listen(): " + ex);
        }
    }

    public void requestFile(DirectoryListEntry file)
    {
        try
        {
            Socket socket = new Socket(send_hostname, send_port);

            
        }
        catch(Exception ex)
        {

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
            while((bytesRead = bs.read(buffer)) != -1)
                os.write(buffer);
            
            os.flush();
            os.close();
            bs.close();
        }
        catch(Exception ex)
        {
            Globals.debug("transmitFile() - " + ex);
        }
    }

    public void setTransmitInfo(String hostname, int port)
    {
        send_hostname = hostname;
        send_port = port;

        Globals.debug("Hostname set to: " + hostname);
        Globals.debug("Port set to: " + port);
    }
}
