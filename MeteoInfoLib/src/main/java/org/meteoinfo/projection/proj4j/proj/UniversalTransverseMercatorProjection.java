/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.projection.proj4j.proj;

/**
 *
 * @author yaqiang
 */
public class UniversalTransverseMercatorProjection extends TransverseMercatorProjection {
    public UniversalTransverseMercatorProjection(){
        proj4Name = "utm";
        name = "Universal_Transverse_Mercator";
    }
    
    @Override
    public void initialize(){
        super.initialize();
        
    }
}
