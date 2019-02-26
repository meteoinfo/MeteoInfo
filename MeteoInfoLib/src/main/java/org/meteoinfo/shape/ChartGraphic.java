/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import org.meteoinfo.global.PointD;
import org.meteoinfo.legend.ChartBreak;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Yaqiang Wang
 */
public class ChartGraphic extends Graphic {

    // <editor-fold desc="Variables">
    private PointD startPosition;

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
        startPosition = (PointD) shape.getPoint().clone();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get start position
     *
     * @return Start position
     */
    public PointD getStartPosition() {
        return startPosition;
    }

    /**
     * Set start postion
     *
     * @param value Start position
     */
    public void setStartPosition(PointD value) {
        startPosition = value;
    }

    /**
     * Set point shape
     *
     * @param aShape Point shape
     */
    public void setShape(PointShape aShape) {
        super.setShape(aShape);
        startPosition = (PointD) aShape.getPoint().clone();
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
        this.addLegend(doc, graphic, this.getLegend(), this.getShape().getShapeType());
        this.addStartPosition(doc, graphic, startPosition);

        parent.appendChild(graphic);
    }

    private void addStartPosition(Document doc, Element parent, PointD pos) {
        Element startPos = doc.createElement("StartPosition");

        Attr xAttr = doc.createAttribute("X");
        Attr yAttr = doc.createAttribute("Y");

        xAttr.setValue(String.valueOf(pos.X));
        yAttr.setValue(String.valueOf(pos.Y));

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
        this.setLegend(loadLegend(legend, this.getShape().getShapeType()));

        Node startPos = graphicNode.getElementsByTagName("StartPosition").item(0);
        if (startPos != null) {
            PointD sP = this.loadStartPosition(startPos);
            if (sP != null) {
                this.startPosition = sP;
            }
        }
    }

    private PointD loadStartPosition(Node startPosNode) {
        PointD sP = null;
        try {
            double x = Double.parseDouble(startPosNode.getAttributes().getNamedItem("X").getNodeValue());
            double y = Double.parseDouble(startPosNode.getAttributes().getNamedItem("Y").getNodeValue());
            sP = new PointD(x, y);
        } catch (DOMException e) {
        } catch (NumberFormatException e) {
        }

        return sP;
    }
    // </editor-fold>
}
