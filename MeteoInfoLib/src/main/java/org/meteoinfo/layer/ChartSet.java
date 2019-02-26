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

import java.awt.Color;
import java.awt.Font;
import org.meteoinfo.legend.AlignType;
import org.meteoinfo.legend.ChartTypes;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.shape.ShapeTypes;
import java.util.ArrayList;
import java.util.List;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class ChartSet {
    // <editor-fold desc="Variables">

    private ChartTypes _chartType;
    private boolean _drawCharts;
    private List<String> _fieldNames;
    private int _xShift;
    private int _yShift;
    private LegendScheme _legendScheme;
    private int _maxSize;
    private int _minSize;
    private float _maxValue;
    private float _minValue;
    private int _barWidth;
    private boolean _avoidCollision;
    private AlignType _alignType;
    private boolean _view3D;
    private int _thickness;
    private boolean drawLabel;
    private Font labelFont;
    private Color labelColor;
    private int decimalDigits;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ChartSet() {
        _chartType = ChartTypes.BarChart;
        _drawCharts = false;
        _fieldNames = new ArrayList<>();
        _xShift = 0;
        _yShift = 0;
        _legendScheme = new LegendScheme(ShapeTypes.Polygon);
        _maxSize = 50;
        _minSize = 0;
        _barWidth = 8;
        _avoidCollision = true;
        _alignType = AlignType.Center;
        _view3D = false;
        _thickness = 5;
        drawLabel = false;
        labelFont = new Font("Arial", Font.PLAIN, 12);
        labelColor = Color.black;
        this.decimalDigits = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get chart type
     *
     * @return Chart type
     */
    public ChartTypes getChartType() {
        return _chartType;
    }

    /**
     * Set chart type
     *
     * @param type Chart type
     */
    public void setChartType(ChartTypes type) {
        _chartType = type;
    }
    
    /**
     * Set chart type
     * @param tstr Chart type string
     */
    public void setChartType(String tstr) {
        switch(tstr.toLowerCase()) {
            case "bar":
                this._chartType = ChartTypes.BarChart;
                break;
            case "pie":
                this._chartType = ChartTypes.PieChart;
                break;
        }
    }

    /**
     * Set if draw charts
     *
     * @return If draw charts
     */
    public boolean isDrawCharts() {
        return _drawCharts;
    }

    /**
     * Set if draw charts
     *
     * @param istrue If draw charts
     */
    public void setDrawCharts(boolean istrue) {
        _drawCharts = istrue;
    }

    /**
     * Get field names
     *
     * @return The file names
     */
    public List<String> getFieldNames() {
        return _fieldNames;
    }

    /**
     * Set field names
     *
     * @param names Field names
     */
    public void setFieldNames(List<String> names) {
        _fieldNames = names;
    }

    /**
     * Get x shift
     *
     * @return X shift
     */
    public int getXShift() {
        return _xShift;
    }

    /**
     * Set x shift
     *
     * @param shift X shift
     */
    public void setXShift(int shift) {
        _xShift = shift;
    }

    /**
     * Get y shift
     *
     * @return Y shift
     */
    public int getYShift() {
        return _yShift;
    }

    /**
     * Set y shift
     *
     * @param shift Y shift
     */
    public void setYShift(int shift) {
        _yShift = shift;
    }

    /**
     * Get legend scheme
     *
     * @return The legend scheme
     */
    public LegendScheme getLegendScheme() {
        return _legendScheme;
    }

    /**
     * Set legend scheme
     *
     * @param ls The legend scheme
     */
    public void setLegendScheme(LegendScheme ls) {
        _legendScheme = ls;
    }

    /**
     * Get maximum size
     *
     * @return Maximum size
     */
    public int getMaxSize() {
        return _maxSize;
    }

    /**
     * Set maximum size
     *
     * @param size Maximum size
     */
    public void setMaxSize(int size) {
        _maxSize = size;
    }

    /**
     * Get minimum size
     *
     * @return Minimum size
     */
    public int getMinSize() {
        return _minSize;
    }

    /**
     * Set minimum size
     *
     * @param size Minimum size
     */
    public void setMinSize(int size) {
        _minSize = size;
    }

    /**
     * Get maximum value
     *
     * @return Maximum value
     */
    public float getMaxValue() {
        return _maxValue;
    }

    public void setMaxValue(float value) {
        _maxValue = value;
    }

    /**
     * Get minimum value
     *
     * @return Minimum value
     */
    public float getMinValue() {
        return _minValue;
    }

    /**
     * Set minimum value
     *
     * @param value Minimum value
     */
    public void setMinValue(float value) {
        _minValue = value;
    }

    /**
     * Get bar width
     *
     * @return Bar width
     */
    public int getBarWidth() {
        return _barWidth;
    }

    /**
     * Set bar width
     *
     * @param width Bar width
     */
    public void setBarWidth(int width) {
        _barWidth = width;
    }

    /**
     * Get if avoid collision
     *
     * @return If avoid collisioin
     */
    public boolean isAvoidCollision() {
        return _avoidCollision;
    }

    /**
     * Set if avoid collision
     *
     * @param istrue If avoid collision
     */
    public void setAvoidCollision(boolean istrue) {
        _avoidCollision = istrue;
    }

    /**
     * Get align type
     *
     * @return Align type
     */
    public AlignType getAlignType() {
        return _alignType;
    }

    /**
     * Set align type
     *
     * @param type Align type
     */
    public void setAlignType(AlignType type) {
        _alignType = type;
    }
    
    /**
     * Set align type
     * @param tstr Align type string
     */
    public void setAlignType(String tstr) {
        switch(tstr.toLowerCase()) {
            case "center":
                this._alignType = AlignType.Center;
                break;
            case "left":
                this._alignType = AlignType.Left;
                break;
            case "right":
                this._alignType = AlignType.Right;
                break;
            case "none":
                this._alignType = AlignType.None;
                break;
        }
    } 

    /**
     * Get if view 3D
     *
     * @return If view 3D
     */
    public boolean isView3D() {
        return _view3D;
    }

    /**
     * Set if view 3D
     *
     * @param istrue If view 3D
     */
    public void setView3D(boolean istrue) {
        _view3D = istrue;
    }

    /**
     * Get 3D thickness
     *
     * @return 3D thickness
     */
    public int getThickness() {
        return _thickness;
    }

    /**
     * Set 3D thickness
     *
     * @param value 3D thickness
     */
    public void setThickness(int value) {
        _thickness = value;
    }
    
    /**
     * Get if draw label
     * @return Boolean
     */
    public boolean isDrawLabel(){
        return this.drawLabel;
    }
    
    /**
     * Set if draw label
     * @param value Boolean
     */
    public void setDrawLabel(boolean value){
        this.drawLabel = value;
    }
    
    /**
     * Get label font
     * @return Label font
     */
    public Font getLabelFont(){
        return this.labelFont;
    }
    
    /**
     * Set label font
     * @param value Label font
     */
    public void setLabelFont(Font value){
        this.labelFont = value;
    }
    
    /**
     * Get label color
     * @return Label color
     */
    public Color getLabelColor(){
        return this.labelColor;
    }
    
    /**
     * Set label color
     * @param value Label color
     */
    public void setLabelColor(Color value){
        this.labelColor = value;
    }
    
    /**
     * Get decimal digits
     * @return Decimal digits
     */
    public int getDecimalDigits(){
        return this.decimalDigits;
    }
    
    /**
     * Set decimal digits
     * @param value Decimal digits
     */
    public void setDecimalDigits(int value){
        this.decimalDigits = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update - avoid the disagree of field names and legend scheme
     */
    public void update(){
        if (this._fieldNames.size() != this._legendScheme.getBreakNum()){
            this._fieldNames = new ArrayList<>();
            _legendScheme = new LegendScheme(ShapeTypes.Polygon);
        }
    }
    // </editor-fold>
}
