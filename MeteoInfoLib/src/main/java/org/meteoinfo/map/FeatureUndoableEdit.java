/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.map;

import javax.swing.undo.AbstractUndoableEdit;

/**
 *
 * @author yaqiang
 */
public class FeatureUndoableEdit extends AbstractUndoableEdit{
    public boolean isFeatureEdit(){
        return true;
    }
}
