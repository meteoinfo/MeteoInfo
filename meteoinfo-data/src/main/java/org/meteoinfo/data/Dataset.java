/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

/**
 *
 * @author wyq
 */
public abstract class Dataset {
    
    /**
     * Get dataset type
     * @return Dataset type
     */
    public abstract DatasetType getDatasetType();
    
}
