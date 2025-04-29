package com.austinramsay.javajotter.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import java.awt.Component;

public class LoadingDialog extends JFrame {

	private JPanel panel;
	private JLabel loadingLabel;
	private JProgressBar progressBar;

	public LoadingDialog() {
		super("JavaJotter");

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new EmptyBorder(0,7,0,7));

		loadingLabel = new JLabel("Loading...");
		loadingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		panel.add(Box.createVerticalGlue());
		panel.add(loadingLabel);
		panel.add(Box.createVerticalGlue());
		panel.add(progressBar);
		panel.add(Box.createVerticalGlue());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int sizeX = (int)(screenSize.width * 0.15);
		int sizeY = (int)(screenSize.height * 0.1);

		add(panel);
		setSize(sizeX, sizeY);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
