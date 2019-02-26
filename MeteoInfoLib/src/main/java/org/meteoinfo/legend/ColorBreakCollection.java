/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.legend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author wyq
 */
public class ColorBreakCollection extends ColorBreak implements Iterator{
    
    private List<ColorBreak> colorBreaks;
    private int index;
    
    /**
     * Constructor
     */
    public ColorBreakCollection(){
        this.index = 0;
        this.colorBreaks = new ArrayList<>();
        this.breakType = BreakTypes.ColorBreakCollection;
    }
    
    /**
     * Constructor
     * @param cbs Color break list
     */
    public ColorBreakCollection(List<ColorBreak> cbs){
        this();
        this.colorBreaks = cbs;
    }
    
     /**
     * Get color break list size
     * @return Color break list size
     */
    public int size(){
        return this.colorBreaks.size();
    }
    
    /**
     * Get is empty or not
     * @return Boolean
     */
    public boolean isEmpty() {
        return this.colorBreaks.isEmpty();
    }
    
    /**
     * Get a color break by index
     * @param idx Index
     * @return Color break
     */
    public ColorBreak get(int idx){
        return this.colorBreaks.get(idx);
    }
    
    /**
     * Add a color break
     * @param cb Color break
     */
    public void add(ColorBreak cb) {
        this.colorBreaks.add(cb);
    }
    
    /**
     * Add a color break
     * @param idx Index
     * @param cb Color break
     */
    public void add(int idx, ColorBreak cb) {
        this.colorBreaks.add(idx, cb);
    }

    @Override
    public boolean hasNext() {
        return index <= this.size() - 1;
    }

    @Override
    public Object next() {
        if (index >= this.size()) {
            throw new NoSuchElementException();
        }
        
        return this.get(index++);
    }
    
}
