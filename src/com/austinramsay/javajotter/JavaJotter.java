package com.austinramsay.javajotter;

import com.austinramsay.javajotter.gui.NewNbWindow;
import com.austinramsay.javajotter.gui.NewNoteWindow;
import com.austinramsay.javajotter.gui.JavaJotterWindow;
import com.austinramsay.javajotter.gui.LoginWindow;
import com.austinramsay.javajotter.gui.MoveEntityWindow;
import com.austinramsay.javajotter.gui.RenameEntityWindow;
import com.austinramsay.javajotterlibrary.*;
import javax.swing.JOptionPane;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
import java.net.URL;

public class JavaJotter {

	private NetworkManager netMgr;
	private ConfigManager configMgr;
	private Synchronizer synchronizer;
	private ContentManager cntMgr;

    private JavaJotterWindow win;
	private LoginWindow loginWin;

	// User and server information variables
	private String username;
	private String serverAddress;
	private String lastSyncTime;
	private boolean isConnected = false;

	private enum AuthReason { EXISTING_CONNECTION, INVALID_CREDENTIALS };

    public JavaJotter() {
        win = new JavaJotterWindow(this);

		// TODO: implement config manager
		configMgr = new ConfigManager();
		if (configMgr.loadConfig()) {
			System.out.println("Config found!");
		} else {
			// Config not found, run setup
		}
		netMgr = new NetworkManager(this);
		cntMgr = new ContentManager(this, win.getEditPanel());

		loginWin = new LoginWindow(this);
		loginWin.setVisible(true);
    }

