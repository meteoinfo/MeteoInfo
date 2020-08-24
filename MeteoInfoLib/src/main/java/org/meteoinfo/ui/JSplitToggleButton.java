package org.meteoinfo.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * An implementation of a "split" button.The left side acts like a normal
 * button, right side has a jPopupMenu attached.
 * This class raises two events.
 * <ol>
 * <li>buttonClicked(e); //when the button is clicked</li>
 * <li>splitButtonClicked(e; //when the split part of the button is clicked)
 * </li>
 * </ol>
 * You need to subscribe to SplitButtonActionListener to handle these
 * events.
 *
 * Use as you wish, but an acknowlegement would be appreciated, ;) 
 * <b>Known Issue:</b>
 * The 'button part' of the splitbutton is being drawn without the border??? and
 * this is only happening in CDE/Motif and Metal Look and Feels. GTK+ and nimbus
 * works perfect. No Idea why? if anybody could point out the mistake that'd be
 * nice.My email naveedmurtuza[at]gmail.com
 * P.S. The fireXXX methods has been directly plagarized from JDK source code,
 * and yes even the javadocs..;)
 * The border bug in metal Look and Feel is now fixed. Thanks to Hervé Guillaume.
 *
 * @author Naveed Quadri
 */
public class JSplitToggleButton extends JToggleButton implements MouseMotionListener, MouseListener, ActionListener, Serializable {

    private int separatorSpacing = 4;
    private int splitWidth = 22;
    private int arrowSize = 8;
    private boolean onSplit;
    private Rectangle splitRectangle;
    private JPopupMenu popupMenu;
    private boolean alwaysDropDown;
    private Color arrowColor = Color.BLACK;
    private Color disabledArrowColor = Color.GRAY;
    private Image image;
    protected SplitButtonActionListener splitButtonActionListener = null;

    /**
     * Creates a button with initial text and an icon.
     *
     * @param text the text of the button
     * @param icon the Icon image to display on the button
     */
    public JSplitToggleButton(String text, Icon icon) {
        super(text, icon);
        addMouseMotionListener(this);
        addMouseListener(this);
        addActionListener(this);
    }

    /**
     * Creates a button with text.
     *
     * @param text the text of the button
     */
    public JSplitToggleButton(String text) {
        this(text, null);
    }

    /**
     * Creates a button with an icon.
     *
     * @param icon the Icon image to display on the button
     */
    public JSplitToggleButton(Icon icon) {
        this(null, icon);
    }

    /**
     * Creates a button with no set text or icon.
     */
    public JSplitToggleButton() {
        this(null, null);
    }

