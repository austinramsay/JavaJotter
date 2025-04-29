package com.austinramsay.javajotter;

import com.austinramsay.javajotter.gui.NoteEditPanel;
import com.austinramsay.javajotterlibrary.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;

public class ContentManager {

	private JavaJotter controller;
	private NoteEditPanel editPanel;

	private Timer timer = new Timer();
	private NoteSweeper noteSweeperTask;
	private boolean contentReadLock = false;
	private int noteSweeperTime = 5000;

	// Notebook and note content
	private ArrayList<Notebook> notebooks;
	private ArrayList<Note> notes;
	private Notebook currentNotebook;
	private Note currentNote;
	private Integer nextNbId;
	private Integer nextNoteId;
	private ArrayList<Transaction> nbTransactions;
	private ArrayList<Transaction> noteTransactions;

	public ContentManager(JavaJotter controller, NoteEditPanel editPanel) {
		this.controller = controller;
		this.editPanel = editPanel;
		nbTransactions = new ArrayList<Transaction>();
		noteTransactions = new ArrayList<Transaction>();
	}

	public void startNoteSweeper() {
		noteSweeperTask = new NoteSweeper(this, editPanel);
		timer.schedule(noteSweeperTask, 0, noteSweeperTime);
	}

	public void sweepNow() {
		noteSweeperTask.run();
	}

	public void resetNoteSweeper() {
		noteSweeperTask.reset();
	}

	public boolean getContentReadLockState() {
		return contentReadLock;
	}

	public void setContentReadLockState(boolean contentReadLock) {
		this.contentReadLock = contentReadLock;
	}

	public ArrayList<Transaction> getNbTransactions() {
		return nbTransactions;
	}

	public ArrayList<Transaction> getNoteTransactions() {
		return noteTransactions;
	}

	public void addNotebookTransaction(Transaction t) {
		nbTransactions.add(t);
	}

	public void addNoteTransaction(Transaction t) {
		noteTransactions.add(t);
	}

	public void clearNbTransactions() {
		nbTransactions.clear();
	}

	public void clearNoteTransactions() {
		noteTransactions.clear();
	}

	public Integer getNextNbId() {
		Integer current = nextNbId.intValue();
		nextNbId++;
		return current;
	}

	public void setNextNbId(Integer lastNbId) {
		this.nextNbId = (lastNbId == null) ? 0 : (lastNbId + 1);
	}

	public Integer getNextNoteId() {
		Integer current = nextNoteId.intValue();
		nextNoteId++;
		return current;
	}

	public void setNextNoteId(Integer lastNoteId) {
		this.nextNoteId = (lastNoteId == null) ? 0 : (lastNoteId + 1);
	}

	public Note getCurrentNote() {
		return currentNote;
	}

	public void setCurrentNote(Note currentNote) {
		this.currentNote = currentNote;
	}

	public Notebook getCurrentNotebook() {
		return currentNotebook;
	}

	public void setCurrentNotebook(Notebook currentNotebook) {
		this.currentNotebook = currentNotebook;
	}

	public void setLocalNotebooks(ArrayList<Notebook> notebooks) {
		this.notebooks = notebooks;
	}

	public void setLocalNotes(ArrayList<Note> notes) {
		this.notes = notes;
	}

	public ArrayList<Notebook> getLocalNotebooks() {
		return notebooks;
	}

	public ArrayList<Note> getLocalNotes() {
		return notes;
	}

	public void newLocalNotebook(Integer parentId, String title) {
		Notebook newNb;
		if (parentId == null) {
			newNb = new Notebook(getNextNbId(), title);
		} else {
			newNb = new Notebook(getNextNbId(), parentId, title);
		}

		getLocalNotebooks().add(newNb);
		controller.rebuildTree();

		Transaction newNbTransaction = new Transaction(Entity.NOTEBOOK, TransactionType.ADD, newNb.getId());
		nbTransactions.add(newNbTransaction);
	}

	public void newLocalNote(Integer notebookId, String title) {
		Note newNote = new Note(getNextNoteId(), notebookId, title, editPanel.getBlankDocument(), new HashMap<URL, String>());
		getLocalNotes().add(newNote);
		controller.rebuildTree();

		Transaction newNoteTransaction = new Transaction(Entity.NOTE, TransactionType.ADD, newNote.getId());
		addNoteTransaction(newNoteTransaction);
	}

	public void renameLocalNotebook(Integer id, String title) {
		//treePanel.deleteNotebook(id);
		Notebook nb = getNotebook(id);
		nb.setTitle(title);
		//treePanel.addNotebook(nb);
		controller.rebuildTree();

		Transaction renameNbTransaction = new Transaction(Entity.NOTEBOOK, TransactionType.UPDATE, id, UpdateType.TITLE, title);
		addNotebookTransaction(renameNbTransaction);
	}

	public void renameLocalNote(Integer id, String title) {
		//treePanel.deleteNote(id);
		Note n = getNote(id);
		Integer nbId = n.getNotebookId();
		n.setTitle(title);
		//treePanel.addNote(nbId, n);
		controller.rebuildTree();

		Transaction renameNoteTransaction = new Transaction(Entity.NOTE, TransactionType.UPDATE, id, UpdateType.TITLE, title);
		addNoteTransaction(renameNoteTransaction);
	}

