/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datacomm;

/**
 *
 * @author l3whalen
 */

import java.net.*; 
import java.io.*;

public class directoryServer {

   public final static int BUFSIZE=128;
   public static void main(String ar[])
   {
      try {
          System.out.println("Initiating server....");
          int port=Integer.parseInt("40110");
          DatagramSocket ds=new DatagramSocket(port);
          byte buffer[]=new byte[BUFSIZE];

          while (true) {
             DatagramPacket dp=new DatagramPacket(buffer,buffer.length);
             ds.receive(dp);
             String str=new String(dp.getData());
             System.out.println(str);
          }
       }
       catch(Exception e) {
          System.err.println(e);
       }
    }
}
