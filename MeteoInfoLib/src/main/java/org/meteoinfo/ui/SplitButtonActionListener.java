
package org.meteoinfo.ui;

import java.awt.event.ActionEvent;
import java.util.EventListener;

/**
 * The listener interface for receiving action events.
 * The class that is interested in processing an action event
 * implements this interface, and the object created with that
 * class is registered with a component, using the component's
 * <code>addSplitButtonActionListener</code> method. When the action event
 * occurs, that object's <code>buttonClicked</code> or <code>splitButtonClicked</code>
 * method is invoked.
 *
 * @see ActionEvent
 *
 * @author Naveed Quadri
 */
public interface SplitButtonActionListener extends EventListener {

    /**
     * Invoked when the button part is clicked.
     * @param e
     */
    public void buttonClicked(ActionEvent e);

    /**
     * Invoked when split part is clicked.
     * @param e
     */
    public void splitButtonClicked(ActionEvent e);

}
