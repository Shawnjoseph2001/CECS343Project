package javazoom.jlgui.basicplayer;

import javax.swing.*;

public class PopupMenu extends JPopupMenu
{
    JMenuItem addSong;
    JMenuItem deleteSong;

    public PopupMenu()
    {
        addSong = new JMenuItem("Add song to database");
        deleteSong = new JMenuItem("Delete song from database");
    }
}
