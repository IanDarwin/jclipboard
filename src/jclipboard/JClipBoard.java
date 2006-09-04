package jclipboard;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

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
	private final static String DIR_NAME = System.getProperty("user.home") + File.separator + DATA_DIR_NAME;
	private static final String PROPERTIES_FILE_NAME = "default.properties";
	private static final String fileName = DIR_NAME + File.separator + PROPERTIES_FILE_NAME;

	public static void main(String[] args) throws IOException {

		JFrame jf = new JFrame("jClipboard");
		JClipBoard program = new JClipBoard(jf, DIR_NAME);
		jf.getContentPane().add(program);
		jf.pack();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}

	/** The main frame */
	final JFrame jf;

	/** The list of name-value pairs loaded from disk */
	final Properties projects;

	final Map<String, JTextField> map = new HashMap<String, JTextField>();

	Preferences p = Preferences.userNodeForPackage(JClipBoard.class);

	/** Construct a JTkr, setting up the VIEW and connecting CONTROLLERs to it */
	JClipBoard(JFrame theFrame, String dirName) throws IOException {
		// this.DIR_NAME = dirName;

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
		fileMenu.add(mi = new JMenuItem("Save"));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doSave();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(jf, e1.toString(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		fileMenu.addSeparator();
		fileMenu.add(mi = new JMenuItem("Exit"));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});

		JMenu editMenu = new JMenu("Edit");
		mb.add(editMenu);
		editMenu.add(mi = new JMenuItem("New"));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newName = JOptionPane.showInputDialog(jf,
						"Field Name", "New Field", JOptionPane.QUESTION_MESSAGE);
				if (newName == null) {
					return;
				}
				if (map.containsKey(newName)) {
					JOptionPane.showMessageDialog(jf,
							String.format("Field %s already exists", newName));
					return;
				}
				addField(newName, "ENTER TEXT");
				jf.pack();
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

		projects = new FileProperties(fileName);

		List<String> keySet = new ArrayList(projects.keySet());
		Collections.sort(keySet);
		Iterator projectsIterator = keySet.iterator();
		while (projectsIterator.hasNext()) {
			String name = (String)projectsIterator.next();
			String describe = (String)projects.get(name);
			addField(name, describe);

		}
	}

	private void addField(String name, String describe) {
		JButton b = new JButton(name);
		add(b);
		JTextField copy = new JTextField(describe);
		map.put(name, copy);
		b.addActionListener(new Copier(copy));
		add(copy);
	}

	class Copier implements ActionListener {
		JTextField b;
		Copier(JTextField b) {
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

	// A sort of model...

	void doSave() throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(fileName));
			out.println("# created by JClipBoard " + new Date());
			Iterator<String> projectsIterator = map.keySet().iterator();
			while (projectsIterator.hasNext()) {
				String name = projectsIterator.next();
				out.println(name + "=" + map.get(name).getText());
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
