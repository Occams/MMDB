import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import net.semanticmetadata.lire.DocumentBuilder;

public class Gui extends JFrame {
	private static final long serialVersionUID = 301471414258988044L;

	

	private JMenuBar menu;
	private JFileChooser fileChooser;
	private JList fileList;
	private JList featureList;
	private List<String> results;
	private Model model;

	public Gui() {
		super("StillImage Indexer And Retrieval");

		/*
		 * Create elements
		 */
		menu = getMenu();
		fileChooser = getFileChooser();
		fileList = getFileList();
		featureList = getFeatureList();
		model = new Model();
		model.addIndexCompleteListener(new Model.IndexCompleteListener() {
			@Override
			public void indexError(File file) {
				JListFileModel listmodel = (JListFileModel) fileList.getModel();
				FileWrapper wrapper = listmodel.find(file);
				if (wrapper != null) {
					wrapper.setState("error");
				}
			}

			@Override
			public void indexComplete(File file, long timeTaken) {
				JListFileModel listmodel = (JListFileModel) fileList.getModel();
				FileWrapper wrapper = listmodel.find(file);
				if (wrapper != null) {
					wrapper.setState("ix, " + timeTaken / 1000 + "s");
				}
			}
		});

		/*
		 * Add Elements
		 */
		setLayout(new BorderLayout());
		setJMenuBar(menu);
		add(fileList, BorderLayout.WEST);
		add(featureList, BorderLayout.EAST);
		add(getStartIndexButton(), BorderLayout.SOUTH);

		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	private JMenuBar getMenu() {
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int state = fileChooser.showOpenDialog(Gui.this);
				if (state == JFileChooser.APPROVE_OPTION) {
					addFiles(fileChooser.getSelectedFiles());
				}
			}
		});

		file.add(open);
		bar.add(file);
		return bar;
	}
	
	private Feature[] getFeatures(List<String> features) {
		Feature[] fs = new Feature[features.size()];
		
		for(int i = 0; i < fs.length; i++) {
			fs[i] = Feature.valueOf(features.get(i));
		}
		
		return fs;
	}

	private JButton getStartIndexButton() {
		JButton button = new JButton("Start indexing");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				model.index(getSelectedFiles(), getFeatures(getSelectedFeatures()));
			}
		});
		return button;
	}

	private List<File> getSelectedFiles() {
		Object values[] = fileList.getSelectedValues();
		List<File> files = new LinkedList<File>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof FileWrapper) {
				files.add(((FileWrapper) values[i]).getFile());
			}
		}
		return files;
	}

	private List<String> getSelectedFeatures() {
		Object values[] = featureList.getSelectedValues();
		List<String> features = new LinkedList<String>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof String) {
				features.add((String) values[i]);
			}
		}
		return features;
	}

	private JFileChooser getFileChooser() {
		JFileChooser chooser = new JFileChooser(new File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		return chooser;
	}

	private JList getFileList() {
		JList list = new JList(new JListFileModel());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		return list;
	}

	private JList getFeatureList() {
		JList list = new JList(Feature.values());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return list;
	}

	private void addFiles(File[] files) {
		JListFileModel listmodel = (JListFileModel) fileList.getModel();
		listmodel.clear();
		for (File f : files)
			listmodel.addElement(new FileWrapper(f));
	}

	private class JListFileModel extends DefaultListModel {
		private static final long serialVersionUID = 1119906040732840566L;

		public FileWrapper find(File file) {
			for (int i = 0; i < getSize(); i++) {
				if (getElementAt(i).equals(file)) {
					return (FileWrapper) getElementAt(i);
				}
			}
			return null;
		}
	}

	private class FileWrapper {
		private File f;
		private String state;

		public FileWrapper(File file) {
			f = file;
		}

		public void setState(String state) {
			this.state = state;
		}

		public File getFile() {
			return f;
		}

		public String toString() {
			String s = f.getName();
			if (state != null)
				s = "(" + state + ") " + s;
			return s;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof FileWrapper) {
				return f.equals(((FileWrapper) arg0).f);
			} else if (arg0 instanceof File) {
				return f.equals(arg0);
			} else {
				return false;
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Gui();
			}
		});
	}

}
