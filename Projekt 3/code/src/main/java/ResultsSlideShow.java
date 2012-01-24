import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class ResultsSlideShow extends JPanel {

	private static final long serialVersionUID = 256636423170918224L;
	public static final int ROWS = 2, COLS = 2, VGAP = 10, HGAP = 10;
	private JScrollPane scrollPane;
	private JPanel imgPanel;

	public ResultsSlideShow() {
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());
		imgPanel = new JPanel();
		imgPanel.setBackground(Color.WHITE);
		scrollPane = new JScrollPane(imgPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane,BorderLayout.CENTER);
	}

	public void display(Hashtable<String, Float> images) {
		imgPanel.removeAll();
		imgPanel.setLayout(new GridLayout(images.size()/2 + 1, 2, HGAP, VGAP));
		
		for(Entry<String,Float> entry : images.entrySet()) {
			ImageIcon img = new ImageIcon(entry.getKey());
			JLabel cell = new JLabel("Distance: "+entry.getValue().toString(), img, JLabel.CENTER);
			cell.setVerticalTextPosition(JLabel.BOTTOM);
			cell.setHorizontalTextPosition(JLabel.CENTER);
			imgPanel.add(cell);
		}
	}


	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ResultsSlideShow slideshow = new ResultsSlideShow();
				JFrame frame = new JFrame();
				frame.add(slideshow);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(1024, 600);
				frame.setVisible(true);
				
				Hashtable<String, Float> imgs = new Hashtable<>();
				for (int i = 0; i < 15; i++) {
					imgs.put("image.orig/" + i + ".jpg",
							new Float(Math.random()));
				}

				slideshow.display(imgs);
			}
		});
	}

}
