package datacomm;


public class DirectoryListEntry
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
        // r(n)= 0.5*r(n-1) + 0.5*R
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