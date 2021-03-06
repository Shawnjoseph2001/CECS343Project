package javazoom.jlgui.basicplayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    static ArrayList<StreamPlayerGUI> players;
    boolean alreadyInLib = false;
    DefaultMutableTreeNode library;
    DefaultMutableTreeNode playlist;
    DefaultMutableTreeNode p;
    JTree libTree;
    JTree playTree;
    JScrollPane playlistScrollp;
    JButton play;
    JButton play2;
    JButton stop;
    JButton stop2;
    JButton pause;
    JButton pause2;
    JButton skipForward;
    JButton skipForward2;
    JButton skipBack;
    JButton skipBack2;
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
    int currentSongID;
    JPopupMenu popupMenu;
    JPopupMenu treePopUp;
    JMenuItem pickPlaylist;

    int playlistId;

    public static Connection connection;
    public static ArrayList<Thread> threads;
    int numRows;
    DefaultTableModel model;
    DefaultTableModel m;
    DefaultTreeModel defaultTreeModel;
    DefaultMutableTreeNode selectedNode;

    ArrayList<JTable> tables;
    ArrayList<JMenuItem> playlistArray;

    JScrollPane scrollPane2;
    JSlider slider;
    JLabel statusLabel;
    Boolean twoWindows;

    public StreamPlayerGUI() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mp3player";
        String username = "root";
        String password = "musicplayer123";
        connection = DriverManager.getConnection(url, username, password);

        currentSongID = 0;
        player = new BasicPlayer();

        main = new JPanel();
        subPanel = new JPanel();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, subPanel, main);
        splitPane.setDividerLocation(100);

        play = new JButton("Play");
        play2 = new JButton("Play");
        pause = new JButton("Pause");
        pause2 = new JButton("Pause");
        stop = new JButton("Stop");
        stop2 = new JButton("Stop");
        skipForward = new JButton(" >> ");
        skipForward2 = new JButton(" >> ");
        skipBack = new JButton(" << ");
        skipBack2 = new JButton(" << ");
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
        //file.add(newWindow);
        deletePlaylist = new JMenuItem("Delete Playlist");

        addToPlaylist = new JMenu("Add to playlist");
        //addToPlaylist.add(new JMenuItem("Playlists: "));
        treePopUp = new JPopupMenu();
        treePopUp.add(newWindow);
        treePopUp.add(deletePlaylist);

        //file.add(newWindow);
        file.add(addToPlaylist);
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

        this.setDropTarget(new MyDropTarget());

        stringSelected = new JFormattedTextField("No string assigned");
        play.addActionListener(new ButtonListener());
        pause.addActionListener(new ButtonListener());
        skipForward.addActionListener(new ButtonListener());
        skipBack.addActionListener(new ButtonListener());
        stop.addActionListener(new ButtonListener());
        play2.addActionListener(new ButtonListener());
        pause2.addActionListener(new ButtonListener());
        skipForward2.addActionListener(new ButtonListener());
        skipBack2.addActionListener(new ButtonListener());
        stop2.addActionListener(new ButtonListener());
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

        MouseListener m = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                for (JTable table : tables) {
                    if (table.isShowing()) {
                        currentRow = Integer.parseInt((String) table.getValueAt(table.getSelectedRow(), 0));
                        System.out.println(table.getValueAt(table.getSelectedRow(), 0));
                    }
                }
            }
        };
        MouseListener tableListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                currentRow = Integer.parseInt((String) playlistT.getValueAt(playlistT.getSelectedRow(), 0));
                System.out.println(playlistT.getValueAt(playlistT.getSelectedRow(), 0));
                if (playTree.getSelectionPath() != null)
                {
                    selectedNode = (DefaultMutableTreeNode) Objects.requireNonNull(playTree.getSelectionPath()).getLastPathComponent();
                    System.out.println(selectedNode.toString());
                }
            }
        };
        boolean tableExists = false;
        try {
            PreparedStatement getCount = connection.prepareStatement("SELECT count(*) AS count FROM information_schema.TABLES WHERE  (TABLE_NAME = 'playlists')");
            ResultSet r = getCount.executeQuery();
            r.next();
            tableExists = r.getBoolean("count");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if(!tableExists) {
            PreparedStatement addTable = connection.prepareStatement("CREATE TABLE playlists (Name VARCHAR(100) null)");
            addTable.execute();
        }

        j.addMouseListener(m);
        j.addMouseListener(new PopClickListener());
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

        playTree.addTreeSelectionListener(e -> {
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
                tab.addMouseListener(new PopClickListener());
                main.removeAll();
                main.add(playlistScrollp);
                main.add(skipBack);
                main.add(play);
                main.add(pause);
                main.add(stop);
                main.add(skipForward);
                main.add(slider);
                main.add(statusLabel);
                main.revalidate();
                main.repaint();
            }
        });

        libTree.addTreeSelectionListener(e -> {
            playTree.removeSelectionPath(playTree.getSelectionPath());
            main.removeAll();
            main.add(scrollPane);
            main.add(skipBack);
            main.add(play);
            main.add(pause);
            main.add(stop);
            main.add(skipForward);
            main.add(slider);
            main.add(statusLabel);
            main.revalidate();
            main.repaint();

        });

        statusLabel = new JLabel("");
        slider = new JSlider(JSlider.HORIZONTAL,0,100,10);

        slider.addChangeListener(e -> {

            try {
                player.setGain((((JSlider)e.getSource()).getValue()  / 100.0) * 1.4);
                System.out.println((((JSlider)e.getSource()).getValue()  / 100.0) * 1.4);
            } catch (BasicPlayerException basicPlayerException) {
                basicPlayerException.printStackTrace();
            }
            statusLabel.setText("Value : " + (((JSlider)e.getSource()).getValue()));
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
        main.add(slider);
        main.add(statusLabel);
        main.add(popupMenu);

        tables = new ArrayList<>();
        tables.add(j);
        playlistArray = new ArrayList<>();
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
        TableRowTransferHandler h = new TableRowTransferHandler();
        j.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        j.setTransferHandler(h);
        j.setDropMode(DropMode.INSERT_ROWS);
        j.setDragEnabled(true);
        j.setFillsViewportHeight(true);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = new Dimension(600, 500);
        this.setSize(d);
        this.setVisible(true);

        PreparedStatement readFromTable = connection.prepareStatement("SELECT Name from playlists");
        ResultSet r = readFromTable.executeQuery();
        while(r.next()) {
            playlist.add(new DefaultMutableTreeNode(r.getString("Name")));
            JMenuItem playlist = new JMenuItem(r.getString("Name"));
            playlistArray.add(playlist);
            addToPlaylist.add(playlist);
            playlist.addActionListener(new ButtonListener());
            //subPanel.add(playTree);
            defaultTreeModel.reload();
            String[] colID = {"ID", "Title", "Genre", "Artist", "Year"};
            DefaultTableModel md = new DefaultTableModel(0, 5);
            md.setColumnIdentifiers(colID);
            playlistT = new JTable(md);
            playlistT.setDropMode(DropMode.USE_SELECTION);
            PreparedStatement addToPlaylist = connection.prepareStatement("SELECT * FROM " + r.getString("Name"));
            ResultSet pl = addToPlaylist.executeQuery();
            while(pl.next()) {
                String[] columnList = new String[5];
                columnList[0] = pl.getString("ID");
                columnList[1] = pl.getString("Title");
                columnList[2] = pl.getString("Genre");
                columnList[3] = pl.getString("Artist");
                columnList[4] = pl.getString("Year");
                md.addRow(columnList);
            }
            tables.add(playlistT);
        }
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
                    //System.out.println(st);
                    //s.setTitle(r.getString("Title"));

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
        twoWindows = false;
        String playlistString = null;
        String newid = null;
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
                    System.out.println("Song is already in library");
                    newid = song.getString("ID");
                    for (int i = 1; i < tables.size(); i++)
                    {
                        if (tables.get(i).isShowing())
                        {
                            DefaultMutableTreeNode playNode = (DefaultMutableTreeNode) playlist.getChildAt(i-1);
                            playlistString = playNode.toString();
                        }
                        if (j.isShowing() && tables.get(i).isShowing())
                        {
                            twoWindows = true;
                        }
                    }
                    if (!twoWindows && j.isShowing())
                    {
                        return;
                    }
                }
            }

        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        id++;
        try {
            for (int i =1; i < tables.size(); i++)
            {
                if (tables.get(i).isShowing())
                {
                    DefaultMutableTreeNode playNode = (DefaultMutableTreeNode) playlist.getChildAt(i-1);
                    playlistString = playNode.toString();
                }
            }
            String query2 = "INSERT INTO " + playlistString + "(ID, Title, Genre, Artist, Year, Filepath) " + "VALUES (?, ?, ?, ?, ?, ?);";
            String query1 = "INSERT INTO songs(ID, Title, Genre, Artist, Year, Filepath) " + "VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(query1);
            PreparedStatement prepareStat = connection.prepareStatement(query2);

            idString = Integer.toString(id);

            preparedStatement.setString(1, idString);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, genre);
            preparedStatement.setString(4, artist);
            preparedStatement.setString(5, year);
            preparedStatement.setString(6, path);

            if (alreadyInLib)
                prepareStat.setString(1, newid);
            else
                prepareStat.setString(1, idString);
            prepareStat.setString(2, title);
            prepareStat.setString(3, genre);
            prepareStat.setString(4, artist);
            prepareStat.setString(5, year);
            prepareStat.setString(6, path);

            if (!alreadyInLib)
            {
                preparedStatement.executeUpdate();
            }
            for (int i = 1; i < tables.size(); i++) {
                if (tables.get(i).isShowing()) {
                    System.out.println(playlistString);
                    prepareStat.executeUpdate();
                }
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        numRows++;
        if (!alreadyInLib && j.isShowing())
        {
            Object[] newSong = {idString, title, genre, artist, year};
            model.addRow(newSong);
	    for (int i = 1; i < tables.size(); i++)
                {
                    if(tables.get(i).isShowing())
                    {
                        DefaultTableModel a = (DefaultTableModel) tables.get(i).getModel();
                        a.addRow(newSong);
                    }
                }
        }
        else if (!alreadyInLib)
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
        int currentSelectedRow = 0;
        for (JTable table : tables) {
            if (table.isShowing()) {
                currentSelectedRow = table.getSelectedRow();
            }
        }
        try {
            for (JTable table : tables) {
                if (table.isShowing()) {
                    String deleteByID = (String) table.getValueAt(currentSelectedRow, 0);
                    String query;
                    if (j.isShowing()) {
                        query = "DELETE FROM songs WHERE ID = ?";
                    } else {
                        DefaultMutableTreeNode deleteSong = (DefaultMutableTreeNode) Objects.requireNonNull(playTree.getSelectionPath()).getLastPathComponent();
                        String delete = deleteSong.toString();
                        query = "DELETE FROM " + delete + " WHERE ID = ?";
                    }
                    PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, deleteByID);
                    preparedStmt.execute();

                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for (JTable table : tables) {
            if (table.isShowing()) {
                DefaultTableModel deleteSong = (DefaultTableModel) table.getModel();
                deleteSong.removeRow(currentSelectedRow);
            }
        }
        //model.removeRow(currentSelectedRow);
        //numRows--;

    }
    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.print(""); //convenient debugger entry spot
            if (e.getSource().equals(stop) || e.getSource().equals(stop2)) {
                try {
                    player.stop();
                    //s.setTitle("Not Playing");
                } catch (BasicPlayerException basicPlayerException) {
                    basicPlayerException.printStackTrace();
                }
            } else if (e.getSource().equals(play) || e.getSource().equals(play2)) {
                playButton();
            }

            else if(addToPlaylist.isMenuComponent((Component) e.getSource())) {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO " + e.getActionCommand() + "  (ID, Title, Artist, Genre, Year, Filepath) SELECT ID, Title, Artist, Genre, Year, Filepath FROM songs WHERE ID='" + j.getValueAt(j.getSelectedRow(), 0) + "'");
                    ps.execute();
		    Object [] newRow = {model.getValueAt(currentRow-1, 0), model.getValueAt(currentRow-1, 1), model.getValueAt(currentRow-1, 2),
                            model.getValueAt(currentRow-1, 3), model.getValueAt(currentRow-1, 4)};
                    for (int i1 = 0; i1 < playlistArray.size(); i1++)
                    {
                        if (e.getSource().equals(playlistArray.get(i1)))
                        {
                            DefaultTableModel a = (DefaultTableModel) tables.get(i1+1).getModel();
                            a.addRow(newRow);
                        }
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
                else if (e.getSource().equals(pause) || e.getSource().equals(pause2)) {
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
                Thread.currentThread().interrupt();
            } else if (e.getSource().equals(skipForward) || e.getSource().equals(skipForward2)) {
                try {
                    String query = "SELECT * FROM songs WHERE ID > " + currentSongID;
                    PreparedStatement p = connection.prepareStatement(query);
                    ResultSet r = p.executeQuery();
                    if (r.next()) {
                        currentSongID = r.getInt("ID");
                        genSet(r);
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
            } else if (e.getSource().equals(skipBack) || e.getSource().equals(skipBack2)) {
                try {
                    String query = "SELECT * FROM songs WHERE ID < " + currentSongID + " ORDER BY ID desc";
                    PreparedStatement p = connection.prepareStatement(query);
                    ResultSet r = p.executeQuery();
                    if (r.next()) {
                        String currentSongtemp = r.getString("ID");
                        currentSongID = Integer.parseInt(currentSongtemp);
                        genSet(r);
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
                boolean tableExists = false;
                try {
                    PreparedStatement getCount = connection.prepareStatement("SELECT count(*) AS count FROM information_schema.TABLES WHERE  (TABLE_NAME = '" + playlistName  + "')");
                    ResultSet r = getCount.executeQuery();
                    r.next();
                    tableExists = r.getBoolean("count");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                /*try {
                    Statement st = connection.createStatement();
                    String q = "INSERT INTO playlists (Name) Values ('" + playlistName +"')";
                    st.executeUpdate(q);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }*/
                if(!tableExists) {
                    try {
                        Statement stmt = connection.createStatement();
                        String query = "CREATE TABLE " + playlistName + " (ID VARCHAR(100), " + "Title VARCHAR(100), " +
                                "Genre VARCHAR(100), " + "Artist VARCHAR(100), " + "Year VARCHAR(100), " + "Filepath VARCHAR(100))";
                        stmt.executeUpdate(query);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    try {
                        PreparedStatement addpt = connection.prepareStatement("INSERT INTO playlists (Name) VALUES ('" + playlistName + "');");
                        addpt.execute();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                playlist.add(p);
                subPanel.add(playTree);
                defaultTreeModel.reload();

                String[] colID = {"ID", "Title", "Genre", "Artist", "Year"};
                m = new DefaultTableModel(0, 5);
                m.setColumnIdentifiers(colID);
                playlistT = new JTable(m);
                playlistT.setDropMode(DropMode.USE_SELECTION);
                tables.add(playlistT);

                pickPlaylist = new JMenuItem(playlistName);
                addToPlaylist.add(pickPlaylist);
                pickPlaylist.addActionListener(new ButtonListener());
                playlistArray.add(pickPlaylist);
               

                TreePath tpath = new TreePath(p.getPath());
                playTree.setSelectionPath(tpath);
            }
            else if (e.getSource().equals(newWindow))
            {
                System.out.println("new window");
                scrollPane2 = new JScrollPane();
                JFrame frame = new JFrame ("Playlist Window");
                frame.setPreferredSize(new Dimension(500, 500));
                frame.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
                frame.pack();
                frame.setDropTarget(new MyDropTarget());

                String[] colID = {"ID", "Title", "Genre", "Artist", "Year"};
                m = new DefaultTableModel(0, 5);
                m.setColumnIdentifiers(colID);

                JTable newWin = new JTable(m);


                DefaultMutableTreeNode openPlaylist = (DefaultMutableTreeNode) Objects.requireNonNull(playTree.getSelectionPath()).getLastPathComponent();                for(int i = 0; i < playlist.getChildCount(); i++) {
                    if (playlist.getChildAt(i).toString().equals(openPlaylist.toString())) {

                        newWin = tables.get(i + 1);
                        frame.setTitle(openPlaylist.toString());
                        newWin.setDropMode(DropMode.USE_SELECTION);
                        newWin.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        newWin.setTransferHandler(j.getTransferHandler());
                        newWin.setDropMode(DropMode.INSERT_ROWS);
                        newWin.setDragEnabled(true);
                        newWin.setFillsViewportHeight(true);

                        /*Runnable task = () -> {
                            try {
                                players.add(new StreamPlayerGUI(openPlaylist.toString()));
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        };
                        Thread t = new Thread(task);
                        threads.add(t);
                        t.start();*/
                    }
                }
                JLabel volume = new JLabel("");
                JSlider slide = new JSlider(JSlider.HORIZONTAL,0,100,10);

                slide.addChangeListener(e12 -> {
                    try {
                        player.setGain(((JSlider) e12.getSource()).getValue());
                        System.out.println("Changed gain to " + ((JSlider) e12.getSource()).getValue() * (1.4 / 100.0));
                    } catch (BasicPlayerException basicPlayerException) {
                        basicPlayerException.printStackTrace();
                    }
                    volume.setText("Value : " + ((JSlider) e12.getSource()).getValue());

                });
                newWin.setVisible(true);
                scrollPane2.add(newWin);
                scrollPane2.setViewportView(newWin);
                scrollPane2.setVisible(true);
                scrollPane2.setPreferredSize(new Dimension(475, 100));
                JPanel pane = new JPanel();
                pane.add(scrollPane2);
                pane.add(popupMenu);
                frame.setJMenuBar(menuBar);
                pane.add(skipBack2);
                pane.add(play2);
                pane.add(pause2);
                pane.add(stop2);
                pane.add(skipForward2);
                pane.add(slide);
                pane.add(volume);
                frame.add(pane);
                scrollPane2.addMouseListener(new PopClickListener());
                frame.setVisible (true);
                playTree.removeSelectionPath(playTree.getSelectionPath());
                TreePath libpath = new TreePath(library.getPath());
                libTree.setSelectionPath(libpath);
            }
            else if (e.getSource().equals(deletePlaylist))
            {
                DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) Objects.requireNonNull(playTree.getSelectionPath()).getLastPathComponent();
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
                    try {
                        Statement stmt = connection.createStatement();
                        String query = "DROP TABLE IF EXISTS " + deleteNode.toString();
                        stmt.executeUpdate(query);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    try {
                        PreparedStatement p = connection.prepareStatement("DELETE FROM playlists WHERE Name='" + deleteNode.toString() + "';");
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    try {
                        Statement stat = connection.createStatement();
                        String q = "DELETE FROM playlists WHERE Name= '" + deleteNode.toString() +"'";
                        stat.executeUpdate(q);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                }
                playTree.removeSelectionPath(playTree.getSelectionPath());
                TreePath libpath = new TreePath(library.getPath());
                libTree.setSelectionPath(libpath);
            }
        }
        private void genSet(ResultSet r) throws SQLException, BasicPlayerException {
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
    class TableRowTransferHandler extends TransferHandler {
        protected final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
        private int[] indices;
        private int addIndex = -1; // Location where items were added
        private int addCount; // Number of items added.
        private JComponent source;
        @Override protected Transferable createTransferable(JComponent c) {
            c.getRootPane().getGlassPane().setVisible(true);
            source = c;
            JTable table = (JTable) c;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            indices = table.getSelectedRows();
            @SuppressWarnings("JdkObsolete")
            List<?> transferData = Arrays.stream(indices).mapToObj(model.getDataVector()::get).collect(Collectors.toList());
            // return new DataHandler(transferData, FLAVOR.getMimeType());
            return new Transferable() {
                @Override public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] {FLAVOR};
                }

                @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return Objects.equals(FLAVOR, flavor);
                }

                @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (isDataFlavorSupported(flavor)) {
                        return transferData;
                    } else {
                        throw new UnsupportedFlavorException(flavor);
                    }
                }
            };
        }

        @Override public boolean canImport(TransferHandler.TransferSupport info) {
            boolean canDrop = info.isDrop() && info.isDataFlavorSupported(FLAVOR);
            // XXX bug? The cursor flickering problem with JTableHeader:
            // info.getComponent().setCursor(canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            Component glassPane = ((JComponent) info.getComponent()).getRootPane().getGlassPane();
            glassPane.setCursor(canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            return canDrop;
        }

        @Override public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
        }

        @Override public boolean importData(TransferHandler.TransferSupport info) {
            TransferHandler.DropLocation tdl = info.getDropLocation();
            if (!(tdl instanceof JTable.DropLocation)) {
                return false;
            }
            JTable.DropLocation dl = (JTable.DropLocation) tdl;
            JTable target = (JTable) info.getComponent();
            DefaultTableModel model = (DefaultTableModel) target.getModel();
            // boolean insert = dl.isInsert();
            /*int max = model.getRowCount();
            int index = dl.getRow();
            // index = index < 0 ? max : index; // If it is out of range, it is appended to the end
            // index = Math.min(index, max);
            index = index >= 0 && index < max ? index : max;
            addIndex = index;*/
            // target.setCursor(Cursor.getDefaultCursor());
            try {
                List<?> values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
                if (Objects.equals(source, target)) {
                    addCount = values.size();
                }
                Object[] type = new Object[0];
                for (Object o: values) {
                    //int row = index++;
                    // model.insertRow(row, (Vector<?>) o);
                    model.addRow(((List<?>) o).toArray(type));
                    //target.getSelectionModel().addSelectionInterval(row, row);
                }
                return true;
            } catch (UnsupportedFlavorException | IOException ex) {
                return false;
            }
        }

        @Override protected void exportDone(JComponent c, Transferable data, int action) {
            cleanup(c, action == TransferHandler.MOVE);
        }

        private void cleanup(JComponent c, boolean remove) {
            c.getRootPane().getGlassPane().setVisible(false);
            // c.setCursor(Cursor.getDefaultCursor());
            if (remove && Objects.nonNull(indices)) {
                DefaultTableModel model = (DefaultTableModel) ((JTable) c).getModel();
                if (addCount > 0) {
                    for (int i = 0; i < indices.length; i++) {
                        if (indices[i] >= addIndex) {
                            indices[i] += addCount;
                        }
                    }
                }
                /*for (int i = indices.length - 1; i >= 0; i--) {
                    model.removeRow(indices[i]);
                }*/
            }
            indices = null;
            addCount = 0;
            addIndex = -1;
        }
    }
    public static void main (String[]args) throws SQLException {
        threads = new ArrayList<>();
        players = new ArrayList<>();
        players.add(new StreamPlayerGUI());

    }

}