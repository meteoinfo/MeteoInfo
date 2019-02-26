package org.meteoinfo.laboratory.codecomplete;

import java.awt.Graphics;

import org.fife.ui.autocomplete.Completion;


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
	 * Used by {@link JythonCellRenderer} to render this completion choice.
	 *
	 * @param g
	 * @param x
	 * @param y
	 * @param selected
	 */
	public void rendererText(Graphics g, int x, int y, boolean selected);


}