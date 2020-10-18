package javazoom.jlgui.basicplayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class StreamPlayerGUI extends JFrame {
    BasicPlayer player;
    JPanel main;
    JScrollPane scrollPane;
    JButton play;
    JButton stop;
    JButton pause;
    JButton skipForward;
    JButton skipBack;
    JTextField stringSelected;
    JTable j;
    JLabel nowPlaying;
    int currentRow;
    String path;
    String filename;
    int id = 0;
    String idString = "";
    String title;
    String artist;
    String genre;
    String year;
    JMenuBar menuBar;
    JMenu file;
    JMenuItem addSong;
    JMenuItem delete;
    JMenuItem open;
    JMenuItem exit;
    String[] columns;

    Statement stmt = null;
    int numRows;
    DefaultTableModel model;

    public StreamPlayerGUI() {
        player = new BasicPlayer();
        main = new JPanel();
        play = new JButton("Play");
        pause = new JButton("Pause");
        stop = new JButton("Stop");
        skipForward = new JButton(" >> ");
        skipBack = new JButton(" << ");
        menuBar = new JMenuBar();
        file = new JMenu("File");
        menuBar.add(file);
        addSong = new JMenuItem("Add song");
        delete = new JMenuItem("Delete song");
        open = new JMenuItem("Open song");
        exit = new JMenuItem("Exit Program");
        file.add(addSong);
        file.add(delete);
        file.add(open);
        file.add(exit);


        String[] columns = {"ID", "Title", "Genre", "Artist", "Year"};
        //Object[][] data = {{"", "", "", "", ""}};
        //j = new JTable(data, columns);
        numRows = 0;
        model = new DefaultTableModel(numRows, columns.length);
        model.setColumnIdentifiers(columns);
        j = new JTable(model);

        stringSelected = new JFormattedTextField("No string assigned");
        play.addActionListener(new ButtonListener());
        pause.addActionListener(new ButtonListener());
        skipForward.addActionListener(new ButtonListener());
        skipBack.addActionListener(new ButtonListener());
        stop.addActionListener(new ButtonListener());
        addSong.addActionListener(new ButtonListener());
        delete.addActionListener(new ButtonListener());
        open.addActionListener(new ButtonListener());

        MouseListener m = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentRow = Integer.parseInt((String)j.getValueAt(j.getSelectedRow(), 0));
                System.out.println(j.getValueAt(j.getSelectedRow(), 0));
            }
        };
        j.addMouseListener(m);
        nowPlaying = new JLabel("Now playing: nothing");
        scrollPane = new JScrollPane(j);
        scrollPane.setPreferredSize(new Dimension(475, 100));
        this.setTitle("StreamPlayer by Shawn Joseph and Amanda Jones");//change the name to yours
        this.add(main);
        main.add(skipBack);
        main.add(play);
        main.add(pause);
        main.add(stop);
        main.add(skipForward);
        main.add(scrollPane);
        this.setJMenuBar(menuBar);

        main.addMouseListener(new PopClickListener());

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = new Dimension(500, 500);
        //j.add(jb);
        //j.setSize(d);
        this.setSize(d);
        this.setVisible(true);
    }

    class PopClickListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                System.out.println("mouse clicked");
                PopupMenu menu = new PopupMenu();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }
    class ButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println();
            if(e.getSource().equals(stop)) {
                try {
                    player.stop();
                } catch (BasicPlayerException basicPlayerException) {
                    basicPlayerException.printStackTrace();
                }
            }
            else if(e.getSource().equals(play)) {
                String query = "SELECT Filepath FROM songs WHERE ID=" + currentRow;
                try {
                    PreparedStatement p = BasicPlayerTest.connection.prepareStatement(query);
                    ResultSet r = p.executeQuery();
                    String s = r.getString(1);
                    player.open(new File(s));
                    player.play();
                } catch (SQLException | BasicPlayerException throwables) {
                    throwables.printStackTrace();
                }
            }
            else if(e.getSource().equals(pause)) {
                try {
                    player.pause();
                } catch (BasicPlayerException basicPlayerException) {
                    basicPlayerException.printStackTrace();
                }
            }
            else if(e.getSource().equals(addSong)) {
                {
                    final JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showOpenDialog(main);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        //stringSelected = new JTextField(fc.getName());
                        path = file.getAbsolutePath();
                        filename = file.getName();

                        try
                        {
                            Mp3File song = new Mp3File(path);
                            if (song.hasId3v2Tag())
                            {
                                ID3v2 id3v2tag = song.getId3v2Tag();
                                title = id3v2tag.getTitle();
                                artist = id3v2tag.getArtist();
                                genre = id3v2tag.getGenreDescription();
                                year = id3v2tag.getYear();
                            }

                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        } catch (UnsupportedTagException unsupportedTagException) {
                            unsupportedTagException.printStackTrace();
                        } catch (InvalidDataException invalidDataException) {
                            invalidDataException.printStackTrace();
                        }

                    }
                    //System.out.println(path);
                    //System.out.println(filename);
                    //System.out.println(title);
                    //System.out.println(artist);
                    //System.out.println(genre);
                    //System.out.println(year);
                    id++;
                    try {
                        //stmt = (Statement) BasicPlayerTest.connection.createStatement();
                            String query1 = "INSERT INTO songs(ID, Title, Genre, Artist, Year, Filepath) " + "VALUES (?, ?, ?, ?, ?, ?);";
                        PreparedStatement preparedStatement = BasicPlayerTest.connection.prepareStatement(query1);

                        idString = Integer.toString(id);

                        preparedStatement.setString(1, idString);
                        preparedStatement.setString(2, title);
                        preparedStatement.setString(3, genre);
                        preparedStatement.setString(4, artist);
                        preparedStatement.setString(5, year);
                        preparedStatement.setString(6, path);

                        preparedStatement.executeUpdate();
                        //stmt.executeUpdate(query1);
                        //query1 = "SELECT title FROM songs";
                        //ResultSet rs = stmt.executeQuery(query1);
                        //System.out.println(rs);
                    } catch (SQLException throwables)
                    {
                        throwables.printStackTrace();
                    }
                    numRows++;
                    Object[] newSong = {idString, title, genre, artist, year};
                    model.addRow(newSong);
                }
            }
            else if(e.getSource().equals(delete)) {
                int currentSelectedRow = j.getSelectedRow();
                try {
                    String deleteByID = (String) model.getValueAt(currentSelectedRow, 0);
                    String query = "DELETE FROM songs WHERE ID = ?";
                    PreparedStatement preparedStmt = BasicPlayerTest.connection.prepareStatement(query);
                    preparedStmt.setString(1, deleteByID);
                    preparedStmt.execute();

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                model.removeRow(currentSelectedRow);
                model.removeRow(currentSelectedRow);
            }
            else if(e.getSource().equals(open)) {
                    final JFileChooser fc = new JFileChooser();
                    String fileName = "";
                    int returnVal = fc.showOpenDialog(main);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        try {
                            player.open(file);
                        } catch (BasicPlayerException basicPlayerException) {
                            basicPlayerException.printStackTrace();
                        }
                    }
                    //System.out.println(path);
            }
            else if(e.getSource().equals(exit)) {
                try {
                    BasicPlayerTest.connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                System.exit(0);
            }
        }
    }
}

