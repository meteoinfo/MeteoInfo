 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.meteodata.grads;

import org.meteoinfo.data.meteodata.Variable;
import java.util.ArrayList;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class VARDEFS {
    // <editor-fold desc="Variables">

    private List<Variable> _vars = new ArrayList<>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get variable number
     *
     * @return Variable number
     */
    public int getVNum() {
        return _vars.size();
    }

    /**
     * Get variables
     *
     * @return Variables
     */
    public List<Variable> getVars() {
        return _vars;
    }

    /**
     * Set variables
     *
     * @param value Variables
     */
    public void setVars(List<Variable> value) {
        _vars = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Add var
     *
     * @param aVar The var
     */
    public void addVar(Variable aVar) {
        _vars.add(aVar);
    }
    // </editor-fold>
}
