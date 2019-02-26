/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.chart.ChartText;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.projection.Reproject;

/**
 *
 * @author Yaqiang Wang
 */
public class ProjLonLatAxis extends LonLatAxis{
    // <editor-fold desc="Variables">
    private ProjectionInfo proj;
    private double x_y;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param label Label
     * @param isX Is x/longitude axis or not
     * @param proj Projection
     */
    public ProjLonLatAxis(String label, boolean isX, ProjectionInfo proj){
        super(label, isX, isX);
        this.proj = proj;
    }
    
    /**
     * Constructor
     * @param label Label
     * @param isX Is x/longitude axis or not
     * @param proj Projection
     * @param xy X or Y value of the axis - using for projection
     */
    public ProjLonLatAxis(String label, boolean isX, ProjectionInfo proj, double xy){
        super(label, isX);
        this.proj = proj;
        this.x_y = xy;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get projection
     * @return Projection
     */
    public ProjectionInfo getProject(){
        return this.proj;
    }
    
    /**
     * Set projection
     * @param value Projection
     */
    public void setProject(ProjectionInfo value){
        this.proj = value;
    }
    
    /**
     * Get x_y value
     * @return x_y value
     */
    public double getX_Y(){
        return this.x_y;
    }
    
    /**
     * Set x_y value
     * @param value x_y value
     */
    public void setX_Y(double value){
        this.x_y = value;
        //this.updateTickValues();
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update tick values
     */
    @Override
    public void updateTickValues() {       
        if (this.proj == null)
            return;
        
        double min = this.getMinValue();
        double max = this.getMaxValue();
        //Calculate min and max lon or lat
        ProjectionInfo toproj = KnownCoordinateSystems.geographic.world.WGS1984;
        double minv, maxv;
        double[][] points = new double[2][];
        if (this.isXAxis()){
            points[0] = new double[]{min, this.x_y};
            points[1] = new double[]{max, this.x_y};
            Reproject.reprojectPoints(points, this.proj, toproj);
            minv = points[0][0];
            maxv = points[1][0];
        } else {
            points[0] = new double[]{this.x_y, min};
            points[1] = new double[]{this.x_y, max};
            Reproject.reprojectPoints(points, this.proj, toproj);
            minv = points[0][1];
            maxv = points[1][1];
        }
        //Get tick values
        List<Object> r = MIMath.getIntervalValues1(minv, maxv);
        double[] values = (double[])r.get(0);
        double[] tickValues = new double[values.length];
        
        this.setTickValues((double[]) r.get(0));
        this.setTickDeltaValue((Double) r.get(1));
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
            if (this.isXAxis()) {
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
            if (this.isDrawDegreeSymbol()) {
                if (lab.endsWith("E") || lab.endsWith("W") || lab.endsWith("N") || lab.endsWith("S")) {
                    lab = lab.substring(0, lab.length() - 1) + String.valueOf((char) 186) + lab.substring(lab.length() - 1);
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
        return (ProjLonLatAxis)super.clone();
    }
    // </editor-fold>
}
