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
package org.meteoinfo.legend;

import java.awt.Color;
import java.awt.Font;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.PointF;
import org.meteoinfo.shape.ShapeTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chart break class
 *
 * @author Yaqiang Wang
 */
public class ChartBreak extends ColorBreak {
    // <editor-fold desc="Variables">

    private ChartTypes _chartType;
    private List<Float> _chartData;
    private int _xShift;
    private int _yShift;
    private LegendScheme _legendScheme;
    private int _maxSize;
    private int _minSize;
    private float _maxValue;
    private float _minValue;
    private int _barWidth;
    private AlignType _alignType;
    private boolean _view3D;
    private int _thickness;
    private int _shpIdx;
    private boolean drawLabel;
    private Font labelFont;
    private Color labelColor;
    private int decimalDigits;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param chartType Chart type
     */
    public ChartBreak(ChartTypes chartType) {
        super();
        this.setBreakType(BreakTypes.ChartBreak);
        _chartType = chartType;
        _chartData = new ArrayList<>();
        _xShift = 0;
        _yShift = 0;
        _legendScheme = new LegendScheme(ShapeTypes.Polygon);
        _maxSize = 50;
        _minSize = 10;
        _barWidth = 10;
        _alignType = AlignType.Center;
        _view3D = false;
        _thickness = 5;
        _shpIdx = 0;
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
     * @param cType Chart type
     */
    public void setChartType(ChartTypes cType) {
        _chartType = cType;
    }

    /**
     * Get chart data
     *
     * @return Chart data list
     */
    public List<Float> getChartData() {
        return _chartData;
    }

    /**
     * Set chart data
     *
     * @param cData Chart data list
     */
    public void setChartData(List<Float> cData) {
        _chartData = cData;
    }

    /**
     * Get chart item number
     *
     * @return Chart item number
     */
    public int getItemNum() {
        return _chartData.size();
    }

    /**
     * Get chart data sum
     *
     * @return Chart data sum
     */
    public float getDataSum() {
        float sum = 0;
        for (float d : _chartData) {
            sum += d;
        }
        return sum;
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
     * @param xshift X shift
     */
    public void setXShift(int xshift) {
        _xShift = xshift;
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
     * @param yshift Y shift
     */
    public void setYShift(int yshift) {
        _yShift = yshift;
    }

    /**
     * Get legend scheme
     *
     * @return Legend Scheme
     */
    public LegendScheme getLegendScheme() {
        return _legendScheme;
    }

    /**
     * Set legend scheme
     *
     * @param ls Legend scheme
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
     * @param maxSize Maximum size
     */
    public void setMaxSize(int maxSize) {
        _maxSize = maxSize;
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
     * @param minSize Minimum size
     */
    public void setMinSize(int minSize) {
        _minSize = minSize;
    }

    /**
     * Get maximum value
     *
     * @return Maximum value
     */
    public float getMaxValue() {
        return _maxValue;
    }

    /**
     * Set maximum value
     *
     * @param maxValue Maximum value
     */
    public void setMaxValue(float maxValue) {
        _maxValue = maxValue;
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
     * @param minValue Minimum value
     */
    public void setMinValue(float minValue) {
        _minValue = minValue;
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
     * @param atype Align type
     */
    public void setAlignType(AlignType atype) {
        _alignType = atype;
    }

    /**
     * Get if view 3D
     *
     * @return Boolean
     */
    public boolean isView3D() {
        return _view3D;
    }

    /**
     * Set if view 3D
     *
     * @param v3d
     */
    public void setView3D(boolean v3d) {
        _view3D = v3d;
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
     * @param thickness 3D thickness
     */
    public void setThickness(int thickness) {
        _thickness = thickness;
    }

    /**
     * Get shape index
     *
     * @return Shape index
     */
    public int getShapeIndex() {
        return _shpIdx;
    }

    /**
     * Set shape index
     *
     * @param sIdx Shape index
     */
    public void setShapeIndex(int sIdx) {
        _shpIdx = sIdx;
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
     * Get bar heights
     *
     * @return Bar heights
     */
    public List<Integer> getBarHeights() {
        List<Integer> heights = new ArrayList<>();
        int i, h;
        for (i = 0; i < _chartData.size(); i++) {
            if (_minSize == 0) {
                h = (int) (_chartData.get(i) / _maxValue * _maxSize);
            } else {
                h = (int) ((_chartData.get(i) - _minValue) / (_maxValue - _minValue)
                        * (_maxSize - _minSize) + _minSize);
            }
            heights.add(h);
        }

        return heights;
    }

    /**
     * Get chart width
     *
     * @return Chart width
     */
    public int getWidth() {
        int width = 0;
        switch (_chartType) {
            case BarChart:
                width = _barWidth * _chartData.size();
                if (_view3D) {
                    width += _thickness;
                }
                break;
            case PieChart:
                if (_minSize == _maxSize) {
                    width = _maxSize;
                } else if (_minSize == 0) {
                    width = (int) (this.getDataSum() / _maxValue * _maxSize);
                } else {
                    width = (int) ((this.getDataSum() - _minValue) / (_maxValue - _minValue)
                            * (_maxSize - _minSize) + _minSize);
                }
                break;
        }

        return width;
    }

    /**
     * Get chart height
     *
     * @return Chart height
     */
    public int getHeight() {
        int height = 0;
        switch (_chartType) {
            case BarChart:
                height = Collections.max(getBarHeights());
                break;
            case PieChart:
                if (_minSize == _maxSize) {
                    height = _maxSize;
                } else if (_minSize == 0) {
                    height = (int) (this.getDataSum() / _maxValue * _maxSize);
                } else {
                    height = (int) ((this.getDataSum() - _minValue) / (_maxValue - _minValue)
                            * (_maxSize - _minSize) + _minSize);
                }

                if (_view3D) {
                    height = height * 2 / 3;
                }
                break;
        }

        if (_view3D) {
            height += _thickness;
        }

        return height;
    }

    /**
     * Get pie angles
     *
     * @return Pie angle list
     */
    public List<List<Float>> getPieAngles() {
        List<List<Float>> angles = new ArrayList<>();
        float sum = this.getDataSum();
        float startAngle = 0;
        float sweepAngle;
        for (Float value : _chartData) {
            sweepAngle = value / sum * 360;
            List<Float> ssa = new ArrayList<>();
            ssa.add(startAngle);
            ssa.add(sweepAngle);
            angles.add(ssa);
            startAngle += sweepAngle;
            if (startAngle > 360) {
                startAngle = startAngle - 360;
            }
        }

        return angles;
    }
    
    /**
     * Get pie ratios
     *
     * @return Pie ratio list
     */
    public List<Float> getPieRatios() {
        List<Float> ratios = new ArrayList<>();
        float sum = this.getDataSum();
        float ratio;
        for (Float value : _chartData) {
            ratio = value / sum;
            ratios.add(ratio);
        }

        return ratios;
    }

    /**
     * Clone
     *
     * @return ChartBreak object
     */
    @Override
    public Object clone() {
        ChartBreak aCB = new ChartBreak(_chartType);
        aCB.setCaption(this.getCaption());
        aCB.setAlignType(_alignType);
        aCB.setBarWidth(_barWidth);
        aCB.setChartData(new ArrayList<>(_chartData));
        aCB.setColor(this.getColor());
        aCB.setDrawShape(this.isDrawShape());
        aCB.setLegendScheme(_legendScheme);
        aCB.setMaxSize(_maxSize);
        aCB.setMaxValue(_maxValue);
        aCB.setMinSize(_minSize);
        aCB.setMinValue(_minValue);
        aCB.setThickness(_thickness);
        aCB.setView3D(_view3D);
        aCB.setXShift(_xShift);
        aCB.setYShift(_yShift);
        aCB.setDrawLabel(this.drawLabel);
        aCB.setLabelColor(this.labelColor);
        aCB.setLabelFont(labelFont);
        aCB.setDecimalDigits(this.decimalDigits);

        return aCB;
    }

    /**
     * Get sample chart break
     *
     * @return Sample chart break
     */
    public ChartBreak getSampleChartBreak() {
        ChartBreak aCB = (ChartBreak) clone();
        int i;
        switch (aCB.getChartType()) {
            case BarChart:
                float min = aCB.getMaxValue() / aCB.getItemNum();
                float dv = (aCB.getMaxValue() - min) / aCB.getItemNum();
                for (i = 0; i < aCB.getItemNum(); i++) {
                    aCB.getChartData().set(i, min + dv * i);
                }
                break;
            case PieChart:
                //float sum = (aCB.getMaxValue() - aCB.getMinValue()) * 2 / 3;
                float sum = aCB.getMaxValue();
                float data = sum / aCB.getItemNum();
                for (i = 0; i < aCB.getItemNum(); i++) {
                    aCB.getChartData().set(i, data);
                }
                aCB.setMinSize(aCB._maxSize);
                //aCB.setMaxSize(20);
                break;
        }

        return aCB;
    }

    /**
     * Get draw extent
     *
     * @param aPoint start point
     * @return draw extent
     */
    public Extent getDrawExtent(PointF aPoint) {
        int width = this.getWidth();
        int height = this.getHeight();
        switch (_alignType) {
            case Center:
                aPoint.X -= width / 2;
                aPoint.Y += height / 2;
                break;
            case Left:
                aPoint.X -= width;
                aPoint.Y += height / 2;
                break;
            case Right:
                aPoint.Y += height / 2;
                break;
        }
        aPoint.X += _xShift;
        aPoint.Y -= _yShift;

        Extent aExtent = new Extent();
        aExtent.minX = aPoint.X;
        aExtent.maxX = aPoint.X + width;
        aExtent.minY = aPoint.Y - height;
        aExtent.maxY = aPoint.Y;

        return aExtent;
    }
    // </editor-fold>
}
