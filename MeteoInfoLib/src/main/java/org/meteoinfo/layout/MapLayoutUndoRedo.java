/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.layout;

import org.meteoinfo.map.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import org.meteoinfo.global.PointF;
import org.meteoinfo.shape.Graphic;

/**
 *
 * @author yaqiang
 */
public class MapLayoutUndoRedo {

    // <editor-fold desc="Undo/Redo">               
    public class AddElementEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        LayoutElement element;

        public AddElementEdit(MapLayout mapLayout, LayoutElement element) {
            this.mapLayout = mapLayout;
            this.element = element;
        }

        @Override
        public String getPresentationName() {
            return "Add a Layout Element";
        }

        @Override
        public void undo() {
            super.undo();
            mapLayout.removeElement(element);
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            mapLayout.addElement(element);
            mapLayout.paintGraphics();
        }
    }

    public class RemoveElementEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        LayoutElement element;

        public RemoveElementEdit(MapLayout mapLayout, LayoutElement element) {
            this.mapLayout = mapLayout;
            this.element = element;
        }

        @Override
        public String getPresentationName() {
            return "Remove a Layout Element";
        }

        @Override
        public void undo() {
            super.undo();
            mapLayout.addElement(element);
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            mapLayout.removeElement(element);
            mapLayout.paintGraphics();
        }
    }

    public class RemoveElementsEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        List<LayoutElement> elements;

        public RemoveElementsEdit(MapLayout mapLayout, List<LayoutElement> elements) {
            this.mapLayout = mapLayout;
            this.elements = new ArrayList<LayoutElement>(elements);
        }

        @Override
        public String getPresentationName() {
            return "Remove Layout Elements";
        }

        @Override
        public void undo() {
            super.undo();
            for (LayoutElement element : elements) {
                mapLayout.addElement(element);
            }
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            for (LayoutElement element : elements) {
                mapLayout.removeElement(element);
            }
            mapLayout.paintGraphics();
        }
    }

    class MoveElementEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        LayoutElement element;
        int deltaX;
        int deltaY;

        public MoveElementEdit(MapLayout mapLayout, LayoutElement element, int deltaX, int deltaY) {
            this.mapLayout = mapLayout;
            this.element = element;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        @Override
        public String getPresentationName() {
            return "Move a Layout Element";
        }

        @Override
        public void undo() {
            super.undo();
            element.setLeft(element.getLeft() - deltaX);
            element.setTop(element.getTop() - deltaY);
            element.moveUpdate();
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            element.setLeft(element.getLeft() + deltaX);
            element.setTop(element.getTop() + deltaY);
            element.moveUpdate();
            mapLayout.paintGraphics();
        }
    }

    class MoveElementsEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        List<LayoutElement> elements;
        int deltaX;
        int deltaY;

        public MoveElementsEdit(MapLayout mapLayout, List<LayoutElement> elements, int deltaX, int deltaY) {
            this.mapLayout = mapLayout;
            this.elements = new ArrayList<LayoutElement>(elements);
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        @Override
        public String getPresentationName() {
            return "Move a Layout Element";
        }

        @Override
        public void undo() {
            super.undo();
            for (LayoutElement element : elements) {
                element.setLeft(element.getLeft() - deltaX);
                element.setTop(element.getTop() - deltaY);
                element.moveUpdate();
            }
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            for (LayoutElement element : elements) {
                element.setLeft(element.getLeft() + deltaX);
                element.setTop(element.getTop() + deltaY);
                element.moveUpdate();
            }
            mapLayout.paintGraphics();
        }
    }

    class MoveGraphicVerticeEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        LayoutGraphic lg;
        int verticeIdx;
        double newX;
        double newY;
        double oldX;
        double oldY;

        public MoveGraphicVerticeEdit(MapLayout mapLayout, LayoutGraphic lg, int vIdx, double newX, double newY) {
            this.mapLayout = mapLayout;
            this.lg = lg;
            this.verticeIdx = vIdx;
            this.newX = newX;
            this.newY = newY;
            this.oldX = lg.getGraphic().getShape().getPoints().get(vIdx).X;
            this.oldY = lg.getGraphic().getShape().getPoints().get(vIdx).Y;
        }

        @Override
        public String getPresentationName() {
            return "Move Grahic vertice";
        }

        @Override
        public void undo() {
            super.undo();
            lg.verticeEditUpdate(verticeIdx, oldX, oldY);
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            lg.verticeEditUpdate(verticeIdx, newX, newY);
            mapLayout.paintGraphics();
        }
    }

    class ResizeElementEdit extends AbstractUndoableEdit {

        MapLayout mapLayout;
        LayoutElement element;
        Rectangle oldRect;
        Rectangle newRect;

        public ResizeElementEdit(MapLayout mapLayout, LayoutElement element, Rectangle newRect) {
            this.mapLayout = mapLayout;
            this.element = element;
            this.newRect = (Rectangle)newRect.clone();
            this.oldRect = (Rectangle)element.getBounds().clone();
        }

        @Override
        public String getPresentationName() {
            return "Resize a Layout Element";
        }

        @Override
        public void undo() {
            super.undo();
            PointF minP = mapLayout.screenToPage((float) oldRect.x, (float) oldRect.y);
            PointF maxP = mapLayout.screenToPage((float) oldRect.x + oldRect.width, oldRect.y + oldRect.height);
            element.setLeft((int) minP.X);
            element.setTop((int) minP.Y);
            element.setWidth((int) (maxP.X - minP.X));
            element.setHeight((int) (maxP.Y - minP.Y));
            element.resizeUpdate();
            mapLayout.paintGraphics();
        }

        @Override
        public void redo() {
            super.redo();
            PointF minP = mapLayout.screenToPage((float) newRect.x, (float) newRect.y);
            PointF maxP = mapLayout.screenToPage((float) newRect.x + newRect.width, newRect.y + newRect.height);
            element.setLeft((int) minP.X);
            element.setTop((int) minP.Y);
            element.setWidth((int) (maxP.X - minP.X));
            element.setHeight((int) (maxP.Y - minP.Y));
            element.resizeUpdate();
            mapLayout.paintGraphics();
        }
    }
    // </editor-fold>
}
