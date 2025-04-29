package com.austinramsay.javajotter.gui;

import javax.swing.JToolBar;
import javax.swing.JButton;
import java.util.ArrayList;

public class NoteTreeToolbar extends JToolBar {

	private ArrayList<JButton> allToolbarButtons;
	private JButton addNotebookButton;
	private JButton addNoteButton;
	private JButton deleteButton;
	private JButton moveButton;
	private JButton renameButton;

	public NoteTreeToolbar(
			CommonAction addNbAction,
			CommonAction addNoteAction,
			CommonAction deleteAction,
			CommonAction moveAction,
			CommonAction renameAction) {

		addNotebookButton = new JButton(addNbAction);
		addNoteButton = new JButton(addNoteAction);
		deleteButton = new JButton(deleteAction);
		moveButton = new JButton(moveAction);
		renameButton = new JButton(renameAction);

		allToolbarButtons = new ArrayList<JButton>();
		allToolbarButtons.add(addNotebookButton);
		allToolbarButtons.add(addNoteButton);
		allToolbarButtons.add(deleteButton);
		allToolbarButtons.add(moveButton);
		allToolbarButtons.add(renameButton);

		for (JButton b : allToolbarButtons) {
			b.setText(null);
			b.setFocusPainted(false);
			add(b);
		}

		setFloatable(false);
	}
}
