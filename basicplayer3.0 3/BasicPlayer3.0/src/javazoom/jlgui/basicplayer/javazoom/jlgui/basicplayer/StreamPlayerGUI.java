package javazoom.jlgui.basicplayer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class StreamPlayerGUI extends JFrame {
    static StreamPlayerGUI s;
    javazoom.jlgui.basicplayer.BasicPlayer player;
    JPanel main;
    JPanel subPanel;
    JSplitPane splitPane;
    JScrollPane scrollPane;

    boolean alreadyInLib = false;
    DefaultMutableTreeNode library;
    DefaultMutableTreeNode playlist;
    DefaultMutableTreeNode p;
    JTree libTree;
    JTree playTree;
    JScrollPane playlistScrollp;
    JButton play;
    JButton stop;
    JButton pause;
    JButton skipForward;
    JButton skipBack;
    JTextField stringSelected;
    JTable j;
    JTable playlistT;

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
    JMenuItem createPlaylist;
    JMenuItem newWindow;
    JMenuItem deletePlaylist;
    JMenu addToPlaylist;
    String[] columns;
    int currentSongID;
    JPopupMenu popupMenu;
    JPopupMenu treePopUp;
    JMenuItem pickPlaylist;

    int playlistId;

    public static Connection connection;
    Statement stmt = null;
    int numRows;
    DefaultTableModel model;
    DefaultTableModel m;
    DefaultTreeModel defaultTreeModel;
    DefaultMutableTreeNode selectedNode;

    ArrayList<JTable> tables;
    ArrayList<JMenuItem> playlistArray;

    public StreamPlayerGUI() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mp3player";
        String username = "root";
        String password = "musicplayer123";
        connection = DriverManager.getConnection(url, username, password);

        currentSongID = 0;
        player = new BasicPlayer();

        main = new JPanel();
        subPanel = new JPanel();
        //subPanel.setBackground(Color.BLUE);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, subPanel, main);
        splitPane.setDividerLocation(100);

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
        createPlaylist = new JMenuItem("Create Playlist");
        add2 = new JMenuItem("Add song");
        delete2 = new JMenuItem("Delete song");
        open2 = new JMenuItem("Open song");
        exit2 = new JMenuItem("Exit Program");

        newWindow = new JMenuItem("Open in new window");
        deletePlaylist = new JMenuItem("Delete Playlist");

        addToPlaylist = new JMenu("Add to playlist");

        treePopUp = new JPopupMenu();
        treePopUp.add(newWindow);
        treePopUp.add(deletePlaylist);

        file.add(addSong);
        file.add(delete);
        file.add(open);
        file.add(createPlaylist);
        file.add(exit);

        popupMenu = new JPopupMenu();
        popupMenu.add(add2);
        popupMenu.add(delete2);
        popupMenu.add(open2);
        popupMenu.add(exit2);
        popupMenu.add(addToPlaylist);

        String[] columns = {"ID", "Title", "Genre", "Artist", "Year"};
        //Object[][] data = {{"", "", "", "", ""}};
        //j = new JTable(data, columns);
        numRows = 0;
        model = new DefaultTableModel(numRows, columns.length);
        model.setColumnIdentifiers(columns);
        j = new JTable(model);

        //j.setDropTarget(new MyDropTarget());
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
        createPlaylist.addActionListener(new ButtonListener());
        add2.addActionListener(new ButtonListener());
        delete2.addActionListener(new ButtonListener());
        open2.addActionListener(new ButtonListener());
        exit2.addActionListener(new ButtonListener());
        deletePlaylist.addActionListener(new ButtonListener());
        newWindow.addActionListener(new ButtonListener());
        //addToPlaylist.addActionListener(new ButtonListener());

        //popupMenu.addMouseListener(new PopClickListener());

        MouseListener m = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentRow = Integer.parseInt((String) j.getValueAt(j.getSelectedRow(), 0));
                System.out.println(j.getValueAt(j.getSelectedRow(), 0));
            }
        };
        MouseListener tableListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentRow = Integer.parseInt((String) playlistT.getValueAt(playlistT.getSelectedRow(), 0));
                System.out.println(playlistT.getValueAt(playlistT.getSelectedRow(), 0));
                TreePath path = playTree.getPathForLocation(e.getX(), e.getY());
                selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                System.out.println(selectedNode.toString());
            }
        };
        j.addMouseListener(m);
        nowPlaying = new JLabel("Now playing: nothing");
        scrollPane = new JScrollPane(j);
        scrollPane.setPreferredSize(new Dimension(475, 100));
        this.setTitle("StreamPlayer by Shawn Joseph and Amanda Jones");//change the name to yours
        this.add(splitPane);

        library = new DefaultMutableTreeNode("Library");
        libTree = new JTree(library);
        playlist = new DefaultMutableTreeNode("Playlists");
        playTree = new JTree(playlist);
        subPanel.add(libTree);
        subPanel.add(playTree);
        playTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        libTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        playTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                libTree.removeSelectionPath(libTree.getSelectionPath());
                System.out.println(tables.size());
                //String node = e.getNewLeadSelectionPath().getLastPathComponent().toString();
                int n = playTree.getMinSelectionRow();
                if (n > 0)
                {
                    JTable tab;
                    tab = tables.get(n);
                    playlistScrollp = new JScrollPane(tab);
                    playlistScrollp.setPreferredSize(new Dimension(475, 100));
                    playlistScrollp.addMouseListener(new PopClickListener());
                    tab.addMouseListener(tableListener);
                    main.removeAll();
                    main.add(playlistScrollp);
                    main.add(skipBack);
                    main.add(play);
                    main.add(pause);
                    main.add(stop);
                    main.add(skipForward);
                    main.revalidate();
                    main.repaint();
                }
            }
        });

        libTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                playTree.removeSelectionPath(playTree.getSelectionPath());
                main.removeAll();
                main.add(scrollPane);
                main.add(skipBack);
                main.add(play);
                main.add(pause);
                main.add(stop);
                main.add(skipForward);
                main.revalidate();
                main.repaint();

            }
        });

        //this.add(main);
        //this.add(nowPlaying);
        main.add(scrollPane);
        this.setJMenuBar(menuBar);
        main.add(skipBack);
        main.add(play);
        main.add(pause);
        main.add(stop);
        main.add(skipForward);

        main.add(popupMenu);

        tables = new ArrayList<JTable>();
        tables.add(j);
        playlistArray = new ArrayList<JMenuItem>();

        defaultTreeModel = new DefaultTreeModel(playlist);
        playTree.setModel(defaultTreeModel);
        j.addMouseListener(new PopClickListener());
        scrollPane.addMouseListener(new PopClickListener());
        main.addMouseListener(new PopClickListener());
        playTree.add(treePopUp);
        playTree.addMouseListener(new PopClickListener());

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
        Dimension d = new Dimension(600, 500);
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
                if (e.getSource() instanceof JTree)
                {
                    treePopUp.show(e.getComponent(), e.getX(), e.getY());
                }
                else
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
        alreadyInLib = false;
        String newid  =null;
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
                    alreadyInLib = true;
                    newid = song.getString("ID");
                    if (j.isShowing())
                    {
                        System.out.println("Song is already in library");
                        return;
                    }
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

            if (alreadyInLib == false)
            {
                preparedStatement.executeUpdate();
            }
            //stmt.executeUpdate(query1);
            //query1 = "SELECT title FROM songs";
            //ResultSet rs = stmt.executeQuery(query1);
            //System.out.println(rs);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        numRows++;
        if (alreadyInLib == false && j.isShowing())
        {
            Object[] newSong = {idString, title, genre, artist, year};
            model.addRow(newSong);
        }
        else if (alreadyInLib == false)
        {
            Object[] newSong = {idString, title, genre, artist, year};
            model.addRow(newSong);
            for (int i = 1; i < tables.size(); i++)
            {
                if (tables.get(i).isShowing())
                {
                    DefaultTableModel a = (DefaultTableModel) tables.get(i).getModel();
                    a.addRow(newSong);
                }
            }
        }
        else if (alreadyInLib)
        {
            Object[] newSong = {newid, title, genre, artist, year};
            for (int i = 1; i < tables.size(); i++)
            {
                if (tables.get(i).isShowing())
                {
                    DefaultTableModel a = (DefaultTableModel) tables.get(i).getModel();
                    a.addRow(newSong);
                }
            }
        }
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
                        int nextIndex = -1;
                        for(int i = 0; i < j.getRowCount(); i++) {
                            if (Integer.parseInt((String) j.getValueAt(i, 0)) == currentSongID) {
                                nextIndex = i;
                            }
                        }
                        if(nextIndex > -1) {
                            j.setRowSelectionInterval(nextIndex, nextIndex);
                        }
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
                        int nextIndex = -1;
                        for(int i = 0; i < j.getRowCount(); i++) {
                            if (Integer.parseInt((String) j.getValueAt(i, 0)) == currentSongID) {
                                nextIndex = i;
                            }
                        }
                        if(nextIndex > -1) {
                            j.setRowSelectionInterval(nextIndex, nextIndex);
                        }
                    }
                } catch (SQLException | BasicPlayerException throwables) {
                    throwables.printStackTrace();
                }
            }
            else if (e.getSource().equals(createPlaylist))
            {
                String playlistName = JOptionPane.showInputDialog("Enter a name for the playlist");
                System.out.println(playlistName);
                p = new DefaultMutableTreeNode(playlistName);

                playlist.add(p);
                subPanel.add(playTree);
                defaultTreeModel.reload();

                String[] colID = {"ID", "Title", "Genre", "Artist", "Year"};
                m = new DefaultTableModel(0, 5);
                m.setColumnIdentifiers(colID);
                playlistT = new JTable(m);
                tables.add(playlistT);

                pickPlaylist = new JMenuItem(playlistName);
                addToPlaylist.add(pickPlaylist);
                playlistArray.add(pickPlaylist);
                for (int i = 0; i < playlistArray.size(); i++)
                {
                    if (playlistArray.get(i).equals(pickPlaylist))
                    {
                        playlistArray.get(i).addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                for (int i = 0; i < playlistArray.size(); i++)
                                {
                                    if (e.getSource().equals(playlistArray.get(i)))
                                    {
                                        String node = playlistArray.get(i).getText();
                                        System.out.println(node);
                                        playlistId = i+1;

                                        Object [] newRow = {model.getValueAt(currentRow-1, 0), model.getValueAt(currentRow-1, 1), model.getValueAt(currentRow-1, 2),
                                                model.getValueAt(currentRow-1, 3), model.getValueAt(currentRow-1, 4)};
                                        DefaultTableModel a = (DefaultTableModel) tables.get(playlistId).getModel();
                                        a.addRow(newRow);
                                    }
                                }
                            }
                        });
                    }
                }

                TreePath tpath = new TreePath(p.getPath());
                playTree.setSelectionPath(tpath);
            }
            else if (e.getSource().equals(newWindow))
            {

            }
            else if (e.getSource().equals(deletePlaylist))
            {
                DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) playTree.getSelectionPath().getLastPathComponent();
                System.out.println(deleteNode.toString());
                for(int i = 0; i < playlist.getChildCount(); i++)
                {
                    if (playlist.getChildAt(i).equals(deleteNode))
                    {
                        playlist.remove(deleteNode);
                        tables.remove(i+1);
                        defaultTreeModel.reload();
                    }
                    if (playlistArray.get(i).getText().equals(deleteNode.toString()))
                    {
                        addToPlaylist.remove(playlistArray.get(i));
                        playlistArray.remove(i);
                    }
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