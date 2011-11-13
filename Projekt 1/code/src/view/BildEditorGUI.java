package view;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Model;
import model.NoImageLoadedException;

/**
 * GUI des Bildeditors
 */
public class BildEditorGUI extends JFrame implements Observer{

	private static final long serialVersionUID = 1L;
	
	/* Komponenten */
	private Container mainContainer;
	private JMenuBar menuBar;
	private JPanel toolBar;
	private BildPanel bildPanel;
	private JScrollPane bildScrollPane;
	
	private JTextField alphaHKField;
	private JTextField betaHKField;
	
	private JComboBox smoothingModusCombo;
	
	/* Referenz auf Model */
	private Model model;
	
	/* Main Methode des Bildeditors */
	public static void main(String[] args){
		Model model = new Model();
		BildEditorGUI gui = new BildEditorGUI(model);
		gui.run();
	}
	
	public BildEditorGUI(Model model)
	{
		this.model = model;
		model.addObserver(this);
	} 
	
	@Override
	public void update(Observable o, Object arg) {
		bildPanel.setImage(model.getImage());
		bildPanel.updateUI();
		pack();
	}
	
	/*Initialisierung der Komponenten*/
	private void run()
	{
		setTitle("*** MMDB-Bildeditor ***");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 400));
		setLocationRelativeTo(null);
		setVisible(true);
		setJMenuBar(initMenu());
		mainContainer = this.getContentPane();
		mainContainer.add(initToolBar(), BorderLayout.NORTH);
		mainContainer.add(initbildPanel(), BorderLayout.CENTER);
		pack();
	}
	
	
	/*Initialisierung vom Menü und deren Komponenten */
	private JMenuBar initMenu()
	{
		menuBar = new JMenuBar();
		// Menüs
		JMenu dateiMenu = new JMenu("Datei");
		JMenu operationMenu = new JMenu("Operation");
		JMenu hilfeMenu = new JMenu("Hilfe");
		JMenu smoothingMenu = new JMenu("Smoothing");
		
		// Menüeinträge (MenuItems)
		JMenuItem oeffnenItem = new JMenuItem("Öffnen");
		oeffnenItem.setMnemonic('ö');
		oeffnenItem.setAccelerator(
				KeyStroke.getKeyStroke('O', InputEvent.CTRL_MASK));
		oeffnenItem.addActionListener(new OeffnenListener());
		
		JMenuItem speichernItem = new JMenuItem("Speichern");
		speichernItem.setMnemonic('S');
		speichernItem.setAccelerator(
				KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
		speichernItem.addActionListener(new SpeichernListener());
		
		JMenuItem speichernUnterItem = new JMenuItem("Speichern unter");
		speichernUnterItem.setMnemonic('A');
		speichernUnterItem.setAccelerator(
				KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK));
		speichernUnterItem.addActionListener(new SpeichernUnterListener());

		JMenuItem beendenItem = new JMenuItem("Beenden");
		beendenItem.setMnemonic('X');
		beendenItem.addActionListener(new BeendenListener());
		beendenItem.setAccelerator(
				KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
		
		JMenuItem hkItem = new JMenuItem("Helligkeit/Kontrast");
		hkItem.addActionListener(new HKListener());
		
		// Smoothing Untermenü
		JMenuItem smoothingMittelItem = new JMenuItem("Mittelwert");
		JMenuItem smoothingPyramideItem = new JMenuItem("Pyramide");
		JMenuItem smoothingKonischItem = new JMenuItem("Konisch");
		smoothingMittelItem.setActionCommand("Mittelwert");
		smoothingPyramideItem.setActionCommand("Pyramide");
		smoothingKonischItem.setActionCommand("Konisch");

		smoothingMittelItem.addActionListener(new SmoothingMenuListener());
		smoothingPyramideItem.addActionListener(new SmoothingMenuListener());
		smoothingKonischItem.addActionListener(new SmoothingMenuListener());

		smoothingMenu.add(smoothingMittelItem);
		smoothingMenu.add(smoothingPyramideItem);
		smoothingMenu.add(smoothingKonischItem);

		JMenuItem grauStufenItem = new JMenuItem("Graustufen");
		grauStufenItem.addActionListener(new GrauStufenListener());
		
		JMenuItem negativItem = new JMenuItem("Negativ");
		negativItem.addActionListener(new NegativListener());
		
		JMenuItem hilfeItem = new JMenuItem("Info");
		hilfeItem.setMnemonic('I');;
		hilfeItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"<html>" +
						"<h2>Bildeditor</h2>" +
								"<p>Übung zu Multimedia-Datenbanken<br><br>" +
						"<p>Lehrstuhl für " +
						"verteilte Informationssysteme" +
						"<p>Universität Passau"+
						"<html>",
						"Über",
						JOptionPane.INFORMATION_MESSAGE);
			}});
		hilfeItem.setAccelerator(
				KeyStroke.getKeyStroke('I', InputEvent.CTRL_MASK));
		
		// Struktur der Menüs
		menuBar.add(dateiMenu);
		menuBar.add(operationMenu);
		menuBar.add(hilfeMenu);
		dateiMenu.add(oeffnenItem);
		dateiMenu.add(speichernItem);
		dateiMenu.add(speichernUnterItem);
		dateiMenu.add(beendenItem);
		operationMenu.add(hkItem);
		operationMenu.add(smoothingMenu);
		operationMenu.add(grauStufenItem);
		operationMenu.add(negativItem);
		hilfeMenu.add(hilfeItem);
		dateiMenu.setMnemonic('D');
		operationMenu.setMnemonic('O');
		hilfeMenu.setMnemonic('H');
		
		return menuBar;
	}
	
	
	/*Initialisierung von toolBar + Komponenten*/
	private JPanel initToolBar(){
		toolBar = new JPanel();
		toolBar.setPreferredSize(new Dimension(600, 50));
		
		//border 
		toolBar.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		//layout
		FlowLayout toolBarLayout = new FlowLayout();
		toolBarLayout.setAlignment(FlowLayout.LEFT);
		toolBar.setLayout(toolBarLayout);
		
		//Dimensionen der Buttons
		Dimension buttondim = new Dimension(80, 40);
		
		//Rahmen der Buttons
		Insets buttonMar = new Insets(0,0,0,0);
		
		/*---- Helligkeit/Kontrast-Bereich ----*/
		JPanel hkPanel = new JPanel();
		//Layout für hk-Bereich
		GridBagLayout hkGridBag = new GridBagLayout();
		hkPanel.setLayout(hkGridBag);
		hkPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		hkPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		GridBagConstraints hkConst = new GridBagConstraints();
		
		hkConst.gridheight = 2;
		JButton hkButton = new JButton("H/K");
		hkButton.setPreferredSize(buttondim);
		hkButton.setMargin(buttonMar);
		hkButton.addActionListener(new HKListener());
		hkGridBag.setConstraints(hkButton, hkConst);
		hkPanel.add(hkButton);
		
		hkConst.gridwidth = GridBagConstraints.REMAINDER;
		hkConst.gridheight = 1;
		alphaHKField = new JTextField(4);
		hkPanel.add(alphaHKField);
		JLabel alphaHKLabel = new JLabel("alpha: ");
		hkGridBag.setConstraints(alphaHKLabel, hkConst);
		hkPanel.add(alphaHKLabel);
		
		betaHKField = new JTextField(4);
		hkPanel.add(betaHKField);
		JLabel Label = new JLabel("beta: ");
		hkGridBag.setConstraints(Label, hkConst);
		hkPanel.add(Label);
		
		toolBar.add(hkPanel);
		
		/*---- Smoothing-Bereich ----*/
		JPanel smoothingPanel = new JPanel();
		//Layout für Smoothing-Bereich
		GridBagLayout smoothingGridBag = new GridBagLayout();
		smoothingPanel.setLayout(smoothingGridBag);
		smoothingPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		smoothingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		GridBagConstraints smoothingConst = new GridBagConstraints();
		
		smoothingConst.gridheight = 2;
		JButton smoothingButton = new JButton("Smoothing");
		smoothingButton.setPreferredSize(buttondim);
		smoothingButton.setMargin(buttonMar);
		smoothingButton.addActionListener(new SmoothingButtonListener());
		smoothingGridBag.setConstraints(smoothingButton, smoothingConst);
		smoothingPanel.add(smoothingButton);
		
		smoothingConst.gridwidth = GridBagConstraints.REMAINDER;
		smoothingConst.gridheight = 1;
		JLabel modusLabel = new JLabel("Modus");
		smoothingGridBag.setConstraints(modusLabel, smoothingConst);
		smoothingPanel.add(modusLabel);

		String smoothingModus[] = {"Mittelwert", "Pyramide", "Konisch"};
		smoothingModusCombo = new JComboBox(smoothingModus);
		smoothingGridBag.setConstraints(hkButton, hkConst);
		smoothingPanel.add(smoothingModusCombo);
		
		toolBar.add(smoothingPanel);
		
		/*---- Graustufen ----*/
		JPanel grauStufenPanel = new JPanel();
		//Layout für Graustufen-Bereich
		GridBagLayout grauStufenGridBag = new GridBagLayout();
		grauStufenPanel.setLayout(grauStufenGridBag);
		grauStufenPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		JButton grauStufenButton = new JButton("<html>Graustufen</html>");
		grauStufenButton.setPreferredSize(buttondim);
		grauStufenButton.setMargin(buttonMar);
		grauStufenButton.addActionListener(new GrauStufenListener());
		grauStufenPanel.add(grauStufenButton);
		
		toolBar.add(grauStufenPanel);
		
		/*---- Negativ-Bereich ----*/
		JPanel negativPanel = new JPanel();
		GridBagLayout negativGridBag = new GridBagLayout();
		negativPanel.setLayout(negativGridBag);
		negativPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		JButton negativButton = new JButton("Negativ");
		negativButton.setPreferredSize(buttondim);
		negativButton.setMargin(buttonMar);
		negativButton.addActionListener(new NegativListener());
		negativPanel.add(negativButton);
		
		toolBar.add(negativPanel);
		
		return toolBar;
	}
	
	//Initialisierung des Bild-Panels (Anzeigebereich des Bildes) 
	private JScrollPane initbildPanel(){
		bildPanel = new BildPanel();
		bildScrollPane = new JScrollPane(bildPanel);
		bildScrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		bildScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		return bildScrollPane;
	}
	

	/**
	 * ActionListener für Event "Speichern unter"
	 */
	class SpeichernUnterListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser speichernChooser = new JFileChooser();
			speichernChooser.setDialogTitle("Bild speichern unter ...");
			speichernChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			speichernChooser.setAcceptAllFileFilterUsed(false);
			speichernChooser.setFileFilter(
	        		new FileNameExtensionFilter("TIFF File | (tiff)", "tiff"));
			speichernChooser.setFileFilter(
	        		new FileNameExtensionFilter("PNG File | (png)", "png"));
			speichernChooser.setFileFilter(
	        		new FileNameExtensionFilter("JPEG File | (jpeg)", "jpeg"));
	        
			if (speichernChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
				if(speichernChooser.getFileFilter().accept(
						speichernChooser.getSelectedFile())){
					try{
						try {
							model.saveImage(speichernChooser.getSelectedFile());
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null,
									"Die Datei konnte nicht gespeichert werden.",
									"Error Message",
									JOptionPane.ERROR_MESSAGE);
						}
					} catch(IllegalArgumentException e){
						JOptionPane.showMessageDialog(null,
								"Die Datei konnte nicht gespeichert werden.",
								"Error Message",
								JOptionPane.ERROR_MESSAGE);
					}	
				} else {
					JOptionPane.showMessageDialog(null,
							"Ungültige Datei! Dateiendung bitte überprüfen.",
							"Error Message",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				speichernChooser.cancelSelection();
			}
		}
	}

	
	/**
	 * ActionListener für Event "Speichern"
	 */
	class SpeichernListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				model.saveImage();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,
						"Die Datei konnte nicht gespeichert werden.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	/**
	 * ActionListener für Event "Öffnen"
	 */
	class OeffnenListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Bilddatei wählen ...");
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(new ImageFilter());
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				if(chooser.getFileFilter().accept(chooser.getSelectedFile())){
					try {
					model.readImage(chooser.getSelectedFile());
					} catch(IOException e){
						JOptionPane.showMessageDialog(null,
								"Die Datei konnte nicht gelesen werden.",
								"Error Message",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"Ungültige Datei! Dateiendung bitte überprüfen.",
							"Error Message",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				chooser.cancelSelection();
			}
				
		}
	}
	
	
	/**
	 * ActionListener für Event "Beenden"
	 */
	class BeendenListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.exit(0);
		}
	}
	
	
	/**
	 * ActionListener für die "H/K" Operation
	 */
	class HKListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				int newAlpha = Integer.parseInt(alphaHKField.getText());
				int newBeta = Integer.parseInt(betaHKField.getText());
				model.hkImage(newAlpha, newBeta);
			} catch (NoImageLoadedException e) {
				JOptionPane.showMessageDialog(null,
						"Kein Bild geladen.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			} catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null,
						"Ungültige Argumente! Nur Integers erlaubt.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	

	/**
	 * ActionListener für Smoothing-Operation (vom Button aufgerufen)
	 */
	class SmoothingButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				model.smootheImage((String)smoothingModusCombo.getSelectedItem());
			} catch (NoImageLoadedException e) {
				JOptionPane.showMessageDialog(null,
						"Kein Bild geladen.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			} 
		}
	}
	
	
	/**
	 * ActionListener für Smoothing-Operation (vom Menü aufgerufen)	 
	 */
	class SmoothingMenuListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				model.smootheImage(arg0.getActionCommand());
			} catch (NoImageLoadedException e) {
				JOptionPane.showMessageDialog(null,
						"Kein Bild geladen.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			} 
		}
	}
	

	/**
	 * ActionListener für Graustufen-Operation
	 */
	class GrauStufenListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				model.transformImageToGreyScale();
			} catch (NoImageLoadedException e) {
				JOptionPane.showMessageDialog(null,
						"Kein Bild geladen.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	/**
	 * ActionListener für Negativ-Operation 
	 */
	class NegativListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				model.negativImage();
			} catch (NoImageLoadedException ex) {
				JOptionPane.showMessageDialog(null,
						"Kein Bild geladen.",
						"Error Message",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
		
	
	/**
	 * Hilfeklasse für Dateiendungen 
	 */
	class ImageFilter extends FileFilter {
		
	    public boolean accept(File f) {
	    	boolean accepted = false;
	        if (f.isDirectory()) {
	        	accepted = true;
	        } else {
		        String extension = model.getExtension(f);
		        if (extension != null) {
		            if (extension.equals("jpeg") ||
		                extension.equals("jpg") ||
		                extension.equals("png") ||
		                extension.equals("tiff"))
		            {
		            	accepted = true;
		            }
		        }
	        }
	        return accepted;
	    }
	    
		@Override
		public String getDescription() {
			return "Nur Bilddateien | (jpeg, jpg, png, tiff)";
		}
	}
	
}
