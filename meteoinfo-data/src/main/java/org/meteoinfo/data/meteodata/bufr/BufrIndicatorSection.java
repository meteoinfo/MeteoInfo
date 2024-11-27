/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.unidata.io.RandomAccessFile;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;

/**
 * A class representing the IndicatorSection (section 0) of a BUFR record.
 * Handles editions 2,3,4.
 *
 * @author Robb Kambic
 * @author caron
 */
@Immutable
public class BufrIndicatorSection {
    private final long startPos;
    private final int bufrLength; // Length in bytes of BUFR record.
    private final int edition;

    // *** constructors *******************************************************

    /**
     * Constructs a <tt>BufrIndicatorSection</tt> object from a raf.
     *
     * @param raf RandomAccessFile with IndicatorSection content
     * @throws IOException on read error
     */
    public BufrIndicatorSection(RandomAccessFile raf) throws IOException {
        this.startPos = raf.getFilePointer() - 4; // start of BUFR message, including "BUFR"
        bufrLength = BufrNumbers.uint3(raf);
        edition = raf.read();
    }

    /**
     * Get the byte length of this BUFR record.
     *
     * @return length in bytes of BUFR record
     */
    public final int getBufrLength() {
        return bufrLength;
    }

    /**
     * Get the edition of the BUFR specification used.
     *
     * @return edition number of BUFR specification
     */
    public final int getBufrEdition() {
        return edition;
    }

    /**
     * Get starting position in the file. This should point to the "BUFR" chars .
     *
     * @return byte offset in file of start of BUFR meessage.
     */
    public final long getStartPos() {
        return startPos;
    }
}
