package com.austinramsay.javajotter.gui;

import com.austinramsay.javajotter.JavaJotter;
import com.austinramsay.javajotterlibrary.Entity;
import javax.swing.JTree;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import com.austinramsay.javajotterlibrary.Notebook;
import com.austinramsay.javajotterlibrary.Note;
import java.util.ArrayList;
import java.util.Enumeration;

public class NoteTreePanel extends JScrollPane implements TreeSelectionListener {

    private JavaJotter controller;
    private JTree notebookTree;
	private DefaultMutableTreeNode top;

	private Entity selectedEntityType;

    public NoteTreePanel(JavaJotter controller) {
        this.controller = controller;

        top = new DefaultMutableTreeNode("Notebooks");

        notebookTree = new JTree(top);
        notebookTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        notebookTree.addTreeSelectionListener(this);

        setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setViewportView(notebookTree);
    }

    /*
     * Method to implement logic when a new item is selected on the tree
     */
    public void valueChanged(TreeSelectionEvent event) {
		controller.treeSelectionChanged();

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
                notebookTree.getLastSelectedPathComponent();

        if (selectedNode == null || selectedNode.isRoot()) {
			controller.noEntitySelected();
			setSelectedEntityType(Entity.NULL);
			return;
		}

		if (selectedNode.getUserObject() instanceof Note) {
			Note selectedNote = (Note) selectedNode.getUserObject();
			controller.newNoteSelected(selectedNote);
			setSelectedEntityType(Entity.NOTE);
		} else if (selectedNode.getUserObject() instanceof Notebook) {
            Notebook selectedNb = (Notebook)selectedNode.getUserObject();
			controller.newNotebookSelected(selectedNb);
			setSelectedEntityType(Entity.NOTEBOOK);
            // TODO: Use the edit panel to print out all available notes
            // in the notebook
			// maybe use some kind of ascii art too?
		}
    }

	public void clearTree() {
		top.removeAllChildren();
		DefaultTreeModel model = (DefaultTreeModel) notebookTree.getModel();
		model.setRoot(top);
	}

	public Entity getSelectedEntityType() {
		return selectedEntityType;
	}

	public void setSelectedEntityType(Entity e) {
		selectedEntityType = e;
	}

	// Returns all notebooks AND note objects currently in the tree
	public ArrayList<Object> getDisplayedContent() {
		ArrayList<Object> treeObjects = new ArrayList<Object>();
		DefaultMutableTreeNode treeNode;

		Enumeration<DefaultMutableTreeNode> e = top.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			treeNode = e.nextElement();
			treeObjects.add(treeNode.getUserObject());
		}

		return treeObjects;
	}

	// Return the tree node that corresponds to a notebook's ID #
	public DefaultMutableTreeNode getNodeAt(Entity type, Integer id) {
		DefaultMutableTreeNode treeNode;

		// We need to traverse the tree, and create a list of where all the notebooks are
		Enumeration<DefaultMutableTreeNode> e = top.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			treeNode = e.nextElement();
			if (type == Entity.NOTEBOOK && treeNode.getUserObject() instanceof Notebook) {
				Notebook nbNode = (Notebook) treeNode.getUserObject();
				if (nbNode.getId().equals(id)) {
					return treeNode;
				}
			} else if (type == Entity.NOTE && treeNode.getUserObject() instanceof Note) {
				Note noteNode = (Note) treeNode.getUserObject();
				if (noteNode.getId().equals(id)) {
					return treeNode;
				}
			}
		}

		return null;
	}

	public String getParentNotebookName(Integer id) {
		DefaultMutableTreeNode nbNode = getNodeAt(Entity.NOTEBOOK, id);
		TreeNode parentNode = nbNode.getParent();
		if (parentNode.equals(top)) {
			return nbNode.toString();
		} else {
			return parentNode.toString();
		}
	}

	public void addNotebook(Notebook newNb) {
		DefaultTreeModel model = (DefaultTreeModel)notebookTree.getModel();
		DefaultMutableTreeNode newNbNode = new DefaultMutableTreeNode(newNb);

		if (newNb.getParentId() == null) {
			model.insertNodeInto(newNbNode, top, top.getChildCount());
		} else {
			DefaultMutableTreeNode parentNotebookNode = getNodeAt(Entity.NOTEBOOK, newNb.getParentId());
			model.insertNodeInto(newNbNode, parentNotebookNode, parentNotebookNode.getChildCount());
		}
	}

	public void deleteNotebook(Integer id) {
		DefaultTreeModel model = (DefaultTreeModel)notebookTree.getModel();
		DefaultMutableTreeNode nbNode = getNodeAt(Entity.NOTEBOOK, id);

		model.removeNodeFromParent(nbNode);
	}

	public void addNote(Integer id, Note newNote) {
		DefaultTreeModel model = (DefaultTreeModel)notebookTree.getModel();
		DefaultMutableTreeNode newNoteNode = new DefaultMutableTreeNode(newNote);
		DefaultMutableTreeNode matchingNotebookNode = getNodeAt(Entity.NOTEBOOK, id);

		model.insertNodeInto(newNoteNode, matchingNotebookNode, matchingNotebookNode.getChildCount());
	}

	public void deleteNote(Integer id) {
		DefaultTreeModel model = (DefaultTreeModel)notebookTree.getModel();
		DefaultMutableTreeNode noteNode = getNodeAt(Entity.NOTE, id);

		model.removeNodeFromParent(noteNode);
	}

	public void expandTree() {
		notebookTree.expandRow(0);
		notebookTree.setSelectionRow(0);
	}
}