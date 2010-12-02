package datacomm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author  l3whalen
 */
public class UI extends javax.swing.JFrame
{

    /** Creates new form UI */
    public UI()
    {
        listen_port = 16000 + new Random().nextInt(1000);
        directory = new ArrayList();

        tcp = new ClientP2PTCPControl(listen_port, "", -1);

        initComponents();
        initRateFrame();
    }

    private void initRateFrame()
    {
        rateFrame = new JFrame("FrameDemo");
        ratingField = new JTextField(3);
        rateButton = new JButton("Rate");

        rateFrame.setSize(300, 100);
        rateFrame.setLayout(new BorderLayout());
        rateFrame.add(new JLabel("Enter a rating (1-100):"), BorderLayout.NORTH);
        rateFrame.add(ratingField, BorderLayout.CENTER);
        rateFrame.add(rateButton, BorderLayout.SOUTH);
        rateFrame.setVisible(false);

        class rateButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            {
                if(directoryList.getSelectedIndex() == -1)
                {
                    Globals.output("You must select an item from the list first!", true);
                    return;
                }

                if(ratingField.getText().isEmpty())
                {
                    Globals.output("Please enter a rating!", true);
                    return;
                }
                String file_name = directoryList.getSelectedValue().toString();
                int rating = Integer.parseInt(ratingField.getText());
                if(rating < 0 || rating > 100)
                {
                    Globals.output("The rating must be between 0 and 100!", true);
                }
                else
                {
                    String[] header = {file_name + ";" + rating};
                    Packet packet = new Packet();
                    packet.buildPacket(Packet.PacketType.RATE_CONTENT, header, "", server_port);
                    boolean ack = UDPSend(packet);
                    if(ack)
                    {
                        directory.get(directoryList.getSelectedIndex()).rate(rating);
                        updateDirectory();
                    }
                }
                directoryList.setSelectedIndex(-1);
                ratingField.setText("");
                rateFrame.setVisible(false);
            }
        }

        rateButton.addActionListener(new rateButtonListener());
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        jDialog1 = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        directoryList = new javax.swing.JList();
        QueryForContent = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        InformAndUpdate = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        RateContent = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ratingList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        Exit = new javax.swing.JButton();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jScrollPane1.setViewportView(directoryList);

