/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.legend;

/**
 *
 * @author Yaqiang Wang
 */
public class StreamlineBreak extends ArrowLineBreak {
    // <editor-fold desc="Variables">
    private int interval;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public StreamlineBreak() {
        super();
        this.arrowHeadWidth = 7;
        this.arrowOverhang = 0.5f;
        this.interval = 10;
    }
    
    /**
     * Constructor
     * @param pb PolylineBreak
     */
    public StreamlineBreak(PolylineBreak pb) {
        super(pb);
        this.interval = 10;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get arrow interval
     * @return Arrow interval
     */
    public int getInterval() {
        return this.interval;
    }
    
    /**
     * Set interval
     * @param value Arrow interval 
     */
    public void setInterval(int value) {
        this.interval = value;
    }
     
    // </editor-fold>
}
