package org.meteoinfo.chart.graphic;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LabelBreak;
import org.meteoinfo.geometry.legend.LegendType;
import org.meteoinfo.geometry.shape.PointShape;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.table.AttributeTable;
import org.meteoinfo.table.Field;

import java.util.List;

public class GeoGraphicCollection extends GraphicCollection {

    protected AttributeTable attributeTable;
    protected ProjectionInfo projInfo;

    /**
     * Constructor
     */
    public GeoGraphicCollection() {
        super();

        this.attributeTable = new AttributeTable();
        this.projInfo = ProjectionInfo.LONG_LAT;
    }

    /**
     * Get attribute table
     * @return Attribute table
     */
    public AttributeTable getAttributeTable() {
        return this.attributeTable;
    }

    /**
     * Set attribute table
     * @param value Attribute table
     */
    public void setAttributeTable(AttributeTable value) {
        this.attributeTable = value;
    }

    /**
     * Get projection info
     * @return Projection info
     */
    public ProjectionInfo getProjInfo() {
        return this.projInfo;
    }

    /**
     * Set projection info
     * @param value Projection info
     */
    public void setProjInfo(ProjectionInfo value) {
        this.projInfo = value;
    }

    /**
     * Get cell value
     *
     * @param fieldName Field name
     * @param shapeIndex Shape index
     * @return Cell value
     */
    public Object getCellValue(String fieldName, int shapeIndex) {
        return attributeTable.getTable().getValue(shapeIndex, fieldName);
    }

    /**
     * Get minimum data value of a field
     *
     * @param fieldName Field name
     * @return Minimum data
     */
    public double getMinValue(String fieldName) {
        if (((Field) attributeTable.getTable().getColumns().get(fieldName)).isNumeric()) {
            double min = 0;
            int dNum = 0;
            for (int i = 0; i < this.getNumGraphics(); i++) {
                double aValue = Double.parseDouble(getCellValue(fieldName, i).toString());
                if (Math.abs(aValue / this.getLegendScheme().getUndefValue() - 1) < 0.01) {
                    continue;
                }

                if (dNum == 0) {
                    min = aValue;
                } else if (min > aValue) {
                    min = aValue;
                }
                dNum += 1;
            }
            return min;
        } else {
            return 0;
        }
    }

    /**
     * Add labels
     */
    protected void addLabelsByColor() {
        int shapeIdx = -1;
        PointD aPoint;

        String dFormat = "%1$.1f";
        boolean isData = false;
        if (((Field) attributeTable.getTable().getColumns().get(labelSet.getFieldName())).isNumeric()) {
            if (labelSet.isAutoDecimal()) {
                double min = getMinValue(labelSet.getFieldName());
                labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
            }

            dFormat = "%1$." + String.valueOf(labelSet.getDecimalDigits()) + "f";
            isData = true;
        }

        for (Graphic graphic : this.graphics) {
            Shape shape = graphic.getShape();
            shapeIdx += 1;
            ColorBreak aCB = graphic.getLegend();
            if (!aCB.isDrawShape()) {
                continue;
            }

            PointShape aPS = new PointShape();
            switch (this.getShapeType()) {
                case POINT:
                case POINT_M:
                case POINT_Z:
                    aPS.setPoint((PointD) ((PointShape) shape).getPoint().clone());
                    break;
                case POLYLINE:
                case POLYLINE_M:
                case POLYLINE_Z:
                    int pIdx = ((PolylineShape) shape).getPoints().size() / 2;
                    aPS.setPoint((PointD) ((PolylineShape) shape).getPoints().get(pIdx - 1).clone());
                    break;
                case POLYGON:
                case POLYGON_M:
                case POLYGON_Z:
                    Extent aExtent = shape.getExtent();
                    aPoint = new PointD();
                    aPoint.X = ((aExtent.minX + aExtent.maxX) / 2);
                    aPoint.Y = ((aExtent.minY + aExtent.maxY) / 2);
                    aPS.setPoint(aPoint);
                    break;
            }

            LabelBreak aLP = new LabelBreak();
            if (isData) {
                if (this.labelSet.isAutoDecimal()) {
                    aLP.setText(DataConvert.removeTailingZeros(getCellValue(labelSet.getFieldName(), shapeIdx).toString()));
                } else {
                    aLP.setText(String.format(dFormat, Double.parseDouble(getCellValue(labelSet.getFieldName(), shapeIdx).toString())));
                }
            } else {
                aLP.setText(getCellValue(labelSet.getFieldName(), shapeIdx).toString());
            }

            if (labelSet.isColorByLegend()) {
                aLP.setColor(aCB.getColor());
            } else {
                aLP.setColor(labelSet.getLabelColor());
            }
            aLP.setFont(labelSet.getLabelFont());
            aLP.setAlignType(labelSet.getLabelAlignType());
            aLP.setYShift(labelSet.getYOffset());
            aLP.setXShift(labelSet.getXOffset());
            Graphic aGraphic = new Graphic(aPS, aLP);
            addLabel(aGraphic);
        }
    }

}
