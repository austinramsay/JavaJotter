package com.austinramsay.javajotter.gui;

import com.austinramsay.javajotter.JavaJotter;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import java.awt.event.ActionEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javax.swing.event.UndoableEditListener;
import javax.swing.SwingWorker;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.HashMap;
import java.net.URL;
import java.awt.Image;

public class NoteEditPanel extends JScrollPane {

    private JTextPane noteContentPane;
	private JavaJotter controller;
	private StatusBarPanel statusPanel;
	private UndoableEditListener undoListener;
	private AttributeSet selectedTextAttrs;
	private HTMLEditorKit htmlKit;
	private ByteArrayOutputStream docByteOutputStream;

    public NoteEditPanel(JavaJotter controller, StatusBarPanel statusPanel, UndoableEditListener undoListener) {
		this.controller = controller;
		this.statusPanel = statusPanel;
		this.undoListener = undoListener;
		htmlKit = new HTMLEditorKit();
		noteContentPane = new JTextPane();
		noteContentPane.setEditorKit(htmlKit);
		noteContentPane.setEnabled(false);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setViewportView(noteContentPane);
    }

	public void modifyFontSize(ActionEvent e, int amount) {
		selectedTextAttrs = htmlKit.getInputAttributes();
		int fontSize = StyleConstants.getFontSize(selectedTextAttrs);
		new HTMLEditorKit.FontSizeAction("Font Size Increment", (fontSize + amount)).actionPerformed(e);
	}

	public void modifyTextColor(ActionEvent e, Color color) {
		new HTMLEditorKit.ForegroundAction("Modify Text Color", color).actionPerformed(e);
	}

    public void updateEditPanel(byte[] content, HashMap<URL, String> imageAttachments) {
		statusPanel.updateStatus("Loading...");
		new UpdateContentTask(noteContentPane, controller, statusPanel, undoListener, content, imageAttachments).execute();
    }

    public void setEditPanelEnabled(boolean enabled) {
        if (!enabled) {
            noteContentPane.setText(null);
        }
        noteContentPane.setEnabled(enabled);
    }

	public byte[] getContent() {
		try {
			HTMLDocument document = (HTMLDocument)noteContentPane.getDocument();
			docByteOutputStream = new ByteArrayOutputStream();
			htmlKit.write(docByteOutputStream, document, 0, document.getLength());
			return docByteOutputStream.toByteArray();
		} catch (Exception e) {
			System.out.println("Failed to extract note content.");
			e.printStackTrace();
			return null;
		}
	}

	public byte[] getBlankDocument() {
		try {
			HTMLDocument blankHTMLDoc = (HTMLDocument)htmlKit.createDefaultDocument();
			docByteOutputStream = new ByteArrayOutputStream();
			htmlKit.write(docByteOutputStream, blankHTMLDoc, 0, blankHTMLDoc.getLength());
			return docByteOutputStream.toByteArray();
		} catch (Exception e) {
			System.out.println("Failed to create blank document.");
			e.printStackTrace();
			return null;
		}
	}

	public void insertImageIntoDocument(String url) {
		try {
			// Defined img width and height
			//htmlKit.insertHTML((HTMLDocument)noteContentPane.getDocument(), noteContentPane.getDocument().getLength(), "<img src=\"" + url + "\" width=\"50\" height=\"50\">", 0, 0, null);

			// Original image size
			//htmlKit.insertHTML((HTMLDocument)noteContentPane.getDocument(), noteContentPane.getDocument().getLength(), "<img src=\"" + url + "\">", 0, 0, null);

			// Trying with a <p></p> wrapper to see if this helps with preserving spacing?
			htmlKit.insertHTML((HTMLDocument)noteContentPane.getDocument(), noteContentPane.getDocument().getLength(), "<p><img src=\"" + url + "\"></p>", 0, 0, null);

			// TODO: Probably is a better way to force the text pane to refresh, but for now.. this works.
			controller.refreshNoteSelected();
		} catch (Exception e) {
			System.out.println("Could not insert image HTML into document.");
			e.printStackTrace();
		}
	}
}

class UpdateContentTask extends SwingWorker<Void, String> {
	private JTextPane noteContentPane;
	private JavaJotter controller;
	private StatusBarPanel statusPanel;
	private UndoableEditListener undoListener;
	private ByteArrayInputStream docByteInputStream;
	private HTMLEditorKit htmlKit;
	private byte[] content;
	private HashMap<URL, String> imageAttachments;

	public UpdateContentTask(
			JTextPane noteContent,
			JavaJotter controller,
			StatusBarPanel statusPanel,
			UndoableEditListener undoListener,
			byte[] content,
			HashMap<URL, String> imageAttachments) {
		this.noteContentPane = noteContent;
		this.controller = controller;
		this.statusPanel = statusPanel;
		this.undoListener = undoListener;
		this.content = content;
		this.imageAttachments = imageAttachments;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			docByteInputStream = new ByteArrayInputStream(content);
			htmlKit = new HTMLEditorKit();
			noteContentPane.setEditorKit(htmlKit);

			HTMLDocument noteContentDoc = (HTMLDocument)noteContentPane.getDocument();
			Dictionary cache = (Dictionary)noteContentDoc.getProperty("imageCache");
			if (cache == null) {
				cache = new Hashtable<URL, Image>();
				noteContentDoc.putProperty("imageCache", cache);
			}

			// Convert all base64 encoded images back to BufferedImage,
			// then, insert into the document's image cache
			for (URL url : imageAttachments.keySet()) {
				if (cache.get(url) == null)
					cache.put(url, controller.decodeImage(imageAttachments.get(url)));
			}

			htmlKit.read(docByteInputStream, noteContentDoc, 0);
			noteContentPane.getStyledDocument().addUndoableEditListener(undoListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void done() {
		controller.getContentManager().setContentReadLockState(false);
		statusPanel.updateStatus(null);
	}
}
/*This was just for testing styled documents.
 final StyleContext styleCtxt = StyleContext.getDefaultStyleContext();
final AttributeSet redFg = styleCtxt.addAttribute(styleCtxt.getEmptySet(), StyleConstants.Foreground, Color.RED);
final AttributeSet blackFg = styleCtxt.addAttribute(styleCtxt.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
DefaultStyledDocument styledDoc = new DefaultStyledDocument() {
	@Override
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offset, str, a);

		setCharacterAttributes(0, getLength(), redFg, true);
	}
};*/