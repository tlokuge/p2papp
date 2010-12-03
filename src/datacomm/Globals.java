
package datacomm;

import java.io.PrintStream;
import javax.swing.JOptionPane;

public abstract class Globals
{
    public static final int BUF_SIZE = 128;
    public static final String CRLF = System.getProperty("line.separator") + " ";

    public static final int SERVER_WELCOME_PORT = 40110;
    public static final int BASE_PORT = 16000;

    public static final String HEADER_APPEND = "#%";

    public static boolean DEBUG = true;

    public static boolean isInDebugMode() { return DEBUG; }

    public static String normalize(String str)
    {
        String normalized = "";
        for(int i = 0; i < str.length(); ++i)
        {
            char ch = str.charAt(i);
            if(Character.isLetter(str.charAt(i)) || Character.isDigit(ch))
                normalized += ch;
        }

        return normalized;
    }

    public static void debug(Object debug)
    {
        if(isInDebugMode())
            output("DEBUG: " + debug);
    }

    public static void output(Object output)
    {
        output(output, false);
    }

    public static void output(Object output, boolean send_dialog)
    {
        out(System.out, output, send_dialog, MessageType.INFO, "Notice");
    }

    public static void error(Object error)
    {
        error(error, false);
    }

    public static void error(Object error, boolean send_dialog)
    {
        out(System.err, error, send_dialog, MessageType.ERROR, "Error");
    }

    public static void exception(Exception ex, String method_name)
    {
        String exception = "Exception in " + method_name + ":\n"
                + ex.getClass().getName() + ": " + ex.getLocalizedMessage();
        out(System.err, exception, true, MessageType.ERROR, "Exception");
    }

    private static void out(PrintStream str, Object msg, boolean dialog, MessageType type, String title)
    {
        if(dialog)
            sendMessage(type, title, msg);

        if(type == MessageType.ERROR)
            msg = "ERROR: " + msg;
        str.println(msg);
    }
    
    public static void sendMessage(MessageType type, String title, Object message)
    {
        int message_type = -1;
        switch(type)
        {
            case ERROR:   message_type = JOptionPane.ERROR_MESSAGE;       break;
            case WARNING: message_type = JOptionPane.WARNING_MESSAGE;     break;
            case QUESTION:message_type = JOptionPane.QUESTION_MESSAGE;    break;
            case INFO:    message_type = JOptionPane.INFORMATION_MESSAGE; break;
            case PLAIN:   message_type = JOptionPane.PLAIN_MESSAGE;       break;
        }

        JOptionPane.showMessageDialog(null, message, title, message_type);
    }
}

enum MessageType
{
    ERROR,
    WARNING,
    QUESTION,
    INFO,
    PLAIN;
}

enum ReplyCode
{
    REPLY_OK,
    REPLY_TIMEOUT,
    REPLY_ERROR
};

class DirectoryListEntry
{
    private String file;
    private String address;
    private String size;
    private double rating;
    private int port;

    public DirectoryListEntry(String file, String address, String size, double rating, int port)
    {
        this.file = file;
        this.address = address;

        this.size = "";
        for(int i = 0; i < size.length(); ++i)
            if(Character.isDigit(size.charAt(i)))
                this.size += size.charAt(i);

        this.rating = rating;
        this.port = port;
    }

    public void rate(int newRating)
    {
        double nR = (double)newRating / (double)100;
        rating = (0.5 * rating) + (0.5 * nR);
        System.out.println("rating: " + rating);
    }

    public String convertToPacketData()
    {
        return file + ";" + size + ";" + address + ";" + port + ";" + rating + ";";
    }

    public String getFile()    { return file; }
    public String getAddress() { return address; }
    public String getSize()    { return size; }
    public double getRating()  { return rating; }
    public int getPort()       { return port; }

    public String toString()
    {
        return "File Listing[" + file + "(" + size + ") - " + address + ":" + port + " - " + rating + "]";
    }
}

