package com.austinramsay.javajotter.gui;

import com.austinramsay.javajotter.JavaJotter;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.Action;
import javax.swing.KeyStroke;

public class CommonAction extends AbstractAction {

	private JavaJotter controller;
	private CommonActionType type;

	public CommonAction(CommonActionType type, JavaJotter controller, String text, ImageIcon icon, String desc, KeyStroke accel) {
		super(text, icon);
		putValue(Action.SHORT_DESCRIPTION, desc);
		putValue(Action.ACCELERATOR_KEY, accel);
		this.type = type;
		this.controller = controller;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (type == CommonActionType.ADD_NOTEBOOK) {
			controller.openNewNbWindow();
		} else if (type == CommonActionType.ADD_NOTE) {
			controller.openNewNoteWindow();
		} else if (type == CommonActionType.DELETE) {
			controller.deleteEntity();
		} else if (type == CommonActionType.MOVE) {
			controller.openMoveEntityWindow();
		} else if (type == CommonActionType.RENAME) {
			controller.openRenameEntityWindow();
		}
	}
}
