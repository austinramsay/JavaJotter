package com.austinramsay.javajotter;

import com.austinramsay.javajotterlibrary.Transaction;
import com.austinramsay.javajotterlibrary.TransactionType;
import com.austinramsay.javajotterlibrary.Entity;
import com.austinramsay.javajotterlibrary.UpdateType;
import com.austinramsay.javajotterlibrary.Notebook;
import com.austinramsay.javajotterlibrary.Note;
import com.austinramsay.javajotter.gui.JavaJotterWindow;
import java.util.TimerTask;
import java.util.Timer;
import java.util.HashMap;
import java.util.ArrayList;

public class Synchronizer {

	private int taskTime = 20000;

	private JavaJotter controller;
	private ContentManager cntMgr;
	private NetworkManager netMgr;
	private Timer timer;
	private SyncCheck syncTask;
	private HashMap<Integer, Integer> nbHashes;
	private HashMap<Integer, Integer> nHashes;
	private HashMap<Integer, Integer> noteContentHashes;
	private HashMap<Integer, Integer> noteImageAttachmentsHashes;

	public Synchronizer(JavaJotter controller, ContentManager cntMgr, NetworkManager nm) {
		this.controller = controller;
		this.cntMgr = cntMgr;
		this.netMgr = nm;

		nbHashes = new HashMap();
		nHashes = new HashMap();
		noteContentHashes = new HashMap();
		noteImageAttachmentsHashes = new HashMap();

		timer = new Timer();
		syncTask = new SyncCheck(this);
	}

	public void startSynchronizer() {
		timer.schedule(syncTask, 0, taskTime);
	}

	public void now() {
		syncTask.run();
	}

	// This method will set a reference point for what the content hashes are
	// after either pulling content off the server on first login, or, just
	// monitoring changes after content has changed since so these hashes changed
	// at some point in time.
	public void updateReferenceHashes(ArrayList<Notebook> nbList, ArrayList<Note> nList) {
		for (Notebook nb : nbList) {
			nbHashes.put(nb.getId(), nb.hashCode());
		}

		for (Note n : nList) {
			nHashes.put(n.getId(), n.hashCode());
			noteContentHashes.put(n.getId(), n.contentHashCode());
		}
	}

	public void diffCheck() {
		// Check for pending notebook transactions to process
		if (!cntMgr.getNbTransactions().isEmpty()) {
			for (Transaction t : cntMgr.getNbTransactions()) {
				if (t.getType() == TransactionType.ADD && cntMgr.getNotebook(t.getId()) != null) {
					netMgr.send(cntMgr.getNotebook(t.getId()));
				} else if (t.getType() == TransactionType.DELETE || t.getType() == TransactionType.UPDATE) {
					netMgr.send(t);
				}
			}
			cntMgr.clearNbTransactions();
		}

		// Check for pending note transactions to process
		if (!cntMgr.getNoteTransactions().isEmpty()) {
			for (Transaction t : cntMgr.getNoteTransactions()) {
				if (t.getType() == TransactionType.ADD && cntMgr.getNote(t.getId()) != null) {
					netMgr.send(cntMgr.getNote(t.getId()));
				} else if (t.getType() == TransactionType.DELETE || t.getType() == TransactionType.UPDATE) {
					netMgr.send(t);
				}
			}
			cntMgr.clearNoteTransactions();
		}

		// Scan notes for changes (content, image attachments)
		for (Note n : cntMgr.getLocalNotes()) {
			// Checking content changes
			int contentHash = n.contentHashCode();
			if (noteContentHashes.containsKey(n.getId()) && contentHash != noteContentHashes.get(n.getId())) {
				// The note has been modified
				Transaction tContent = new Transaction(Entity.NOTE, TransactionType.UPDATE, n.getId(), UpdateType.CONTENT, n.getContent());
				netMgr.send(tContent);

				// If content changed, image attachments could have possibly changed as well
				// Checking image attachment changes
				int imgAttachmentsHash = n.imageAttachmentsHashCode();
				if (noteImageAttachmentsHashes.containsKey(n.getId()) && imgAttachmentsHash != noteImageAttachmentsHashes.get(n.getId())) {
					// Image attachments have been modified
					Transaction tImgAttachments = new Transaction(Entity.NOTE, TransactionType.UPDATE, n.getId(), UpdateType.IMAGE_ATTACHMENTS, n.getImageAttachmentsBytes());
					netMgr.send(tImgAttachments);

					// Reset image attachments reference hash
					noteImageAttachmentsHashes.put(n.getId(), n.imageAttachmentsHashCode());
				}

				// Reset content reference hash
				nHashes.put(n.getId(), n.hashCode());
				noteContentHashes.put(n.getId(), n.contentHashCode());
			} else if (!nHashes.containsKey(n.getId())) {
				// This must be a new note. We need to record its hash.
				nHashes.put(n.getId(), n.hashCode());
				noteContentHashes.put(n.getId(), n.contentHashCode());
				noteImageAttachmentsHashes.put(n.getId(), n.imageAttachmentsHashCode());
			}
		}

		controller.updateLastSyncTime(JavaJotterWindow.timeFormat.format(new java.util.Date()));
	}
}

class SyncCheck extends TimerTask {

	private Synchronizer synchronizer;

	public SyncCheck(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}

	@Override
	public void run() {
		synchronizer.diffCheck();
	}
}