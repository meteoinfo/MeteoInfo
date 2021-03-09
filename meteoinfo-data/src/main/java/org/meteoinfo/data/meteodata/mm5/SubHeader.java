/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.mm5;

/**
 *
 * @author yaqiang
 */
public class SubHeader {
    public int ndim;
    public int[] start_index = new int[4];
    public int[] end_index = new int[4];
    public float xtime;
    public String staggering;
    public String ordering;
    public String current_date;
    public String name;
    public String unit;
    public String description;
    public int timeIndex;
    public long position;
    public int length;
}
