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
package org.meteoinfo.layer;

import org.meteoinfo.global.Extent;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.shape.ShapeTypes;

/**
 * Map layer class
 *
 * @author Yaqiang Wang
 */
public class MapLayer implements Cloneable {
    // <editor-fold desc="Variables">

    private LayerTypes _layerType;
    private ShapeTypes _shapeType;
    private int _handle;
    private String _layerName;
    private String _fileName;
    protected ProjectionInfo _projInfo;
    private Extent _extent;
    private boolean _visible;
    private LayerDrawType _layerDrawType;
    private boolean _isMaskout;
    private LegendScheme _legendScheme;
    private boolean _expanded;
    private int _transparencyPerc;
    private String _tag;
    private VisibleScale _visibleScale;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MapLayer() {
        _layerName = "layer";
        _fileName = "";
        _projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
        _handle = -1;
        _extent = new Extent();
        _visible = true;
        _isMaskout = false;
        _expanded = false;
        _transparencyPerc = 0;
        _layerDrawType = LayerDrawType.Map;
        _tag = "";
        _visibleScale = new VisibleScale();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get layer type
     *
     * @return Layer type
     */
    public LayerTypes getLayerType() {
        return _layerType;
    }

    /**
     * Set layer type
     *
     * @param lt Layer type
     */
    public void setLayerType(LayerTypes lt) {
        _layerType = lt;
    }

    /**
     * Get shape type
     *
     * @return Shape type
     */
    public ShapeTypes getShapeType() {
        return _shapeType;
    }

    /**
     * Set shape type
     *
     * @param st Shape type
     */
    public void setShapeType(ShapeTypes st) {
        _shapeType = st;
    }

    /**
     * Get layer draw type
     *
     * @return Layer draw type
     */
    public LayerDrawType getLayerDrawType() {
        return _layerDrawType;
    }

    /**
     * Set layer draw type
     *
     * @param ldt Layer draw type
     */
    public void setLayerDrawType(LayerDrawType ldt) {
        _layerDrawType = ldt;
    }

    /**
     * Get file name
     *
     * @return File name
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set file name
     *
     * @param fn File name
     */
    public void setFileName(String fn) {
        _fileName = fn;
    }

    /**
     * Get layer handle
     *
     * @return Layer handle
     */
    public int getHandle() {
        return _handle;
    }

    /**
     * Set layer handle
     *
     * @param handle Layer handle
     */
    public void setHandle(int handle) {
        _handle = handle;
    }

    /**
     * Get layer name
     *
     * @return Layer name
     */
    public String getLayerName() {
        return _layerName;
    }

    /**
     * Set layer name
     *
     * @param lName Layer name
     */
    public void setLayerName(String lName) {
        _layerName = lName;
    }

    /**
     * Get extent
     *
     * @return Extent
     */
    public Extent getExtent() {
        return _extent;
    }

    /**
     * Set extent
     *
     * @param extent Extent
     */
    public void setExtent(Extent extent) {
        _extent = extent;
    }

    /**
     * Get if layer is visible
     *
     * @return Boolean
     */
    public boolean isVisible() {
        return _visible;
    }

    /**
     * Set if layer is visible
     *
     * @param isTrue Boolean
     */
    public void setVisible(boolean isTrue) {
        _visible = isTrue;
    }

    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return _legendScheme;
    }
    
    /**
     * Get if is maskout
     * @return Boolean
     */
    public boolean isMaskout(){
        return this._isMaskout;
    }
    
    /**
     * Set if is maskout
     * @param istrue Boolean
     */
    public void setMaskout(boolean istrue){
        this._isMaskout = istrue;
    }

    /**
     * Set legend scheme
     *
     * @param ls
     */
    public void setLegendScheme(LegendScheme ls) {
        _legendScheme = ls;
    }

    /**
     * Get transparency percent
     *
     * @return Transparency percent
     */
    public int getTransparency() {
        return this._transparencyPerc;
    }

    /**
     * Set transparency percent
     *
     * @param trans Transparency percent
     */
    public void setTransparency(int trans) {
        this._transparencyPerc = trans;
    }

    /**
     * Get if is expanded
     *
     * @return Boolean
     */
    public boolean isExpanded() {
        return this._expanded;
    }

    /**
     * Set if expand
     *
     * @param istrue Boolean
     */
    public void setExpanded(boolean istrue) {
        this._expanded = istrue;
    }
    
    /**
     * Get projection info
     * @return Projection info
     */
    public ProjectionInfo getProjInfo(){
        return _projInfo;
    }
    
    /**
     * Set projection info
     * @param projInfo Projection info
     */
    public void setProjInfo(ProjectionInfo projInfo){
        _projInfo = projInfo;
    }
    
    /**
     * Get tag
     * @return Tag
     */
    public String getTag(){
        return _tag;
    }
    
    /**
     * Set tag
     * @param value Tag value
     */
    public void setTag(String value){
        _tag = value;
    }
    
    /**
     * Get visible scale
     * @return Visible scale
     */
    public VisibleScale getVisibleScale(){
        return _visibleScale;
    }
    
    /**
     * Set visible scale
     * @param value Visible scale
     */
    public void setVisibleScale(VisibleScale value){
        _visibleScale = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * If the layer has legend schem or not
     * @return Boolean
     */
    public boolean hasLegendScheme(){
        return this._legendScheme != null;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        MapLayer aLayer = (MapLayer)super.clone();
        if (_legendScheme != null)
            aLayer._legendScheme = (LegendScheme)this._legendScheme.clone();
        
        return aLayer;
    }
    
    /**
     * To string
     * @return String
     */
    @Override
    public String toString(){
        return this.getLayerName();
    }
    
    /**
     * To string
     * @return String
     */
    public String getLayerInfo(){
        String str = "Layer name: " + this.getLayerName();
        str += System.getProperty("line.separator") + "Layer file: " + this.getFileName();
        str += System.getProperty("line.separator") + "Layer type: " + this.getLayerType();
        str += System.getProperty("line.separator") + "Shape type: " + this.getShapeType();
        
        return str;
    }
    
    /**
     * Save layer to a file
     */
    public void saveFile(){}
    
    /**
     * Save layer to a file
     * @param fileName File name
     */
    public void saveFile(String fileName){}
    // </editor-fold>

}
