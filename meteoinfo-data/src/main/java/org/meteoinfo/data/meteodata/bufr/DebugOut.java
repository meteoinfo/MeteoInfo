/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr;

import ucar.nc2.util.Indent;

import java.util.Formatter;

/**
 * Helper class for debugging BUFR descriptors
 *
 * @author caron
 * @since Nov 16, 2009
 */
class DebugOut {
    Formatter f;
    Indent indent;
    int fldno; // track fldno to compare with EU output

    DebugOut(Formatter f) {
        this.f = f;
        this.indent = new Indent(2);
        this.indent.setIndentLevel(0);
        this.fldno = 1;
    }

    String indent() {
        return indent.toString();
    }

}

