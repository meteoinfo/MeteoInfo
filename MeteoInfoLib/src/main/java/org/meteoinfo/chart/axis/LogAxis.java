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

/**
 *
 * @author Yaqiang Wang
 */
public class LogAxis extends Axis {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param axis Axis
     */
    public LogAxis(Axis axis){
        super(axis);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update tick values
     */
    @Override
    public void updateTickValues() {
        double[] r = MIMath.getIntervalValues_Log(this.getMinValue(), this.getMaxValue());
        this.setTickValues((double[]) r);
        this.setTickDeltaValue(1);
    }
    
    @Override
    public void updateTickLabels(){
        List<ChartText> tls = new ArrayList<>();
        String lab;
        if (this.isAutoTick()) {
            if (this.getTickValues() == null) {
                return;
            }
            for (double value : this.getTickValues()) {
                lab = String.valueOf(value);
                lab = DataConvert.removeTailingZeros(lab);
                tls.add(new ChartText(lab));
            }
        } else {
            for (int i = 0; i < this.getTickLocations().size(); i++) {
                if (i >= this.getTickLabels().size()) {
                    break;
                }
                double v = this.getTickLocations().get(i);
                if (v >= this.getMinValue() && v <= this.getMaxValue()) {
                    tls.add(this.getTickLabels().get(i));
                }
            }
        }
        
        List<Double> values = new ArrayList<>();
        for (ChartText tl : tls){
            values.add(Double.parseDouble(tl.getText()));
        }
        tls.clear();
        int e;
        for (Double v : values){
            e = (int) Math.floor(Math.log10(v));
            tls.add(new ChartText("$10^{" + String.valueOf(e) + "}$"));
        }

        this.setTickLabels(tls);
    }
    
    // </editor-fold>
}
