package com.austinramsay.javajotter.gui;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class StatusBarPanel extends JPanel {

	private final String connStatusStr = "Connected to server: ";
    private final String connSyncStatusStr = "Last sync time: ";
	private JLabel connToServerLabel;
	private JLabel syncTimeLabel;
	private JLabel statusLabel;

	public StatusBarPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		connToServerLabel = new JLabel();
		syncTimeLabel = new JLabel();
		statusLabel = new JLabel();
		add(connToServerLabel);
		add(Box.createGlue());
		add(statusLabel);
		add(Box.createGlue());
		add(syncTimeLabel);
		setBorder(new EmptyBorder(0,5,3,5));
	}

    public void updateServerName(String serverName) {
        connToServerLabel.setText(connStatusStr + serverName);
    }

	public void updateLastSyncTime(String time) {
		syncTimeLabel.setText(connSyncStatusStr + time);
	}

	public void updateStatus(String status) {
		statusLabel.setText(status);
	}
}