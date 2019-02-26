/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.codecomplete;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.meteoinfo.console.NameCompletion;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyMethod;
import org.python.core.PyObject;
import org.python.core.PyReflectedFunction;
import org.python.core.PySystemState;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Yaqiang Wang
 */
public class JIntrospect implements NameCompletion {

    private final Pattern IMPORT_PACKAGE = Pattern.compile("import\\s+(.+)\\.");
    private final Pattern FROM_PACKAGE_IMPORT = Pattern.compile("from\\s+(\\w+(?:\\.\\w+)*)\\.?(?:\\s*import\\s*)?");
    private final PythonInterpreter interp;

    /**
     * Constructor
     *
     * @param interp
     */
    public JIntrospect(PythonInterpreter interp) {
        this.interp = interp;
    }

    /**
     * Complete package name
     *
     * @param target Target
     * @return Package names
     */
    public List<String> completePackageName(String target) {
        String[] targetComponents = target.split("\\.");
        String base = targetComponents[0];
        PySystemState state = interp.getSystemState();
        PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
        PyObject module = importer.__call__(Py.newString(base));
        if (targetComponents.length > 1) {
            for (int i = 1; i < targetComponents.length; i++) {
                module = module.__getattr__(targetComponents[i]);
            }
        }
        PyList plist = (PyList) module.__dir__();
        List<String> list = new ArrayList<>();
        String name;
        for (int i = 0; i < plist.__len__(); i++) {
            name = plist.get(i).toString();
            if (!name.startsWith("__")) {
                list.add(name);
            }
        }
        //list.add("*");

        return list;
    }

