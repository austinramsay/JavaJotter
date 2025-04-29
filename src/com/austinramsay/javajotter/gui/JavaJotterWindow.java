package com.austinramsay.javajotter.gui;

import com.austinramsay.javajotter.JavaJotter;
import com.austinramsay.javajotter.ContentManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.Action;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import javax.swing.undo.UndoManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.text.StyledEditorKit;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JavaJotterWindow extends JFrame {

	private JavaJotter controller;

    private NoteTreePanel treePanel;
	private NoteTreeToolbar treeToolbar;
	private NoteEditPanel editPanel;
	private NoteEditToolbar editToolbar;
	private MainMenuBar menuBar;
	private StatusBarPanel statusPanel;

	private CommonAction addNotebookAction;
	private CommonAction addNoteAction;
	private CommonAction deleteAction;
	private CommonAction renameAction;
	private CommonAction moveAction;

	private UndoManager undoMgr = new UndoManager();
	private UndoAction undoAction;
	private RedoAction redoAction;

	private Action attachImageAction;
	private Action copyAction;
	private Action cutAction;
	private Action pasteAction;

	public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public JavaJotterWindow(
            JavaJotter controller) {
        super("JavaJotter Notes");
		this.controller = controller;

		// If user clicks X to close window, request exit from controller
		// Will force sync between server before closing and save all note content
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				requestExit();
			}
		});

		// Initialize shared actions between the tree toolbar and the actions menu
		initSharedActions();

		statusPanel = new StatusBarPanel();
		treePanel = new NoteTreePanel(controller);
		treeToolbar = new NoteTreeToolbar(
				addNotebookAction,
				addNoteAction,
				deleteAction,
				moveAction,
				renameAction);
		editPanel = new NoteEditPanel(
				controller,
				statusPanel,
				new EditPanelUndoableEditListener(undoAction, redoAction));
		editToolbar = new NoteEditToolbar(
				editPanel,
				copyAction,
				cutAction,
				pasteAction,
				undoAction,
				redoAction,
				attachImageAction);
		menuBar = new MainMenuBar(
				controller,
				addNotebookAction,
				addNoteAction,
				deleteAction,
				moveAction,
				renameAction,
				undoAction,
				redoAction,
				copyAction,
				cutAction,
				pasteAction,
				attachImageAction);

		JPanel treeSide = new JPanel();
		treeSide.setLayout(new BorderLayout());
		treeSide.add(treeToolbar, BorderLayout.NORTH);
		treeSide.add(treePanel, BorderLayout.CENTER);

		JPanel noteSide = new JPanel();
		noteSide.setLayout(new BorderLayout());
		noteSide.add(editToolbar, BorderLayout.NORTH);
		noteSide.add(editPanel, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeSide, noteSide);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int sizeX = (int)(screenSize.width * 0.56);
		int sizeY = (int)(screenSize.height * 0.5);

		setLayout(new BorderLayout());
		add(menuBar, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(statusPanel, BorderLayout.SOUTH);

		setSize(sizeX, sizeY);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Disable unusable actions without any initial content selected
		noContentSelected();
    }

	class EditPanelUndoableEditListener implements UndoableEditListener {
		private UndoAction undoAction;
		private RedoAction redoAction;

		public EditPanelUndoableEditListener(UndoAction undoAction, RedoAction redoAction) {
			this.undoAction = undoAction;
			this.redoAction = redoAction;
		}

		public void undoableEditHappened(UndoableEditEvent e) {
			undoMgr.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	}

	class UndoAction extends AbstractAction {

		public UndoAction(String text, ImageIcon icon, String desc, KeyStroke accel) {
			super(text, icon);
			super.putValue(Action.SHORT_DESCRIPTION, desc);
			super.putValue(Action.ACCELERATOR_KEY, accel);
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undoMgr.undo();
			} catch (CannotUndoException ex) {
				ex.printStackTrace();
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		public void updateUndoState() {
			if (undoMgr.canUndo()) {
				setEnabled(true);
			} else {
				super.setEnabled(false);
			}
		}
	}

	class AttachImageAction extends AbstractAction {

		private JFrame parent;

		public AttachImageAction(JFrame parent, String text, ImageIcon icon, String desc, KeyStroke accel) {
			super(text, icon);
			super.putValue(Action.SHORT_DESCRIPTION, desc);
			super.putValue(Action.ACCELERATOR_KEY, accel);
			this.parent = parent;
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"PNG Images", "png");
				fileChooser.setFileFilter(filter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int choice = fileChooser.showOpenDialog(parent);
				if (choice == JFileChooser.APPROVE_OPTION) {
					controller.attachImage(fileChooser.getSelectedFile());
				}
			} catch (Exception ex) {
				System.out.println("Failed to attach image.");
				ex.printStackTrace();
			}
		}
	}

	class RedoAction extends AbstractAction {

		public RedoAction(String text, ImageIcon icon, String desc, KeyStroke accel) {
			super(text, icon);
			super.putValue(Action.SHORT_DESCRIPTION, desc);
			super.putValue(Action.ACCELERATOR_KEY, accel);
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undoMgr.redo();
			} catch (CannotRedoException ex) {
				ex.printStackTrace();
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		public void updateRedoState() {
			if (undoMgr.canRedo()) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}
	}

	public void initSharedActions() {
		addNotebookAction = new CommonAction(
				CommonActionType.ADD_NOTEBOOK,
				controller,
				"Add Notebook",
				ResourceLoader.getScaledImageIcon("assets/add_notebook.png"),
				"Create a new notebook",
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

		addNoteAction = new CommonAction(
				CommonActionType.ADD_NOTE,
				controller,
				"Add Note",
				ResourceLoader.getScaledImageIcon("assets/add_note.png"),
				"Create a new note",
				KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

		deleteAction = new CommonAction(
				CommonActionType.DELETE,
				controller,
				"Delete",
				ResourceLoader.getScaledImageIcon("assets/delete.png"),
				"Delete",
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));

		renameAction = new CommonAction(
				CommonActionType.RENAME,
				controller,
				"Rename",
				ResourceLoader.getScaledImageIcon("assets/rename.png"),
				"Rename",
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));

		moveAction = new CommonAction(
				CommonActionType.MOVE,
				controller,
				"Move to..",
				ResourceLoader.getScaledImageIcon("assets/move.png"),
				"Move to..",
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));

		undoAction = new UndoAction(
				"Undo",
				ResourceLoader.getScaledImageIcon("assets/undo.png"),
				"Undo",
				KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));

		redoAction = new RedoAction(
				"Redo",
				ResourceLoader.getScaledImageIcon("assets/redo.png"),
				"Redo",
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));

		attachImageAction = new AttachImageAction(
				this,
				"Attach Image..",
				ResourceLoader.getScaledImageIcon("assets/attach_image.png"),
				"Attach Image",
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));

		copyAction = new StyledEditorKit.CopyAction();
		copyAction.putValue(Action.NAME, "Copy");
		copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy");
		copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		copyAction.putValue(Action.SMALL_ICON, ResourceLoader.getScaledImageIcon("assets/copy.png"));

		cutAction = new StyledEditorKit.CutAction();
		cutAction.putValue(Action.NAME, "Cut");
		cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut");
		cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		cutAction.putValue(Action.SMALL_ICON, ResourceLoader.getScaledImageIcon("assets/cut.png"));

		pasteAction = new StyledEditorKit.PasteAction();
		pasteAction.putValue(Action.NAME, "Paste");
		pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste");
		pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		pasteAction.putValue(Action.SMALL_ICON, ResourceLoader.getScaledImageIcon("assets/paste.png"));
	}

	public NoteEditPanel getEditPanel() {
		return editPanel;
	}

	public NoteTreePanel getTreePanel() {
		return treePanel;
	}

	/*public void updateStatus(String status) {
		statusPanel.updateStatus(status);
	}

	public void clearStatus() {
		statusPanel.clearStatus();
	}*/

    public void updateServerName(String serverName) {
		statusPanel.updateServerName(serverName);
    }

	public void updateLastSyncTime(String time) {
		statusPanel.updateLastSyncTime(time);
	}

	public void notebookSelected() {
		addNotebookAction.setEnabled(true);
		addNoteAction.setEnabled(true);
		deleteAction.setEnabled(true);
		renameAction.setEnabled(true);
		moveAction.setEnabled(true);
		disableNoteEditingActions();
	}

	public void noteSelected() {
		addNotebookAction.setEnabled(false);
		addNoteAction.setEnabled(false);
		deleteAction.setEnabled(true);
		renameAction.setEnabled(true);
		moveAction.setEnabled(true);
		enableNoteEditingActions();
	}

	public void noContentSelected() {
		addNotebookAction.setEnabled(true);
		addNoteAction.setEnabled(false);
		deleteAction.setEnabled(false);
		renameAction.setEnabled(false);
		moveAction.setEnabled(false);
		disableNoteEditingActions();
	}

	private void disableNoteEditingActions() {
		copyAction.setEnabled(false);
		cutAction.setEnabled(false);
		pasteAction.setEnabled(false);
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
		attachImageAction.setEnabled(false);
		editToolbar.disableNoteEditingActions();
	}

	private void enableNoteEditingActions() {
		copyAction.setEnabled(true);
		cutAction.setEnabled(true);
		pasteAction.setEnabled(true);
		undoAction.setEnabled(true);
		redoAction.setEnabled(true);
		attachImageAction.setEnabled(true);
		editToolbar.enableNoteEditingActions();
	}

	public void requestExit() {
		controller.exit();
	}
}