    /**
     * Returns the JPopupMenu if set, null otherwise.
     *
     * @return JPopupMenu
     */
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    /**
     * Sets the JPopupMenu to be displayed, when the split part of the button is
     * clicked.
     *
     * @param popupMenu
     */
    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
        image = null; //to repaint the arrow image
    }

    /**
     * Returns the separatorSpacing. Separator spacing is the space above and
     * below the separator( the line drawn when you hover your mouse over the
     * split part of the button).
     *
     * @return separatorSpacingimage = null; //to repaint the image with the new
     * size
     */
    public int getSeparatorSpacing() {
        return separatorSpacing;
    }

    /**
     * Sets the separatorSpacing.Separator spacing is the space above and below
     * the separator( the line drawn when you hover your mouse over the split
     * part of the button).
     *
     * @param separatorSpacing
     */
    public void setSeparatorSpacing(int separatorSpacing) {
        this.separatorSpacing = separatorSpacing;
    }

    /**
     * Show the dropdown menu, if attached, even if the button part is clicked.
     *
     * @return true if alwaysDropdown, false otherwise.
     */
    public boolean isAlwaysDropDown() {
        return alwaysDropDown;
    }

    /**
     * Show the dropdown menu, if attached, even if the button part is clicked.
     *
     * @param alwaysDropDown true to show the attached dropdown even if the
     * button part is clicked, false otherwise
     */
    public void setAlwaysDropDown(boolean alwaysDropDown) {
        this.alwaysDropDown = alwaysDropDown;
    }

    /**
     * Gets the color of the arrow.
     *
     * @return arrowColor
     */
    public Color getArrowColor() {
        return arrowColor;
    }

    /**
     * Set the arrow color.
     *
     * @param arrowColor
     */
    public void setArrowColor(Color arrowColor) {
        this.arrowColor = arrowColor;
        image = null; //to repaint the image with the new color
    }

    /**
     * gets the disabled arrow color
     *
     * @return disabledArrowColor color of the arrow if no popup attached.
     */
    public Color getDisabledArrowColor() {
        return disabledArrowColor;
    }

    /**
     * sets the disabled arrow color
     *
     * @param disabledArrowColor color of the arrow if no popup attached.
     */
    public void setDisabledArrowColor(Color disabledArrowColor) {
        this.disabledArrowColor = disabledArrowColor;
        image = null; //to repaint the image with the new color
    }

    /**
     * Splitwidth is the width of the split part of the button.
     *
     * @return splitWidth
     */
    public int getSplitWidth() {
        return splitWidth;
    }

    /**
     * Splitwidth is the width of the split part of the button.
     *
     * @param splitWidth
     */
    public void setSplitWidth(int splitWidth) {
        this.splitWidth = splitWidth;
    }

    /**
     * Get split rectangle
     * @return Split rectangle
     */
    public Rectangle getSplitRectangle() { return this.splitRectangle; }

    /**
     * Get if the mouse point on split rectangle
     * @return Boolean
     */
    public boolean isOnSplit() { return this.onSplit; }

    /**
     * gets the size of the arrow.
     *
     * @return size of the arrow
     */
    public int getArrowSize() {
        return arrowSize;
    }

    /**
     * sets the size of the arrow
     *
     * @param arrowSize
     */
    public void setArrowSize(int arrowSize) {
        this.arrowSize = arrowSize;
        image = null; //to repaint the image with the new size
    }

    /**
     * Gets the image to be drawn in the split part. If no is set, a new image
     * is created with the triangle.
     *
     * @return image
     */
    public Image getImage() {
        if (image != null) {
            return image;
        } else {
            Graphics2D g = null;
            BufferedImage img = new BufferedImage(arrowSize, arrowSize, BufferedImage.TYPE_INT_RGB);
            g = (Graphics2D) img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            g.setColor(popupMenu != null ? arrowColor : disabledArrowColor);
            //this creates a triangle facing right >
            g.fillPolygon(new int[]{0, 0, arrowSize / 2}, new int[]{0, arrowSize, arrowSize / 2}, 3);
            g.dispose();
            //rotate it to face downwards
            img = rotate(img, 90);
            BufferedImage dimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            g = (Graphics2D) dimg.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(img, null, 0, 0);
            g.dispose();
            for (int i = 0; i < dimg.getHeight(); i++) {
                for (int j = 0; j < dimg.getWidth(); j++) {
                    if (dimg.getRGB(j, i) == Color.WHITE.getRGB()) {
                        dimg.setRGB(j, i, 0x8F1C1C);
                    }
                }
            }

            image = Toolkit.getDefaultToolkit().createImage(dimg.getSource());
            return image;
        }
    }

    /**
     * Sets the image to draw instead of the triangle.
     *
     * @param image
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics gClone = g.create();//EDIT: Hervé Guillaume
        Color oldColor = gClone.getColor();
        splitRectangle = new Rectangle(getWidth() - splitWidth, 0, splitWidth, getHeight());
        gClone.translate(splitRectangle.x, splitRectangle.y);
        int mh = getHeight() / 2;
        int mw = splitWidth / 2;
        gClone.drawImage(getImage(), mw - arrowSize / 2, mh + 2 - arrowSize / 2, null);
        if (onSplit && !alwaysDropDown && popupMenu != null) {
            gClone.setColor(UIManager.getLookAndFeelDefaults().getColor("Button.background"));
            gClone.drawLine(1, separatorSpacing + 2, 1, getHeight() - separatorSpacing - 2);
            gClone.setColor(UIManager.getLookAndFeelDefaults().getColor("Button.shadow"));
            gClone.drawLine(2, separatorSpacing + 2, 2, getHeight() - separatorSpacing - 2);
        }
        gClone.setColor(oldColor);
    }

    /**
     * Rotates the given image with the specified angle.
     *
     * @param img image to rotate
     * @param angle angle of rotation
     * @return rotated image
     */
    private BufferedImage rotate(BufferedImage img, int angle) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(w, h, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.rotate(Math.toRadians(angle), w / 2, h / 2);
        g.drawImage(img, null, 0, 0);
        return dimg;
    }

    /**
     * Adds an <code>SplitButtonActionListener</code> to the button.
     *
     * @param l the <code>ActionListener</code> to be added
     */
    public void addSplitButtonActionListener(SplitButtonActionListener l) {
        listenerList.add(SplitButtonActionListener.class, l);
    }

    /**
     * Removes an <code>SplitButtonActionListener</code> from the button. If the
     * listener is the currently set <code>Action</code> for the button, then
     * the <code>Action</code> is set to <code>null</code>.
     *
     * @param l the listener to be removed
     */
    public void removeSplitButtonActionListener(SplitButtonActionListener l) {
        if ((l != null) && (getAction() == l)) {
            setAction(null);
        } else {
            listenerList.remove(SplitButtonActionListener.class, l);
        }
    }

    /**
     *
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if (splitRectangle.contains(e.getPoint())) {
            onSplit = true;
        } else {
            onSplit = false;
        }
        repaint(splitRectangle);
    }

    /**
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (popupMenu == null) {
            fireButtonClicked(e);
        } else if (alwaysDropDown) {
            //popupMenu.show(this, getWidth() - (int) popupMenu.getPreferredSize().getWidth(), getHeight());
            popupMenu.show(this, 0, getHeight());
            fireButtonClicked(e);
        } else if (onSplit) {
            //popupMenu.show(this, getWidth() - (int) popupMenu.getPreferredSize().getWidth(), getHeight());
            popupMenu.show(this, 0, getHeight());
            fireSplitbuttonClicked(e);
        } else {
            fireButtonClicked(e);
        }
    }

    /**
     *
     * @param e
     */
    public void mouseExited(MouseEvent e) {
        onSplit = false;
        repaint(splitRectangle);
    }
