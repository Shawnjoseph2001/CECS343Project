package javazoom.jlgui.basicplayer;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.util.logging.*;
import com.mpatric.mp3agic.ID3v1Genres;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javazoom.spi.mpeg.sampled.file.tag.MP3Tag;

public class StreamPlayerGUI extends JFrame {
    BasicPlayer player;
    JPanel main;
    JScrollPane scrollPane;
    JButton play;
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
    Statement stmt = null;
    int numRows;
    DefaultTableModel model;

    public StreamPlayerGUI() {
        player = new BasicPlayer();
        main = new JPanel();
        play = new JButton("Play");
        pause = new JButton("Pause");
        skipForward = new JButton(" >> ");
        skipBack = new JButton(" << ");

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
        scrollPane = new JScrollPane(j);
        scrollPane.setPreferredSize(new Dimension(475, 100));
        this.setTitle("StreamPlayer by Shawn Joseph and Amanda Jones");//change the name to yours
        this.add(main);

        main.add(skipBack);
        main.add(play);
        main.add(pause);
        main.add(skipForward);
        main.add(scrollPane);

        MenuBar menu = new MenuBar();
        this.setJMenuBar(menu.createMenuBar());

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
            final JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(main);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                stringSelected = new JTextField(fc.getName());
            }


        }
    }
    class MenuBar
    {
        public JMenuBar createMenuBar()
        {
            JMenuBar menuBar = new JMenuBar();
            JMenu file = new JMenu("File");

            JMenuItem add = new JMenuItem(new AbstractAction("Add song")
            {
                public void actionPerformed(ActionEvent e)
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
                        String query1 = "INSERT INTO songs(ID, Title, Genre, Artist, Year, Filepath) " + "VALUES (?, ?, ?, ?, ?, ?)";
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
            });

            JMenuItem delete = new JMenuItem(new AbstractAction("Delete song")
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
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
                }
            });

            JMenuItem open = new JMenuItem(new AbstractAction("Open song")
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JFileChooser fc = new JFileChooser();
                    String fileName = "";
                    int returnVal = fc.showOpenDialog(main);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        fileName = fc.getSelectedFile().toURI().toString();
                    }
                    Media media = new Media(fileName);
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();
                    //System.out.println(path);
                }
            });

            JMenuItem exit = new JMenuItem(new AbstractAction("Exit application") {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    try {
                        BasicPlayerTest.connection.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    System.exit(0);
                }
            });

            file.add(add);
            file.add(delete);
            file.add(open);
            file.add(exit);
            menuBar.add(file);

            return menuBar;
        }
    }

}

