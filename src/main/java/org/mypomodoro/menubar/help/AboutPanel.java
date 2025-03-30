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
package org.mypomodoro.menubar.help;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.mypomodoro.Main;
import org.mypomodoro.gui.ImageIcons;
import org.mypomodoro.util.BareBonesBrowserLaunch;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.Labels;

/**
 * GUI for myPomodoro about menu. Using JDialog to remove minimize and maximize
 * icons
 *
 */
public class AboutPanel extends JDialog {

    public static final int FRAME_WIDTH = 630;
    public static final int FRAME_HEIGHT = 360;
    private final GridBagConstraints gbc = new GridBagConstraints();

    public AboutPanel(String str) {
        setTitle(str);
        setFont((new JPanel()).getFont()); // this is the only place where the font has to be set manually
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // garbage collection
        setIconImage(ImageIcons.MAIN_ICON.getImage());
        setResizable(false);

        JPanel about = new JPanel();
        about.setLayout(new GridBagLayout());
        about.setOpaque(true);
        add(about);

        setContentPane(about);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        setLayout(new GridBagLayout());

        addMyAgilePomodoroImage();
        addAbout();
        addLicence();
    }

    private void addMyAgilePomodoroImage() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JLabel backgroundImage = new JLabel(new ImageIcon(
                Main.class.getResource("/images/mAPAbout.png")));
        JPanel panel = new JPanel();
        panel.add(backgroundImage);
        panel.setBackground(ColorUtil.WHITE); // This stays White despite the background or the current theme
        add(panel, gbc);
    }

    private void addAbout() {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS); // left
        // alignment
        panel.setLayout(layout);
        panel.setForeground(ColorUtil.BLACK);  // This stays Black despite the foreground or the current theme
        panel.setBackground(ColorUtil.WHITE);  // This stays White despite the background or the current theme
        GridBagConstraints gbcpanel = new GridBagConstraints();
        gbcpanel.gridx = 0;
        gbcpanel.gridy = 0;
        gbcpanel.fill = GridBagConstraints.BOTH;
        JLabel title = new JLabel("myAgilePomodoro");
        title.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize() + 24));
        title.setForeground(ColorUtil.BLACK); // This stays Black despite the foreground or the current theme
        panel.add(title, gbcpanel);
        /*gbcpanel.gridx = 0;
         gbcpanel.gridy = 1;
         gbcpanel.fill = GridBagConstraints.BOTH;
         JLabel version = new JLabel(MainPanel.MYPOMODORO_VERSION);
         version.setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize() + 2));
         panel.add(version, gbcpanel);*/
        gbcpanel.gridx = 0;
        gbcpanel.gridy = 1;
        gbcpanel.fill = GridBagConstraints.BOTH;
        JButton checkButton = new JButton(
                Labels.getString("AboutPanel.Check for Updates"));
        checkButton.setFocusPainted(false);
        checkButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                BareBonesBrowserLaunch.openURL("http://sourceforge.net/projects/mypomodoro/files");
            }
        });
        panel.add(checkButton, gbcpanel);
        gbcpanel.gridx = 0;
        gbcpanel.gridy = 2;
        gbcpanel.fill = GridBagConstraints.BOTH;
        String about = Labels.getString("AboutPanel.myPomodoro is a time management tool");
        JTextArea aboutTextArea = new JTextArea();
        aboutTextArea.setEditable(false);
        aboutTextArea.setLineWrap(true);
        aboutTextArea.setWrapStyleWord(true);
        aboutTextArea.setText(about);
        aboutTextArea.setFont(getFont().deriveFont(Font.PLAIN));
        aboutTextArea.setOpaque(false);
        aboutTextArea.setAlignmentX(LEFT_ALIGNMENT); // left alignment
        aboutTextArea.setForeground(ColorUtil.BLACK);  // This stays Black despite the foreground or the current theme
        aboutTextArea.setBackground(ColorUtil.WHITE);  // This stays White despite the background or the current theme
        panel.add(aboutTextArea, gbcpanel);
        gbcpanel.gridx = 0;
        gbcpanel.gridy = 3;
        gbcpanel.fill = GridBagConstraints.BOTH;
        String credits = Labels.getString("AboutPanel.Consider donating if you can");
        JTextArea creditsTextArea = new JTextArea();
        creditsTextArea.setEditable(false);
        creditsTextArea.setLineWrap(true);
        creditsTextArea.setWrapStyleWord(true);
        creditsTextArea.setText(credits);
        creditsTextArea.setFont(getFont().deriveFont(Font.PLAIN));
        creditsTextArea.setOpaque(false);
        creditsTextArea.setAlignmentX(LEFT_ALIGNMENT); // left alignment
        creditsTextArea.setForeground(ColorUtil.BLACK);  // This stays Black despite the foreground or the current theme
        creditsTextArea.setBackground(ColorUtil.WHITE);  // This stays White despite the background or the current theme
        panel.add(creditsTextArea, gbcpanel);

        add(panel, gbc);
    }

    private void addLicence() {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        String license = Labels.getString("AboutPanel.myPomodoro is open-source software");
        license += "<br>"
                + Labels.getString("AboutPanel.All documentation and images are licensed");
        JEditorPane editorPane = new JEditorPane("text/html", license);
        editorPane.setForeground(new JPanel().getForeground()); // set theme foreground color
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                Boolean.TRUE);
        editorPane.setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize() - 4));
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    BareBonesBrowserLaunch.openURL(hle.getURL().toString());
                }
            }
        });

        JPanel panel = new JPanel();
        // Wrap content!
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        panel.add(editorPane, constraints);
        add(panel, gbc);
    }
}
