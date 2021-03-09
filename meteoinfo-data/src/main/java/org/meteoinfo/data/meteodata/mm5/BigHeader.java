/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.mm5;

/**
 *
 * @author yaqiang
 */
public class BigHeader {
    public int[][] bhi = new int[50][20];
    public float[][] bhr = new float[20][20];
    public String[][] bhic = new String[50][20];
    public String[][] bhrc = new String[20][20];
    
    /**
     * Get data type index
     * @return Data type index
     */
    public int getIndex(){
        return bhi[0][0];
    }
    
    /**
     * Get y dimension length
     * @return Y dimension length
     */
    public int getYNum(){
        if (this.getIndex() <= 2 && bhi[7][0] == 1 && bhi[14][0] == 0){
            return bhi[8][0];
        } else {
            return bhi[15][0];
        }
    }
    
    /**
     * Get x dimension length
     * @return X dimension length
     */
    public int getXNum(){
        if (this.getIndex() <= 2 && bhi[7][0] == 1 && bhi[14][0] == 0){
            return bhi[9][0];
        } else {
            return bhi[16][0];
        }
    }
    
    /**
     * Get z dimension length
     * @return Z dimension length
     */
    public int getZNum() {
        if (this.getIndex() == 1)
            return 1;
        else
            return bhi[11][this.getIndex() - 1];
    }
    
    /**
     * Get IBLTYP: 0=FRICTIONLESS; 1=BULK;2=BLACKADAR;3=B-T;4=ETA M-Y;5=MRF;6=G-S;7=PX
     * @return IBLTYP
     */
    public int getIBLTYP(){
        return bhi[3][12];
    }
    
    /**
     * Get COARSE DOMAIN CENTER LATITUDE (degree)
     * @return COARSE DOMAIN CENTER LATITUDE (degree)
     */
    public float getXLATC(){
        return bhr[1][0];
    }
    
    /**
     * Get COARSE DOMAIN CENTER LONGITUDE (degree)
     * @return COARSE DOMAIN CENTER LONGITUDE (degree)
     */
    public float getXLONC(){
        return bhr[2][0];
    }
          
    /**
     * Get NEST LEVEL (0: COARSE MESH)
     * @return NEST LEVEL (0: COARSE MESH)
     */
    public int getNestLevel(){
        return bhi[14][0];
    }
    
    /**
     * THE PRESSURE (Pa) AT THE MODEL TOP
     * @return THE PRESSURE (Pa) AT THE MODEL TOP
     */
    public float getModelTop(){
        return bhr[1][1];
    }
    
    /**
     * DOMAIN GRID DISTANCE (KM)
     * @return DOMAIN GRID DISTANCE (KM)
     */
    public float getDeltaX(){
        return bhr[8][0];
    }
    
    /**
     * NONHYDROSTATIC BASE STATE SEA LEVEL PRESSURE (PA)
     * @return NONHYDROSTATIC BASE STATE SEA LEVEL PRESSURE (PA)
     */
    public float getP00(){
        return bhr[1][4];
    }
    
    /**
     * NONHYDROSTATIC BASE STATE SEA LEVEL TEMPERATURE (K)
     * @return NONHYDROSTATIC BASE STATE SEA LEVEL TEMPERATURE (K)
     */
    public float getTS0(){
        return bhr[2][4];
    }
    
    /**
     * NONHYDROSTATIC BASE STATE LAPSE RATE D(T)/D(LN P)
     * @return NONHYDROSTATIC BASE STATE LAPSE RATE D(T)/D(LN P)
     */
    public float getTLP(){
        return bhr[3][4];
    }
    
    /**
     * IICE: 1=CLOUD ICE AND SNOW ARRAYS PRESENT, 0=NOT PRESENT
     * @return IICE
     */
    public int getIICE(){
        return bhi[17][this.getIndex() - 1];
    }
    
    /**
     * MAP PROJECTION. 1: LAMBERT CONFORMAL, 2: POLAR STEREOGRAPHIC, 3: MERCATOR
     * @return MAP PROJECTION. 1: LAMBERT CONFORMAL, 2: POLAR STEREOGRAPHIC, 3: MERCATOR
     */
    public int getMapProj(){
        return bhi[6][0];
    }
    
    /**
     * TRUE LATITUDE 1 (DEGREE)
     * @return TRUE LATITUDE 1 (DEGREE)
     */
    public float getTrueLatNorth(){
        return bhr[4][0];
    }
    
    /**
     * TRUE LATITUDE 2 (DEGREE)
     * @return TRUE LATITUDE 2 (DEGREE)
     */
    public float getTrueLatSouth(){
        return bhr[5][0];
    }
}
