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

import org.meteoinfo.data.meteodata.DrawType2D;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.colors.ColorMap;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.shape.ShapeTypes;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static org.meteoinfo.shape.ShapeTypes.Point;
import static org.meteoinfo.shape.ShapeTypes.PointM;
import static org.meteoinfo.shape.ShapeTypes.PointZ;
import static org.meteoinfo.shape.ShapeTypes.Polygon;
import static org.meteoinfo.shape.ShapeTypes.PolygonM;
import static org.meteoinfo.shape.ShapeTypes.PolygonZ;
import static org.meteoinfo.shape.ShapeTypes.Polyline;
import static org.meteoinfo.shape.ShapeTypes.PolylineM;
import static org.meteoinfo.shape.ShapeTypes.PolylineZ;
import org.w3c.dom.DOMException;

/**
 * Legend scheme class
 *
 * @author Yaqiang Wang
 */
public class LegendScheme {
    // <editor-fold desc="Variables">

    private String fieldName = "";
    private LegendType legendType = LegendType.SingleSymbol;
    private ShapeTypes shapeType;
    private List<ColorBreak> legendBreaks;
    private boolean hasNoData;
    private double minValue;
    private double maxValue;
    private double undef;
    private Map<Object, ColorBreak> uniqueValueMap;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public LegendScheme(){
        this.shapeType = ShapeTypes.Image;
        legendBreaks = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param aShapeType
     */
    public LegendScheme(ShapeTypes aShapeType) {
        shapeType = aShapeType;        
        legendBreaks = new ArrayList<>();
    }
    
    /**
     * Constructor
     *
     * @param aShapeType
     * @param n Break number
     */
    public LegendScheme(ShapeTypes aShapeType, int n) {
        shapeType = aShapeType;        
        legendBreaks = new ArrayList<>();
        ColorBreak cb;
        for (int i = 0; i < n; i++) {
            switch (aShapeType) {
                case Point:
                    legendBreaks.add(cb = new PointBreak());                    
                    break;
                case Polyline:
                    legendBreaks.add(cb = new PolylineBreak());
                    break;
                case Polygon:
                    legendBreaks.add(cb = new PolygonBreak());
                    break;
                default:
                    legendBreaks.add(cb = new ColorBreak());
                    break;
            }
            cb.setColor(ColorUtil.getCommonColor(i));
        }
    }
    
    /**
     * Constructor
     * @param lbs Legend breaks
     */
    public LegendScheme(List<ColorBreak> lbs){
        this.legendBreaks = lbs;
        ColorBreak lb = lbs.get(0);
        switch (lb.getBreakType()){
            case PointBreak:
                this.shapeType = ShapeTypes.Point;
                break;
            case PolylineBreak:
                this.shapeType = ShapeTypes.Polyline;
                break;
            case PolygonBreak:
                this.shapeType = ShapeTypes.Polygon;
                break;
            default:
                this.shapeType = ShapeTypes.Image;
                break;
        }
        if (lbs.size() == 1)
            this.legendType = LegendType.SingleSymbol;
        else {
            if (lbs.get(0).getStartValue() == lbs.get(0).getEndValue())
                this.legendType = LegendType.UniqueValue;
            else
                this.legendType = LegendType.GraduatedColor;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get field name
     *
     * @return The field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Set field name
     *
     * @param fn The field name
     */
    public void setFieldName(String fn) {
        fieldName = fn;
    }
    
    /**
     * Is Geometry legend scheme or not
     * @return Boolean
     */
    public boolean isGeometry(){
        return this.fieldName.equals("Geometry_M") || this.fieldName.equals("Geometry_Z");
    }

    /**
     * Get legend type
     *
     * @return The legend type
     */
    public LegendType getLegendType() {
        return legendType;
    }

    /**
     * Set legend type
     *
     * @param lt The legend type
     */
    public void setLegendType(LegendType lt) {
        legendType = lt;
        if (lt == LegendType.UniqueValue)
            this.updateUniqueValueMap();
    }

    /**
     * Get shape type
     *
     * @return The shape type
     */
    public ShapeTypes getShapeType() {
        return shapeType;
    }

    /**
     * Set shape type
     *
     * @param st The shape type
     */
    public void setShapeType(ShapeTypes st) {
        shapeType = st;
    }

    /**
     * Get break type
     *
     * @return The break type
     */
    public BreakTypes getBreakType() {
        BreakTypes breakType = BreakTypes.ColorBreak;
        switch (this.shapeType) {
            case Point:
            case PointM:
            case PointZ:
                breakType = BreakTypes.PointBreak;
                break;
            case Polyline:
            case PolylineM:
            case PolylineZ:
                breakType = BreakTypes.PolylineBreak;
                break;
            case Polygon:
            case PolygonM:
            case PolygonZ:
                breakType = BreakTypes.PolygonBreak;
                break;
        }
        return breakType;
    }

//    /**
//     * Set break type
//     *
//     * @param bt The break type
//     */
//    public void setBreakType(BreakTypes bt) {
//        breakType = bt;
//    }

    /**
     * Get legend breaks
     *
     * @return The legend breaks
     */
    public List<ColorBreak> getLegendBreaks() {
        return legendBreaks;
    }
    
    /**
     * Get legend break by index
     * @param i Index
     * @return A legend break
     */
    public ColorBreak getLegendBreak(int i) {
        return this.legendBreaks.get(i);
    }

    /**
     * Set legend breaks
     *
     * @param breaks The legend breaks
     */
    public void setLegendBreaks(List<ColorBreak> breaks) {
        legendBreaks = breaks;
        if (this.legendType == LegendType.UniqueValue)
            this.updateUniqueValueMap();
    }
    
    /**
     * Set a legendBreak
     * @param i Index
     * @param value Legend break
     */
    public void setLegendBreak(int i, ColorBreak value) {
        this.legendBreaks.set(i, value);
    }

    /**
     * Get if has no data
     *
     * @return If has no data
     */
    public boolean getHasNoData() {
        return hasNoData;
    }

    /**
     * Set if has no data
     *
     * @param istrue If has no data
     */
    public void setHasNoData(boolean istrue) {
        hasNoData = istrue;
    }

    /**
     * Get minimum value
     *
     * @return Minimum value
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Set minimum value
     *
     * @param min
     */
    public void setMinValue(double min) {
        minValue = min;
    }

    /**
     * Get maximum value
     *
     * @return Maximum value
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Set maximum value
     *
     * @param max Maximum value
     */
    public void setMaxValue(double max) {
        maxValue = max;
    }

    /**
     * Get undefine value
     *
     * @return Undefine value
     */
    public double getUndefValue() {
        return undef;
    }

    /**
     * Set undefine value
     *
     * @param uv Undefine value
     */
    public void setUndefValue(double uv) {
        undef = uv;
    }

    /**
     * Get legend break number
     *
     * @return Legend break number
     */
    public int getBreakNum() {
        return legendBreaks.size();
    }

    /**
     * Get visible legend breaks number
     *
     * @return The visible legend breaks number
     */
    public int getVisibleBreakNum() {
        int n = 0;
        for (ColorBreak aCB : this.legendBreaks) {
            if (aCB.isDrawShape()) {
                n += 1;
            }
        }

        return n;
    }

    /**
     * Get visible break number
     *
     * @return Visible break number
     */
    public int VisibleBreakNum() {
        int n = 0;
        for (ColorBreak aCB : legendBreaks) {
            if (aCB.isDrawShape()) {
                n += 1;
            }
        }

        return n;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Add a legend break
     * @param lb Legend break
     */
    public void addLegendBreak(ColorBreak lb){
        this.legendBreaks.add(lb);
        if (this.legendType == LegendType.UniqueValue)
            this.updateUniqueValueMap();
    }
    
    /**
     * Add a legend breaks
     * @param lb Legend breaks
     */
    public void addLegendBreak(List<ColorBreak> lb){
        this.legendBreaks.addAll(lb);
        if (this.legendType == LegendType.UniqueValue)
            this.updateUniqueValueMap();
    }
    
    /**
     * Find breaks
     * @param values Values
     * @return Color breaks
     */
    public List<ColorBreak> findBreaks(List<Double> values){
        List<ColorBreak> cbs = new ArrayList<>();
        for (double v : values) {
            cbs.add(findLegendBreak(v));
        }
        
        return cbs;
    }
    
    /**
     * Find legend break by value
     * @param v Value
     * @return Legend break
     */
    public ColorBreak findLegendBreak(Number v){
        switch (this.legendType) {
            case SingleSymbol:
                return this.legendBreaks.get(0);
            case UniqueValue:
                if (this.uniqueValueMap == null || this.uniqueValueMap.size() != this.legendBreaks.size())
                    this.updateUniqueValueMap();
                if (this.uniqueValueMap.containsKey(v)) {
                    return this.uniqueValueMap.get(v);
                } else {
                    return this.legendBreaks.get(0);
                }
            default:
                double sv, ev;
                for (ColorBreak cb : this.legendBreaks){
                    sv = Double.parseDouble(cb.getStartValue().toString());
                    ev = Double.parseDouble(cb.getEndValue().toString());
                    if (sv == ev){
                        if (v.doubleValue() == sv)
                            return cb;
                    } else {
                        if (v.doubleValue() >= sv && v.doubleValue() < ev){
                            return cb;
                        }
                    }
                }
                if (v.doubleValue() >= this.getMaxValue())
                    return this.legendBreaks.get(this.getBreakNum() - 1);
                else
                    return this.legendBreaks.get(0);
        }
    }
    
    /**
     * Get legend break index by value
     * @param v Value
     * @return Legend break index
     */
    public int legendBreakIndex(double v) {
        double sv, ev;
        for (int i = 0; i < this.legendBreaks.size(); i ++){
            ColorBreak cb = this.legendBreaks.get(i);
            sv = Double.parseDouble(cb.getStartValue().toString());
            ev = Double.parseDouble(cb.getEndValue().toString());
            if (sv == ev){
                if (v == sv)
                    return i;
            } else {
                if (v >= sv && v < ev){
                    return i;
                }
            }
        }
        if (v >= this.getMaxValue())
            return this.getBreakNum() - 1;
        else
            return 0;

    }

    /**
     * Get color list
     *
     * @return Color list
     */
    public List<Color> getColors() {
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < legendBreaks.size(); i++) {
            colors.add(legendBreaks.get(i).getColor());
        }

        return colors;
    }

    private void updateUniqueValueMap() {
        this.uniqueValueMap = new HashMap<Object, ColorBreak>();
        boolean isDouble = true;
        for (ColorBreak cb : this.legendBreaks) {
            if (!DataConvert.isDouble(cb.getStartValue().toString())) {
                isDouble = false;
                break;
            }
        }
        if (isDouble) {
            for (ColorBreak cb : this.legendBreaks) {
                this.uniqueValueMap.put(Double.parseDouble(cb.getStartValue().toString()), cb);
            }
        } else {
            for (ColorBreak cb : this.legendBreaks) {
                this.uniqueValueMap.put(cb.getStartValue(), cb);
            }
        }
    }

    /**
     * Update legend colors by color map
     * @param colorMap The color map
     */
    public void updateColors(ColorMap colorMap) {
        int n = this.legendBreaks.size();
        for (ColorBreak lb : this.legendBreaks) {
            if (lb.isNoData()) {
                n -= 1;
            }
        }
        Color[] colors = colorMap.getColors(n);
        int i = 0;
        for (ColorBreak lb : this.legendBreaks) {
            if (!lb.isNoData()) {
                lb.setColor(colors[i]);
                i += 1;
            }
        }
    }

    /**
     * Set fill value color
     * @param color The fill value color
     */
    public void setFillColor(Color color) {
        for (ColorBreak lb : this.legendBreaks) {
            if (lb.isNoData()) {
                lb.setColor(color);
            }
        }
    }

    /**
     * Judge if shape type is consistent with draw type
     *
     * @param drawTyp Draw type
     * @return Boolean
     */
    public boolean isConsistent(DrawType2D drawTyp) {
        switch (shapeType) {
            case Point:
            case PointZ:
            case StationModel:
            case WeatherSymbol:
            case WindArraw:
            case WindBarb:
                switch (drawTyp) {
                    case Grid_Point:
                    case Station_Info:
                    case Station_Model:
                    case Station_Point:
                    case Traj_Point:
                    case Traj_StartPoint:
                    case Weather_Symbol:
                    case Barb:
                        return true;
                    default:
                        return false;
                }
            case Polyline:
            case PolylineZ:
                switch (drawTyp) {
                    case Contour:
                    case Streamline:
                    case Traj_Line:
                        return true;
                    default:
                        return false;
                }
            case Polygon:
                switch (drawTyp) {
                    case Shaded:
                        return true;
                    default:
                        return false;
                }
            case Image:
                switch (drawTyp) {
                    case Raster:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }
    
    /**
     * Convert to other shape type
     * @param shapeType The shape type
     * @return Result legend scheme
     */
    public LegendScheme convertTo(ShapeTypes shapeType){
        if (this.shapeType == shapeType){
            return this;
        }
        
        LegendScheme ls = new LegendScheme(shapeType);
        ls.fieldName = this.fieldName;
        ls.hasNoData = this.hasNoData;
        ls.legendType = this.legendType;
        ls.minValue = this.minValue;
        ls.maxValue = this.maxValue;
        ls.undef = this.undef;        
        for (ColorBreak cb : this.legendBreaks){
            switch(shapeType){
                case Point:
                    PointBreak pb = new PointBreak();
                    pb.setColor(cb.getColor());
                    pb.setStartValue(cb.getStartValue());
                    pb.setEndValue(cb.getEndValue());
                    pb.setCaption(cb.getCaption());
                    pb.setNoData(cb.isNoData());
                    pb.setDrawShape(cb.isDrawShape());
                    pb.setTag(cb.getTag());
                    ls.legendBreaks.add(pb);
                    break;
                case Polyline:
                    PolylineBreak plb = new PolylineBreak();
                    plb.setColor(cb.getColor());
                    plb.setStartValue(cb.getStartValue());
                    plb.setEndValue(cb.getEndValue());
                    plb.setCaption(cb.getCaption());
                    plb.setNoData(cb.isNoData());
                    plb.setDrawShape(cb.isDrawShape());
                    plb.setTag(cb.getTag());
                    ls.legendBreaks.add(plb);
                    break;
                case Polygon:
                    PolygonBreak pgb = new PolygonBreak();
                    pgb.setColor(cb.getColor());
                    //System.out.println(pgb.getColor().getAlpha());
                    pgb.setStartValue(cb.getStartValue());
                    pgb.setEndValue(cb.getEndValue());
                    pgb.setCaption(cb.getCaption());
                    pgb.setNoData(cb.isNoData());
                    pgb.setDrawShape(cb.isDrawShape());
                    pgb.setDrawOutline(false);
                    pgb.setTag(cb.getTag());
                    ls.legendBreaks.add(pgb);
                    break;
                case Image:
                    ColorBreak ncb = new ColorBreak();
                    ncb.setColor(cb.getColor());
                    ncb.setStartValue(cb.getStartValue());
                    ncb.setEndValue(cb.getEndValue());
                    ncb.setCaption(cb.getCaption());
                    ncb.setNoData(cb.isNoData());
                    ncb.setDrawShape(cb.isDrawShape());
                    ncb.setTag(cb.getTag());
                    ls.legendBreaks.add(ncb);
                    break;
            }
        }
        
        return ls;
    }

    /**
     * Convert to other shape type
     * @param shapeType The shape type
     * @param edgeColor Colors to edge colors
     * @return Result legend scheme
     */
    public LegendScheme convertTo(ShapeTypes shapeType, boolean edgeColor){
        if (this.shapeType == shapeType){
            return this;
        }

        LegendScheme ls = new LegendScheme(shapeType);
        ls.fieldName = this.fieldName;
        ls.hasNoData = this.hasNoData;
        ls.legendType = this.legendType;
        ls.minValue = this.minValue;
        ls.maxValue = this.maxValue;
        ls.undef = this.undef;
        for (ColorBreak cb : this.legendBreaks){
            switch(shapeType){
                case Point:
                    PointBreak pb = new PointBreak();
                    if (edgeColor)
                        pb.setOutlineColor(cb.getColor());
                    else
                        pb.setColor(cb.getColor());
                    pb.setStartValue(cb.getStartValue());
                    pb.setEndValue(cb.getEndValue());
                    pb.setCaption(cb.getCaption());
                    pb.setNoData(cb.isNoData());
                    pb.setDrawShape(cb.isDrawShape());
                    pb.setTag(cb.getTag());
                    ls.legendBreaks.add(pb);
                    break;
                case Polyline:
                    PolylineBreak plb = new PolylineBreak();
                    plb.setColor(cb.getColor());
                    plb.setStartValue(cb.getStartValue());
                    plb.setEndValue(cb.getEndValue());
                    plb.setCaption(cb.getCaption());
                    plb.setNoData(cb.isNoData());
                    plb.setDrawShape(cb.isDrawShape());
                    plb.setTag(cb.getTag());
                    ls.legendBreaks.add(plb);
                    break;
                case Polygon:
                    PolygonBreak pgb = new PolygonBreak();
                    if (edgeColor)
                        pgb.setOutlineColor(cb.getColor());
                    else
                        pgb.setColor(cb.getColor());
                    pgb.setStartValue(cb.getStartValue());
                    pgb.setEndValue(cb.getEndValue());
                    pgb.setCaption(cb.getCaption());
                    pgb.setNoData(cb.isNoData());
                    pgb.setDrawShape(cb.isDrawShape());
                    pgb.setDrawOutline(true);
                    pgb.setTag(cb.getTag());
                    ls.legendBreaks.add(pgb);
                    break;
                case Image:
                    ColorBreak ncb = new ColorBreak();
                    ncb.setColor(cb.getColor());
                    switch (this.shapeType) {
                        case Point:
                            if (edgeColor)
                                ncb.setColor(((PointBreak)cb).getOutlineColor());
                            break;
                        case Polygon:
                            if (edgeColor)
                                ncb.setColor(((PolygonBreak)cb).getOutlineColor());
                            break;
                    }
                    ncb.setStartValue(cb.getStartValue());
                    ncb.setEndValue(cb.getEndValue());
                    ncb.setCaption(cb.getCaption());
                    ncb.setNoData(cb.isNoData());
                    ncb.setDrawShape(cb.isDrawShape());
                    ncb.setTag(cb.getTag());
                    ls.legendBreaks.add(ncb);
                    break;
            }
        }

        return ls;
    }
    
    /**
     * Convert point legend to arrow legend
     */
    public void asArrow() {        
        if (this.getShapeType() != ShapeTypes.Point)
            return;
        
        if (this.legendBreaks.get(0) instanceof ArrowBreak) {
            return;
        }
        
        for (int i = 0; i < this.legendBreaks.size(); i++) {
            this.legendBreaks.set(i, new ArrowBreak((PointBreak)this.legendBreaks.get(i)));
        }
    }

    /**
     * Export to xml document
     *
     * @param doc xml document
     * @param parent Parent xml element
     */
    public void exportToXML(Document doc, Element parent) {
        Element root = doc.createElement("LegendScheme");
        Attr fieldNameAttr = doc.createAttribute("FieldName");
        Attr legendTypeAttr = doc.createAttribute("LegendType");
        Attr shapeTypeAttr = doc.createAttribute("ShapeType");
        Attr breakNumAttr = doc.createAttribute("BreakNum");
        Attr hasNoDataAttr = doc.createAttribute("HasNoData");
        Attr minValueAttr = doc.createAttribute("MinValue");
        Attr maxValueAttr = doc.createAttribute("MaxValue");
        Attr undefAttr = doc.createAttribute("UNDEF");

        fieldNameAttr.setValue(this.fieldName);
        legendTypeAttr.setValue(this.legendType.toString());
        shapeTypeAttr.setValue(this.shapeType.toString());
        breakNumAttr.setValue(String.valueOf(this.getBreakNum()));
        hasNoDataAttr.setValue(String.valueOf(this.hasNoData));
        minValueAttr.setValue(String.valueOf(this.minValue));
        maxValueAttr.setValue(String.valueOf(this.maxValue));
        undefAttr.setValue(String.valueOf(this.undef));

        root.setAttributeNode(fieldNameAttr);
        root.setAttributeNode(legendTypeAttr);
        root.setAttributeNode(shapeTypeAttr);
        root.setAttributeNode(breakNumAttr);
        root.setAttributeNode(hasNoDataAttr);
        root.setAttributeNode(minValueAttr);
        root.setAttributeNode(maxValueAttr);
        root.setAttributeNode(undefAttr);

        Element breaks = doc.createElement("Breaks");
        Element brk;
        Attr caption;
        Attr startValue;
        Attr endValue;
        Attr color;
        Attr drawShape;
        Attr size;
        Attr style;
        Attr outlineColor, outlineSize;
        Attr drawOutline;
        Attr drawFill;
        Attr tagAttr;
        switch (this.shapeType) {
            case Point:
            case PointZ:
                Attr isNoData;
                for (ColorBreak aCB : this.legendBreaks) {
                    PointBreak aPB = (PointBreak) aCB;
                    brk = doc.createElement("Break");
                    caption = doc.createAttribute("Caption");
                    startValue = doc.createAttribute("StartValue");
                    endValue = doc.createAttribute("EndValue");
                    color = doc.createAttribute("Color");
                    drawShape = doc.createAttribute("DrawShape");
                    outlineColor = doc.createAttribute("OutlineColor");
                    outlineSize = doc.createAttribute("OutlineSize");
                    size = doc.createAttribute("Size");
                    style = doc.createAttribute("Style");
                    drawOutline = doc.createAttribute("DrawOutline");
                    drawFill = doc.createAttribute("DrawFill");
                    isNoData = doc.createAttribute("IsNoData");
                    Attr markerType = doc.createAttribute("MarkerType");
                    Attr fontName = doc.createAttribute("FontName");
                    Attr charIndex = doc.createAttribute("CharIndex");
                    Attr imagePath = doc.createAttribute("ImagePath");
                    Attr angle = doc.createAttribute("Angle");
                    tagAttr = doc.createAttribute("Tag");

                    caption.setValue(aPB.getCaption());
                    startValue.setValue(String.valueOf(aPB.getStartValue()));
                    endValue.setValue(String.valueOf(aPB.getEndValue()));
                    color.setValue(ColorUtil.toHexEncoding(aPB.getColor()));
                    drawShape.setValue(String.valueOf(aPB.isDrawShape()));
                    outlineColor.setValue(ColorUtil.toHexEncoding(aPB.getOutlineColor()));
                    outlineSize.setValue(String.valueOf(aPB.getOutlineSize()));
                    size.setValue(String.valueOf(aPB.getSize()));
                    style.setValue(aPB.getStyle().toString());
                    drawOutline.setValue(String.valueOf(aPB.isDrawOutline()));
                    drawFill.setValue(String.valueOf(aPB.isDrawFill()));
                    isNoData.setValue(String.valueOf(aPB.isNoData()));
                    markerType.setValue(aPB.getMarkerType().toString());
                    fontName.setValue(aPB.getFontName());
                    charIndex.setValue(String.valueOf(aPB.getCharIndex()));
                    imagePath.setValue(aPB.getImagePath());
                    angle.setValue(String.valueOf(aPB.getAngle()));
                    tagAttr.setValue(aPB.getTag());

                    brk.setAttributeNode(caption);
                    brk.setAttributeNode(startValue);
                    brk.setAttributeNode(endValue);
                    brk.setAttributeNode(color);
                    brk.setAttributeNode(drawShape);
                    brk.setAttributeNode(outlineColor);
                    brk.setAttributeNode(outlineSize);
                    brk.setAttributeNode(size);
                    brk.setAttributeNode(style);
                    brk.setAttributeNode(drawOutline);
                    brk.setAttributeNode(drawFill);
                    brk.setAttributeNode(isNoData);
                    brk.setAttributeNode(markerType);
                    brk.setAttributeNode(fontName);
                    brk.setAttributeNode(charIndex);
                    brk.setAttributeNode(imagePath);
                    brk.setAttributeNode(angle);
                    brk.setAttributeNode(tagAttr);

                    breaks.appendChild(brk);
                }
                break;
            case Polyline:
            case PolylineZ:
                for (ColorBreak aCB : this.legendBreaks) {
                    PolylineBreak aPLB = (PolylineBreak) aCB;
                    brk = doc.createElement("Break");
                    caption = doc.createAttribute("Caption");
                    startValue = doc.createAttribute("StartValue");
                    endValue = doc.createAttribute("EndValue");
                    color = doc.createAttribute("Color");
                    drawShape = doc.createAttribute("DrawShape");
                    size = doc.createAttribute("Size");
                    style = doc.createAttribute("Style");
                    Attr drawSymbol = doc.createAttribute("DrawSymbol");
                    Attr symbolSize = doc.createAttribute("SymbolSize");
                    Attr symbolStyle = doc.createAttribute("SymbolStyle");
                    Attr symbolColor = doc.createAttribute("SymbolColor");                    
                    Attr symbolInterval = doc.createAttribute("SymbolInterval");
                    Attr fillSymbol = doc.createAttribute("FillSymbol");
                    Attr symbolFillColor = doc.createAttribute("SymbolFillColor");
                    tagAttr = doc.createAttribute("Tag");

                    caption.setValue(aPLB.getCaption());
                    startValue.setValue(String.valueOf(aPLB.getStartValue()));
                    endValue.setValue(String.valueOf(aPLB.getEndValue()));
                    color.setValue(ColorUtil.toHexEncoding(aPLB.getColor()));
                    drawShape.setValue(String.valueOf(aPLB.isDrawShape()));
                    size.setValue(String.valueOf(aPLB.getWidth()));
                    style.setValue(aPLB.getStyle().toString());
                    drawSymbol.setValue(String.valueOf(aPLB.getDrawSymbol()));
                    symbolSize.setValue(String.valueOf(aPLB.getSymbolSize()));
                    symbolStyle.setValue(aPLB.getSymbolStyle().toString());                    
                    symbolColor.setValue(ColorUtil.toHexEncoding(aPLB.getSymbolColor()));
                    symbolInterval.setValue(String.valueOf(aPLB.getSymbolInterval()));
                    fillSymbol.setValue(String.valueOf(aPLB.isFillSymbol()));
                    symbolFillColor.setValue(ColorUtil.toHexEncoding(aPLB.getSymbolFillColor()));
                    tagAttr.setValue(aPLB.getTag());

                    brk.setAttributeNode(caption);
                    brk.setAttributeNode(startValue);
                    brk.setAttributeNode(endValue);
                    brk.setAttributeNode(color);
                    brk.setAttributeNode(drawShape);
                    brk.setAttributeNode(size);
                    brk.setAttributeNode(style);
                    brk.setAttributeNode(drawSymbol);
                    brk.setAttributeNode(symbolSize);
                    brk.setAttributeNode(symbolStyle);
                    brk.setAttributeNode(symbolColor);
                    brk.setAttributeNode(symbolInterval);
                    brk.setAttributeNode(fillSymbol);
                    brk.setAttributeNode(symbolFillColor);
                    brk.setAttributeNode(tagAttr);

                    breaks.appendChild(brk);
                }
                break;
            case Polygon:
            case PolygonM:
            case PolygonZ:
                for (ColorBreak aCB : this.legendBreaks) {
                    PolygonBreak aPGB = (PolygonBreak) aCB;
                    brk = doc.createElement("Break");
                    caption = doc.createAttribute("Caption");
                    startValue = doc.createAttribute("StartValue");
                    endValue = doc.createAttribute("EndValue");
                    color = doc.createAttribute("Color");
                    drawShape = doc.createAttribute("DrawShape");
                    outlineColor = doc.createAttribute("OutlineColor");
                    drawOutline = doc.createAttribute("DrawOutline");
                    drawFill = doc.createAttribute("DrawFill");
                    outlineSize = doc.createAttribute("OutlineSize");
                    style = doc.createAttribute("Style");
                    Attr styleSize = doc.createAttribute("StyleSize");
                    Attr backColor = doc.createAttribute("BackColor");
                    tagAttr = doc.createAttribute("Tag");

                    caption.setValue(aPGB.getCaption());
                    startValue.setValue(String.valueOf(aPGB.getStartValue()));
                    endValue.setValue(String.valueOf(aPGB.getEndValue()));
                    color.setValue(ColorUtil.toHexEncoding(aPGB.getColor()));
                    drawShape.setValue(String.valueOf(aPGB.isDrawShape()));
                    outlineColor.setValue(ColorUtil.toHexEncoding(aPGB.getOutlineColor()));
                    drawOutline.setValue(String.valueOf(aPGB.isDrawOutline()));
                    drawFill.setValue(String.valueOf(aPGB.isDrawFill()));
                    outlineSize.setValue(String.valueOf(aPGB.getOutlineSize()));
                    style.setValue(aPGB.getStyle().toString());
                    styleSize.setValue(String.valueOf(aPGB.getStyleSize()));
                    backColor.setValue(ColorUtil.toHexEncoding(aPGB.getBackColor()));
                    tagAttr.setValue(aPGB.getTag());

                    brk.setAttributeNode(caption);
                    brk.setAttributeNode(startValue);
                    brk.setAttributeNode(endValue);
                    brk.setAttributeNode(color);
                    brk.setAttributeNode(drawShape);
                    brk.setAttributeNode(outlineColor);
                    brk.setAttributeNode(drawOutline);
                    brk.setAttributeNode(drawFill);
                    brk.setAttributeNode(outlineSize);
                    brk.setAttributeNode(style);
                    brk.setAttributeNode(styleSize);
                    brk.setAttributeNode(backColor);
                    brk.setAttributeNode(tagAttr);

                    breaks.appendChild(brk);
                }
                break;
            case Image:
                for (ColorBreak aCB : this.legendBreaks) {
                    brk = doc.createElement("Break");
                    caption = doc.createAttribute("Caption");
                    startValue = doc.createAttribute("StartValue");
                    endValue = doc.createAttribute("EndValue");
                    color = doc.createAttribute("Color");
                    isNoData = doc.createAttribute("IsNoData");
                    tagAttr = doc.createAttribute("Tag");

                    caption.setValue(aCB.getCaption());
                    startValue.setValue(String.valueOf(aCB.getStartValue()));
                    endValue.setValue(String.valueOf(aCB.getEndValue()));
                    color.setValue(ColorUtil.toHexEncoding(aCB.getColor()));
                    isNoData.setValue(String.valueOf(aCB.isNoData()));
                    tagAttr.setValue(aCB.getTag());

                    brk.setAttributeNode(caption);
                    brk.setAttributeNode(startValue);
                    brk.setAttributeNode(endValue);
                    brk.setAttributeNode(color);
                    brk.setAttributeNode(isNoData);
                    brk.setAttributeNode(tagAttr);

                    breaks.appendChild(brk);
                }
                break;
        }

        root.appendChild(breaks);
        parent.appendChild(root);
    }

    /**
     * Export to xml file
     *
     * @param aFile xml file path
     * @throws ParserConfigurationException
     */
    public void exportToXMLFile(String aFile) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("MeteoInfo");
        File af = new File(aFile);
        Attr fn = doc.createAttribute("File");
        Attr type = doc.createAttribute("Type");
        fn.setValue(af.getName());
        type.setValue("LegendScheme");
        root.setAttributeNode(fn);
        root.setAttributeNode(type);
        doc.appendChild(root);

        exportToXML(doc, root);

        //Save to file
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(doc);
            //transformer.setOutputProperty(OutputKeys.ENCODING, "GB2312");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            PrintWriter pw = new PrintWriter(new FileOutputStream(aFile));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
        } catch (TransformerException | IOException mye) {
        }
    }

    /**
     * Import legend scheme from XML node
     *
     * @param LSNode xml node
     */
    public void importFromXML(Node LSNode) {
        importFromXML(LSNode, true);
    }

    /**
     * Import legend scheme from xml node
     *
     * @param LSNode xml node
     * @param keepShape if keep the legend shape type
     */
    public void importFromXML(Node LSNode, boolean keepShape) {
        legendBreaks = new ArrayList<>();

        if (LSNode.getAttributes().getNamedItem("FieldName") != null) {
            fieldName = LSNode.getAttributes().getNamedItem("FieldName").getNodeValue();
        }
        legendType = LegendType.valueOf(LSNode.getAttributes().getNamedItem("LegendType").getNodeValue());
        ShapeTypes aShapeType = ShapeTypes.valueOf(LSNode.getAttributes().getNamedItem("ShapeType").getNodeValue());

        //BreakNum = Convert.ToInt32(LSNode.Attributes["BreakNum"].InnerText);
        hasNoData = Boolean.parseBoolean(LSNode.getAttributes().getNamedItem("HasNoData").getNodeValue());
        minValue = Double.parseDouble(LSNode.getAttributes().getNamedItem("MinValue").getNodeValue());
        maxValue = Double.parseDouble(LSNode.getAttributes().getNamedItem("MaxValue").getNodeValue());
        undef = Double.parseDouble(LSNode.getAttributes().getNamedItem("UNDEF").getNodeValue());

        if (!keepShape) {
            shapeType = aShapeType;
        }
        boolean sameShapeType = (shapeType.isSameLegendType(aShapeType));
        importBreaks(LSNode, sameShapeType);
    }

    private void importBreaks(Node parent, boolean sameShapeType) {
        Node breaksNode = ((Element)parent).getElementsByTagName("Breaks").item(0);

        NodeList breaks = ((Element)breaksNode).getElementsByTagName("Break");
        if (sameShapeType) {
            switch (shapeType) {
                case Point:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        PointBreak aPB = new PointBreak();
                        try {
                            aPB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                            aPB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                            aPB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                            aPB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                            aPB.setDrawShape(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                            aPB.setDrawFill(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawFill").getNodeValue()));
                            aPB.setDrawOutline(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawOutline").getNodeValue()));
                            aPB.setNoData(Boolean.parseBoolean(brk.getAttributes().getNamedItem("IsNoData").getNodeValue()));
                            aPB.setOutlineColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("OutlineColor").getNodeValue()));
                            aPB.setSize(Float.parseFloat(brk.getAttributes().getNamedItem("Size").getNodeValue()));
                            aPB.setStyle(PointStyle.valueOf(brk.getAttributes().getNamedItem("Style").getNodeValue()));
                            aPB.setMarkerType(MarkerType.valueOf(brk.getAttributes().getNamedItem("MarkerType").getNodeValue()));
                            aPB.setFontName(brk.getAttributes().getNamedItem("FontName").getNodeValue());
                            aPB.setCharIndex(Integer.parseInt(brk.getAttributes().getNamedItem("CharIndex").getNodeValue()));
                            aPB.setImagePath(brk.getAttributes().getNamedItem("ImagePath").getNodeValue());
                            aPB.setAngle(Float.parseFloat(brk.getAttributes().getNamedItem("Angle").getNodeValue()));
                            if (brk.getAttributes().getNamedItem("Tag") != null)
                                aPB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                            if (brk.getAttributes().getNamedItem("OutlineSize") != null)
                                aPB.setOutlineSize(Float.parseFloat(brk.getAttributes().getNamedItem("OutlineSize").getNodeValue()));
                        } catch (DOMException | NumberFormatException e) {
                        } finally {
                            legendBreaks.add(aPB);
                        }
                    }
                    break;
                case Polyline:
                case PolylineZ:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        PolylineBreak aPLB = new PolylineBreak();
                        try {
                            aPLB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                            aPLB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                            aPLB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                            aPLB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                            aPLB.setDrawPolyline(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                            aPLB.setWidth(Float.parseFloat(brk.getAttributes().getNamedItem("Size").getNodeValue()));
                            aPLB.setStyle(LineStyles.valueOf(brk.getAttributes().getNamedItem("Style").getNodeValue().toUpperCase()));
                            aPLB.setDrawSymbol(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawSymbol").getNodeValue()));
                            aPLB.setSymbolSize(Float.parseFloat(brk.getAttributes().getNamedItem("SymbolSize").getNodeValue()));
                            aPLB.setSymbolStyle(PointStyle.valueOf(brk.getAttributes().getNamedItem("SymbolStyle").getNodeValue()));
                            aPLB.setSymbolColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("SymbolColor").getNodeValue()));
                            aPLB.setSymbolInterval(Integer.parseInt(brk.getAttributes().getNamedItem("SymbolInterval").getNodeValue()));
                            if (brk.getAttributes().getNamedItem("FillSymbol") != null) {
                                aPLB.setFillSymbol(Boolean.parseBoolean(brk.getAttributes().getNamedItem("FillSymbol").getNodeValue()));
                                aPLB.setSymbolFillColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("SymbolFillColor").getNodeValue()));
                            }
                            if (brk.getAttributes().getNamedItem("Tag") != null)
                                aPLB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            legendBreaks.add(aPLB);
                        }
                    }
                    break;
                case Polygon:
                case PolygonM:
                case PolygonZ:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        PolygonBreak aPGB = new PolygonBreak();
                        try {
                            aPGB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                            aPGB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                            aPGB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                            aPGB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                            aPGB.setDrawShape(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                            aPGB.setDrawFill(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawFill").getNodeValue()));
                            aPGB.setDrawOutline(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawOutline").getNodeValue()));
                            aPGB.setOutlineSize(Float.parseFloat(brk.getAttributes().getNamedItem("OutlineSize").getNodeValue()));
                            aPGB.setOutlineColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("OutlineColor").getNodeValue()));
                            aPGB.setStyle(HatchStyle.valueOf(brk.getAttributes().getNamedItem("Style").getNodeValue()));
                            aPGB.setBackColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("BackColor").getNodeValue()));
                            if (brk.getAttributes().getNamedItem("Tag") != null)
                                aPGB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                            aPGB.setStyleSize(Integer.parseInt(brk.getAttributes().getNamedItem("StyleSize").getNodeValue()));
                        } catch (Exception e) {
                        } finally {
                            legendBreaks.add(aPGB);
                        }
                    }
                    break;
                case Image:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        ColorBreak aCB = new ColorBreak();
                        try {
                            aCB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                            aCB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                            aCB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                            aCB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                            aCB.setDrawShape(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                            aCB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                        } catch (Exception e) {
                        } finally {
                            legendBreaks.add(aCB);
                        }
                    }
                    break;
            }
        } else {
            switch (shapeType) {
                case Point:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        PointBreak aPB = new PointBreak();
                        try {
                            aPB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                            aPB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                            aPB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                            aPB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                            aPB.setDrawShape(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                            aPB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                        } catch (Exception e) {
                        } finally {
                            legendBreaks.add(aPB);
                        }
                    }
                    break;
                case Polyline:
                case PolylineZ:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        PolylineBreak aPLB = new PolylineBreak();
                        try {
                            if (!"NoData".equals(brk.getAttributes().getNamedItem("Caption").getNodeValue())) {
                                aPLB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                                aPLB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                                aPLB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                                aPLB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                                aPLB.setDrawPolyline(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                                aPLB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                            }
                        } catch (Exception e) {
                        } finally {
                            legendBreaks.add(aPLB);
                        }
                    }
                    break;
                case Polygon:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        PolygonBreak aPGB = new PolygonBreak();
                        try {
                            if (!"NoData".equals(brk.getAttributes().getNamedItem("Caption").getNodeValue())) {
                                aPGB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                                aPGB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                                aPGB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                                aPGB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                                aPGB.setDrawShape(Boolean.parseBoolean(brk.getAttributes().getNamedItem("DrawShape").getNodeValue()));
                                aPGB.setDrawFill(true);
                                aPGB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                            }
                        } catch (Exception e) {
                        } finally {
                            legendBreaks.add(aPGB);
                        }
                    }

                    break;
                case Image:
                    for (int i = 0; i < breaks.getLength(); i++) {
                        Node brk = breaks.item(i);
                        ColorBreak aCB = new ColorBreak();
                        try {
                            aCB.setCaption(brk.getAttributes().getNamedItem("Caption").getNodeValue());
                            aCB.setStartValue(brk.getAttributes().getNamedItem("StartValue").getNodeValue());
                            aCB.setEndValue(brk.getAttributes().getNamedItem("EndValue").getNodeValue());
                            aCB.setColor(ColorUtil.parseToColor(brk.getAttributes().getNamedItem("Color").getNodeValue()));
                            aCB.setTag(brk.getAttributes().getNamedItem("Tag").getNodeValue());
                        } catch (Exception e) {
                        } finally {
                            legendBreaks.add(aCB);
                        }
                    }
                    break;
            }
            //breakNum = LegendBreaks.Count;
        }
    }

    /**
     * Import legend scheme from XML file
     *
     * @param aFile File path
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void importFromXMLFile(String aFile) throws ParserConfigurationException, SAXException, IOException {
        importFromXMLFile(aFile, true);
    }

    /**
     * Import legend scheme from XML file
     *
     * @param aFile file path
     * @param keepShape If keep shape type
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void importFromXMLFile(String aFile, boolean keepShape) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(aFile));

        Element root = doc.getDocumentElement();
        Node LSNode;
        if ("MeteoInfo".equals(root.getNodeName())) {
            LSNode = root.getElementsByTagName("LegendScheme").item(0);
        } else {
            LSNode = root;
        }

        importFromXML(LSNode, keepShape);
    }

    /**
     * Import legend scheme from an image color palette file
     *
     * @param filePath File path
     */
    public void importFromPaletteFile_Unique(String filePath) {
        BufferedReader sr = null;
        try {
            File aFile = new File(filePath);
            sr = new BufferedReader(new FileReader(aFile));
            this.shapeType = ShapeTypes.Image;
            this.legendType = LegendType.UniqueValue;
            this.legendBreaks = new ArrayList<>();
            ColorBreak aCB;
            String[] dataArray;
            sr.readLine();
            String aLine = sr.readLine();
            while (aLine != null) {
                aLine = aLine.trim();
                if (aLine.isEmpty()){
                    aLine = sr.readLine();
                    continue;
                }
                dataArray = aLine.split("\\s+");
                Color aColor = new Color(Integer.parseInt(dataArray[3]), Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[1]));
                aCB = new ColorBreak();
                aCB.setColor(aColor);
                aCB.setStartValue(dataArray[0]);
                aCB.setEndValue(dataArray[0]);
                aCB.setCaption(String.valueOf(aCB.getStartValue()));
                this.legendBreaks.add(aCB);

                aLine = sr.readLine();
            }
            sr.close();
        } catch (IOException ex) {
            Logger.getLogger(LegendScheme.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sr != null)
                    sr.close();
            } catch (IOException ex) {
                Logger.getLogger(LegendScheme.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Import legend scheme from an image color palette file
     *
     * @param filePath File path
     */
    public void importFromPaletteFile_Graduated(String filePath) {
        BufferedReader sr = null;
        try {
            File aFile = new File(filePath);
            sr = new BufferedReader(new FileReader(aFile));
            this.shapeType = ShapeTypes.Image;
            this.legendType = LegendType.GraduatedColor;
            this.legendBreaks = new ArrayList<>();
            List<Color> colorList = new ArrayList<>();
            List<Integer> values = new ArrayList<>();
            ColorBreak aCB;
            String[] dataArray;
            sr.readLine();
            String aLine = sr.readLine();
            while (aLine != null) {
                dataArray = aLine.split("\\s+");
                Color aColor = new Color(Integer.parseInt(dataArray[3]), Integer.parseInt(dataArray[2]), Integer.parseInt(dataArray[1]));
                if (colorList.isEmpty()) {
                    colorList.add(aColor);
                } else {
                    if (!colorList.contains(aColor)) {
                        aCB = new ColorBreak();
                        aCB.setColor(aColor);
                        aCB.setStartValue(Collections.min(values));
                        aCB.setEndValue(Collections.max(values));
                        if (String.valueOf(aCB.getStartValue()).equals(String.valueOf(aCB.getEndValue()))) {
                            aCB.setCaption(String.valueOf(aCB.getStartValue()));
                        } else {
                            if (this.legendBreaks.isEmpty()) {
                                aCB.setCaption("< " + String.valueOf(aCB.getEndValue()));
                            } else {
                                aCB.setCaption(String.valueOf(aCB.getStartValue()) + " - " + String.valueOf(aCB.getEndValue()));
                            }
                        }
                        this.legendBreaks.add(aCB);

                        values.clear();
                        colorList.add(aColor);
                    }
                }
                values.add(Integer.parseInt(dataArray[0]));

                aLine = sr.readLine();
            }
            sr.close();
            aCB = new ColorBreak();
            aCB.setColor(colorList.get(colorList.size() - 1));
            aCB.setStartValue(Collections.min(values));
            aCB.setEndValue(Collections.max(values));
            aCB.setCaption("> " + String.valueOf(aCB.getStartValue()));
            this.legendBreaks.add(aCB);
        } catch (IOException ex) {
            Logger.getLogger(LegendScheme.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (sr != null)
                    sr.close();
            } catch (IOException ex) {
                Logger.getLogger(LegendScheme.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Clone
     *
     * @return Legend scheme
     */
    @Override
    public Object clone() {
        LegendScheme bLS = new LegendScheme(shapeType);
        bLS.setFieldName(fieldName);
        //bLS.breakNum = breakNum;
        bLS.setHasNoData(hasNoData);
        bLS.setLegendType(legendType);
        bLS.setMinValue(minValue);
        bLS.setMaxValue(maxValue);
        bLS.setUndefValue(undef);
        for (ColorBreak aCB : legendBreaks) {
            bLS.getLegendBreaks().add((ColorBreak) aCB.clone());
        }

        return bLS;
    }
    // </editor-fold>
}
