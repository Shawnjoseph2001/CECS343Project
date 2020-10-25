package javazoom.jlgui.basicplayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.io.IOException;
import java.sql.*;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class StreamPlayerGUI extends JFrame {
    static StreamPlayerGUI s;
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
    String path = "";
    String filename = "";
    int id;
    String idString = "";
    String title = "";
    String artist = "";
    String genre = "";
    String year = "";
    JMenuBar menuBar;
    JMenu file;
    JMenuItem addSong;
    JMenuItem delete;
    JMenuItem open;
    JMenuItem exit;
    JMenuItem add2;
    JMenuItem delete2;
    JMenuItem open2;
    JMenuItem exit2;
    String[] columns;
    int currentSongID;
    JPopupMenu popupMenu;
    public static Connection connection;
    Statement stmt = null;
    int numRows;
    DefaultTableModel model;

    public StreamPlayerGUI() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/Data?serverTimezone=UTC", password = "potato123"; //Shawn's DB info, use other line on Amanda's PC
        //String url = "jdbc:mysql://localhost:3306/mp3player", password = "musicplayer123";//Amanda's DB info, use other line on Shawn's PC
        String username = "root";
        connection = DriverManager.getConnection(url, username, password);

        currentSongID = 0;
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

        add2 = new JMenuItem("Add song");
        delete2 = new JMenuItem("Delete song");
        open2 = new JMenuItem("Open song");
        exit2 = new JMenuItem("Exit Program");

        file.add(addSong);
        file.add(delete);
        file.add(open);
        file.add(exit);

        popupMenu = new JPopupMenu();
        popupMenu.add(add2);
        popupMenu.add(delete2);
        popupMenu.add(open2);
        popupMenu.add(exit2);

        String[] columns = {"ID", "Title", "Genre", "Artist", "Year"};
        //Object[][] data = {{"", "", "", "", ""}};
        //j = new JTable(data, columns);
        numRows = 0;
        model = new DefaultTableModel(numRows, columns.length);
        model.setColumnIdentifiers(columns);
        j = new JTable(model);

        j.setDropTarget(new MyDropTarget());
        this.setDropTarget(new MyDropTarget());
        //scrollPane.setDropTarget(new MyDropTarget());

        stringSelected = new JFormattedTextField("No string assigned");
        play.addActionListener(new ButtonListener());
        pause.addActionListener(new ButtonListener());
        skipForward.addActionListener(new ButtonListener());
        skipBack.addActionListener(new ButtonListener());
        stop.addActionListener(new ButtonListener());
        addSong.addActionListener(new ButtonListener());
        delete.addActionListener(new ButtonListener());
        open.addActionListener(new ButtonListener());
        exit.addActionListener(new ButtonListener());

        add2.addActionListener(new ButtonListener());
        delete2.addActionListener(new ButtonListener());
        open2.addActionListener(new ButtonListener());
        exit2.addActionListener(new ButtonListener());

        //popupMenu.addMouseListener(new PopClickListener());

        MouseListener m = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentRow = Integer.parseInt((String) j.getValueAt(j.getSelectedRow(), 0));
                System.out.println(j.getValueAt(j.getSelectedRow(), 0));
            }
        };
        j.addMouseListener(m);
        nowPlaying = new JLabel("Now playing: nothing");
        scrollPane = new JScrollPane(j);
        scrollPane.setPreferredSize(new Dimension(475, 100));
        this.setTitle("StreamPlayer by Shawn Joseph and Amanda Jones");//change the name to yours
        this.add(main);
        //this.add(nowPlaying);
        main.add(scrollPane);
        this.setJMenuBar(menuBar);
        main.add(skipBack);
        main.add(play);
        main.add(pause);
        main.add(stop);
        main.add(skipForward);

        main.add(popupMenu);

        j.addMouseListener(new PopClickListener());
        scrollPane.addMouseListener(new PopClickListener());
        main.addMouseListener(new PopClickListener());

        PreparedStatement populate = connection.prepareStatement("SELECT * FROM songs");
        ResultSet songList = populate.executeQuery();
        while (songList.next()) {
            String[] columnList = new String[5];
            columnList[0] = songList.getString("ID");
            columnList[1] = songList.getString("Title");
            columnList[2] = songList.getString("Genre");
            columnList[3] = songList.getString("Artist");
            columnList[4] = songList.getString("Year");
            model.addRow(columnList);
            int idt = songList.getInt("ID");
            if (idt > id) {
                id = idt + 1;
            }
        }

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = new Dimension(500, 500);
        //j.add(jb);
        //j.setSize(d);
        this.setSize(d);
        this.setVisible(true);
    }

    class PopClickListener extends MouseAdapter
    {
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                System.out.println("mouse clicked");
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else
                System.out.println("no");
        }

    }
    void playButton() {
        if(player.getStatus() == 1) {
            try {
                player.resume();
            } catch (BasicPlayerException basicPlayerException) {
                basicPlayerException.printStackTrace();
            }
        }
        else if(player.getStatus() == -1 || player.getStatus() == 2) {
            String query = "SELECT * FROM songs WHERE ID=" + currentRow;
            try {
                PreparedStatement p = connection.prepareStatement(query);
                ResultSet r = p.executeQuery();
                if (r.next()) {
                    String st = r.getString("Filepath");
                    currentSongID = r.getInt("ID");
                    player.open(new File(st));
                    player.play();
                    s.setTitle(r.getString("Title"));

                }
            } catch (SQLException | BasicPlayerException throwables) {
                throwables.printStackTrace();
            }
        }
        else if(player.getStatus() == 0){
            System.out.println("Player is already playing");
        }
    }
    void addSongFromFile(File file) {
        path = file.getAbsolutePath();
        filename = file.getName();

        try {
            Mp3File song = new Mp3File(path);
            if (song.hasId3v2Tag()) {
                ID3v2 id3v2tag = song.getId3v2Tag();
                title = id3v2tag.getTitle();
                artist = id3v2tag.getArtist();
                genre = id3v2tag.getGenreDescription();
                year = id3v2tag.getYear();
            }

        } catch (IOException | UnsupportedTagException | InvalidDataException ioException) {
            ioException.printStackTrace();
        }
        try{
            PreparedStatement test = connection.prepareStatement("SELECT * FROM songs");
            ResultSet song = test.executeQuery();
            while(song.next()) {
                if (song.getString("Title").equals(title))
                {
                    System.out.println("Song is already in library");
                    return;
                }
            }

        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        id++;
        try {
            //stmt = (Statement) BasicPlayerTest.connection.createStatement();
            String query1 = "INSERT INTO songs(ID, Title, Genre, Artist, Year, Filepath) " + "VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(query1);

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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        numRows++;
        Object[] newSong = {idString, title, genre, artist, year};
        model.addRow(newSong);
    }

    void deleteSelectedSong() {
        int currentSelectedRow = j.getSelectedRow();
        try {
            String deleteByID = (String) model.getValueAt(currentSelectedRow, 0);
            String query = "DELETE FROM songs WHERE ID = ?";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, deleteByID);
            preparedStmt.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        model.removeRow(currentSelectedRow);
        numRows--;

    }
    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.print(""); //convenient debugger entry spot
            if (e.getSource().equals(stop)) {
                try {
                    player.stop();
                    s.setTitle("Not Playing");
                } catch (BasicPlayerException basicPlayerException) {
                    basicPlayerException.printStackTrace();
                }
            } else if (e.getSource().equals(play)) {
                playButton();
            } else if (e.getSource().equals(pause)) {
                try {
                    player.pause();
                } catch (BasicPlayerException basicPlayerException) {
                    basicPlayerException.printStackTrace();
                }
            } else if (e.getSource().equals(addSong) || e.getSource().equals(add2)) {
                {
                    final JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showOpenDialog(main);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        //stringSelected = new JTextField(fc.getName());
                        addSongFromFile(file);
                    }
                }
            } else if (e.getSource().equals(delete) || e.getSource().equals(delete2)) {
                System.out.println("deleted");
                deleteSelectedSong();
            } else if (e.getSource().equals(open) || e.getSource().equals(open2)) {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(main);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        player.open(file);
                        player.play();
                        s.setTitle("Playing file " + file.getAbsolutePath());
                    } catch (BasicPlayerException basicPlayerException) {
                        basicPlayerException.printStackTrace();
                    }
                }
            } else if (e.getSource().equals(exit) || e.getSource().equals(exit2)) {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                System.exit(0);
            } else if (e.getSource().equals(skipForward)) {
                try {
                    String query = "SELECT * FROM songs WHERE ID > " + currentSongID;
                    PreparedStatement p = connection.prepareStatement(query);
                    ResultSet r = p.executeQuery();
                    if (r.next()) {
                        currentSongID = r.getInt("ID");
                        String fp = r.getString("Filepath");
                        player.open(new File(fp));
                        player.play();
                        s.setTitle(r.getString("Title"));
                    }
                } catch (SQLException | BasicPlayerException throwables) {
                    throwables.printStackTrace();
                }
            } else if (e.getSource().equals(skipBack)) {
                try {
                    String query = "SELECT * FROM songs WHERE ID < " + currentSongID + " ORDER BY ID desc";
                    PreparedStatement p = connection.prepareStatement(query);
                    ResultSet r = p.executeQuery();
                    if (r.next()) {
                        String currentSongtemp = r.getString("ID");
                        currentSongID = Integer.parseInt(currentSongtemp);
                        String fp = r.getString("Filepath");
                        player.open(new File(fp));
                        player.play();
                        s.setTitle(r.getString("Title"));
                    }
                } catch (SQLException | BasicPlayerException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
            class MyDropTarget extends DropTarget {
                public void drop(DropTargetDropEvent event) {
                    Transferable transferable = event.getTransferable();
                    DataFlavor[] flavors = transferable.getTransferDataFlavors();
                    for (DataFlavor flavor : flavors) {
                        if (flavor.isFlavorJavaFileListType()) {
                            event.acceptDrop(DnDConstants.ACTION_COPY);

                            try {
                                @SuppressWarnings("unchecked")
                                List<File> list = (List<File>) transferable.getTransferData(flavor);
                                for (File fil : list) {
                                    addSongFromFile(fil);
                                }

                            } catch (UnsupportedFlavorException | IOException ex) {
                                ex.printStackTrace();
                                event.rejectDrop();
                            }
                            event.dropComplete(true);
                            System.out.println("drop complete");
                            return;
                        }
                        event.rejectDrop();

                    }
                }

            }
            public static void main (String[]args) throws SQLException {
                s = new StreamPlayerGUI();
            }

        }