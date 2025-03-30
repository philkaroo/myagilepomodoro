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
package org.mypomodoro.gui.activities;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.mypomodoro.buttons.DefaultButton;
import org.mypomodoro.gui.IListPanel;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.HtmlEditor;
import org.mypomodoro.util.Labels;

/**
 * Panel that displays comment on the current Activity and allows editing it
 *
 */
public class CommentPanel extends JPanel {

    private final GridBagConstraints gbc = new GridBagConstraints();
    private final JPanel iconPanel = new JPanel();
    private final IListPanel panel;
    private final DefaultButton saveButton = new DefaultButton(Labels.getString("Common.Save"));
    private final DefaultButton cancelButton = new DefaultButton(Labels.getString("Common.Cancel"));
    private final DefaultButton previewButton = new DefaultButton(Labels.getString("Common.Preview"));
    private final DefaultButton htmlButton = new DefaultButton("HTML");
    private final DefaultButton boldButton = new DefaultButton("B");
    private final DefaultButton italicButton = new DefaultButton("I");
    private final DefaultButton underlineButton = new DefaultButton("U");
    private final DefaultButton backgroundColorButton = new DefaultButton("ab");
    private final DefaultButton foregroundColorButton = new DefaultButton("A");
    private final JTextField linkTextField = new JTextField();
    private final DefaultButton linkButton = new DefaultButton(">>");
    private final HtmlEditor informationArea = new HtmlEditor();
    private final JScrollPane scrollPaneInformationArea;
    private boolean showIconLabel = false;
    // left and rigth 'small' arrows
    private final String rightArrow = " " + (getFont().canDisplay('\u25b6') ? "\u25b6" : ">") + " ";
    private final String leftArrow = " " + (getFont().canDisplay('\u25c0') ? "\u25c0" : "<") + " ";
    // Expand/Fold button
    private final DefaultButton expandButton = new DefaultButton(rightArrow);

    // Record
    private int currentlySelectedActivityId = -1;
    protected String currentlySelectedActivityText = "";
    protected int currentlySelectedActivityCaretPosition = 0;

    public CommentPanel(IListPanel iListPanel) {
        this(iListPanel, false);
    }

