/*
 * 01/11/2010
 *
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.meteoinfo.console.autocomplete;


/**
 * A marker for a variable declaration.  This can be used by
 * {@link LanguageSupport}s to mark variables, and is especially helpful
 * when used in conjunction with {@link CodeBlock}.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see CodeBlock
 */
public class VariableDeclaration {

	private String type;
	private String name;
	private int offset;


	public VariableDeclaration(String name, int offset) {
		this(null, name, offset);
	}


	public VariableDeclaration(String type, String name, int offset) {
		this.type = type;
		this.name = name;
		this.offset = offset;
	}


	public String getName() {
		return name;
	}


	public int getOffset() {
		return offset;
	}


	/**
	 * Returns the type of this variable.
	 *
	 * @return The variable's type, or <code>null</code> if none.
	 */
	public String getType() {
		return type;
	}


}