	public void deleteLocalNotebook(Integer id) {
		deleteLocalNotebookAndChildren(id);
		controller.rebuildTree();

		// Note: there doesn't need to be a transaction made for every child NB or note deleted
		// The server handles this with it's own algorithm
		Transaction deleteNbTransaction = new Transaction(Entity.NOTEBOOK, TransactionType.DELETE, id);
		nbTransactions.add(deleteNbTransaction);
	}

	private void deleteLocalNotebookAndChildren(Integer id) {
		// Get all note IDs in associated with this notebook
		ArrayList<Note> notesToDelete = new ArrayList<Note>();
		for (Note n : getLocalNotes()) {
			if (n.getNotebookId().equals(id)) {
				notesToDelete.add(n);
			}
		}
		getLocalNotes().removeAll(notesToDelete);

		// Fetch all IDs of notebooks that this notebook is a parent for
		ArrayList<Integer> notebookIdsToDelete = new ArrayList<Integer>();
		for (Notebook nb : getLocalNotebooks()) {
			if (nb.getParentId() != null && nb.getParentId().equals(id)) {
				notebookIdsToDelete.add(nb.getId());
			}
		}

		// Recursion call to delete child notebooks and its' notes
		for (Integer i : notebookIdsToDelete) {
			deleteLocalNotebookAndChildren(i);
		}

		// Finally, delete the original notebook itself
		getLocalNotebooks().remove(getNotebook(id));
	}

	public void deleteLocalNote(Integer id) {
		getLocalNotes().remove(getNote(id));
		controller.rebuildTree();

		Transaction deleteNoteTransaction = new Transaction(Entity.NOTE, TransactionType.DELETE, id);
		addNoteTransaction(deleteNoteTransaction);
	}

	public void moveEntityTo(Entity e, Integer entityId, Integer newParentId) {
		if (e == Entity.NOTEBOOK) {
			Notebook nb = getNotebook(entityId);
			if (nb == null)
				return;
			nb.setParentId(newParentId);
			Transaction moveNbTransaction = new Transaction(e, TransactionType.UPDATE, entityId, UpdateType.MAP_NOTEBOOK_TO_PARENT_NOTEBOOK_ID, newParentId);
			addNotebookTransaction(moveNbTransaction);
		} else if (e == Entity.NOTE) {
			Note n = getNote(entityId);
			if (n == null)
				return;
			n.setNotebookId(newParentId);
			Transaction moveNoteTransaction = new Transaction(e, TransactionType.UPDATE, entityId, UpdateType.MAP_NOTE_TO_NOTEBOOK_ID, newParentId);
			addNoteTransaction(moveNoteTransaction);
		}
		controller.rebuildTree();
	}

	public ArrayList<Notebook> getAllChildNotebooks(Integer nbId) {
		ArrayList<Notebook> childNbs = new ArrayList<Notebook>();
		for (Notebook nb : getLocalNotebooks()) {
			if (nb.getParentId() != null && nb.getParentId().equals(nbId)) {
				childNbs.add(nb);
			}
		}

		ArrayList<Notebook> resultsFromChildren = new ArrayList<Notebook>();
		for (Notebook n : childNbs) {
			resultsFromChildren.addAll(getAllChildNotebooks(n.getId()));
		}
		childNbs.addAll(resultsFromChildren);
		return childNbs;
	}

	public Notebook getNotebook(Integer id) {
		for (Notebook nb : getLocalNotebooks()) {
			if (nb.getId().equals(id))
				return nb;
		}

		return null;
	}

	public Note getNote(Integer id) {
		for (Note n : getLocalNotes()) {
			if (n.getId().equals(id))
					return n;
		}

		return null;
	}
}

class NoteSweeper extends TimerTask {

	private ContentManager cntMgr;
	private NoteEditPanel editPanel;
	private boolean hasReadContent;
	private int lastRecordedContentHash;

	public NoteSweeper(ContentManager cntMgr, NoteEditPanel editPanel) {
		this.cntMgr = cntMgr;
		this.editPanel = editPanel;
		hasReadContent = false;
	}

	@Override
	public void run() {
		if (cntMgr.getCurrentNote() != null) {
			checkReadContent();
			checkContent();
		}
	}

	private void checkReadContent() {
		if (!hasReadContent && !cntMgr.getContentReadLockState()) {
			lastRecordedContentHash = cntMgr.getCurrentNote().getContent().hashCode();
			hasReadContent = true;
		}
	}

	private void checkContent() {
		//TODO: we need to test how this works when you have a note that you delete all content from
		byte[] content = editPanel.getContent();

		if (content == null) {
			return;
		}

		int contentHash = content.hashCode();

		if (contentHash != lastRecordedContentHash) {
			// Content has changed, let's update the note and reset reference hash
			cntMgr.getCurrentNote().setContent(content);
			lastRecordedContentHash = contentHash;
		}
	}

	public void reset() {
		hasReadContent = false;
	}
}