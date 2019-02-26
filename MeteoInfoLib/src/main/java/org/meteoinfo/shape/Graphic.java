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
package org.meteoinfo.shape;

import org.meteoinfo.legend.MarkerType;
import org.meteoinfo.legend.PointStyle;
import org.meteoinfo.global.colors.ColorUtil;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layout.ResizeAbility;
import org.meteoinfo.legend.BreakTypes;
import org.meteoinfo.legend.ChartBreak;
import org.meteoinfo.legend.ChartTypes;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.LineStyles;
import org.meteoinfo.legend.PointBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.legend.PolylineBreak;
import org.meteoinfo.legend.VectorBreak;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.Extent;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Graphic class
 *
 * @author Yaqiang Wang
 */
public class Graphic {
    // <editor-fold desc="Variables">

    private Shape _shape = null;
    private ColorBreak _legend = null;
    private ResizeAbility _resizeAbility = ResizeAbility.ResizeAll;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Graphic() {
    }

    /**
     * Constructor
     *
     * @param shape a shape
     * @param legend a legend
     */
    public Graphic(Shape shape, ColorBreak legend) {
        _shape = shape;
        _legend = legend;
        updateResizeAbility();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get shape
     *
     * @return Shape
     */
    public Shape getShape() {
        return _shape;
    }

    /**
     * Set shape
     *
     * @param aShape a shape
     */
    public void setShape(Shape aShape) {
        _shape = aShape;
        updateResizeAbility();
    }

    /**
     * Get legend
     *
     * @return Legend
     */
    public ColorBreak getLegend() {
        return _legend;
    }

    public void setLegend(ColorBreak legend) {
        _legend = legend;
        updateResizeAbility();
        updateResizeAbility();
    }

    /**
     * Get resize ability
     *
     * @return Resize ability
     */
    public ResizeAbility getResizeAbility() {
        return _resizeAbility;
    }
    
    /**
     * Get extent
     *
     * @return The extent
     */
    public Extent getExtent() {
        return this._shape.getExtent();
    }
    
    /**
     * Set extent
     * @param value The extent 
     */
    public void setExtent(Extent value){
        this._shape.setExtent(value);
    }
    
    /**
     * Get is single legend or not
     * @return Boolean
     */
    public boolean isSingleLegend(){
        return true;
    }
    
    /**
     * Get if is GraphicCollection
     * @return Boolean
     */
    public boolean isCollection(){
        return false;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get graphics number
     * @return 1
     */
    public int getNumGraphics(){
        return 1;
    }
    
    /**
     * Get Graphic by index
     * @param idx Index
     * @return Graphic
     */
    public Graphic getGraphicN(int idx){
        return this;
    }
    
    /**
     * Get graphic list
     * @return Graphic list
     */
    public List<Graphic> getGraphics(){
        List<Graphic> gs = new ArrayList<>();
        gs.add(this);
        return gs;
    }
    
    private void updateResizeAbility() {
        if (_shape != null && _legend != null) {
            switch (_shape.getShapeType()) {
                case Point:
                    switch (_legend.getBreakType()) {
                        case PointBreak:
                            _resizeAbility = ResizeAbility.SameWidthHeight;
                            break;
                        case LabelBreak:
                        case ChartBreak:
                            _resizeAbility = ResizeAbility.None;
                            break;
                    }
                    break;
                case Circle:
                    _resizeAbility = ResizeAbility.SameWidthHeight;
                    break;
                default:
                    _resizeAbility = ResizeAbility.ResizeAll;
                    break;
            }
        }
    }

    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param newX New X
     * @param newY New Y
     */
    public void verticeMoveUpdate(int vIdx, double newX, double newY) {
        List<PointD> points = (List<PointD>)_shape.getPoints();        
        switch (_shape.getShapeType()){
            case Polygon:
            case CurvePolygon:
            case Rectangle:
                int last = points.size() - 1;
                if (vIdx == 0) {                    
                    if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                        points.get(last).X = newX;
                        points.get(last).Y = newY;
                    }
                } else if (vIdx == last){
                    if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                        points.get(0).X = newX;
                        points.get(0).Y = newY;
                    }
                }
                break;
        }
        
        PointD aP = points.get(vIdx);
        aP.X = newX;
        aP.Y = newY;
        //points.set(vIdx, aP);
        _shape.setPoints(points);
    }
    
    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param point The add vertice
     */
    public void verticeAddUpdate(int vIdx, PointD point) {
        List<PointD> points = (List<PointD>)_shape.getPoints();      
        points.add(vIdx, point);        
        _shape.setPoints(points);
    }
    
    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     */
    public void verticeRemoveUpdate(int vIdx) {
        List<PointD> points = (List<PointD>)_shape.getPoints();      
        points.remove(vIdx);        
        _shape.setPoints(points);
    }
    