        QueryForContent.setText("Search");
        QueryForContent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QueryForContentActionPerformed(evt);
            }
        });

        downloadButton.setText("Download");
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        InformAndUpdate.setText("Upload Files");
        InformAndUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InformAndUpdateActionPerformed(evt);
            }
        });

        RateContent.setText("Rate");
        RateContent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RateContentActionPerformed(evt);
            }
        });

        jLabel1.setText("File");

        jLabel2.setText("P2P Downloading App");

        jLabel3.setText("Designed by: Lyndsie Whalen 500143650");

        jLabel4.setText("                      Thavisha Lokuge");

        jLabel5.setText("                      Jenny Ta");

        jScrollPane2.setViewportView(ratingList);

        jLabel6.setText("Rating");

        jLabel7.setText("Search Results:");

        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(searchField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(QueryForContent))
                            .addComponent(InformAndUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                            .addComponent(jLabel5))
                        .addGap(98, 98, 98))
                    .addComponent(downloadButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 308, Short.MAX_VALUE)
                                .addComponent(RateContent, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(Exit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(56, 56, 56))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                        .addComponent(InformAndUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(QueryForContent))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RateContent, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Exit, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateDirectory()
    {
        String[] dirText = new String[directory.size()];
        String[] ratText = new String[directory.size()];
        for(int i = 0; i < directory.size(); ++i)
        {
            dirText[i] = directory.get(i).getFile();
            ratText[i] = directory.get(i).getRating() + "";
        }

        directoryList.setListData(dirText);
        ratingList.setListData(ratText);
    }

    private ReplyCode waitForAck()
    {
        Globals.debug("CLIENT: Waiting for ACK on port: " + listen_port);
        DatagramSocket ds = null;
        try
        {
            ds = new DatagramSocket(listen_port);
            byte buffer[] = new byte[Globals.BUF_SIZE];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            packet.setPort(server_port);
            ds.setSoTimeout(timeout_time);
            ds.receive(packet);
            ds.close();
            String str = new String(packet.getData());
            Globals.debug("CLIENT: RECEIVED PACKET: " + str);

            return ReplyCode.REPLY_OK;
        }
        catch(SocketTimeoutException ex)
        {
            if(ds != null)
                ds.close();
            Globals.debug("CLIENT: Socket Timed out before received ACK");
            return ReplyCode.REPLY_TIMEOUT;
        }
        catch(Exception ex)
        {
            if(ds != null)
                ds.close();
            Globals.debug("waitForAck(): " + ex);
            ex.printStackTrace();

            return ReplyCode.REPLY_ERROR;
        }
    }

    private void sendWelcomePacket()
    {
        DatagramSocket ds = null;
        try
        {
            long before = System.currentTimeMillis();
            ds = new DatagramSocket(listen_port);
            ds.send(Packet.buildClientWelcomePacket());

            byte buffer[] = new byte[Globals.BUF_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            ds.setSoTimeout(timeout_time);
            ds.receive(packet);
            ds.close();

            long after = System.currentTimeMillis();
            sample_rtt = (int) (after - before);
            estimated_rtt = (int) (estimated_rtt * (1-0.125) + sample_rtt * (0.125));
            dev_rtt = (int) (0.25 * Math.abs(sample_rtt - estimated_rtt));
            timeout_time = estimated_rtt + (4 * dev_rtt);
            Globals.debug("Estimated RTT: " + estimated_rtt);
            Globals.debug("Sample RTT: " + sample_rtt);
            Globals.debug("Dev RTT: " + dev_rtt);
            Globals.debug("Timeout Time: " + timeout_time);

            server_port = packet.getPort();
            Globals.debug("SendWelcomePacket() - Server Port: " + server_port);
        }
        catch(SocketTimeoutException ex)
        {
            if(ds != null)
                ds.close();

            Globals.debug("sendWelcomePacket(): Reply timed out!");
        }
        catch(Exception ex)
        {
            if(ds != null)
                ds.close();

            Globals.debug("sendWelcomePacket(): " + ex);
        }
    }
    
    private boolean UDPSend(Packet packet)
    {
        if(server_port <= 0)
        {
            try
            {
                sendWelcomePacket();
                Thread.currentThread().sleep(500);
            }
            catch(Exception ex)
            {
                Globals.debug("UDPSend sleep ex: " + ex);
            }
        }

        DatagramSocket ds = null;
        ReplyCode code = null;
        try
        {
            for(DatagramPacket p : packet.getPackets())
            {
                code = ReplyCode.REPLY_TIMEOUT;
                int numTries = -1;
                long before = -1;
                while(code == ReplyCode.REPLY_TIMEOUT && numTries < 5)
                {
                    ++numTries;
                    if(p.getPort() != server_port)
                        p.setPort(server_port);
                    ds = new DatagramSocket();

                    Globals.debug("C: Port ( " + ds.getPort() + ") transmitting packet: " + new String(p.getData()));
                    before = System.currentTimeMillis();
                    ds.send(p);
                    ds.close();

                    code = waitForAck();
                }
                if(before > 0 && code == ReplyCode.REPLY_OK)
                {
                    long after = System.currentTimeMillis();
                    sample_rtt = (int) (after - before);
                    estimated_rtt = (int) (estimated_rtt * (1-0.125) + sample_rtt * (0.125));
                    dev_rtt = (int) ((1 - 0.25) * dev_rtt + 0.25 * Math.abs(sample_rtt - estimated_rtt));
                    timeout_time = estimated_rtt + (4 * dev_rtt);
                    
                    Globals.debug("Estimated RTT: " + estimated_rtt);
                    Globals.debug("Sample RTT: " + sample_rtt);
                    Globals.debug("Dev RTT: " + dev_rtt);
                    Globals.debug("Timeout Time: " + timeout_time);
                }
                if(code == ReplyCode.REPLY_TIMEOUT)
                {
                    Globals.error("Client: Packet fully timed out:\n" + new String(p.getData()), true);
                    break;
                }
            }
            ds = new DatagramSocket();
            ds.send(Packet.buildEmptyClientPacket(Packet.PacketType.FIN, server_port));
            ds.close();
        }
        catch (Exception ex)
        {
            if(ds != null)
                ds.close();
            
            Globals.debug("UDPSend(): " + ex);

            code = ReplyCode.REPLY_ERROR;
        }

        if(code == ReplyCode.REPLY_ERROR || code == ReplyCode.REPLY_TIMEOUT)
            return false;
        
        return true;
    }

    private void sendExitPacket()
    {
        Packet packet = new Packet();
        packet.buildPacket(Packet.PacketType.EXIT, null, "", server_port);
        UDPSend(packet);

        System.exit(0);
    }
    
private void InformAndUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InformAndUpdateActionPerformed

    if(fileChooser == null)
    {
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        //fileChooser.setFileFilter(new JPEGExtensionFilter());
    }

    try
    {
        fileChooser.setCurrentDirectory(new File("."));
        if(fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION)
            return;

        File[] files = fileChooser.getSelectedFiles();

        String[] header = new String[files.length];
        for(int i = 0; i < files.length; ++i)// Spaces can cause issues, so we replace them with '&%'
            header[i] = files[i].getName().replaceAll(" ", "&%") + ";" + files[i].length();

        Packet packet = new Packet();
        packet.buildPacket(Packet.PacketType.INFORM_AND_UPDATE, header, "", server_port);
        UDPSend(packet);
    }
    catch(Exception ex)
    {
        Globals.debug("InformAndUpdate: " + ex);
    }
}//GEN-LAST:event_InformAndUpdateActionPerformed


private void QueryForContentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_QueryForContentActionPerformed

    if(searchField.getText().isEmpty())
    {
        Globals.error("Server cannot be queried with an empty search. Please enter some text in the search field before searching", true);
        return;
    }

    Object[] listdata = {""};
    directoryList.setListData(listdata);
    ratingList.setListData(listdata);
    String file_name[] = {searchField.getText()};

    Packet query = new Packet();
    query.buildPacket(Packet.PacketType.QUERY_FOR_CONTENT, file_name, null, server_port);
    UDPSend(query);

    DatagramSocket ds = null;
    int num_timeouts = 0;
    try
    {
        String type = "";
        ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();

        do
        {
            ds = new DatagramSocket(listen_port);

            byte buffer[] = new byte[Globals.BUF_SIZE];
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            Globals.debug("queryForContent: Waiting for packet");
            ds.setSoTimeout(timeout_time);
            ds.receive(p);
            ds.close();

            String data = new String(p.getData());
            type = data.split(" ")[2];
            type = type.replaceAll("\n", "");
            type = type.replaceAll("\r", "");
            Globals.debug("C: Received Packet:\n" + data);
            Globals.debug("Type: '" + type + "'");
            if(!type.equalsIgnoreCase(Packet.PacketType.FIN.toString()))
            {
                packets.add(p);
                ds = new DatagramSocket();
                ds.send(Packet.buildEmptyClientPacket(Packet.PacketType.ACK, server_port));
                ds.close();
            }

            num_timeouts = 0;
        }while(!type.equalsIgnoreCase(Packet.PacketType.FIN.toString()));

        DatagramPacket packet = Packet.assemblePackets(packets);

        Globals.debug("CLIENT: RECEIVED PACKET: " + new String(packet.getData()));
        directory.clear();
        String header[] = new String(packet.getData()).split(Packet.CRLF);
        for(int i = 1; i < header.length; ++i)
        {
            Globals.debug("File " + i + " : " + header[i]);
            String splat[] = header[i].split(";");
            if(splat.length < 2)
                continue;
            String file = splat[0];
            String size = splat[1];
            String address = splat[2];
            int port = Integer.parseInt(splat[3]);
            double rating = Double.parseDouble(splat[4]);
            //String file, String address, long size, int rating, int port
            directory.add(new DirectoryListEntry(file, address, size, rating, port));
        }

        updateDirectory();
    }
    catch(SocketTimeoutException ex)
    {
        if(ds != null)
            ds.close();
        num_timeouts++;
        if(num_timeouts > 5)
        {
            Globals.error("Client: Query fully timed out. Aborting.", true);
            return;
        }
        
        Globals.debug("CLIENT: Query Packet Response LOST!");
    }
    catch(Exception ex)
    {
        if(ds != null)
            ds.close();
        Globals.debug("QueryForContent: " + ex);
        return;
    }

}//GEN-LAST:event_QueryForContentActionPerformed

