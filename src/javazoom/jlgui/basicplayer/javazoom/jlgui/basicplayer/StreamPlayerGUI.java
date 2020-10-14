package javazoom.jlgui.basicplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.*;
import java.util.logging.*;

public class StreamPlayerGUI extends JFrame {
    BasicPlayer player;
    JPanel main;
    JScrollPane jt;
    JButton jb;
    JTextField stringSelected;
    JTable j;
    JLabel nowPlaying;
    int currentRow;

    public StreamPlayerGUI() {
        player = new BasicPlayer();
        main = new JPanel();
        // j = new JTable();
        jb = new JButton("Play");
        stringSelected = new JFormattedTextField("No string assigned");
        jb.addActionListener(new ButtonListener());
        MouseListener m = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentRow = j.getSelectedRow();
                System.out.println(currentRow);
            }
        };

        // j.addMouseListener(m);
        // j.add(jb);
        nowPlaying = new JLabel("Now playing: nothing");
        //this.add(nowPlaying);
        this.setTitle("StreamPlayer by Shawn Joseph");//change the name to yours
        this.add(main);
        main.add(jb);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = new Dimension(500, 500);
        //j.add(jb);
        //j.setSize(d);
        this.setSize(d);
        this.setVisible(true);


    }

    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(main);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                stringSelected = new JTextField(fc.getName());
            }


        }
    }
}
