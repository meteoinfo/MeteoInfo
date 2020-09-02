/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.meteoinfo.console.autocomplete;

import org.meteoinfo.console.autocomplete.IconFactory.IconData;
import org.meteoinfo.console.jython.JythonSourceCompletion;

/**
 * Extra methods defined by a completion for a Java member (fields and methods).
 *
 * @author Robert Futrell
 * @version 1.0
 */
interface MemberCompletion extends JythonSourceCompletion {

    /**
     * Returns the name of the enclosing class.
     *
     * @param fullyQualified Whether the name returned should be fully
     * qualified.
     * @return The class name.
     */
    public String getEnclosingClassName(boolean fullyQualified);

    /**
     * Returns the signature of this member.
     *
     * @return The signature.
     */
    public String getSignature();

    /**
     * Returns the type of this member (the return type for methods).
     *
     * @return The type of this member.
     */
    public String getType();

    /**
     * Returns whether this member is deprecated.
     *
     * @return Whether this member is deprecated.
     */
    public boolean isDeprecated();

    /**
     * Meta data about the member. Member completions will be constructed from a
     * concrete instance of this interface. This is because there are two
     * sources that member completions come from - parsing Java source files and
     * parsing compiled class files (in libraries).
     */
    public static interface Data extends IconData {

        /**
         * Returns the name of the enclosing class.
         *
         * @param fullyQualified Whether the name returned should be fully
         * qualified.
         * @return The class name.
         */
        public String getEnclosingClassName(boolean fullyQualified);

        /**
         * Returns the signature of this member.
         *
         * @return The signature.
         * @see MemberCompletion#getSignature()
         */
        public String getSignature();

        /**
         * Returns the summary description (should be HTML) for this member.
         *
         * @return The summary description, or <code>null</code> if there is
         * none.
         * @see MemberCompletion#getSummary()
         */
        public String getSummary();

        /**
         * Returns the type of this member (the return type for methods).
         *
         * @return The type of this member.
         * @see MemberCompletion#getType()
         */
        public String getType();

        /**
         * Returns whether this member is a constructor.
         *
         * @return Whether this member is a constructor.
         */
        public boolean isConstructor();

    }

}
