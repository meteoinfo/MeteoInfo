/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

/**
 *
 * @author Yaqiang Wang
 */
public class CapPolylineShape extends PolylineShape{
    private float capLen = 10;
    private float capAngle = 0;
    
    /**
     * Get cap length
     * @return Cap length
     */
    public float getCapLen(){
        return capLen;
    }
    
    /**
     * Set cap length
     * @param value Cap length
     */
    public void setCapLen(float value){
        capLen = value;
    }
    
    /**
     * Get cap angle
     * @return Cap angle
     */
    public float getCapAngle(){
        return this.capAngle;
    }
    
    /**
     * Set cap angle
     * @param value Cap angle
     */
    public void setCapAngle(float value){
        this.capAngle = value;
    }
}
