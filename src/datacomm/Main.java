
package datacomm;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

public class Main extends JFrame
{
    private JButton spawnServerButton;
    private JButton spawnClientButton;

    private boolean server_created;

    public Main()
    {
        super();

        server_created = false;
        
        initComponents();

        this.setLayout(new BorderLayout());
        this.add(spawnServerButton, BorderLayout.WEST);
        this.add(spawnClientButton, BorderLayout.EAST);
        this.setSize(250, 100);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void initComponents()
    {
        spawnServerButton = new JButton("Spawn Server");
        spawnClientButton = new JButton("Client Server");

        class spawnServerListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            {
                if(!server_created)
                    new MainThread(DirectoryServer.class.getName());
            }
        }

        class spawnClientListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            {
                new MainThread(Client.class.getName());
            }
        }

        spawnServerButton.addActionListener(new spawnServerListener());
        spawnClientButton.addActionListener(new spawnClientListener());
    }

    public static void main(String[] args)
    {
        new MainThread(Main.class.getName());
    }
}

class MainThread implements Runnable
{
    private String class_name;
    private Thread thread;

    public MainThread(String class_name)
    {
        this.class_name = class_name;

        thread = new Thread(this);
        thread.start();
    }
    
    public void run()
    {
        try
        {
            Class.forName(class_name).newInstance();
            Globals.debug("Main: Starting new instance of " + class_name);
        }
        catch(Exception ex)
        {
            Globals.exception(ex, "MainThread run");
        }
    }

}