    /**
     * Export to XML document
     * @param doc XML document
     * @param parent Parent XML element
     */
    public void exportToXML(Document doc, Element parent) {
        Element graphic = doc.createElement("Graphic");
        addShape(doc, graphic, _shape);
        addLegend(doc, graphic, _legend, _shape.getShapeType());

        parent.appendChild(graphic);
    }

    /**
     * Add shape to XML document
     * @param doc XML document
     * @param parent Parent XML element
     * @param aShape The shape
     */
    protected void addShape(Document doc, Element parent, Shape aShape) {
        Element shape = doc.createElement("Shape");
        boolean hasAngle = aShape.getShapeType() == ShapeTypes.Ellipse;        

        //Add general attribute
        Attr shapeType = doc.createAttribute("ShapeType");
        Attr visible = doc.createAttribute("Visible");
        Attr selected = doc.createAttribute("Selected");        

        //shapeType.InnerText = Enum.GetName(typeof(ShapeTypes), aShape.ShapeType);
        shapeType.setValue(aShape.getShapeType().toString());
        visible.setValue(String.valueOf(aShape.isVisible()));
        selected.setValue(String.valueOf(aShape.isSelected()));

        shape.setAttributeNode(shapeType);
        shape.setAttributeNode(visible);
        shape.setAttributeNode(selected);
        
        if (hasAngle){
            Attr angle = doc.createAttribute("Angle");
            angle.setValue(String.valueOf(((EllipseShape)aShape).getAngle()));
            shape.setAttributeNode(angle);
        }

        //Add points
        Element points = doc.createElement("Points");
        List<PointD> pointList = (List<PointD>)aShape.getPoints();
        for (PointD aPoint : pointList) {
            Element point = doc.createElement("Point");
            Attr x = doc.createAttribute("X");
            Attr y = doc.createAttribute("Y");
            x.setValue(String.valueOf(aPoint.X));
            y.setValue(String.valueOf(aPoint.Y));
            point.setAttributeNode(x);
            point.setAttributeNode(y);

            points.appendChild(point);
        }

        shape.appendChild(points);

        parent.appendChild(shape);
    }

