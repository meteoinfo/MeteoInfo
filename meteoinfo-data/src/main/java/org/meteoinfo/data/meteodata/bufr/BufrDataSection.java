/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import javax.annotation.concurrent.Immutable;

/**
 * Represents Section 4 of a BUFR message.
 *
 * @author caron
 * @since May 10, 2008
 */
@Immutable
public class BufrDataSection {
    private final long dataPos;
    private final int dataLength;

    public BufrDataSection(long dataPos, int dataLength) {
        this.dataPos = dataPos;
        this.dataLength = dataLength;
    }

    public long getDataPos() {
        return dataPos;
    }

    public int getDataLength() {
        return dataLength;
    }

}
