package com.austinramsay.javajotter.gui;

import com.austinramsay.javajotter.JavaJotter;
import javax.swing.*;
import java.awt.event.*;

public class MainMenuBar extends JMenuBar {

	private JavaJotter controller;

	private JMenu fileMenu;
	private JMenuItem exportMenuItem;
	private JMenuItem importMenuItem;
	private JMenuItem exitMenuItem;

	private JMenu editMenu;
	private JMenuItem attachImageMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem cutMenuItem;
	private JMenuItem pasteMenuItem;
	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem searchMenuItem;

	private JMenu actionsMenu;
	private JMenuItem syncMenuItem;
	private JMenuItem newNbMenuItem;
	private JMenuItem newNoteMenuItem;
	private JMenuItem moveMenuItem;
	private JMenuItem renameMenuItem;
	private JMenuItem deleteMenuItem;

	private JMenu optionsMenu;
	private JMenuItem serverOptionsMenuItem;

	private JMenu helpMenu;
	private JMenuItem helpMenuItem;
	private JMenuItem aboutMenuItem;

    public MainMenuBar(
			JavaJotter controller,
			CommonAction addNbAction,
			CommonAction addNoteAction,
			CommonAction deleteAction,
			CommonAction moveAction,
			CommonAction renameAction,
			Action undoAction,
			Action redoAction,
			Action copyAction,
			Action cutAction,
			Action pasteAction,
			Action attachImageAction) {

		this.controller = controller;

        fileMenu = new JMenu("File");
        exportMenuItem = new JMenuItem("Export..");
        importMenuItem = new JMenuItem("Import..");
        exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestExit();
			}
		});
        fileMenu.add(exportMenuItem);
        fileMenu.add(importMenuItem);
		fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

		editMenu = new JMenu("Edit");
		attachImageMenuItem = new JMenuItem(attachImageAction);
		attachImageMenuItem.setIcon(null);
		copyMenuItem = new JMenuItem(copyAction);
		copyMenuItem.setIcon(null);
		cutMenuItem = new JMenuItem(cutAction);
		cutMenuItem.setIcon(null);
		pasteMenuItem = new JMenuItem(pasteAction);
		pasteMenuItem.setIcon(null);
		undoMenuItem = new JMenuItem(undoAction);
		undoMenuItem.setIcon(null);
		redoMenuItem = new JMenuItem(redoAction);
		redoMenuItem.setIcon(null);
		searchMenuItem = new JMenuItem("Search..");
		editMenu.add(attachImageMenuItem);
		editMenu.addSeparator();
		editMenu.add(copyMenuItem);
		editMenu.add(cutMenuItem);
		editMenu.add(pasteMenuItem);
		editMenu.addSeparator();
		editMenu.add(undoMenuItem);
		editMenu.add(redoMenuItem);
		editMenu.addSeparator();
		editMenu.add(searchMenuItem);

        actionsMenu = new JMenu("Actions");
        syncMenuItem = new JMenuItem("Sync with Server");
        newNbMenuItem = new JMenuItem(addNbAction);
        newNoteMenuItem = new JMenuItem(addNoteAction);
		moveMenuItem = new JMenuItem(moveAction);
		renameMenuItem = new JMenuItem(renameAction);
        deleteMenuItem = new JMenuItem(deleteAction);
		newNbMenuItem.setIcon(null);
		newNoteMenuItem.setIcon(null);
		moveMenuItem.setIcon(null);
		renameMenuItem.setIcon(null);
		deleteMenuItem.setIcon(null);
        actionsMenu.add(syncMenuItem);
		actionsMenu.addSeparator();
        actionsMenu.add(newNbMenuItem);
        actionsMenu.add(newNoteMenuItem);
		actionsMenu.addSeparator();
		actionsMenu.add(moveMenuItem);
		actionsMenu.addSeparator();
		actionsMenu.add(renameMenuItem);
		actionsMenu.addSeparator();
        actionsMenu.add(deleteMenuItem);

		syncMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				syncNow();
			}
		});

        optionsMenu = new JMenu("Options");
        serverOptionsMenuItem = new JMenuItem("Configuration");
        optionsMenu.add(serverOptionsMenuItem);

        helpMenu = new JMenu("Help");
        helpMenuItem = new JMenuItem("Help..");
        aboutMenuItem = new JMenuItem("About JavaJotter");
        helpMenu.add(helpMenuItem);
        helpMenu.add(aboutMenuItem);

        add(fileMenu);
		add(editMenu);
        add(actionsMenu);
        add(optionsMenu);
        add(helpMenu);
    }

	private void syncNow() {
		controller.syncNow();
	}

	private void requestExit() {
		controller.exit();
	}
}