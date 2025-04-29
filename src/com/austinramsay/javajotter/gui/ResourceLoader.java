package com.austinramsay.javajotter.gui;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class ResourceLoader {
	public static ImageIcon getScaledImageIcon(String assetLocation) {
		URL imageURL = NoteTreeToolbar.class.getResource(assetLocation);
		ImageIcon icon = new ImageIcon(imageURL);
		Image scaledImage = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		return new ImageIcon(scaledImage);
	}
}
