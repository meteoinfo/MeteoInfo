/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.axis;

/**
 *
 * @author yaqiang
 */
public class AxisProperty {
    // <editor-fold desc="Variables">
    private boolean visible;
    private boolean drawTick;
    private boolean drawLabel;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public AxisProperty(){
        this.visible = true;
        this.drawTick = true;
        this.drawLabel = true;
    }
    
    /**
     * Constructor
     * @param visible Is visible
     * @param drawTick Is draw tick
     * @param drawLabel Is draw label
     */
    public AxisProperty(boolean visible, boolean drawTick, boolean drawLabel){
        this.visible = visible;
        this.drawTick = drawTick;
        this.drawLabel = drawLabel;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * If is visible
     *
     * @return Boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set if is visible
     *
     * @param value Boolean
     */
    public void setVisible(boolean value) {
        visible = value;
    }
    
    /**
     * Get if draw tick lines
     * @return Boolean
     */
    public boolean isDrawTick(){
        return this.drawTick;
    }
    
    /**
     * Set if draw tick lines
     * @param value Boolean
     */
    public void setDrawTick(boolean value){
        this.drawTick = value;
    }

    /**
     * Get if draw label
     *
     * @return Boolean
     */
    public boolean isDrawLabel() {
        return this.drawLabel;
    }

    /**
     * Set if draw label
     *
     * @param value Boolean
     */
    public void setDrawLabel(boolean value) {
        this.drawLabel = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
