/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class ShapeSelection implements Transferable {
    private List<Shape> shapes;
    private final DataFlavor shapeFlavor = new DataFlavor(org.meteoinfo.shape.Shape.class, "Shape Object");
    
    /**
     * Constructor
     * @param shapes Shape
     */
    public ShapeSelection(List<Shape> shapes){
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
