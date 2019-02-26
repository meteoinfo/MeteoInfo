/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.meteoinfo.chart.Margin;
import org.meteoinfo.data.CategoryDataset;
import org.meteoinfo.data.Dataset;

/**
 *
 * @author yaqiang
 */
public class CategoryPlot extends Plot{

    CategoryDataset dataset;
    
    @Override
    public Dataset getDataset(){
        return dataset;
    }
    
    @Override
    public void setDataset(Dataset value){
        dataset = (CategoryDataset)value;
    }
    
    @Override
    public PlotType getPlotType() {
        return PlotType.CATEGORY;
    }

    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        
    }

    @Override
    public Rectangle2D getPositionArea() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rectangle2D getPositionArea(Rectangle2D figureArea) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Rectangle2D getOuterPositionArea(Rectangle2D area) {
        return null;
    }

}