private void ExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ExitActionPerformed
{//GEN-HEADEREND:event_ExitActionPerformed
    sendExitPacket();
}//GEN-LAST:event_ExitActionPerformed

private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
{//GEN-HEADEREND:event_formWindowClosing
    sendExitPacket();
}//GEN-LAST:event_formWindowClosing

private void RateContentActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_RateContentActionPerformed
{//GEN-HEADEREND:event_RateContentActionPerformed

    rateFrame.setVisible(true);


}//GEN-LAST:event_RateContentActionPerformed

private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_downloadButtonActionPerformed
{//GEN-HEADEREND:event_downloadButtonActionPerformed
    if(directoryList.getSelectedIndex() == -1)
    {
        Globals.error("Please select a file to be downloaded!", true);
        return;
    }
    
    if(directoryList.getSelectedIndices().length > 1)
    {
        Globals.error("You can only download one file at a time (Sorry!).\nSo please try again with only one file selected!", true);
        return;
    }

    DirectoryListEntry entry = directory.get(directoryList.getSelectedIndex());
    
    tcp.setTransmitInfo(entry.getAddress(), entry.getPort());
    tcp.requestFile(entry);
    //tcp.transmitFile(f);
}//GEN-LAST:event_downloadButtonActionPerformed


    class JPEGExtensionFilter extends FileFilter
    {
        public boolean accept(File file)
        {
            if(file.getName().toLowerCase().endsWith(".jpeg")
                    || file.getName().toLowerCase().endsWith(".jpg"))
                return true;

            return false;
        }

        public String getDescription()
        {
            return ".jpeg and .jpg files only";
        }
    }

    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new UI().setVisible(true);
            }
        });
    }

    private int listen_port = 0;
    private int server_port = 0;
    private int estimated_rtt = 100;
    private int sample_rtt = 0;
    private int dev_rtt = 0;
    private int timeout_time = 5000;
    
    private JFileChooser fileChooser;
    private ArrayList<DirectoryListEntry> directory;

    private JTextField ratingField;
    private JButton rateButton;
    private JFrame rateFrame;

    private ClientP2PTCPControl tcp;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Exit;
    private javax.swing.JButton InformAndUpdate;
    private javax.swing.JButton QueryForContent;
    private javax.swing.JButton RateContent;
    private javax.swing.JList directoryList;
    private javax.swing.JButton downloadButton;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList ratingList;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
