package org.meteoinfo.console.jython;

import org.fife.ui.autocomplete.Completion;

import java.awt.*;


/**
 * Interface for Jython source code completions.
 *
 * @author Yaqiang Wang
 */
public interface JythonSourceCompletion extends Completion {


	/**
	 * Force subclasses to override equals().
	 * TODO: Remove me
	 */
	@Override
	public boolean equals(Object obj);


	/**
	 * Used by JythonCellRenderer to render this completion choice.
	 *
	 * @param g
	 * @param x
	 * @param y
	 * @param selected
	 */
	public void rendererText(Graphics g, int x, int y, boolean selected);


}