    /**
     * Add legend to XML document
     * @param doc XML document
     * @param parent Parent XML element
     * @param aLegend The legend
     * @param shapeType The shape type
     */
    protected void addLegend(Document doc, Element parent, ColorBreak aLegend, ShapeTypes shapeType) {
        Element legend = doc.createElement("Legend");
        Attr color = doc.createAttribute("Color");
        color.setValue(ColorUtil.toHexEncoding(aLegend.getColor()));
        legend.setAttributeNode(color);

        Attr legendType = doc.createAttribute("LegendType");
        Attr size;
        Attr style;
        Attr outlineColor;
        Attr drawOutline;
        Attr drawFill;
        legendType.setValue(aLegend.getBreakType().toString());
        switch (aLegend.getBreakType()) {
            case PointBreak:
                PointBreak aPB = (PointBreak) aLegend;
                outlineColor = doc.createAttribute("OutlineColor");
                size = doc.createAttribute("Size");
                style = doc.createAttribute("Style");
                drawOutline = doc.createAttribute("DrawOutline");
                drawFill = doc.createAttribute("DrawFill");
                Attr markerType = doc.createAttribute("MarkerType");
                Attr fontName = doc.createAttribute("FontName");
                Attr charIndex = doc.createAttribute("CharIndex");
                Attr imagePath = doc.createAttribute("ImagePath");
                Attr angle = doc.createAttribute("Angle");

                //legendType.InnerText = "PointBreak";
                outlineColor.setValue(ColorUtil.toHexEncoding(aPB.getOutlineColor()));
                size.setValue(String.valueOf(aPB.getSize()));
                style.setValue(aPB.getStyle().toString());
                drawOutline.setValue(String.valueOf(aPB.isDrawOutline()));
                drawFill.setValue(String.valueOf(aPB.isDrawFill()));
                markerType.setValue(aPB.getMarkerType().toString());
                fontName.setValue(aPB.getFontName());
                charIndex.setValue(String.valueOf(aPB.getCharIndex()));
                imagePath.setValue(aPB.getImagePath());
                angle.setValue(String.valueOf(aPB.getAngle()));

                legend.setAttributeNode(legendType);
                legend.setAttributeNode(outlineColor);
                legend.setAttributeNode(size);
                legend.setAttributeNode(style);
                legend.setAttributeNode(drawOutline);
                legend.setAttributeNode(drawFill);
                legend.setAttributeNode(markerType);
                legend.setAttributeNode(fontName);
                legend.setAttributeNode(charIndex);
                legend.setAttributeNode(imagePath);
                legend.setAttributeNode(angle);
                break;
            case LabelBreak:
                LabelBreak aLB = (LabelBreak) aLegend;
                Attr text = doc.createAttribute("Text");
                angle = doc.createAttribute("Angle");
                fontName = doc.createAttribute("FontName");
                Attr fontSize = doc.createAttribute("FontSize");
                Attr fontBold = doc.createAttribute("FontBold");
                Attr yShift = doc.createAttribute("YShift");

                //legendType.InnerText = "LabelBreak";
                text.setValue(aLB.getText());
                angle.setValue(String.valueOf(aLB.getAngle()));
                fontName.setValue(aLB.getFont().getName());
                fontSize.setValue(String.valueOf(aLB.getFont().getSize()));
                fontBold.setValue(String.valueOf(aLB.getFont().isBold()));
                yShift.setValue(String.valueOf(aLB.getYShift()));

                legend.setAttributeNode(legendType);
                legend.setAttributeNode(text);
                legend.setAttributeNode(angle);
                legend.setAttributeNode(fontName);
                legend.setAttributeNode(fontSize);
                legend.setAttributeNode(fontBold);
                legend.setAttributeNode(yShift);
                break;
            case ChartBreak:
                ChartBreak aChB = (ChartBreak) aLegend;
                Attr shapeIndex = doc.createAttribute("ShapeIndex");
                Attr chartType = doc.createAttribute("ChartType");
                Attr chartData = doc.createAttribute("ChartData");
                Attr xShift = doc.createAttribute("XShift");
                yShift = doc.createAttribute("YShift");
                fontName = doc.createAttribute("FontName");
                fontSize = doc.createAttribute("FontSize"); 
                Attr labelColor = doc.createAttribute("LabelColor");

                shapeIndex.setValue(String.valueOf(aChB.getShapeIndex()));
                //legendType.InnerText = "ChartBreak";
                chartType.setValue(aChB.getChartType().toString());
                String cdata = "";
                for (int i = 0; i < aChB.getItemNum(); i++) {
                    if (i == 0) {
                        cdata = String.valueOf(aChB.getChartData().get(i));
                    } else {
                        cdata += "," + String.valueOf(aChB.getChartData().get(i));
                    }
                }
                chartData.setValue(cdata);
                xShift.setValue(String.valueOf(aChB.getXShift()));
                yShift.setValue(String.valueOf(aChB.getYShift()));
                fontName.setValue(aChB.getLabelFont().getFontName());
                fontSize.setValue(String.valueOf(aChB.getLabelFont().getSize()));
                labelColor.setValue(ColorUtil.toHexEncoding(aChB.getLabelColor()));

                legend.setAttributeNode(legendType);
                legend.setAttributeNode(shapeIndex);
                legend.setAttributeNode(chartType);
                legend.setAttributeNode(chartData);
                legend.setAttributeNode(xShift);
                legend.setAttributeNode(yShift);
                legend.setAttributeNode(fontName);
                legend.setAttributeNode(fontSize);
                legend.setAttributeNode(labelColor);
                break;
            case VectorBreak:
                //legendType.InnerText = "VectorBreak";
                legend.setAttributeNode(legendType);
                break;
            case PolylineBreak:
                PolylineBreak aPLB = (PolylineBreak) aLegend;
                size = doc.createAttribute("Size");
                style = doc.createAttribute("Style");
                Attr drawSymbol = doc.createAttribute("DrawSymbol");
                Attr symbolSize = doc.createAttribute("SymbolSize");
                Attr symbolStyle = doc.createAttribute("SymbolStyle");
                Attr symbolColor = doc.createAttribute("SymbolColor");
                Attr symbolInterval = doc.createAttribute("SymbolInterval");

                //legendType.InnerText = "PolylineBreak";
                size.setValue(String.valueOf(aPLB.getWidth()));
                style.setValue(aPLB.getStyle().toString());
                drawSymbol.setValue(String.valueOf(aPLB.getDrawSymbol()));
                symbolSize.setValue(String.valueOf(aPLB.getSymbolSize()));
                symbolStyle.setValue(String.valueOf(aPLB.getSymbolStyle()));
                symbolColor.setValue(ColorUtil.toHexEncoding(aPLB.getSymbolColor()));
                symbolInterval.setValue(String.valueOf(aPLB.getSymbolInterval()));

                legend.setAttributeNode(legendType);
                legend.setAttributeNode(size);
                legend.setAttributeNode(style);
                legend.setAttributeNode(drawSymbol);
                legend.setAttributeNode(symbolSize);
                legend.setAttributeNode(symbolStyle);
                legend.setAttributeNode(symbolColor);
                legend.setAttributeNode(symbolInterval);
                break;
            case PolygonBreak:
                PolygonBreak aPGB = (PolygonBreak) aLegend;
                outlineColor = doc.createAttribute("OutlineColor");
                drawOutline = doc.createAttribute("DrawOutline");
                drawFill = doc.createAttribute("DrawFill");
                Attr outlineSize = doc.createAttribute("OutlineSize");
                //Attr usingHatchStyle = doc.createAttribute("UsingHatchStyle");
                //style = doc.createAttribute("Style");
                Attr backColor = doc.createAttribute("BackColor");
                //Attr transparencyPer = doc.createAttribute("TransparencyPercent");
                Attr isMaskout = doc.createAttribute("IsMaskout");

                //legendType.InnerText = "PolygonBreak";
                outlineColor.setValue(ColorUtil.toHexEncoding(aPGB.getOutlineColor()));
                drawOutline.setValue(String.valueOf(aPGB.isDrawOutline()));
                drawFill.setValue(String.valueOf(aPGB.isDrawFill()));
                outlineSize.setValue(String.valueOf(aPGB.getOutlineSize()));
                //usingHatchStyle.setValue(String.valueOf(aPGB.getUsingHatchStyle()));
                //style.setValue(String.valueOf(aPGB.getStyle()));
                backColor.setValue(ColorUtil.toHexEncoding(aPGB.getBackColor()));
                //transparencyPer.InnerText = aPGB.TransparencyPercent.ToString();
                isMaskout.setValue(String.valueOf(aPGB.isMaskout()));

                legend.setAttributeNode(legendType);
                legend.setAttributeNode(outlineColor);
                legend.setAttributeNode(drawOutline);
                legend.setAttributeNode(drawFill);
                legend.setAttributeNode(outlineSize);
                //legend.setAttributeNode(usingHatchStyle);
                //legend.setAttributeNode(style);
                legend.setAttributeNode(backColor);
                //legend.setAttributeNode(transparencyPer);
                legend.setAttributeNode(isMaskout);
                break;
        }

        parent.appendChild(legend);
    }

