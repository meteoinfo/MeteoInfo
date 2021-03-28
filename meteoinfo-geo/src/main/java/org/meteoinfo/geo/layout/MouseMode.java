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
package org.meteoinfo.geo.layout;

/**
 *
 * @author yaqiang
 */
public enum MouseMode {

    DEFAULT,
    SELECT,
    CREATE_SELECTION,
    MOVE_SELECTION,
    RESIZE_SELECTED,
    INSERT_NEW_ELEMENT,
    START_INSERT_NEW_ELEMENT,
    START_PAN_MAP,
    PAN_MAP,

    NEW_POINT,
    NEW_LABEL,
    NEW_POLYLINE,
    NEW_FREEHAND,
    NEW_CURVE,
    NEW_CURVE_POLYGON,
    NEW_POLYGON,
    NEW_RECTANGLE,
    NEW_ELLIPSE,
    NEW_CIRCLE,
    EDIT_VERTICES,
    IN_EDITING_VERTICES,

    MAP_ZOOM_IN,
    MAP_ZOOM_OUT,
    MAP_PAN,
    MAP_IDENTIFIER,
    MAP_SELECT_FEATURES_RECTANGLE,
    MAP_SELECT_FEATURES_POLYGON,
    MAP_SELECT_FEATURES_LASSO,
    MAP_SELECT_FEATURES_CIRCLE,
    MAP_MEASUREMENT,

    MAP_EDIT_TOOL,
    MAP_EDIT_MOVE_SELECTION,
    MAP_EDIT_NEW_FEATURE,
    MAP_EDIT_FEATURE_VERTICES,
    MAP_EDIT_ADD_RING,
    MAP_EDIT_FILL_RING,
    MAP_EDIT_DELETE_RING,
    MAP_EDIT_REFORM_FEATURE,
    MAP_EDIT_SPLIT_FEATURE;
}
