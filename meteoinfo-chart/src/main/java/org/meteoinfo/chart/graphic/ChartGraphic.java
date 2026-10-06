/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.graphic;

import org.meteoinfo.geometry.legend.ChartBreak;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.shape.PointShape;
import org.w3c.dom.*;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartGraphic extends Graphic {

    // <editor-fold desc="Variables">
    private Coordinate startPosition;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartGraphic() {

    }

    /**
     * Constructor
     *
     * @param shape Point shape
     * @param legend Chart break
     */
    public ChartGraphic(PointShape shape, ChartBreak legend) {
        super(shape, legend);
        startPosition = (Coordinate) shape.getPoint().clone();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get start position
     *
     * @return Start position
     */
    public Coordinate getStartPosition() {
        return startPosition;
    }

    /**
     * Set start postion
     *
     * @param value Start position
     */
    public void setStartPosition(Coordinate value) {
        startPosition = value;
    }

    /**
     * Set point shape
     *
     * @param aShape Point shape
     */
    public void setShape(PointShape aShape) {
        super.setShape(aShape);
        startPosition = (Coordinate) aShape.getPoint().clone();
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Export to XML document
     *
     * @param doc XML document
     * @param parent Parent XML element
     */
    @Override
    public void exportToXML(Document doc, Element parent) {
        Element graphic = doc.createElement("Graphic");
        this.addShape(doc, graphic, this.getShape());
        this.addLegend(doc, graphic, this.getLegendBreak(), this.getShape().getShapeType());
        this.addStartPosition(doc, graphic, startPosition);

        parent.appendChild(graphic);
    }

    private void addStartPosition(Document doc, Element parent, Coordinate pos) {
        Element startPos = doc.createElement("StartPosition");

        Attr xAttr = doc.createAttribute("X");
        Attr yAttr = doc.createAttribute("Y");

        xAttr.setValue(String.valueOf(pos.x));
        yAttr.setValue(String.valueOf(pos.y));

        startPos.setAttributeNode(xAttr);
        startPos.setAttributeNode(yAttr);

        parent.appendChild(startPos);
    }

    /**
     * Import from xml node
     *
     * @param graphicNode Graphic xml node
     */
    @Override
    public void importFromXML(Element graphicNode) {
        Node shape = graphicNode.getElementsByTagName("Shape").item(0);
        this.setShape((PointShape)loadShape(shape));

        Node legend = graphicNode.getElementsByTagName("Legend").item(0);
        this.setLegendBreak(loadLegend(legend, this.getShape().getShapeType()));

        Node startPos = graphicNode.getElementsByTagName("StartPosition").item(0);
        if (startPos != null) {
            Coordinate sP = this.loadStartPosition(startPos);
            if (sP != null) {
                this.startPosition = sP;
            }
        }
    }

    private Coordinate loadStartPosition(Node startPosNode) {
        Coordinate sP = null;
        try {
            double x = Double.parseDouble(startPosNode.getAttributes().getNamedItem("X").getNodeValue());
            double y = Double.parseDouble(startPosNode.getAttributes().getNamedItem("Y").getNodeValue());
            sP = new Coordinate(x, y);
        } catch (DOMException e) {
        } catch (NumberFormatException e) {
        }

        return sP;
    }
    // </editor-fold>
}
