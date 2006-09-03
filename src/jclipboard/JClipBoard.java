package jclipboard;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.darwinsys.swingui.UtilGUI;
import com.darwinsys.util.FileProperties;

/**
 * JClipBoard - program to display common items and copy them to
 * the system clipboard on request
 * <br/>
 * @version $Id$
 */
public class JClipBoard extends JComponent {

	private static final long serialVersionUID = 3258689901418723377L;
	public static final String DATA_DIR_NAME = ".jclipboard";
	private static final String PROPERTIES_FILE_NAME = "default.properties";

	public static void main(String[] args) throws IOException {

		JFrame jf = new JFrame("jClipboard");
		String dirName = System.getProperty("user.home") + File.separator + DATA_DIR_NAME;
		JClipBoard program = new JClipBoard(jf, dirName);
		jf.getContentPane().add(program);
		jf.pack();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}

	/** The main frame */
	final JFrame jf;

	/** The directory */
	final String dirName;

	/** The list of name-value pairs loaded from disk */
	final FileProperties projects;

	Preferences p = Preferences.userNodeForPackage(JClipBoard.class);

	/** Construct a JTkr, setting up the VIEW and connecting CONTROLLERs to it */
	JClipBoard(JFrame theFrame, String dirName) throws IOException {
		this.dirName = dirName;

		jf = theFrame;
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame's location to saved value, if set, and arrange
		// for the location to be set whenever the window is moved.
		UtilGUI.monitorWindowPosition(jf, p);

		JMenuItem mi;
		JMenuBar mb = new JMenuBar();
		jf.setJMenuBar(mb);
		JMenu fileMenu = new JMenu("File");
		mb.add(fileMenu);
		fileMenu.add(mi = new JMenuItem("Exit"));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});
		JMenu helpMenu = new JMenu("Help");
		mb.add(helpMenu);
		helpMenu.add(mi = new JMenuItem("About"));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(jf,
					"<html><font color='red'>jClipboard</font> - Ian's 'Clipper'<br>" +
					"$Id$",
					"jTkr", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		setLayout(new GridLayout(0, 2));

		projects = new FileProperties(dirName + File.separator + PROPERTIES_FILE_NAME);

		Iterator projectsIterator = projects.keySet().iterator();
		while (projectsIterator.hasNext()) {
			String name = (String)projectsIterator.next();
			JButton b = new JButton(name);
			add(b);
			String describe = (String)projects.get(name);
			JTextArea copy = new JTextArea(describe);
			copy.setEditable(false);
			b.addActionListener(new Copier(copy));
			add(copy);

		}
	}

	class Copier implements ActionListener {
		JTextArea b;
		Copier(JTextArea b) {
			this.b = b;
		}
		public void actionPerformed(ActionEvent e) {
			// System.out.println("Copier.actionPerformed()");
			b.setSelectionStart(0);
			b.setSelectionEnd(b.getText().length());
			b.copy();
		}
	}

	/**
	 * Set the saved location to the current location,
	 * but either coordinate will be set to 0 if negative.
	 */
	protected void setSavedLocation(ComponentEvent e) {
		Point where = jf.getLocation();
		int x = (int)where.getX();
		p.putInt("mainwindow.x", Math.max(0, x));
		int y = (int)where.getY();
		p.putInt("mainwindow.y", Math.max(0, y));
	}

	/**
	 * Return the saved location from Preferences.
	 * Either coordinate will be set to 0 if it is not found
	 * in Preferences or if it is less than 0 (this seems to happen
	 * with KDE or FVWM).
	 */
	protected Point getSavedLocation() {
		int x = Math.max(0, p.getInt("mainwindow.x", 0));
		int y = Math.max(0, p.getInt("mainwindow.y", 0));
		return new Point(x, y);
	}
}
