/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console.editor;

import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.meteoinfo.console.NameCompletion;
import org.meteoinfo.console.Popup;
import org.meteoinfo.console.Tip;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Yaqiang
 */
public class MITextEditorPane extends TextEditorPane {

    // <editor-fold desc="Variables">
    private Segment seg;
    private NameCompletion nameCompletion;
    private Popup popup;
    private Tip tip;
    private int dotWidth;
    private int textHeight;
    private final Pattern FROM_PACKAGE_IMPORT = Pattern.compile("from\\s+(\\w+(?:\\.\\w+)*)\\.?(?:\\s*import\\s*)?");

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MITextEditorPane() {
        popup = new Popup(null, this);
        tip = new Tip(null);
        FontMetrics metrics = this.getFontMetrics(this.getFont());
        this.dotWidth = metrics.stringWidth(".");
        this.textHeight = metrics.getHeight();
        seg = new Segment();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                type(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (popup.isVisible()) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        popup.type(e);
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {                
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tip.isVisible()) {
                    tip.setVisible(false);
                }
                if (popup.isVisible()) {
                    popup.setVisible(false);
                }
            }
        });
    }

    private synchronized void type(KeyEvent e) {
        if (this.popup.isVisible()) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                this.popup.type(e);
            }
            return;
        }

        switch (e.getKeyCode()) {
            case (KeyEvent.VK_PERIOD):
                showPopup();
                e.consume();
                break;
            case (KeyEvent.VK_SPACE):
                String command = getCurrentText();
                Matcher match = FROM_PACKAGE_IMPORT.matcher(command);
                if (match.matches()) {
                    showPopup();
                }
                e.consume();
                break;
            case (KeyEvent.VK_9):
                if (e.isShiftDown()) {
                    showTip();
                }
                e.consume();
                break;
            case (KeyEvent.VK_0):
                if (e.isShiftDown()) {
                    tip.setVisible(false);
                }
                e.consume();
                break;
            case (KeyEvent.VK_LEFT):
            case (KeyEvent.VK_BACK_SPACE):
            case (KeyEvent.VK_DELETE):
                if (tip.isVisible()) {
                    tip.setVisible(false);
                }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Set name completion
     *
     * @param value Name completion
     */
    public void setNameCompletion(NameCompletion value) {
        this.nameCompletion = value;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    private String getCurrentText() {
        Document doc = this.getDocument();
        int dot = this.getCaretPosition();
        Element root = doc.getDefaultRootElement();
        int index = root.getElementIndex(dot);
        Element elem = root.getElement(index);
        int start = elem.getStartOffset();
        int len = dot - start;
        try {
            doc.getText(start, len, seg);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
            return "";
        }
        return seg.toString();
    }

    private void showPopup() {
        if (nameCompletion == null) {
            return;
        }

        String part = this.getCurrentText();
        if (part.length() < 2) // reasonable completion length
        {
            return;
        }

        String[] complete = nameCompletion.completeName(part);
        if (complete == null) {
            //java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        if (complete.length == 0) {
            //java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        // Found one completion (possibly what we already have
        if (complete.length == 1 && !complete.equals(part)) {
            //String append = complete[0].substring(part.length());
            int slen = this.getCaretPosition();
            this.select(slen, slen);
            this.replaceSelection(complete[0]);
            //append(complete[0]);
            return;
        }

        this.popup.showMethodCompletionList(complete, this.getDisplayPoint());
    }

    private void showTip() {
        if (nameCompletion == null) {
            return;
        }

        String part = this.getCurrentText();
        if (part.length() < 2) // reasonable completion length
        {
            return;
        }
//        String s = part.trim().substring(part.length() - 2, part.length() - 1);
//        if (!Character.isLetter(s.charAt(0)))
//            return;

        if (this.popup.isVisible()) {
            this.popup.setVisible(false);
        }

        String[] callTip = nameCompletion.getTip(part);
        String tipstr = callTip[2];
        if (!tipstr.isEmpty()) {
            Point displayPoint = this.getDisplayPoint();
            this.tip.setText(tipstr);
            this.tip.showTip(displayPoint);
        }
    }

    private Point getDisplayPoint() {
        //Get the point where the popup window should be displayed
        Point screenPoint = this.getLocationOnScreen();
        Point caretPoint = this.getCaret().getMagicCaretPosition();
        if (caretPoint == null) {
            caretPoint = new Point(0, 0);
        }

        int x = (int) (screenPoint.getX() + caretPoint.getX() + this.dotWidth);
        int y = (int) (screenPoint.getY() + caretPoint.getY() + this.textHeight) + 5;
        if (y < 0) {
            //y = this.getY() + this.getHeight();
            y = this.getLocationOnScreen().y;
        }

        return new Point(x, y);
    }
    // </editor-fold>
}
