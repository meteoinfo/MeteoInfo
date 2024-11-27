/*
 * Copyright (c) 1998-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package org.meteoinfo.data.meteodata.bufr.point;

import ucar.nc2.ft.point.bufr.BufrCdmIndexProto;

import java.util.List;

/**
 * Abstraction for BUFR field.
 * Used in writing index, so we can make changes in BufrCdmIndexPanel
 *
 * @author caron
 * @since 8/20/13
 */
public interface BufrField {
  String getName();

  String getDesc();

  String getUnits();

  short getFxy();

  String getFxyName();

  BufrCdmIndexProto.FldAction getAction();

  BufrCdmIndexProto.FldType getType();

  boolean isSeq();

  int getMin();

  int getMax();

  int getScale();

  int getReference();

  int getBitWidth();

  List<? extends BufrField> getChildren();

}
