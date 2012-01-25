import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

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
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void display(Hashtable<String, Float> images) {
		if (images != null) {
			imgPanel.removeAll();
			imgPanel.setLayout(new GridLayout(images.size() / 2 + 1, 2, HGAP,
					VGAP));

			List<Entry<String, Float>> list = new LinkedList<Entry<String, Float>>(
					images.entrySet());
			Collections.sort(list, new Comparator<Entry<String, Float>>() {

				@Override
				public int compare(Entry<String, Float> o1,
						Entry<String, Float> o2) {
					return (int) - Math.signum(o1.getValue() - o2.getValue());
				}
			});

			for (Entry<String, Float> entry : list) {
				System.out.println(entry.getValue());
				ImageIcon img = scale(new ImageIcon(entry.getKey()).getImage(),300,200);
				JLabel cell = new JLabel(new File(entry.getKey()).getName()+" - Score: "
						+ entry.getValue().toString(), img, JLabel.CENTER);
				cell.setVerticalTextPosition(JLabel.BOTTOM);
				cell.setHorizontalTextPosition(JLabel.CENTER);
				imgPanel.add(cell);
			}
			imgPanel.validate();
		}
	}
	
	 private ImageIcon scale(Image src, int maxwidth, int maxheight) {
	        float scaleW = (float) maxwidth / (float)src.getWidth(this);
	        float scaleH =(float) maxheight / (float)src.getHeight(this);
	        float scale = Math.min(scaleW, scaleH);
	        int w = (int)(scale*src.getWidth(this));
	        int h = (int)(scale*src.getHeight(this));
	        int type = BufferedImage.TYPE_INT_RGB;
	        BufferedImage dst = new BufferedImage(w, h, type);
	        Graphics2D g2 = dst.createGraphics();
	        g2.drawImage(src, 0, 0, w, h, this);
	        g2.dispose();
	        return new ImageIcon(dst);
	    }

}
