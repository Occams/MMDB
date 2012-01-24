import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

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
		JScrollPane fileListScroll = new JScrollPane(fileList);
		featureList = getFeatureList();
		JScrollPane featureListScroll = new JScrollPane(featureList);
		model = new Model();
		model.addIndexCompleteListener(new Model.IndexCompleteListener() {
			@Override
			public void indexError(File file) {
				JListFileModel listmodel = (JListFileModel) fileList.getModel();
				int index = listmodel.find(file);
				FileWrapper wrapper = (FileWrapper) listmodel.get(index);
				if (wrapper != null) {
					wrapper.setState("error");
				}
				listmodel.refresh(index);
			}

			@Override
			public void indexComplete(File file, long timeTaken) {
				JListFileModel listmodel = (JListFileModel) fileList.getModel();
				int index = listmodel.find(file);
				FileWrapper wrapper = (FileWrapper) listmodel.get(index);
				if (wrapper != null) {
					wrapper.setState(String.format("%.2f s", timeTaken / 1000f));
				}
				listmodel.refresh(index);
			}
		});

		/*
		 * Add Elements
		 */
		setJMenuBar(menu);
		setLayout(new BorderLayout());
		add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileListScroll,
				featureListScroll), BorderLayout.WEST);
		add(new JLabel("Center and image preview..."), BorderLayout.CENTER);

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
		JMenuItem startindex = new JMenuItem("Start indexing");
		startindex.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						model.index(getSelectedFiles(),
								getFeatures(getSelectedFeatures()));
					}
				});
				t.start();
			}
		});

		file.add(open);
		file.add(startindex);
		bar.add(file);
		return bar;
	}

	private Feature[] getFeatures(List<Feature> features) {
		return features.toArray(new Feature[0]);
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

	private List<Feature> getSelectedFeatures() {
		Object values[] = featureList.getSelectedValues();
		List<Feature> features = new LinkedList<Feature>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof Feature) {
				features.add((Feature) values[i]);
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
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

		public int find(File file) {
			for (int i = 0; i < getSize(); i++) {
				if (getElementAt(i).equals(file)) {
					return i;
				}
			}
			return -1;
		}

		public void refresh(int index) {
			fireContentsChanged(this, index, index);
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
			return String.format(
					"%-" + (30 - f.getName().length()) + "s | %-10s",
					f.getName(), (state == null) ? "" : state).trim();
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
