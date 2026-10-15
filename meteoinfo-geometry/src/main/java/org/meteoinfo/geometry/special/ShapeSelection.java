/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.special;

import org.meteoinfo.geometry.Geometry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class ShapeSelection implements Transferable {
    private List<Geometry> shapes;
    private final DataFlavor shapeFlavor = new DataFlavor(Geometry.class, "Shape Object");
    
    /**
     * Constructor
     * @param shapes Shape
     */
    public ShapeSelection(List<Geometry> shapes){
        this.shapes = shapes;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { shapeFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return this.shapeFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return shapes;
    }
}
