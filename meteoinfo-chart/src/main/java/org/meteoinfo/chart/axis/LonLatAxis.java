/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

import org.meteoinfo.chart.ChartText;
import org.meteoinfo.common.DataConvert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wyq
 */
public class LonLatAxis extends Axis implements Cloneable {

    private boolean drawDegreeSymbol;
    private boolean longitude;
    private boolean degreeSpace;
    
    /**
     * Constructor
     * @param label Axis label
     * @param isX Is x axis or not
     * @param longitude Is longitude or not
     */
    public LonLatAxis(String label, boolean isX, boolean longitude){
        super(label, isX);
        
        this.drawDegreeSymbol = true;
        this.longitude = longitude;
        this.degreeSpace = false;
    }
    
    /**
     * Constructor
     * @param label Axis label
     * @param isX Is x axis or not
     */
    public LonLatAxis(String label, boolean isX){
        this(label, isX, isX);
    }
    
    /**
     * Constructor
     * @param axis Axis
     */
    public LonLatAxis(Axis axis) {
        this(axis.getLabel().getText(), axis.isXAxis());
        this.autoTick = axis.autoTick;
        this.drawLabel = axis.drawLabel;
        this.drawTickLabel = axis.drawTickLabel;
        this.drawTickLine = axis.drawTickLine;
        this.insideTick = axis.insideTick;
        this.inverse = axis.inverse;
        this.setLabelColor(axis.getLabelColor());
        this.lineWidth = axis.lineWidth;
        this.lineStyle = axis.lineStyle;
        this.location = axis.location;
        this.maxValue = axis.maxValue;
        this.minValue = axis.minValue;
        this.minorTickNum = axis.minorTickNum;
        this.minorTickVisible = axis.minorTickVisible;
        //this.setShift(axis.getShift());
        this.tickColor = axis.tickColor;
        this.tickDeltaValue = axis.tickDeltaValue;
        this.tickLabelColor = axis.tickLabelColor;
        this.tickLabelFont = axis.tickLabelFont;
        this.tickLength = axis.tickLength;
        this.tickWidth = axis.tickWidth;
        this.visible = axis.visible;
        this.positionType = axis.positionType;
        this.position = axis.position;
    }
    
    /**
     * Get if draw degree symbol
     * @return Boolean
     */
    public boolean isDrawDegreeSymbol(){
        return this.drawDegreeSymbol;
    }

    /**
     * Set if draw degree symbol
     * @param value Boolean
     */
    public void setDrawDegreeSymbol(boolean value){
        this.drawDegreeSymbol = value;
    }      
    
    /**
     * Get is longitude or not
     * @return Longitude or not
     */
    public boolean isLongitude(){
        return this.longitude;
    }
    
    /**
     * Set is longitude or not
     * @param value Longitude or not
     */
    public void setLongitude(boolean value){
        this.longitude = value;
    }

    /**
     * Get if using space between degree and E/W/S/N
     * @return Boolean
     */
    public boolean isDegreeSpace() {
        return this.degreeSpace;
    }

    /**
     * Set if using space between degree and E/W/S/N
     * @param value Boolean
     */
    public void setDegreeSpace(boolean value) {
        this.degreeSpace = value;
    }
    
    /**
     * Get tick labels
     *
     */
    @Override
    public void updateTickLabels() {
        List<ChartText> tls = new ArrayList<>();
        String lab;
        for (double v : this.getTickValues()) {
            double value = v;
            if (value > 180) {
                value = value - 360;
            }
            lab = String.valueOf(value);
            lab = DataConvert.removeTailingZeros(lab);
            if (this.isLongitude()) {
                if (value == -180) {
                    lab = "180";
                } else if (!(value == 0 || value == 180)) {
                    if (lab.substring(0, 1).equals("-")) {
                        lab = lab.substring(1) + "W";
                    } else {
                        lab = lab + "E";
                    }
                }
            } else {
                if (!(value == 0)) {
                    if (lab.substring(0, 1).equals("-")) {
                        lab = lab.substring(1) + "S";
                    } else {
                        lab = lab + "N";
                    }
                }
            }
            if (drawDegreeSymbol) {
                if (lab.endsWith("E") || lab.endsWith("W") || lab.endsWith("N") || lab.endsWith("S")) {
                    if (degreeSpace) {
                        lab = lab.substring(0, lab.length() - 1) + String.valueOf((char) 186) + " " +
                                lab.substring(lab.length() - 1);
                    } else {
                        lab = lab.substring(0, lab.length() - 1) + String.valueOf((char) 186) +
                                lab.substring(lab.length() - 1);
                    }
                } else {
                    lab = lab + String.valueOf((char) 186);
                }
            }
            tls.add(new ChartText(lab));
        }

        this.setTickLabels(tls);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return (LonLatAxis)super.clone();
    }
}
