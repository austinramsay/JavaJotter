package com.austinramsay.javajotter.gui;

import com.austinramsay.javajotter.ConfigManager;
import javax.swing.*;

public class SetupWindow extends JFrame {

	private ConfigManager confMgr;

	public SetupWindow(ConfigManager confMgr, int windowSizeX, int windowSizeY) {
		super("JavaJotter Setup");
		setSize(windowSizeX, windowSizeY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.confMgr = confMgr;

		JLabel topLabel;
	}
}
