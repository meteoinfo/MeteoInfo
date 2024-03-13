package org.meteoinfo.chart.graphic;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.common.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.PointShape;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.table.AttributeTable;
import org.meteoinfo.table.Field;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    /**
     * Create legend scheme
     *
     * @param aLT Legend type
     * @param fieldName Field name
     * @return Legend scheme
     */
    public LegendScheme createLegendScheme(LegendType aLT, String fieldName) {
        double min, max;
        ShapeTypes aST = this.getShapeType();
        LegendScheme aLS = new LegendScheme(this.getShapeType());

        min = aLS.getMinValue();
        max = aLS.getMaxValue();
        switch (aLT) {
            case SINGLE_SYMBOL:
                Color aColor = Color.black;
                float size = 1.0F;
                switch (aST) {
                    case POINT:
                    case POINT_M:
                    case POINT_Z:
                        aColor = Color.black;
                        size = 5;
                        break;
                    case POLYLINE:
                    case POLYLINE_M:
                    case POLYLINE_Z:
                        aColor = Color.black;
                        break;
                    case POLYGON:
                    case POLYGON_M:
                    case POLYGON_Z:
                    case IMAGE:
                        aColor = new Color(255, 251, 195);
                        break;
                }

                aLS = LegendManage.createSingleSymbolLegendScheme(aST, aColor, size);
                break;
            case UNIQUE_VALUE:
                Color[] colors;
                List<String> valueList = new ArrayList<>();
                boolean isDateField = false;
                DataType colType = this.getAttributeTable().getTable().getColumns().get(fieldName).getDataType();
                if (colType == DataType.DATE) {
                    isDateField = true;
                }

                List<String> captions = new ArrayList<>();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/M/d");

                for (int i = 0; i < this.getAttributeTable().getTable().getRows().size(); i++) {
                    Object value = this.getAttributeTable().getTable().getRows().get(i).getValue(fieldName);
                    if (!valueList.contains(value.toString())) {
                        valueList.add(value.toString());
                        if (isDateField) {
                            captions.add(format.format((LocalDateTime) value));
                        }
                    }
                }

                if (valueList.size() <= 13) {
                    colors = LegendManage.createRainBowColors(valueList.size());
                } else {
                    colors = LegendManage.createRandomColors(valueList.size());
                }
                Color[] newcolors = new Color[colors.length + 1];
                newcolors[0] = Color.white;
                for (int i = 1; i < newcolors.length; i++) {
                    newcolors[i] = colors[i - 1];
                }

                if (isDateField) {
                    aLS = LegendManage.createUniqValueLegendScheme(valueList, captions, newcolors, aST, min,
                            max, aLS.getHasNoData(), aLS.getUndefValue());
                } else {
                    aLS = LegendManage.createUniqValueLegendScheme(valueList, newcolors,
                            aST, min, max, aLS.getHasNoData(), aLS.getUndefValue());
                }

                aLS.setFieldName(fieldName);
                break;
            case GRADUATED_COLOR:
                double[] S = new double[this.getAttributeTable().getTable().getRows().size()];
                for (int i = 0; i < S.length; i++) {
                    S[i] = Double.parseDouble(this.getAttributeTable().getTable().getRows().get(i).getValue(fieldName).toString());
                }
                double[] minmax = MIMath.getMinMaxValue(S, aLS.getUndefValue());
                min = minmax[0];
                max = minmax[1];

                if (min == max) {
                    JOptionPane.showMessageDialog(null, "The values of all shapes are same!");
                    break;
                }

                double[] CValues;
                CValues = MIMath.getIntervalValues(min, max);
                colors = LegendManage.createRainBowColors(CValues.length + 1);

                aLS = LegendManage.createGraduatedLegendScheme(CValues, colors,
                        aST, min, max, aLS.getHasNoData(), aLS.getUndefValue());
                aLS.setFieldName(fieldName);
                break;
        }

        return aLS;
    }

    /**
     * Update legend scheme
     *
     * @param aLT Legend type
     * @param fieldName Field name
     */
    public void updateLegendScheme(LegendType aLT, String fieldName) {
        this.setLegendScheme(createLegendScheme(aLT, fieldName));
    }

}
