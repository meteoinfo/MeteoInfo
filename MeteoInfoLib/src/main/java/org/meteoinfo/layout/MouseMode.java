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
package org.meteoinfo.layout;

/**
 *
 * @author yaqiang
 */
public enum MouseMode {
    /// <summary>
    /// The cursor is currently in default mode
    /// </summary>

    Default,
    /// <summary>
    /// The cursor is currently in select mode
    /// </summary>
    Select,
    /// <summary>
    /// The cursor is currently being used to create a new selection
    /// </summary>
    CreateSelection,
    /// <summary>
    /// The cursor is currently is move selection mode
    /// </summary>
    MoveSelection,
    /// <summary>
    /// The cursor is in resize mode because its over the edge of a selected item
    /// </summary>
    ResizeSelected,
    /// <summary>
    /// When in this mode the user can click on the map select an area and an element is inserted at that spot
    /// </summary>
    InsertNewElement,
    /// <summary>
    /// In this mode a cross hair is shown letting the user create a new Insert rectangle
    /// </summary>
    StartInsertNewElement,
    /// <summary>
    /// Puts the mouse into a mode that allows map panning
    /// </summary>
    StartPanMap,
    /// <summary>
    /// The mouse is actually panning a map
    /// </summary>
    PanMap,
    /// <summary>
    /// New point
    /// </summary>
    New_Point,
    /// <summary>
    /// New label
    /// </summary>
    New_Label,
    /// <summary>
    /// New polyline
    /// </summary>
    New_Polyline,
    /// <summary>
    /// New freehand
    /// </summary>
    New_Freehand,
    /// <summary>
    /// New curve
    /// </summary>
    New_Curve,
    /// <summary>
    /// New curve polygon
    /// </summary>
    New_CurvePolygon,
    /// <summary>
    /// New polygon
    /// </summary>
    New_Polygon,
    /// <summary>
    /// New rectangle
    /// </summary>
    New_Rectangle,
    /// <summary>
    /// New ellipse
    /// </summary>
    New_Ellipse,
    /// <summary>
    /// New circle
    /// </summary>
    New_Circle,
    /// <summary>
    /// Edit vertices of polyline or polygon
    /// </summary>
    EditVertices,
    /// <summary>
    /// In editing vertices status
    /// </summary>
    InEditingVertices,
    /// <summary>
    /// Map zoom in
    /// </summary>
    Map_ZoomIn,
    /// <summary>
    /// Map zoom out
    /// </summary>
    Map_ZoomOut,
    /// <summary>
    /// Map pan
    /// </summary>
    Map_Pan,
    /// <summary>
    /// Map identifer
    /// </summary>
    Map_Identifer,
    /// <summary>
    /// Map select Features
    /// </summary>
    Map_SelectFeatures_Rectangle,
    Map_SelectFeatures_Polygon,
    Map_SelectFeatures_Lasso,
    Map_SelectFeatures_Circle,
    /// <summary>
    /// Map measurement
    /// </summary>
    Map_Measurement,
    Map_Edit_Tool,
    Map_Edit_MoveSelection,    
    Map_Edit_NewFeature,
    Map_Edit_FeatureVertices,
    Map_Edit_AddRing,
    Map_Edit_FillRing,
    Map_Edit_DeleteRing,
    Map_Edit_ReformFeature,
    Map_Edit_SplitFeature,
}
