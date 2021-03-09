/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.mm5;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yaqiang
 */
public class DataHead {
    public int iversion;
    public String hdate;
    public float xfcst;
    public String field;
    public String units;
    public String desc;
    public float level;
    public int idim;
    public int jdim;
    public int llflag;
    public float startlat;
    public float startlon;
    public float deltalat;
    public float deltalon;
    public long position;
    public int length;
    
    /**
     * Get date
     * @return Date
     */
    public LocalDateTime getDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        return LocalDateTime.parse(hdate, format);
    }
}