    /**
     * Get package name
     *
     * @param command The command
     * @return Package name
     */
    public String getPackageName(String command) {
        Matcher match = IMPORT_PACKAGE.matcher(command);
        if (match.find()) {
            return match.group(1);
        } else {
            match = FROM_PACKAGE_IMPORT.matcher(command);
            if (match.find()) {
                return match.group(1);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Get auto complete list
     * @param command Command string
     * @return Complete list string
     * @throws IOException 
     */
    public List<String> getAutoCompleteList(String command) throws IOException{
        return this.getAutoCompleteList(command, true, true, true);
    }

    /**
     * Get auto complete list
     *
     * @param command The command
     * @param includeMagic
     * @param includeSingle
     * @param includeDouble
     * @return Auto complete list
     * @throws java.io.IOException
     */
    public List<String> getAutoCompleteList(String command, boolean includeMagic,
            boolean includeSingle, boolean includeDouble) throws IOException {
        // Temp KLUDGE here rather than in console.py
        //command += ".";
        if (command.startsWith("import ") || command.startsWith("from ")) {
            String target = getPackageName(command);
            if (target == null) {
                return null;
            }
            return completePackageName(target);
        }
        
        String root = this.getRoot(command, ".");
        if (root.isEmpty())
            return null;
        
        try {
            PyObject object = this.interp.eval(root);
            PyList plist = (PyList) object.__dir__();
            List<String> list = new ArrayList<>();
            String name;
            for (int i = 0; i < plist.__len__(); i++) {
                name = plist.get(i).toString();
                if (!name.startsWith("__")) {
                    list.add(name);
                }
            }
            return list;
        } catch (Exception e){
            return null;
        }
    }
    
    private boolean ispython(Object object){
        if (object instanceof Class)
            return false;
        else if (object instanceof Object)
            return false;
        else if (object instanceof PyReflectedFunction)
            return false;
        else
            return true;
    }

    @Override
    public String[] completeName(String paramString) {
        List<String> names = null;
        try {
            names = this.getAutoCompleteList(paramString, true, true, true);
        } catch (IOException ex) {
            Logger.getLogger(JIntrospect.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (names == null) {
            return null;
        }
        Collections.sort(names);
        return names.toArray(new String[names.size()]);
    }
    
    /**
     * Return the rightmost root portion of an arbitrary Python command.
     * @param command The command
     * @param terminator The terminator - '(' or '.'
     * @return Rightmost root portion
     * @throws java.io.IOException
     */
    public String getRoot(String command, String terminator) throws IOException{
        String[] temp = command.split("\n");
        command = temp[temp.length - 1];
        if (command.startsWith("... ")){
            command = command.substring(4);
        }
        command = command.trim();
        if (terminator.equals("(")){
            if (command.endsWith("("))
                command = command.substring(0, command.length() - 1);
        } else
            command = rtrimTerminus(command, terminator);
        PyList tokens = this.getTokens(command);
        if (tokens == null || tokens.isEmpty())
            return "";
        
        PyTuple token = (PyTuple)tokens.get(tokens.size() - 1);
        if ((int)token.get(0) == 0)
            tokens.remove(tokens.size() - 1);
        
        if (tokens.isEmpty())
            return "";
        
        token = (PyTuple)tokens.get(tokens.size() - 1);
        if (terminator.equals(".") && (!token.get(1).toString().equals(".") ||
                (int)token.get(0) != 51)){
            return "";
        } else {
            if (command.endsWith(terminator)){
                int size = terminator.length();
                command = command.substring(0, command.length() - size);
            }
        }
        
        command = command.trim();
        tokens = this.getTokens(command);
        tokens.reverse();
        
        String line = "";
        Integer start = null;
        String prefix = "";
        String laststring = ".";
        List<String> emptyTypes = new ArrayList<>();
        emptyTypes.add("[]");
        emptyTypes.add("()");
        emptyTypes.add("{}");
        for (int i = 0; i < tokens.size(); i++){
            token = (PyTuple)tokens.get(i);
            int tokentype = (int)token.get(0);
            String tokenstring = token.get(1).toString();
            line = token.get(4).toString();
            if (tokentype == 0)
                continue;
            if (tokentype == 1 || tokentype == 2 || tokentype == 3){
                    if (!laststring.equals(".")){
                        int idx = (int)((PyTuple)token.get(3)).get(1);
                        if (!prefix.isEmpty() && line.substring(idx, idx + 1).equals(" "))
                            prefix = "";
                        break;
                    }
            }
            if (tokentype == 1 || tokentype == 2 || tokentype == 3 || (tokentype == 51 &&
                    tokenstring.equals("."))){
                if (!prefix.isEmpty()){
                    prefix = "";
                    break;
                } else {
                    start = (int)((PyTuple)token.get(2)).get(1);
                }
            } else if (tokenstring.length() == 1 && "[({])}".contains(tokenstring)){
                if (emptyTypes.contains(prefix) && "[({".contains(tokenstring)){
                    break;
                } else {
                    prefix = tokenstring + prefix;
                }
            } else 
                break;
            
            laststring = tokenstring;
        }
        
        if (start == null){
            start = line.length();
        }
        String root = line.substring(start);
        if (emptyTypes.contains(prefix)){
            root = prefix + root;
        }
        
        return root;
    }
    
    /**
     * Return list of token for command.
     * @param command The command
     * @return Token list
     * @throws java.io.UnsupportedEncodingException
     */
    public PyList getTokens(String command) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("import cStringIO");
        sb.append("\n");
        sb.append("import tokenize");
        sb.append("\n");
        sb.append("command = str('");
        sb.append(command);
        sb.append("')");
        sb.append("\n");
        sb.append("f = cStringIO.StringIO(command)");
        sb.append("\n");
        sb.append("tokens = []");
        sb.append("\n");
        sb.append("def eater(*args):");
        sb.append("\n");
        sb.append("    tokens.append(args)");
        sb.append("\n");
        sb.append("tokenize.tokenize_loop(f.readline, eater)");
        sb.append("\n");
        String code = sb.toString();
        String encoding = "utf-8";
        PythonInterpreter pi = new PythonInterpreter();
        try {
            pi.execfile(new ByteArrayInputStream(code.getBytes(encoding)));
            PyList tokens = (PyList)pi.get("tokens");            
            return tokens;
        } catch (Exception e){
            return null;
        }
    }
    
    /**
     * Return command minus anything that follows the final terminator.
     * @param command The command
     * @param terminator The terminator - '(' or '.'
     * @return Result string
     */
    public String rtrimTerminus(String command, String terminator){
        if (terminator != null){
            String[] pieces = command.split(terminator);
            if (pieces.length > 1){
                command = pieces[0];
                for (int i = 1; i < pieces.length - 1; i++){
                    command = command + terminator + pieces[i];
                }
                command = command + terminator;
            }
        }
        return command;
    }
    
    /**
     * For a command, return a tuple of object name, argspec, tip text.
     * @param command Command string
     * @return Tip text
     * @throws java.io.IOException
     */
    public String[] getCallTipJava(String command) throws IOException{
        String[] callTip = new String[]{"","",""};
        String root = getRoot(command, "(");
        try {
            PyObject object = this.interp.eval(root);
            if (object instanceof PyFunction){
                PyFunction func = (PyFunction)object;
                String name = func.__doc__.toString();
                callTip[2] = name;
            } else if (object instanceof PyMethod){
                PyMethod method = (PyMethod)object;
                PyFunction func = (PyFunction)method.__func__;
                String name = func.__doc__.toString();
                callTip[2] = name;
            } else {
                callTip[2] = object.toString();
            }
        } catch (Exception e){
            
        }
        
        return callTip;
    }
    
    @Override
    public String[] getTip(String command){
        try {
            return this.getCallTipJava(command);
        } catch (IOException ex) {
            Logger.getLogger(JIntrospect.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