	public ContentManager getContentManager() {
		return cntMgr;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void login(String username, char[] password, String serverAddress, String serverPort) {
		loginWin.loginButtonEnabled(false);

		int serverPortInt;

		if (username.length() < 1 || password.length < 1) {
			JOptionPane.showMessageDialog(loginWin, "Username and password cannot be left blank.", "JavaJotter Login", JOptionPane.ERROR_MESSAGE);
			loginWin.loginButtonEnabled(true);
			return;
		}

		try {
			if (serverAddress.length() < 1) {
				throw new Exception();
			}
			serverPortInt = Integer.parseInt(serverPort);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(loginWin, "Check formatting of server properties.", "JavaJotter Login", JOptionPane.ERROR_MESSAGE);
			loginWin.loginButtonEnabled(true);
			return;
		}

		// Attempt to connect to the server and establish a socket connection
		if (!netMgr.establishConnection(serverAddress, serverPortInt)) {
			JOptionPane.showMessageDialog(loginWin, "Could not establish a connection to the server.", "JavaJotter Login", JOptionPane.ERROR_MESSAGE);
			loginWin.loginButtonEnabled(true);
			return;
		}

		// Send authentication info to the server
		netMgr.send(Builder.buildAuthenticator(username, password, serverAddress));

		// Await callback from processing returned authenticator form
	}

	private void loginSuccessful(String username, String serverAddress) {
		// If all went well, set variables
		this.username = username;
		this.serverAddress = serverAddress;
		isConnected = true;

		win.updateServerName(serverAddress);

		// Begin background synchronizer thread (checks in with server at intervals)
		synchronizer = new Synchronizer(this, cntMgr, netMgr);
		cntMgr.startNoteSweeper();

		loginWin.dispose();
		win.setVisible(true);
	}

	private void loginUnsuccessful(AuthReason resp) {
		if (resp == AuthReason.INVALID_CREDENTIALS) {
			JOptionPane.showMessageDialog(loginWin, "The username or password was incorrect.", "JavaJotter Login", JOptionPane.ERROR_MESSAGE);
		} else if (resp == AuthReason.EXISTING_CONNECTION) {
			JOptionPane.showMessageDialog(loginWin, "There's already an existing session for this user.", "JavaJotter Login", JOptionPane.ERROR_MESSAGE);
		}
		loginWin.loginButtonEnabled(true);
	}

	public void process(Object recvObj) {
		if (recvObj instanceof Authenticator) {
			// Check if successful
			Authenticator auth = (Authenticator) recvObj;
			if (auth.hasExistingConnection()) {
				loginUnsuccessful(AuthReason.EXISTING_CONNECTION);
			} else if (auth.isAuthenticated()) {
				loginSuccessful(auth.getUsername(), auth.getServerAddress());
			} else {
				loginUnsuccessful(AuthReason.INVALID_CREDENTIALS);
			}
		} else if (recvObj instanceof ContentPackage) {
			ContentPackage content = (ContentPackage) recvObj;
			populate(content);
		} else {
			System.out.println("Unknown or unwanted object received.");
		}
	}

	public void syncNow() {
		if (synchronizer != null) {
			synchronizer.diffCheck();
		}
	}

	public String getLastSyncTime() {
		return lastSyncTime;
	}

	public void updateLastSyncTime(String time) {
		lastSyncTime = time;
		win.updateLastSyncTime(time);
	}

	/*
	 * Retrieve the user's notebooks, notes, correlate them and populate into
	 * the UI.
	 */
    private void populate(ContentPackage content) {
		updateLastSyncTime(JavaJotterWindow.timeFormat.format(new Date()));

		// TODO: do we need to have getters/setters for this, and is this even needed?
		cntMgr.setLocalNotebooks(content.getNotebooks());
		cntMgr.setLocalNotes(content.getNotes());
		cntMgr.setNextNbId(content.getLastNbId());
		cntMgr.setNextNoteId(content.getLastNoteId());

		buildTree();

		// Now that content is populated, begin the synchronizer task which
		// will continuously monitor for updated content, then push it to the
		// server when changes are detected.
		synchronizer.updateReferenceHashes(cntMgr.getLocalNotebooks(), cntMgr.getLocalNotes());
		synchronizer.startSynchronizer();
    }

	private void buildTree() {
		buildTree(false);
	}

	public void rebuildTree() {
		buildTree(true);
	}

	private void buildTree(boolean clearExistingChildren) {
		if (clearExistingChildren)
			win.getTreePanel().clearTree();

		ArrayList<Notebook> nbQueue = new ArrayList();
		HashMap<Integer, Integer> nbParentIdMap = new HashMap();

		for (Notebook nb : cntMgr.getLocalNotebooks()) {
			nbQueue.add(nb);
			if (nb.getParentId() != null) {
				nbParentIdMap.put(nb.getId(), nb.getParentId());
			}
		}

		// Algorithm to determine order of adding notebooks
		ArrayList<Integer> processedNotebooks = new ArrayList();

		// This will consist of independent (first level) notebooks
		for (Notebook testNotebook : cntMgr.getLocalNotebooks()) {
			// Check if notebook is independent (it doesn't have a parent ID)
			if (!nbParentIdMap.containsKey(testNotebook.getId())) {
				win.getTreePanel().addNotebook(testNotebook);
				nbQueue.remove(testNotebook);
				processedNotebooks.add(testNotebook.getId());
			}
		}

		// Check through remaining notebooks to find if their dependency has been processed
		// If their dependency has been added, we can add to the tree now
		// Continue iterating until all dependencies are solved
		HashSet<Notebook> removeFromQueue = new HashSet();
		while (!nbQueue.isEmpty()) {
			for (Notebook nb : nbQueue) {
				if (processedNotebooks.contains(nbParentIdMap.get(nb.getId()))) {
					win.getTreePanel().addNotebook(nb);
					processedNotebooks.add(nb.getId());
					removeFromQueue.add(nb);
				}
			}
			for (Notebook i : removeFromQueue) {
				nbQueue.remove(i);
			}
		}

		// Notebooks are all added in..
		// Let's populate the notes into the notebooks now
		for (Note n : cntMgr.getLocalNotes()) {
			win.getTreePanel().addNote(n.getNotebookId(), n);
		}

		// Expand the tree
		win.getTreePanel().expandTree();
	}

	// Have to deal with null current notebook, can happen if no content selected
	public void openNewNbWindow() {
		Integer parentNbId = null;
		String assocNbTitle;
		if (cntMgr.getCurrentNotebook() == null) {
			assocNbTitle = "(none)";
		} else {
			parentNbId = cntMgr.getCurrentNotebook().getId();
			assocNbTitle = cntMgr.getCurrentNotebook().getTitle();
		}
		NewNbWindow newNbWin = new NewNbWindow(
				cntMgr,
				win,
				parentNbId,
				assocNbTitle);
	}

	// Don't have to handle null currentNotebook
	// UI menu bar is locked upon no content selected
	public void openNewNoteWindow() {
		NewNoteWindow newNoteWin = new NewNoteWindow(
				cntMgr,
				win,
				cntMgr.getCurrentNotebook().getId(),
				cntMgr.getCurrentNotebook().getTitle());
	}

	public void openMoveEntityWindow() {
		Entity e = win.getTreePanel().getSelectedEntityType();
		Integer currentEntityId = null;
		String currentEntityParentName = null;
		if (e == Entity.NOTEBOOK) {
			currentEntityId = cntMgr.getCurrentNotebook().getId();
			currentEntityParentName = (cntMgr.getCurrentNotebook().getParentId() != null) ? cntMgr.getNotebook(cntMgr.getCurrentNotebook().getParentId()).getTitle() : "(none)";
		} else if (e == Entity.NOTE) {
			currentEntityId = cntMgr.getCurrentNote().getId();
			currentEntityParentName = cntMgr.getNotebook(cntMgr.getCurrentNote().getNotebookId()).getTitle();
		}

		if (currentEntityId != null && currentEntityParentName != null) {
			MoveEntityWindow moveWin = new MoveEntityWindow(
					cntMgr,
					win,
					e,
					currentEntityId,
					currentEntityParentName);
		} else {
			JOptionPane.showMessageDialog(win, "Failed to extract entity information.", "JavaJotter Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void openRenameEntityWindow() {
		Entity e = win.getTreePanel().getSelectedEntityType();
		Integer currentEntityId = null;
		String currentEntityName = null;
		if (e == Entity.NOTEBOOK) {
			currentEntityId = cntMgr.getCurrentNotebook().getId();
			currentEntityName = cntMgr.getCurrentNotebook().getTitle();
		} else if (e == Entity.NOTE) {
			currentEntityId = cntMgr.getCurrentNote().getId();
			currentEntityName = cntMgr.getCurrentNote().getTitle();
		}

		if (currentEntityId != null && currentEntityName != null) {
			RenameEntityWindow renameWin = new RenameEntityWindow(
					cntMgr,
					win,
					e,
					currentEntityId,
					currentEntityName);
		} else {
			JOptionPane.showMessageDialog(win, "Failed to extract entity information.", "JavaJotter Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void deleteEntity() {
		Entity e = win.getTreePanel().getSelectedEntityType();

		if (e == Entity.NOTEBOOK) {
			deleteSelectedNb();
		} else if (e == Entity.NOTE) {
			deleteSelectedNote();
		}
	}

	private void deleteSelectedNb() {
		cntMgr.deleteLocalNotebook(cntMgr.getCurrentNotebook().getId());
	}

	private void deleteSelectedNote() {
		cntMgr.deleteLocalNote(cntMgr.getCurrentNote().getId());
	}

	public void treeSelectionChanged() {
		// Before changing content, force the sweeper to run now to collect
		// and push any content changes if a note's selected
		cntMgr.sweepNow();
	}

	public void setContentReadLockState(boolean state) {
		cntMgr.setContentReadLockState(state);
	}

    public void newNotebookSelected(Notebook nb) {
		cntMgr.setCurrentNotebook(nb);
		cntMgr.setCurrentNote(null);
        win.getEditPanel().setEditPanelEnabled(false);
		win.notebookSelected();
    }

    public void newNoteSelected(Note note) {
		cntMgr.setContentReadLockState(true);	// Do not let NoteSweeper interact while loading
		cntMgr.setCurrentNotebook(null);
		cntMgr.setCurrentNote(note);
		cntMgr.resetNoteSweeper();
		win.getEditPanel().setEditPanelEnabled(true);
		win.getEditPanel().updateEditPanel(note.getContent(), note.getImageAttachments());
		win.noteSelected();

		// When loading is complete, the SwingWorker that loads the HTML document will unlock content read lock
    }

	// TODO: We probably need to remove this and find a better way to refresh the text pane when needed
	// (Ex. when an image in inserted and we have to force regenerating the document using new cache source)
	public void refreshNoteSelected() {
		cntMgr.sweepNow();
		win.getEditPanel().updateEditPanel(cntMgr.getCurrentNote().getContent(), cntMgr.getCurrentNote().getImageAttachments());
	}

	public void noEntitySelected() {
		cntMgr.setCurrentNotebook(null);
		cntMgr.setCurrentNote(null);
		win.getEditPanel().setEditPanelEnabled(false);
		win.noContentSelected();
	}

	public void attachImage(File imageFile) {
		try {
			// Encode the image to base64
			BASE64Encoder b64Encoder = new BASE64Encoder();
			BufferedImage bufImg = ImageIO.read(imageFile);
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			ImageIO.write(bufImg, "png", byteOutputStream);
			byte[] imgBytes = byteOutputStream.toByteArray();
			String imgBase64 = b64Encoder.encode(imgBytes);

			// Construct the URL to be used in imageCache property of document
			int imgHash = imgBase64.hashCode();
			Integer imgHashInt = new Integer(imgHash);
			String imgUrlStr = "http://" + imgHashInt.toString();
			URL imgUrl = new URL(imgUrlStr);

			// Finally, add the URL and base64 encoding to the note.
			// When note is selected, the imageCache property will be populated
			// with the BufferedImage reconstructed from base64
			cntMgr.getCurrentNote().addImageAttachment(imgUrl, imgBase64);

			// Insert the image into the HTML document using the URL we constructed
			win.getEditPanel().insertImageIntoDocument(imgUrlStr);
		} catch (Exception e) {
			System.out.println("Could not attach image to note.");
			e.printStackTrace();
		}
	}

	public BufferedImage decodeImage(String encodedImg) {
		try {
			BASE64Decoder b64Decoder = new BASE64Decoder();
			byte[] decodedImg = b64Decoder.decodeBuffer(encodedImg);
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(decodedImg);
			BufferedImage bufImg = (BufferedImage)ImageIO.read(byteInputStream);
			return bufImg;
		} catch (Exception e) {
			System.out.println("Could not decode image.");
			e.printStackTrace();
			return null;
		}
	}

	public void lostConnection() {
		if (isConnected()) {
			JOptionPane.showMessageDialog(win, "Connection to the server was lost.", "JavaJotter Network", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public void exit() {
		cntMgr.sweepNow();
		synchronizer.now();
		System.exit(0);
	}
}