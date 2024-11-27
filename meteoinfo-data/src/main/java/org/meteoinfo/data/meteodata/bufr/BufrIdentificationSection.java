/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package org.meteoinfo.data.meteodata.bufr;

import ucar.nc2.time.CalendarDate;
import ucar.unidata.io.RandomAccessFile;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;


/**
 * A class representing the IdentificationSection (section 1) of a BUFR record.
 * Handles editions 2,3,4.
 *
 * @author Robb Kambic
 * @author caron
 */
@Immutable
public class BufrIdentificationSection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BufrIdentificationSection.class);
    private static final boolean warnDate = false;

    /**
     * Master Table number.
     */
    private final int master_table;

    /**
     * Identification of subcenter .
     */
    private final int subcenter_id;

    /**
     * Identification of center.
     */
    private final int center_id;

    /**
     * Update Sequence Number.
     */
    private final int update_sequence;

    /**
     * Optional section exists.
     */
    private final boolean hasOptionalSection;
    private final int optionalSectionLen;
    private final long optionalSectionPos;

    /**
     * Data category.
     */
    private final int category;

    /**
     * Data sub category.
     */
    private final int subCategory;

    private final int localSubCategory; // edition >= 4

    /**
     * Table Version numbers.
     */
    private final int master_table_version;
    private final int local_table_version;

    /**
     * Time of the obs (nominal)
     */
    private final int year, month, day, hour, minute, second;

    private final byte[] localUse;

    // *** constructors *******************************************************

    /**
     * Constructs a <tt>BufrIdentificationSection</tt> object from a raf.
     *
     * @param raf RandomAccessFile with Section 1 content
     * @param is  the BufrIndicatorSection, needed for the bufr edition number
     * @throws IOException if raf contains no valid BUFR file
     */
    public BufrIdentificationSection(RandomAccessFile raf, BufrIndicatorSection is) throws IOException {

        // section 1 octet 1-3 (length of section)
        int length = BufrNumbers.int3(raf);

        // master table octet 4
        master_table = raf.read();

        if (is.getBufrEdition() < 4) {
            if (length < 17)
                throw new IOException("Invalid BUFR message on " + raf.getLocation());

            if (is.getBufrEdition() == 2) {
                subcenter_id = 255;
                // Center octet 5-6
                center_id = BufrNumbers.int2(raf);

            } else { // edition 3
                // Center octet 5
                subcenter_id = raf.read();
                // Center octet 6
                center_id = raf.read();
            }

            // Update sequence number octet 7
            update_sequence = raf.read();

            // Optional section octet 8
            int optional = raf.read();
            hasOptionalSection = (optional & 0x80) != 0;

            // Category octet 9
            category = raf.read();

            // Category octet 10
            subCategory = raf.read();
            localSubCategory = -1; // not used

            // master table version octet 11
            master_table_version = raf.read();

            // local table version octet 12
            local_table_version = raf.read();

            // octets 13-17 (reference time of forecast)
            int lyear = raf.read();
            if (lyear > 100)
                lyear -= 100;
            year = lyear + 2000;
            int tempMonth = raf.read();
            month = (tempMonth == 0) ? 1 : tempMonth; // joda time does not allow 0 month
            int tempDay = raf.read();
            day = (tempDay == 0) ? 1 : tempDay; // joda time does not allow 0 day
            hour = raf.read();
            minute = raf.read();
            second = 0;
            if (warnDate && (tempMonth == 0 || tempDay == 0)) {
                // From manual on codes
                // When accuracy of the time does not define a time unit, then the value for this unit shall be set to zero
                // (e.g. for a
                // SYNOP observation at 09 UTC, minute = 0, second = 0.
                // NCEP codes their BUFR table messages with 0/0/0 0:0:0 in edition 3
                log.warn(raf.getLocation() + ": month or day is zero, set to 1. {}/{}/{} {}:{}:{}", year, tempMonth, tempDay,
                        hour, minute, second);
            }

            int n = length - 17;
            localUse = new byte[n];
            int nRead = raf.read(localUse);
            if (nRead != localUse.length)
                throw new IOException("Error reading BUFR local use field.");
        } else { // BUFR Edition 4 and above are slightly different
            if (length < 22)
                throw new IOException("Invalid BUFR message");

            // Center octet 5 - 6
            center_id = BufrNumbers.int2(raf);

            // Sub Center octet 7-8
            subcenter_id = BufrNumbers.int2(raf);

            // Update sequence number octet 9
            update_sequence = raf.read();

            // Optional section octet 10
            int optional = raf.read();
            // Most Sig. Bit = 1 : has optional section
            // 0 : does not have an optional section
            hasOptionalSection = (optional & 0x80) != 0;

            // Category octet 11
            category = raf.read();

            // International Sub Category octet 12
            subCategory = raf.read();

            // Local Sub Category Octet 13 - just read this for now
            localSubCategory = raf.read();

            // master table version octet 14
            master_table_version = raf.read();

            // local table version octet 15
            local_table_version = raf.read();
            // octets 16-22 (reference time of forecast)

            // Octet 16-17 is the 4-digit year
            year = BufrNumbers.int2(raf);
            month = raf.read();
            day = raf.read();
            hour = raf.read();
            minute = raf.read();
            second = raf.read();

            int n = length - 22;
            localUse = new byte[n];
            int nRead = raf.read(localUse);
            if (nRead != localUse.length)
                throw new IOException("Error reading BUFR local use field.");
        }

        // skip optional section, but store position so can read if caller wants it
        if (hasOptionalSection) {
            int optionalLen = BufrNumbers.int3(raf);
            if (optionalLen % 2 != 0)
                optionalLen++;
            optionalSectionLen = optionalLen - 4;
            raf.skipBytes(1);
            optionalSectionPos = raf.getFilePointer();
            raf.skipBytes(optionalSectionLen);

        } else {
            optionalSectionLen = -1;
            optionalSectionPos = -1;
        }
    }

    /**
     * Identification of center.
     *
     * @return center id as int
     */
    public final int getCenterId() {
        return center_id;
    }

    /**
     * Identification of subcenter.
     *
     * @return subcenter as int
     */
    public final int getSubCenterId() {
        return subcenter_id;
    }

    /**
     * Get update sequence.
     *
     * @return update_sequence
     */
    public final int getUpdateSequence() {
        return update_sequence;
    }

    /**
     * return record header time as a CalendarDate
     *
     * @return referenceTime
     */
    public final CalendarDate getReferenceTime() {
        int sec = (second < 0 || second > 59) ? 0 : second;
        return CalendarDate.of(null, year, month, day, hour, minute, sec);
    }

    public final int getCategory() {
        return category;
    }

    public final int getSubCategory() {
        return subCategory;
    }

    public final int getLocalSubCategory() {
        return localSubCategory;
    }

    public final int getMasterTableId() {
        return master_table;
    }

    public final int getMasterTableVersion() {
        return master_table_version;
    }

    public final int getLocalTableVersion() {
        return local_table_version;
    }

    /**
     * last bytes of the id section are "reserved for local use by ADP centers.
     *
     * @return local use bytes, if any.
     */
    public final byte[] getLocalUseBytes() {
        return localUse;
    }

    public final byte[] getOptiondsalSection(RandomAccessFile raf) throws IOException {
        if (!hasOptionalSection)
            return null;

        byte[] optionalSection = new byte[optionalSectionLen - 4];
        raf.seek(optionalSectionPos);
        int nRead = raf.read(optionalSection);
        if (nRead != optionalSection.length)
            log.warn("Error reading optional section -- expected " + optionalSection.length + " but read " + nRead);
        return optionalSection;
    }

}
