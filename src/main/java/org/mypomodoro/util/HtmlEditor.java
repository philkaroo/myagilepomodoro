/*
 * Copyright (C) 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mypomodoro.util;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.mypomodoro.Main;

/**
 * HTML editor
 *
 */
public class HtmlEditor extends JTextPane {

    /**
     * Override paint method to turn on the anti-aliasing property
     * http://stackoverflow.com/questions/15868894/unordered-list-bullets-look-pixelated-in-jeditorpane
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g2d);
        g2d.dispose();
    }

    public HtmlEditor() {
        setEditorKit(new HTMLEditorKit()); // content type = text/html        
        setContentType("text/html;charset=UTF-8"); // make editor utf-8 compliant but won't add any metatag charset directive to the header of the html doc        
        // Ignore metatag charset directive (JTextPane doesn't support it)
        getDocument().putProperty("IgnoreCharsetDirective", true);

        // Turn on bi-directional text
        getDocument().putProperty("i18n", Boolean.TRUE);
        // Right to Left
        // setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // set default HTML body settings        
        /*String bodyRule = "body {"
         + "color: #000;"
         + "font-family: " + getFont().getFamily() + ";"
         + "font-size: " + getFont().getSize() + "pt;"
         + "margin: 1px;"
         + "}";
         ((HTMLDocument) getDocument()).getStyleSheet().addRule(bodyRule);*/
        // This line replaces the previous rule by instructing the editor to use the font of the UIManager        
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        // Set colors according to input settings and themes
        setBackground(new JTextField().getBackground());
        setForeground(new JTextField().getForeground());
        setCaretColor(new JTextField().getForeground());