    /**
     * Import from xml node
     *
     * @param graphicNode Graphic xml node
     */
    public void importFromXML(Element graphicNode) {
        Node shape = graphicNode.getElementsByTagName("Shape").item(0);
        _shape = loadShape(shape);

        Node legend = graphicNode.getElementsByTagName("Legend").item(0);
        _legend = loadLegend(legend, _shape.getShapeType());

        updateResizeAbility();
    }

    protected Shape loadShape(Node shapeNode) {
        Shape aShape = null;
        try {
            ShapeTypes shapeType = ShapeTypes.valueOf(shapeNode.getAttributes().getNamedItem("ShapeType").getNodeValue());
            switch (shapeType) {
                case Point:
                    aShape = new PointShape();
                    break;
                case WindArraw:
                    aShape = new WindArrow();
                    break;
                case Polyline:
                    aShape = new PolylineShape();
                    break;
                case CurveLine:
                    aShape = new CurveLineShape();
                    break;
                case Circle:
                    aShape = new CircleShape();
                    break;
                case Polygon:
                case Rectangle:
                    aShape = new PolygonShape();
                    break;
                case CurvePolygon:
                    aShape = new CurvePolygonShape();
                    break;
                case Ellipse:
                    aShape = new EllipseShape();
                    break;
            }

            aShape.setVisible(Boolean.parseBoolean(shapeNode.getAttributes().getNamedItem("Visible").getNodeValue()));
            aShape.setSelected(Boolean.parseBoolean(shapeNode.getAttributes().getNamedItem("Selected").getNodeValue()));
            if (aShape.getShapeType() == ShapeTypes.Ellipse){
                Node angleNode = shapeNode.getAttributes().getNamedItem("Angle");
                if (angleNode != null)
                    ((EllipseShape)aShape).setAngle(Float.parseFloat(angleNode.getNodeValue()));
            }

            List<PointD> pointList = new ArrayList<>();
            Node pointsNode = ((Element)shapeNode).getElementsByTagName("Points").item(0);
            NodeList nl = ((Element)pointsNode).getElementsByTagName("Point");
            for (int i = 0; i < nl.getLength(); i++) {
                Node pNode = nl.item(i);
                PointD aPoint = new PointD(Double.parseDouble(pNode.getAttributes().getNamedItem("X").getNodeValue()),
                        Double.parseDouble(pNode.getAttributes().getNamedItem("Y").getNodeValue()));
                pointList.add(aPoint);
            }
            aShape.setPoints(pointList);
        } catch (Exception e) {
        }

        return aShape;
    }

