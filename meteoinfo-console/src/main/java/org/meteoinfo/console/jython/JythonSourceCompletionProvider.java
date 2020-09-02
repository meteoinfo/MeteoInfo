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
package org.meteoinfo.console.jython;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.meteoinfo.console.autocomplete.CodeBlock;
import org.meteoinfo.console.autocomplete.FieldCompletion;
import org.meteoinfo.console.autocomplete.TokenScanner;
import org.meteoinfo.console.autocomplete.VariableDeclaration;
import org.python.util.PythonInterpreter;

import javax.swing.text.JTextComponent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The completion provider used for Groovy source code.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JythonSourceCompletionProvider extends DefaultCompletionProvider {

    //private JarManager jarManager;
    private static final char[] KEYWORD_DEF = {'d', 'e', 'f'};
    private PythonInterpreter interp = new PythonInterpreter();

    /**
     * Constructor.
     */
    public JythonSourceCompletionProvider() {
        setParameterizedCompletionParams('(', ", ", ')');
        setAutoActivationRules(false, "."); // Default - only activate after '.'
    }
    
    /**
     * Set python interpreter
     * @param value Python interpreter
     */
    public void setInterp(PythonInterpreter value){
        this.interp = value;
    }

    private CodeBlock createAst(JTextComponent comp) {

        CodeBlock ast = new CodeBlock(0);

        RSyntaxTextArea textArea = (RSyntaxTextArea) comp;
        TokenScanner scanner = new TokenScanner(textArea);
        parseCodeBlock(scanner, ast);

        return ast;

    }
    
    private String getSegment(){
        return this.seg.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List<Completion> getCompletionsImpl(JTextComponent comp) {

        completions.clear();

        CodeBlock ast = createAst(comp);
        int dot = comp.getCaretPosition();
        recursivelyAddLocalVars(completions, ast, dot);

//        String code = comp.getText();
//        String[] codes = code.split("\n");
//        try {
//            for (int i = 0; i < codes.length - 1; i++) {
//                String c = codes[i];
//                this.interp.exec(c);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Cut down the list to just those matching what we've typed.
        String text = this.getSegment();
        if (!text.startsWith("import ") && !text.startsWith("from "))
            text = getAlreadyEnteredText(comp);
        JIntrospect nameComplete = new JIntrospect(this.interp);
        try {
            List<String> list = nameComplete.getAutoCompleteList(text);
            if (list != null) {
                for (String str : list) {
                    completions.add(new FieldCompletion(this, str));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(JythonSourceCompletionProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

        return completions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isValidChar(char ch) {
        return Character.isJavaIdentifierPart(ch) || ch == '.';
    }

    private void parseCodeBlock(TokenScanner scanner, CodeBlock block) {

        Token t = scanner.next();
        while (t != null) {
            if (t.isRightCurly()) {
                block.setEndOffset(t.getOffset());
                return;
            } else if (t.isLeftCurly()) {
                CodeBlock child = block.addChildCodeBlock(t.getOffset());
                parseCodeBlock(scanner, child);
            } else if (t.is(Token.RESERVED_WORD, KEYWORD_DEF)) {
                t = scanner.next();
                if (t != null) {
                    VariableDeclaration varDec = new VariableDeclaration(t
                            .getLexeme(), t.getOffset());
                    block.addVariable(varDec);
                }
            }
            t = scanner.next();
        }

    }

    private void recursivelyAddLocalVars(List<Completion> completions,
            CodeBlock block, int dot) {

        if (!block.contains(dot)) {
            return;
        }

        // Add local variables declared in this code block
        for (int i = 0; i < block.getVariableDeclarationCount(); i++) {
            VariableDeclaration dec = block.getVariableDeclaration(i);
            int decOffs = dec.getOffset();
            if (decOffs < dot) {
                BasicCompletion c = new BasicCompletion(this, dec.getName());
                completions.add(c);
            } else {
                break;
            }
        }

        // Add any local variables declared in a child code block
        for (int i = 0; i < block.getChildCodeBlockCount(); i++) {
            CodeBlock child = block.getChildCodeBlock(i);
            if (child.contains(dot)) {
                recursivelyAddLocalVars(completions, child, dot);
                return; // No other child blocks can contain the dot
            }
        }

    }

}