        // limit the number of characters to 1000 to avoid java head size issue
        // ((AbstractDocument) getDocument()).setDocumentFilter(new SizeFilter(1000)); // this will make mergind of tasks (comments) difficult
        // Remove some formatting when typing before or after a formatted text
        // we do it the same way MICROSOFT Word Office does:
        // - Formatting is preserved after: bold, italic, underline, foreground style --> nothing to do here
        // - Formatting is removed after: background style
        // - Formating is removed before and after: links
        addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent event) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        int start = HtmlEditor.this.getSelectionStart();
                        //int end = HtmlEditor.this.getSelectionEnd();
                        AttributeSet selectionAttributes = HtmlEditor.this.getStyledDocument().getCharacterElement(start).getAttributes();
                        MutableAttributeSet inputAttr = HtmlEditor.this.getInputAttributes();
                        /*MutableAttributeSet BOLD = new SimpleAttributeSet();
                         StyleConstants.setBold(BOLD, true);
                         if (!selectionAttributes.containsAttributes(BOLD)) {
                         inputAttr.removeAttribute(StyleConstants.Bold);
                         }
                         MutableAttributeSet ITALIC = new SimpleAttributeSet();
                         StyleConstants.setItalic(ITALIC, true);
                         if (!selectionAttributes.containsAttributes(ITALIC)) {
                         inputAttr.removeAttribute(StyleConstants.Italic);
                         }
                         MutableAttributeSet UNDERLINE = new SimpleAttributeSet();
                         StyleConstants.setUnderline(UNDERLINE, true);
                         if (!selectionAttributes.containsAttributes(UNDERLINE)) {
                         inputAttr.removeAttribute(StyleConstants.Underline);
                         }*/
                        // Background
                        MutableAttributeSet BACKGROUND = new SimpleAttributeSet();
                        StyleConstants.setBackground(BACKGROUND, StyleConstants.getBackground(selectionAttributes));
                        if (!selectionAttributes.containsAttributes(BACKGROUND)) {
                            inputAttr.removeAttribute(StyleConstants.Background);
                            Object spanTag = selectionAttributes.getAttribute(HTML.Tag.SPAN); // we must also take care of the SPAN tag used to set the background (see CommentPanel#backgroundColorButton#actionPerformed)
                            if (spanTag == null) {
                                inputAttr.removeAttribute(HTML.Tag.SPAN);
                            }
                        }
                        /*MutableAttributeSet FOREGROUND = new SimpleAttributeSet();
                         StyleConstants.setBackground(FOREGROUND, StyleConstants.getForeground(selectionAttributes));
                         if (!selectionAttributes.containsAttributes(FOREGROUND)) {
                         inputAttr.removeAttribute(StyleConstants.Foreground);
                         }*/
                        // Hyperlinks
                        Object tag = selectionAttributes.getAttribute(HTML.Tag.A);
                        if (tag == null) {
                            inputAttr.removeAttribute(HTML.Tag.A);
                        }
                    }
                });
            }
        });

        addHyperlinkListener(new MyHyperlinkListener());
    }

    // Make hyperlinks clickable in preview mode
    class MyHyperlinkListener implements HyperlinkListener {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED
                    && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException ignored) {
                } catch (URISyntaxException ignored) {
                }
            }
        }
    }

    // Set tool tip on hyperlinks in preview mode
    @Override
    public String getToolTipText(MouseEvent event) {
        String toolTip = null;
        JTextPane editor = (JTextPane) event.getSource();
        if (!editor.isEditable()
                && editor.getCursor().getType() == Cursor.HAND_CURSOR) {
            Point pt = new Point(event.getX(), event.getY());
            int pos = editor.viewToModel(pt);
            if (pos >= 0) {
                Element e = ((HTMLDocument) editor.getDocument()).getCharacterElement(pos);
                SimpleAttributeSet attribute = (SimpleAttributeSet) e.getAttributes().getAttribute(HTML.Tag.A);
                if (attribute != null) {
                    String href = (String) attribute.getAttribute(HTML.Attribute.HREF);
                    toolTip = href;
                }
            } else {
                toolTip = null;
            }
        }
        return toolTip;
    }

    // Insert text at the cursor position
    public void insertText(int start, String text, Tag insertTag) throws BadLocationException, IOException {
        insertText(start, text, 0, insertTag);
    }

    // Insert text at the cursor position
    public void insertText(int start, String text, int popDepth, Tag insertTag) throws BadLocationException, IOException {
        ((HTMLEditorKit) getEditorKit()).insertHTML((HTMLDocument) getDocument(), start, text, popDepth, 0, insertTag);
    }

    // Get raw text out of html content
    public String getRawText() {
        String text = "";
        try {
            text = getDocument().getText(0, getDocument().getLength());
        } catch (BadLocationException ex) {
            Main.logger.error("Problem extracting raw content out of html content", ex);
        }
        return text;
    }

    public boolean isEditorOrPreviewMode() {
        return getContentType().equals("text/html");
    }

    public boolean isHTMLMode() {
        return getContentType().equals("text/plain");
    }

    public String getClipboard() {
        String clipboardText = "";
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            clipboardText = (String) systemClipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ignored) {
        } catch (IOException ignored) {
        }
        return clipboardText;
    }

    class SizeFilter extends DocumentFilter {

        private final int maxCharacters;

        public SizeFilter(int maxChars) {
            maxCharacters = maxChars;
        }

        // Paste
        @Override
        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
                throws BadLocationException {
            if ((fb.getDocument().getLength() + str.length()) <= maxCharacters) {
                super.insertString(fb, offs, str, a);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        // Write
        @Override
        public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a)
                throws BadLocationException {
            if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
                super.replace(fb, offs, length, str, a);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public boolean isParentElement(HTML.Tag tag) {
        boolean isParentElement = false;
        Element e = getCurrentParentElement();
        if (e.getName().equalsIgnoreCase(tag.toString())) {
            isParentElement = true;
        }
        return isParentElement;
    }

    public Element getCurrentParentElement() {
        return ((HTMLDocument) getDocument()).getParagraphElement(getCaretPosition()).getParentElement();
    }
}
