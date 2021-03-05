/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

/**
 *
 * @author yaqiang
 */
public enum LegendPosition {
    UPPER_RIGHT,
    UPPER_LEFT,
    UPPER_CENTER,
    LOWER_LEFT,
    LOWER_RIGHT,
    LOWER_CENTER,  
    RIGHT,
    CENTER_LEFT,
    CENTER_RIGHT,      
    CENTER,
    LEFT,
    UPPER_RIGHT_OUTSIDE,
    UPPER_LEFT_OUTSIDE,
    UPPER_CENTER_OUTSIDE,
    LOWER_LEFT_OUTSIDE,
    LOWER_RIGHT_OUTSIDE,
    LOWER_CENTER_OUTSIDE,
    LEFT_OUTSIDE,
    RIGHT_OUTSIDE,
    CUSTOM;
    
    /**
     * If the position is custom
     * @return Boolean
     */
    public boolean isCustom(){
        switch (this){
            case CUSTOM:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get LegendPostion from string
     * @param loc Location string
     * @return LegenPosition
     */
    public static LegendPosition fromString(String loc){
        LegendPosition lp = LegendPosition.UPPER_RIGHT;
        loc = loc.toLowerCase();
        switch (loc){
            case "upper left":
                lp = LegendPosition.UPPER_LEFT;
                break;
            case "upper right":
                lp = LegendPosition.UPPER_RIGHT;
                break;  
            case "upper center":
                lp = LegendPosition.UPPER_CENTER;
                break;
            case "upper center outside":
                lp = LegendPosition.UPPER_CENTER_OUTSIDE;
                break;
            case "lower left":
                lp = LegendPosition.LOWER_LEFT;
                break;
            case "lower right":
                lp = LegendPosition.LOWER_RIGHT;
                break;
            case "lower center":
                lp = LegendPosition.LOWER_CENTER;
                break;
            case "lower center outside":
                lp = LegendPosition.LOWER_CENTER_OUTSIDE;
                break;
            case "left":
                lp = LegendPosition.LEFT;
                break;
            case "left outside":
                lp = LegendPosition.LEFT_OUTSIDE;
                break;
            case "right":
                lp = LegendPosition.RIGHT;
                break;
            case "right outside":
                lp = LegendPosition.RIGHT_OUTSIDE;
                break;
            case "custom":
                lp = LegendPosition.CUSTOM;
                break;
        }
        
        return lp;
    }
}