    protected ColorBreak loadLegend(Node legendNode, ShapeTypes shapeType) {
        ColorBreak legend = new ColorBreak();
        try {
            Color color = ColorUtil.parseToColor(legendNode.getAttributes().getNamedItem("Color").getNodeValue());
            String legendType = legendNode.getAttributes().getNamedItem("LegendType").getNodeValue();
            BreakTypes breakType = BreakTypes.valueOf(legendType);
            switch (breakType) {
                case PointBreak:
                    PointBreak aPB = new PointBreak();
                    try {
                        aPB.setColor(color);
                        aPB.setDrawFill(Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("DrawFill").getNodeValue()));
                        aPB.setDrawOutline(Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("DrawOutline").getNodeValue()));
                        aPB.setOutlineColor(ColorUtil.parseToColor(legendNode.getAttributes().getNamedItem("OutlineColor").getNodeValue()));
                        aPB.setSize(Float.parseFloat(legendNode.getAttributes().getNamedItem("Size").getNodeValue()));
                        aPB.setStyle(PointStyle.valueOf(legendNode.getAttributes().getNamedItem("Style").getNodeValue()));
                        aPB.setMarkerType(MarkerType.valueOf(legendNode.getAttributes().getNamedItem("MarkerType").getNodeValue()));
                        aPB.setFontName(legendNode.getAttributes().getNamedItem("FontName").getNodeValue());
                        aPB.setCharIndex(Integer.parseInt(legendNode.getAttributes().getNamedItem("CharIndex").getNodeValue()));
                        aPB.setImagePath(legendNode.getAttributes().getNamedItem("ImagePath").getNodeValue());
                        aPB.setAngle(Float.parseFloat(legendNode.getAttributes().getNamedItem("Angle").getNodeValue()));
                    } catch (Exception e) {
                    } finally {
                        legend = aPB;
                    }
                    break;
                case LabelBreak:
                    LabelBreak aLB = new LabelBreak();
                    try {
                        aLB.setColor(color);
                        aLB.setAngle(Float.parseFloat(legendNode.getAttributes().getNamedItem("Angle").getNodeValue()));
                        aLB.setText(legendNode.getAttributes().getNamedItem("Text").getNodeValue());
                        String fontName = legendNode.getAttributes().getNamedItem("FontName").getNodeValue();
                        float fontSize = Float.parseFloat(legendNode.getAttributes().getNamedItem("FontSize").getNodeValue());
                        boolean fontBold = Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("FontBold").getNodeValue());
                        if (fontBold) {
                            aLB.setFont(new Font(fontName, Font.BOLD, (int) fontSize));
                        } else {
                            aLB.setFont(new Font(fontName, Font.PLAIN, (int) fontSize));
                        }

                        aLB.setYShift(Float.parseFloat(legendNode.getAttributes().getNamedItem("YShift").getNodeValue()));
                    } catch (Exception e) {
                    } finally {
                        legend = aLB;
                    }
                    break;
                case ChartBreak:
                    ChartBreak aChB = new ChartBreak(ChartTypes.BarChart);
                    try {
                        ChartTypes chartType = ChartTypes.valueOf(legendNode.getAttributes().getNamedItem("ChartType").getNodeValue());
                        aChB = new ChartBreak(chartType);
                        aChB.setShapeIndex(Integer.parseInt(legendNode.getAttributes().getNamedItem("ShapeIndex").getNodeValue()));
                        List<Float> cData = new ArrayList<Float>();
                        String[] cDataStr = legendNode.getAttributes().getNamedItem("ChartData").getNodeValue().split(",");
                        for (int i = 0; i < cDataStr.length; i++) {
                            cData.add(Float.parseFloat(cDataStr[i]));
                        }

                        aChB.setChartData(cData);
                        aChB.setXShift(Integer.parseInt(legendNode.getAttributes().getNamedItem("XShift").getNodeValue()));
                        aChB.setYShift(Integer.parseInt(legendNode.getAttributes().getNamedItem("YShift").getNodeValue()));
                        String fontName = legendNode.getAttributes().getNamedItem("FontName").getNodeValue();
                        float fontSize = Float.parseFloat(legendNode.getAttributes().getNamedItem("FontSize").getNodeValue());
                        aChB.setLabelFont(new Font(fontName, Font.PLAIN, (int)fontSize));
                        aChB.setLabelColor(ColorUtil.parseToColor(legendNode.getAttributes().getNamedItem("LabelColor").getNodeValue()));
                    } catch (Exception e) {
                    } finally {
                        legend = aChB;
                    }
                    break;
                case VectorBreak:
                    VectorBreak aVB = new VectorBreak();
                    try {
                        aVB.setColor(color);
                    } catch (Exception e) {
                    } finally {
                        legend = aVB;
                    }
                    break;
                case PolylineBreak:
                    PolylineBreak aPLB = new PolylineBreak();
                    try {
                        aPLB.setColor(color);
                        aPLB.setWidth(Float.parseFloat(legendNode.getAttributes().getNamedItem("Size").getNodeValue()));
                        aPLB.setStyle(LineStyles.valueOf(legendNode.getAttributes().getNamedItem("Style").getNodeValue()));
                        aPLB.setDrawSymbol(Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("DrawSymbol").getNodeValue()));
                        aPLB.setSymbolSize(Float.parseFloat(legendNode.getAttributes().getNamedItem("SymbolSize").getNodeValue()));
                        aPLB.setSymbolStyle(PointStyle.valueOf(legendNode.getAttributes().getNamedItem("SymbolStyle").getNodeValue()));
                        aPLB.setSymbolColor(ColorUtil.parseToColor(legendNode.getAttributes().getNamedItem("SymbolColor").getNodeValue()));
                        aPLB.setSymbolInterval(Integer.parseInt(legendNode.getAttributes().getNamedItem("SymbolInterval").getNodeValue()));
                    } catch (Exception e) {
                    } finally {
                        legend = aPLB;
                    }
                    break;
                case PolygonBreak:
                    PolygonBreak aPGB = new PolygonBreak();
                    try {
                        aPGB.setColor(color);
                        aPGB.setDrawFill(Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("DrawFill").getNodeValue()));
                        aPGB.setDrawOutline(Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("DrawOutline").getNodeValue()));
                        aPGB.setOutlineSize(Float.parseFloat(legendNode.getAttributes().getNamedItem("OutlineSize").getNodeValue()));
                        aPGB.setOutlineColor(ColorUtil.parseToColor(legendNode.getAttributes().getNamedItem("OutlineColor").getNodeValue()));
                        //aPGB.UsingHatchStyle = bool.Parse(legendNode.Attributes["UsingHatchStyle"].InnerText);
                        //aPGB.Style = (HatchStyle)Enum.Parse(typeof(HatchStyle), legendNode.Attributes["Style"].InnerText, true);
                        aPGB.setBackColor(ColorUtil.parseToColor(legendNode.getAttributes().getNamedItem("BackColor").getNodeValue()));
                        //aPGB.TransparencyPercent = int.Parse(legendNode.Attributes["TransparencyPercent"].InnerText);
                        aPGB.setMaskout(Boolean.parseBoolean(legendNode.getAttributes().getNamedItem("IsMaskout").getNodeValue()));
                    } catch (Exception e) {
                    } {
                    legend = aPGB;
                }
                break;
            }
        } catch (Exception e) {
        }
        return legend;
    }
    // </editor-fold>
}