    public CommentPanel(IListPanel iListPanel, boolean showIconLabel) {
        this.panel = iListPanel;
        this.showIconLabel = showIconLabel;

        // Register the pane with ToolTipManager for tool tip to show
        ToolTipManager.sharedInstance().registerComponent(informationArea);

        // This will disable the wrapping of JTextPane
        // http://stackoverflow.com/questions/4702891/toggling-text-wrap-in-a-jtextpane/4705323#4705323        
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(informationArea);
        scrollPaneInformationArea = new JScrollPane(noWrapPanel);

        setLayout(new GridBagLayout());
        setBorder(null);

        addEditorButtons();
        addExpandButton();
        addCommentArea();
        addSaveButton();
        addCancelButton();

        // Display the buttons in editor mode when clicking or selecting with the mouse
        informationArea.addMouseListener(new MouseAdapter() {

            // click
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!informationArea.isEditable()) {
                    informationArea.setEditable(true);
                    expandButton.setVisible(true);
                    // show caret
                    informationArea.getCaret().setVisible(true);
                    informationArea.setCaretPosition(informationArea.getCaretPosition()); // reset position (activates automatic horizontal scrolling in case of long line)
                    informationArea.requestFocusInWindow();
                }
            }
        });

        /**
         * Displays the save and cancel buttons. Records the current version and
         * caret position.
         */
        informationArea.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // Nothing to do here
            }

            /**
             * KeyPressed makes the necessary checks and record the text BEFORE
             * modification
             *
             */
            @Override
            public void keyPressed(KeyEvent e) {
                // The area must be editable 
                // Excluding: key Control and shift, arrows, home/end and page up/down 
                if (informationArea.isEditable()
                        && e.getKeyCode() != KeyEvent.VK_CONTROL
                        && e.getKeyCode() != KeyEvent.VK_SHIFT
                        && e.getKeyCode() != KeyEvent.VK_UP
                        && e.getKeyCode() != KeyEvent.VK_KP_UP
                        && e.getKeyCode() != KeyEvent.VK_DOWN
                        && e.getKeyCode() != KeyEvent.VK_KP_DOWN
                        && e.getKeyCode() != KeyEvent.VK_LEFT
                        && e.getKeyCode() != KeyEvent.VK_KP_LEFT
                        && e.getKeyCode() != KeyEvent.VK_RIGHT
                        && e.getKeyCode() != KeyEvent.VK_KP_RIGHT
                        && e.getKeyCode() != KeyEvent.VK_HOME
                        && e.getKeyCode() != KeyEvent.VK_END
                        && e.getKeyCode() != KeyEvent.VK_PAGE_UP
                        && e.getKeyCode() != KeyEvent.VK_PAGE_DOWN) {

                    // Add item to list when pressing ENTER within a list (overriding default behaviour)
                    if (e.getKeyCode() == KeyEvent.VK_ENTER
                            && informationArea.isParentElement(HTML.Tag.LI)) {
                        Element element = informationArea.getCurrentParentElement();
                        try {
                            e.consume(); // the event must be 'consumed' before inserting!
                            String item = "<li></li>";
                            ((HTMLDocument) informationArea.getDocument()).insertAfterEnd(element, item);
                            informationArea.setCaretPosition(element.getEndOffset());
                            // Show caret
                            informationArea.requestFocusInWindow();
                        } catch (BadLocationException ignored) {
                        } catch (IOException ignored) {
                        }
                    }
                    displaySaveCancelButton();
                }
            }

            /**
             * KeyReleased is only used to record the current edited text and
             * position (text AFTER modification)
             *
             */
            @Override
            public void keyReleased(KeyEvent e) {
                currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                currentlySelectedActivityText = informationArea.getText();
            }
        });

        // Override SHIFT + '>' and SHIFT + '<' to prevent conflicts with list SHIFT + '>' and SHIFT + '<' shortcuts  
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_MASK), "donothing");
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_MASK), "donothing");
        class doNothing extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Do nothing
            }
        }
        informationArea.getActionMap().put("donothing", new doNothing());

        // Override Control + V and SHIFT + INSERT shortcut to get rid of all formatting       
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_MASK), "Shift Insert");
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "Control V");
        class paste extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                String clipboardText = informationArea.getClipboard();
                if (!clipboardText.isEmpty()) {
                    int start = informationArea.getSelectionStart();
                    int end = informationArea.getSelectionEnd();
                    try {
                        if (start != end) {
                            informationArea.getDocument().remove(start, end - start);
                        }
                        informationArea.getDocument().insertString(start, clipboardText, null);
                        displaySaveCancelButton();
                        currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                        currentlySelectedActivityText = informationArea.getText();
                        // Show caret
                        informationArea.requestFocusInWindow();
                    } catch (BadLocationException ignored) {
                    }
                }
            }
        }
        informationArea.getActionMap().put("Shift Insert", new paste());
        informationArea.getActionMap().put("Control V", new paste());

        // Ordered and unordered lists
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "Create List");
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), "Create Ordered List");
        class createList extends AbstractAction {

            private String type = HTML.Tag.UL.toString(); // unordered
            private HTML.Tag tag = HTML.Tag.UL; // unordered

            public createList() {
            }

            public createList(HTML.Tag tag) {
                this.type = tag.toString();
                this.tag = tag;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int start = informationArea.getSelectionStart();
                    String list = "<" + type + "><li></li></" + type + ">";
                    list += "<p style=\"margin-top: 0\"></p>"; // make sure we have a placeholder under the list to keep writing
                    informationArea.insertText(start, list, 1, tag);
                    informationArea.setCaretPosition(start + 1);
                    displaySaveCancelButton();
                    currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                    currentlySelectedActivityText = informationArea.getText();
                } catch (BadLocationException ignored) {
                } catch (IOException ignored) {
                }
                // Show caret
                informationArea.requestFocusInWindow();
            }
        }
        informationArea.getActionMap().put("Create List", new createList());
        informationArea.getActionMap().put("Create Ordered List", new createList(HTML.Tag.OL));

        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "Remove List item");
        class removeList extends AbstractAction {

            public removeList() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (informationArea.isParentElement(HTML.Tag.LI)) {
                    Element element = informationArea.getCurrentParentElement();
                    try {
                        informationArea.getDocument().remove(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
                        displaySaveCancelButton();
                        currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                        currentlySelectedActivityText = informationArea.getText();
                    } catch (BadLocationException ignored) {
                    }
                }
                // Show caret
                informationArea.requestFocusInWindow();
            }
        }
        informationArea.getActionMap().put("Remove List item", new removeList());
    }

    private void addEditorButtons() {
        boldButton.setFont(getFont().deriveFont(Font.BOLD));
        boldButton.setMargin(new Insets(0, 0, 0, 0));

        italicButton.setFont(getFont().deriveFont(Font.ITALIC));
        italicButton.setMargin(new Insets(0, 0, 0, 0));

        Map attributes = getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        underlineButton.setFont(getFont().deriveFont(attributes));
        underlineButton.setMargin(new Insets(0, 0, 0, 0));

        backgroundColorButton.setForeground(Color.BLUE);
        backgroundColorButton.setFont(getFont().deriveFont(attributes).deriveFont(Font.BOLD));
        backgroundColorButton.setMargin(new Insets(0, 0, 0, 0));

        foregroundColorButton.setForeground(Color.BLUE);
        foregroundColorButton.setFont(getFont().deriveFont(Font.BOLD));
        foregroundColorButton.setMargin(new Insets(0, 0, 0, 0));

        linkButton.setMargin(new Insets(0, 0, 0, 0));
        // Preview button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1; // 10 %
        gbc.weighty = 0.5;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        previewButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean htmlMode = informationArea.isHTMLMode();
                displayPreviewMode();
                expandButton.setText(rightArrow);
                expandButton.setToolTipText(Labels.getString("Common.Show editor"));
                informationArea.setEditable(false);
                if (htmlMode) {
                    currentlySelectedActivityCaretPosition = 0; // reset
                    informationArea.setCaretPosition(0); // the caret position in HTML mode doesn't apply to preview mode                    
                } else {
                    informationArea.setCaretPosition(informationArea.getDocument().getEndPosition().getOffset() > currentlySelectedActivityCaretPosition ? currentlySelectedActivityCaretPosition : informationArea.getDocument().getEndPosition().getOffset() - 1);
                }
            }
        });
        previewButton.setVisible(false);
        add(previewButton, gbc);
        // html/editor button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.weighty = 0.3;
        gbc.gridwidth = 5;
        htmlButton.addActionListener(new ActionListener() {

            /**
             * Switch modes (Editor <--> HTML)
             *
             * Problem unsolved: when switching, there is no apparent way to
             * know the corresponding caret position (viewToModel won't help)
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (informationArea.isEditorOrPreviewMode()) { // editor mode --> html mode;                    
                    displayHTMLMode();
                } else { // html mode --> editor mode
                    displayEditorMode();
                }
                currentlySelectedActivityCaretPosition = 0; // reset
                // disable auto scrolling
                informationArea.setCaretPosition(0);
                // Show caret
                informationArea.requestFocusInWindow();
            }
        });
        htmlButton.setVisible(false);
        htmlButton.setToolTipText("HTML 3.2");
        add(htmlButton, gbc);
        // Formatting actions
        class boldAction extends StyledEditorKit.BoldAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                String selectedText = informationArea.getSelectedText();
                if (selectedText != null && selectedText.length() > 0) {
                    //informationArea.setCaretPosition(informationArea.getSelectionEnd());
                    displaySaveCancelButton();
                    currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                    currentlySelectedActivityText = informationArea.getText();
                }
                // show caret
                informationArea.requestFocusInWindow();
            }
        }
        class italicAction extends StyledEditorKit.ItalicAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                String selectedText = informationArea.getSelectedText();
                if (selectedText != null && selectedText.length() > 0) {
                    //informationArea.setCaretPosition(informationArea.getSelectionEnd());
                    displaySaveCancelButton();
                    currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                    currentlySelectedActivityText = informationArea.getText();
                }
                // show caret
                informationArea.requestFocusInWindow();
            }
        }
        class underlineAction extends StyledEditorKit.UnderlineAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                String selectedText = informationArea.getSelectedText();
                if (selectedText != null && selectedText.length() > 0) {
                    //informationArea.setCaretPosition(informationArea.getSelectionEnd());
                    displaySaveCancelButton();
                    currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                    currentlySelectedActivityText = informationArea.getText();
                }
                // show caret
                informationArea.requestFocusInWindow();
            }
        }
        // bold button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.02;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        boldButton.addActionListener(new boldAction());
        boldButton.setToolTipText("CTRL + B");
        boldButton.setVisible(false);
        add(boldButton, gbc);
        // set the keystroke on the area (to work in preview mode as well)
        // CTRL B: Bold
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK), "Bold");
        informationArea.getActionMap().put("Bold", new boldAction());
        // italic button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.02;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        italicButton.addActionListener(new italicAction());
        italicButton.setToolTipText("CTRL + I");
        italicButton.setVisible(false);
        add(italicButton, gbc);
        // set the keystroke on the area (to work in preview mode as well)
        // CTRL I: Italic
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "Italic");
        informationArea.getActionMap().put("Italic", new italicAction());
        // underline button
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.02;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        underlineButton.addActionListener(new underlineAction());
        underlineButton.setToolTipText("CTRL + U");
        underlineButton.setVisible(false);
        add(underlineButton, gbc);
        // set the keystroke on the area (to work in preview mode as well)
        // CTRL U: Underline
        informationArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "Underline");
        informationArea.getActionMap().put("Underline", new underlineAction());
        // background color button
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 0.02;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        backgroundColorButton.addActionListener(new ActionListener() {

            /**
             * SPAN tag is to be used as the editor doesn't do the job properly
             *
             * http://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                informationArea.requestFocus(); // display selection
                Color newColor = JColorChooser.showDialog(
                        null,
                        Labels.getString("BurndownChartPanel.Choose a color"),
                        ColorUtil.YELLOW_HIGHLIGHT);
                if (newColor != null) {
                    //Add span Tag
                    String htmlStyle = "background-color:" + getHTMLColor(newColor);
                    SimpleAttributeSet attr = new SimpleAttributeSet();
                    attr.addAttribute(HTML.Attribute.STYLE, htmlStyle);
                    MutableAttributeSet COLOR = new SimpleAttributeSet();
                    COLOR.addAttribute(HTML.Tag.SPAN, attr);
                    StyleConstants.setBackground(COLOR, newColor);
                    String selectedText = informationArea.getSelectedText();
                    if (selectedText != null && selectedText.length() > 0) {
                        int start = informationArea.getSelectionStart();
                        /* There seems to be no easy way to completely remove a style (not replace). Using an EMPTY set of attributes on setCharacterAttributes is completely buggy.
                         if (getHTMLColor(newColor).equalsIgnoreCase("#FFFFFF")) {
                         informationArea.getStyledDocument().setCharacterAttributes(start, informationArea.getSelectionEnd() - start, SimpleAttributeSet.EMPTY, true);                            
                         } else {*/
                        informationArea.getStyledDocument().setCharacterAttributes(start, selectedText.length(), COLOR, false);
                        //}
                        //informationArea.setCaretPosition(informationArea.getSelectionEnd());
                        displaySaveCancelButton();
                        currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                        currentlySelectedActivityText = informationArea.getText();
                    }
                }
                // show caret
                informationArea.requestFocusInWindow();
            }

            /**
             * Convert a Java Color to equivalent HTML Color.
             *
             * @param color The Java Color
             * @return The String containing HTML Color.
             */
            public String getHTMLColor(Color color) {
                if (color == null) {
                    return "#000000";
                }
                return "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
            }
        });
        backgroundColorButton.setVisible(false);
        add(backgroundColorButton, gbc);
        // foreground color button
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 0.02;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        foregroundColorButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                informationArea.requestFocus(); // display selection
                Color newColor = JColorChooser.showDialog(
                        null,
                        Labels.getString("BurndownChartPanel.Choose a color"),
                        Color.BLACK);
                if (newColor != null) {
                    MutableAttributeSet COLOR = new SimpleAttributeSet();
                    StyleConstants.setForeground(COLOR, newColor);
                    String selectedText = informationArea.getSelectedText();
                    if (selectedText != null && selectedText.length() > 0) {
                        int start = informationArea.getSelectionStart();
                        informationArea.getStyledDocument().setCharacterAttributes(start, selectedText.length(), COLOR, false);
                        //informationArea.setCaretPosition(informationArea.getSelectionEnd());
                        displaySaveCancelButton();
                        currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                        currentlySelectedActivityText = informationArea.getText();
                    }
                }
                // show caret
                informationArea.requestFocusInWindow();
            }
        });
        foregroundColorButton.setVisible(false);
        add(foregroundColorButton, gbc);
        // add link field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.08;
        gbc.weighty = 0.1;
        gbc.gridwidth = 4;
        linkTextField.setText("http://");
        linkTextField.setVisible(false);
        add(linkTextField, gbc);
        // add link button
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.weightx = 0.02;
        gbc.weighty = 0.1;
        gbc.gridwidth = 1;
        if (getFont().canDisplay('\u21e8')) {
            linkButton.setText("\u21e8");
        } else {
            linkButton.setText(">>");
        }
        // ENTER: insert link (requires focus on the link text field) (WHEN_IN_FOCUSED_WINDOW must be used here)        
        linkButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Create link");
        class linkEnterAction extends AbstractAction {

            /**
             * Insert HTML link
             *
             * Problem unsolved: after carriage return, the link is inserted at
             * the end of the previous line not the beginning of the new line
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int start = informationArea.getCaretPosition();
                    if (!linkTextField.getText().isEmpty()) {
                        String href = linkTextField.getText().startsWith("www") ? ("http://" + linkTextField.getText()) : linkTextField.getText();
                        String link = "<a href=\"" + href + "\">" + linkTextField.getText() + "</a>";
                        informationArea.insertText(start, link, HTML.Tag.A);
                        informationArea.setCaretPosition(start + linkTextField.getText().length());
                        // Show caret
                        informationArea.requestFocusInWindow();
                        linkTextField.setText("http://"); // reset field
                        displaySaveCancelButton();
                        currentlySelectedActivityCaretPosition = informationArea.getCaretPosition();
                        currentlySelectedActivityText = informationArea.getText();
                    }
                } catch (BadLocationException ignored) {
                } catch (IOException ignored) {
                }
                // Show caret
                informationArea.requestFocusInWindow();
            }
        }
        linkButton.getActionMap().put("Create link", new linkEnterAction());
        linkButton.setToolTipText("ENTER");
        linkButton.addActionListener(new linkEnterAction());
        linkButton.setVisible(false);
        add(linkButton, gbc);
    }

    private void addExpandButton() {
        // Expand button
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0.02; // 2 %
        gbc.weighty = 1.0;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.BOTH;
        //Remove marging /transparent border around text 
        expandButton.setBorder(null);
        expandButton.setBorderPainted(false);
        expandButton.setMargin(new Insets(0, 0, 0, 0));
        expandButton.setToolTipText(Labels.getString("Common.Show editor"));
        expandButton.addMouseListener(new ExpandMouseAdapter());
        expandButton.setVisible(false);
        add(expandButton, gbc);
    }

    class ExpandMouseAdapter extends MouseAdapter {

        private Robot robot = null; // used to move the cursor

        public ExpandMouseAdapter() {
            try {
                robot = new Robot();
            } catch (AWTException ignored) {
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (informationArea.isEditable()) {
                Point pOriginal = e.getLocationOnScreen(); // original location on screen
                if (previewButton.isVisible()) {
                    hideEditorButtons();
                    expandButton.setText(rightArrow);
                    expandButton.setToolTipText(Labels.getString("Common.Show editor"));
                } else {
                    showEditorButtons();
                    expandButton.setText(leftArrow);
                    expandButton.setToolTipText(Labels.getString("Common.Hide editor"));
                }
                // Show caret
                informationArea.requestFocusInWindow();
                // The following line is required to get the cursor to move correctly        
                CommentPanel.this.validate();
                // Center cursor on button
                if (robot != null) {
                    Point pFinal = expandButton.getLocationOnScreen(); // final location on screen
                    // Set cursor at the same original Y position
                    robot.mouseMove((int) pFinal.getX() + expandButton.getWidth() / 2, (int) pOriginal.getY());
                }
            }
        }
    }

    private void addCommentArea() {
        JPanel commentArea = new JPanel();
        commentArea.setLayout(new GridBagLayout());
        GridBagConstraints commentgbc = new GridBagConstraints();
        if (showIconLabel) { // icon label panel (ToDo list / Iteration panel)
            commentgbc.gridx = 0;
            commentgbc.gridy = 0;
            commentgbc.fill = GridBagConstraints.BOTH;
            commentgbc.weightx = 1.0;
            iconPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            commentArea.add(iconPanel, commentgbc);
        }
        // add the comment area
        commentgbc.gridx = 0;
        commentgbc.gridy = showIconLabel ? 1 : 0;
        commentgbc.fill = GridBagConstraints.BOTH;
        commentgbc.weightx = 1.0;
        commentgbc.weighty = 1.0;
        informationArea.setEditable(false);
        commentArea.add(scrollPaneInformationArea, commentgbc);
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.weighty = 1.0;
        gbc.gridheight = 4;
        add(commentArea, gbc);
    }

    private void addSaveButton() {
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 0.8;
        gbc.gridheight = 3;
        // set the keystrokes on the button (won't work on the text pane)
        // CTRL S: Save
        Action saveAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Metatag charset directive
                // using the HTML 5 short directive http://www.w3schools.com/tags/att_meta_charset.asp
                // <meta charset="UTF-8">
                // IgnoreCharsetDirective set to true on HTMLEditor
                String comment = informationArea.getText();
                // Jsoup: parsing the html content without reformating (because JSoup is HTML 5 compliant only - not 3.2)            
                Document doc = Jsoup.parse(comment, "UTF-8", Parser.xmlParser());
                Elements elements = doc.getElementsByTag(HTML.Tag.META.toString());
                if (elements.isEmpty()) {
                    org.jsoup.nodes.Element tagMetaCharset = new org.jsoup.nodes.Element(Tag.valueOf("meta"), "");
                    tagMetaCharset.attr("charset", "UTF-8");
                    doc.head().appendChild(tagMetaCharset);
                    comment = doc.toString();
                }
                panel.getCurrentTable().saveComment(StringEscapeUtils.unescapeHtml4(comment)); // remove HTML encoding; eg: &nbsp; --> semicolon 
                //if (previewButton.isVisible()) { // editor opened; no switch to preview mode
                // show caret
                informationArea.requestFocusInWindow();
                /*} else {
                 previewButton.getActionListeners()[0].actionPerformed(e);
                 }*/
                hideSaveCancelButton();
                Activity activity = panel.getCurrentTable().getActivityFromSelectedRow();
                currentlySelectedActivityText = ""; // reset
                showInfo(activity);
            }
        };
        // WHEN_IN_FOCUSED_WINDOW makes Save shortcut work (WHEN_FOCUSED doesn't)
        saveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "Save");
        saveButton.getActionMap().put("Save", saveAction);
        saveButton.setToolTipText(Labels.getString("Common.Save") + " (CTRL + S)");
        saveButton.setVisible(false);
        saveButton.addActionListener(saveAction);
        // Set the width of the button to make it shorter
        Dimension dimension = saveButton.getSize();
        dimension.width = 50;
        saveButton.setMinimumSize(dimension);
        saveButton.setPreferredSize(dimension);
        add(saveButton, gbc);
    }

    private void addCancelButton() {
        gbc.gridx = 7;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        gbc.weighty = 0.2;
        gbc.gridheight = 1;
        cancelButton.setVisible(false);
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                previewButton.getActionListeners()[0].actionPerformed(e);
                hideSaveCancelButton();
                Activity activity = panel.getCurrentTable().getActivityFromSelectedRow();
                currentlySelectedActivityCaretPosition = 0; // reset
                currentlySelectedActivityText = ""; // reset
                showInfo(activity);
            }
        });
        add(cancelButton, gbc);
    }

    public void showInfo(final Activity activity) {
        String comment = activity.getNotes().trim();
        // Backward compatility 3.0.X and imported data
        // Using Jsoup to check for HTML tag; if none is found, replace trailing return carriage with P tag 
        if (!comment.isEmpty()) {
            // Jsoup: parsing the html content without reformating (because JSoup is HTML 5 compliant only - not 3.2)            
            Document doc = Jsoup.parse(comment, "UTF-8", Parser.xmlParser());
            Elements elements = doc.getElementsByTag(HTML.Tag.BODY.toString());
            if (elements.isEmpty()) {
                comment = "<p style=\"margin-top: 0\">" + comment;
                // Using regex \\r|\\n rather than System.getProperty("line.separator")
                // 1- System.getProperty("line.separator") does not seem to work in this peculiar case
                // 2- the database could come from any system
                comment = comment.replaceAll("\\r|\\n", "</p><p style=\"margin-top: 0\">");
                comment = comment + "</p>";
            }
        }
        if (comment.isEmpty() && activity.isStory()) {
            // default template for User Story type
            comment = "<p style=\"margin-top: 0\">";
            comment += "<b>Story line</b>";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += Labels.getString("Agile.ActivityListPanel.As a role");
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "<b>User acceptance criteria</b>";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "+ ...";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "+ ...";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "+ ...";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "<b>Test cases</b>";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "+ ...";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "+ ...";
            comment += "</p>";
            comment += "<p style=\"margin-top: 0\">";
            comment += "+ ...";
            comment += "</p>";
        }
        int selectedActivityId = panel.getCurrentTable().getActivityIdFromSelectedRow();
        if (selectedActivityId == activity.getId()) { // Activity actually selected
            if (selectedActivityId != currentlySelectedActivityId) { // New activity selected (compare to the current selected one)
                previewButton.doClick(); // equivalent to previewButton.getActionListeners()[0].actionPerformed(e);
                hideSaveCancelButton();
                currentlySelectedActivityId = selectedActivityId;
                currentlySelectedActivityText = comment; // init
                currentlySelectedActivityCaretPosition = 0; // reset
            } else if (currentlySelectedActivityText.length() > 0
                    && !comment.equalsIgnoreCase(currentlySelectedActivityText)) { // Currently selected activity was previously modified
                comment = currentlySelectedActivityText;
                displaySaveCancelButton();
            }
            if (informationArea != null) { // informationArea may be null when hovering the mouse over the table while prioritising, moving, deleting... rows
                informationArea.setText(comment);
                // Set caret position            
                //try {
                informationArea.setCaretPosition(informationArea.getDocument().getEndPosition().getOffset() > currentlySelectedActivityCaretPosition ? currentlySelectedActivityCaretPosition : informationArea.getDocument().getEndPosition().getOffset() - 1);
                //} catch (java.lang.IllegalArgumentException ex) { // bad position exception
                //}
            }
            // Warning: do not request focus in Window here. Focus will be lost on table hence prevent shorcuts and combos from working
        } else { // Activity actually hovered on with the mouse
            hideSaveCancelButton();
            informationArea.setText(comment);
            // disable auto scrolling
            informationArea.setCaretPosition(0);
        }
    }

    public JPanel getIconPanel() {
        return iconPanel;
    }

    private void displayHTMLMode() {
        String text = informationArea.getText();
        informationArea.setContentType("text/plain");
        informationArea.setText(text);
        htmlButton.setText(Labels.getString("Common.Editor"));
        htmlButton.setToolTipText(null);
        boldButton.setVisible(false);
        italicButton.setVisible(false);
        underlineButton.setVisible(false);
        backgroundColorButton.setVisible(false);
        foregroundColorButton.setVisible(false);
        linkTextField.setVisible(false);
        linkButton.setVisible(false);
        expandButton.setVisible(false);
    }

    private void displayEditorMode() {
        String text = informationArea.getText();
        informationArea.setContentType("text/html");
        informationArea.setText(text);
        htmlButton.setText("HTML");
        htmlButton.setToolTipText("HTML 3.2");
        boldButton.setVisible(true);
        italicButton.setVisible(true);
        underlineButton.setVisible(true);
        backgroundColorButton.setVisible(true);
        foregroundColorButton.setVisible(true);
        linkTextField.setVisible(true);
        linkButton.setVisible(true);
        expandButton.setVisible(true);
    }

    private void displayPreviewMode() {
        String text = informationArea.getText();
        informationArea.setContentType("text/html");
        informationArea.setText(text);
        htmlButton.setText("HTML");
        htmlButton.setToolTipText("HTML 3.2");
        previewButton.setVisible(false);
        htmlButton.setVisible(false);
        boldButton.setVisible(false);
        italicButton.setVisible(false);
        underlineButton.setVisible(false);
        backgroundColorButton.setVisible(false);
        foregroundColorButton.setVisible(false);
        linkTextField.setVisible(false);
        linkButton.setVisible(false);
        expandButton.setVisible(false);
    }

    private void displaySaveCancelButton() {
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
    }

    private void hideSaveCancelButton() {
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    private void hideEditorButtons() {
        previewButton.setVisible(false);
        htmlButton.setVisible(false);
        boldButton.setVisible(false);
        italicButton.setVisible(false);
        underlineButton.setVisible(false);
        backgroundColorButton.setVisible(false);
        foregroundColorButton.setVisible(false);
        linkTextField.setVisible(false);
        linkButton.setVisible(false);
    }

    private void showEditorButtons() {
        previewButton.setVisible(true);
        htmlButton.setVisible(true);
        boldButton.setVisible(true);
        italicButton.setVisible(true);
        underlineButton.setVisible(true);
        backgroundColorButton.setVisible(true);
        foregroundColorButton.setVisible(true);
        linkTextField.setVisible(true);
        linkButton.setVisible(true);
    }
}
