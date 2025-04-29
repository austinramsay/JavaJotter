package com.austinramsay.javajotter.gui;

import java.awt.Color;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.Action;
import javax.swing.text.StyledEditorKit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JColorChooser;

public class NoteEditToolbar extends JToolBar {

	private NoteEditPanel editPanel;

	private int incrementAmount = 1;
	private int decrementAmount = -1;

	private ArrayList<JButton> allToolbarButtons;
	private JButton fontSizeIncrButton;
	private JButton fontSizeDecrButton;
	private JButton colorButton;
	private JButton boldButton;
	private JButton italicButton;
	private JButton underlineButton;
	private JButton undoButton;
	private JButton redoButton;
	private JButton copyButton;
	private JButton cutButton;
	private JButton pasteButton;
	private JButton attachImageButton;

	private Action boldAction;
	private Action italicAction;
	private Action underlineAction;

	public NoteEditToolbar(
			NoteEditPanel editPanel,
			Action copyAction,
			Action cutAction,
			Action pasteAction,
			Action undoAction,
			Action redoAction,
			Action attachImageAction) {
		this.editPanel = editPanel;

		fontSizeIncrButton = new JButton(ResourceLoader.getScaledImageIcon("assets/font_increase_size.png"));
		fontSizeIncrButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modifyFontSize(e, incrementAmount);
			}
		});
		fontSizeIncrButton.setToolTipText("Increase Font Size");

		fontSizeDecrButton = new JButton(ResourceLoader.getScaledImageIcon("assets/font_decrease_size.png"));
		fontSizeDecrButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modifyFontSize(e, decrementAmount);
			}
		});
		fontSizeDecrButton.setToolTipText("Decrease Font Size");

		colorButton = new JButton(ResourceLoader.getScaledImageIcon("assets/color.png"));
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modifyTextColor(e);
			}
		});
		colorButton.setToolTipText("Font Foreground Color");

		boldAction = new StyledEditorKit.BoldAction();
		boldAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Bold Font");
		boldButton = new JButton(boldAction);
		boldButton.setIcon(ResourceLoader.getScaledImageIcon("assets/bold_font.png"));

		italicAction = new StyledEditorKit.ItalicAction();
		italicAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Italic Font");
		italicButton = new JButton(italicAction);
		italicButton.setIcon(ResourceLoader.getScaledImageIcon("assets/italic_font.png"));

		underlineAction = new StyledEditorKit.UnderlineAction();
		underlineAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Underlined Font");
		underlineButton = new JButton(underlineAction);
		underlineButton.setIcon(ResourceLoader.getScaledImageIcon("assets/underline.png"));

		undoButton = new JButton(undoAction);
		redoButton = new JButton(redoAction);

		copyButton = new JButton(copyAction);
		cutButton = new JButton(cutAction);
		pasteButton = new JButton(pasteAction);

		attachImageButton = new JButton(attachImageAction);

		allToolbarButtons = new ArrayList<JButton>();
		allToolbarButtons.add(fontSizeIncrButton);
		allToolbarButtons.add(fontSizeDecrButton);
		allToolbarButtons.add(colorButton);
		allToolbarButtons.add(boldButton);
		allToolbarButtons.add(italicButton);
		allToolbarButtons.add(underlineButton);
		allToolbarButtons.add(undoButton);
		allToolbarButtons.add(redoButton);
		allToolbarButtons.add(copyButton);
		allToolbarButtons.add(cutButton);
		allToolbarButtons.add(pasteButton);
		allToolbarButtons.add(attachImageButton);

		for (JButton b : allToolbarButtons) {
			b.setText(null);
			b.setFocusPainted(false);
		}

		setFloatable(false);

		add(copyButton);
		add(cutButton);
		add(pasteButton);
		addSeparator();
		add(undoButton);
		add(redoButton);
		addSeparator();
		add(fontSizeIncrButton);
		add(fontSizeDecrButton);
		add(colorButton);
		addSeparator();
		add(boldButton);
		add(italicButton);
		add(underlineButton);
		addSeparator();
		add(attachImageButton);
	}

	private void modifyFontSize(ActionEvent e, int amount) {
		editPanel.modifyFontSize(e, amount);
	}

	private void modifyTextColor(ActionEvent e) {
		Color color = JColorChooser.showDialog(copyButton, "Change Text Color", Color.BLACK);
		if (color != null)
			editPanel.modifyTextColor(e, color);
	}

	public void disableNoteEditingActions() {
		boldAction.setEnabled(false);
		italicAction.setEnabled(false);
		underlineAction.setEnabled(false);
		fontSizeIncrButton.setEnabled(false);
		fontSizeDecrButton.setEnabled(false);
		colorButton.setEnabled(false);
	}

	public void enableNoteEditingActions() {
		boldAction.setEnabled(true);
		italicAction.setEnabled(true);
		underlineAction.setEnabled(true);
		fontSizeIncrButton.setEnabled(true);
		fontSizeDecrButton.setEnabled(true);
		colorButton.setEnabled(true);
	}
}