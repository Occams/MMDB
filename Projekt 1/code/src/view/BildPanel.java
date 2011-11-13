package view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class BildPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private BufferedImage bild;
	
	public void setImage(BufferedImage bild){
		this.bild = bild;
		this.setPreferredSize(
				new Dimension(bild.getWidth(), bild.getHeight()));
	}
    
    @Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bild, 0, 0, this);
    }

}