// <editor-fold defaultstate="collapsed" desc="Unused Listeners">

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }
// </editor-fold>

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * <code>event</code> parameter.
     *
     * @param event the <code>ActionEvent</code> object
     * @see EventListenerList
     */
    private void fireButtonClicked(ActionEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SplitButtonActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                    String actionCommand = event.getActionCommand();
                    if (actionCommand == null) {
                        actionCommand = getActionCommand();
                    }
                    e = new ActionEvent(JSplitToggleButton.this,
                            ActionEvent.ACTION_PERFORMED,
                            actionCommand,
                            event.getWhen(),
                            event.getModifiers());
                }
                ((SplitButtonActionListener) listeners[i + 1]).buttonClicked(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * <code>event</code> parameter.
     *
     * @param event the <code>ActionEvent</code> object
     * @see EventListenerList
     */
    private void fireSplitbuttonClicked(ActionEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SplitButtonActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                    String actionCommand = event.getActionCommand();
                    if (actionCommand == null) {
                        actionCommand = getActionCommand();
                    }
                    e = new ActionEvent(JSplitToggleButton.this,
                            ActionEvent.ACTION_PERFORMED,
                            actionCommand,
                            event.getWhen(),
                            event.getModifiers());
                }
                ((SplitButtonActionListener) listeners[i + 1]).splitButtonClicked(e);
            }
        }
    }
}
