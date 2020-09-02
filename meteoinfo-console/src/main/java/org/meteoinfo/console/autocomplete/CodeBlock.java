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

import java.util.ArrayList;
import java.util.List;


/**
 * A block of code.  This can be used to implement <em>very</em> simple
 * parsing for languages that have some concept of code blocks, such as C,
 * Perl, Java, etc.  Currently, using <code>CodeBlock</code>s provides a
 * means of remembering where variables are defined, as well as their scopes.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see VariableDeclaration
 */
public class CodeBlock {

	private int start;
	private int end;
	private CodeBlock parent;
	private List<CodeBlock> children;
	private List<VariableDeclaration> varDecs;


	/**
	 * Constructor.
	 *
	 * @param start The starting offset of the code block.
	 */
	public CodeBlock(int start) {
		this.start = start;
		end = Integer.MAX_VALUE;
	}


	/**
	 * Creates and returns a child (nested) code block.
	 *
	 * @param start The starting offset of the nested code block.
	 * @return The code block.
	 */
	public CodeBlock addChildCodeBlock(int start) {
		CodeBlock child = new CodeBlock(start);
		child.parent = this;
		if (children==null) {
			children = new ArrayList<>();
		}
		children.add(child);
		return child;
	}


	/**
	 * Adds a variable declaration.
	 *
	 * @param varDec The variable declaration.
	 */
	public void addVariable(VariableDeclaration varDec) {
		if (varDecs==null) {
			varDecs = new ArrayList<>();
		}
		varDecs.add(varDec);
	}


	/**
	 * Returns whether this code block contains a given offset.
	 *
	 * @param offset The offset.
	 * @return Whether this code block contains that offset.
	 */
	public boolean contains(int offset) {
		return offset>=start && offset<end;
	}


	/**
	 * Returns a child code block.
	 *
	 * @param index The index of the child code block.
	 * @return The child code block.
	 * @see #getChildCodeBlockCount()
	 */
	public CodeBlock getChildCodeBlock(int index) {
		return children.get(index);
	}


	/**
	 * Returns the number of child code blocks.
	 *
	 * @return The child code block count.
	 * @see #getChildCodeBlock(int)
	 */
	public int getChildCodeBlockCount() {
		return children==null ? 0 : children.size();
	}


	/**
	 * Returns the deepest code block nested under this one (or this one
	 * itself) containing a given offset.
	 *
	 * @param offs The offset to look for.
	 * @return The deepest-nested code block containing the offset, or
	 *         <code>null</code> if this code block and none of its children
	 *         contain the offset.
	 */
	public CodeBlock getDeepestCodeBlockContaining(int offs) {
		if (!contains(offs)) {
			return null;
		}
		for (int i=0; i<getChildCodeBlockCount(); i++) {
			CodeBlock child = getChildCodeBlock(i);
			if (child.contains(offs)) {
				return child.getDeepestCodeBlockContaining(offs);
			}
		}
		return this;
	}


	/**
	 * Returns the end offset of this code block.
	 *
	 * @return The end offset.
	 * @see #getStartOffset()
	 * @see #setEndOffset(int)
	 */
	public int getEndOffset() {
		return end;
	}


	/**
	 * Returns the parent code block.
	 *
	 * @return The parent code block, or <code>null</code> if there isn't one.
	 */
	public CodeBlock getParent() {
		return parent;
	}


	/**
	 * Returns the start offset of this code block.
	 *
	 * @return The start offset.
	 * @see #getEndOffset()
	 */
	public int getStartOffset() {
		return start;
	}


	/**
	 * Returns a variable declaration.
	 *
	 * @param index The index of the declaration.
	 * @return The declaration.
	 * @see #getVariableDeclarationCount()
	 */
	public VariableDeclaration getVariableDeclaration(int index) {
		return varDecs.get(index);
	}


	/**
	 * Returns the number of variable declarations in this code block.
	 *
	 * @return The number of variable declarations.
	 * @see #getVariableDeclaration(int)
	 */
	public int getVariableDeclarationCount() {
		return varDecs==null ? 0 : varDecs.size();
	}


	/**
	 * Returns all local variables declared before a given offset, both in
	 * this code block and in all parent blocks.
	 *
	 * @param offs The offset.
	 * @return The {@link VariableDeclaration}s, or an empty list of none were
	 *         declared before the offset.
	 */
	public List<VariableDeclaration> getVariableDeclarationsBefore(int offs) {

		List<VariableDeclaration> vars = new ArrayList<>();

		int varCount = getVariableDeclarationCount();
		for (int i=0; i<varCount; i++) {
			VariableDeclaration localVar = getVariableDeclaration(i);
			if (localVar.getOffset()<offs) {
				vars.add(localVar);
			}
			else {
				break;
			}
		}

		if (parent!=null) {
			vars.addAll(parent.getVariableDeclarationsBefore(offs));
		}

		return vars;

	}


	/**
	 * Sets the end offset of this code block.
	 *
	 * @param end The end offset.
	 * @see #getEndOffset()
	 */
	public void setEndOffset(int end) {
		this.end = end;
	}


}