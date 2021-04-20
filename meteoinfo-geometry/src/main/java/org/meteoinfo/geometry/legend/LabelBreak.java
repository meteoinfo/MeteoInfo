 /* Copyright 2012 Yaqiang Wang,
  * yaqiang.wang@gmail.com
  *
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or (at
  * your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  */
 package org.meteoinfo.geometry.legend;

 import org.meteoinfo.common.util.GlobalUtil;
 import org.meteoinfo.ui.event.ISizeChangedListener;
 import org.meteoinfo.ui.event.SizeChangedEvent;

 import javax.swing.event.EventListenerList;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;

 /**
  * Label break class
  *
  * @author Yaqiang Wang
  */
 public class LabelBreak extends ColorBreak {
     // <editor-fold desc="Variables">

     private EventListenerList listeners = new EventListenerList();
     private List<String> text;
     private float angle;
     private Font font;
     private AlignType alignType;
     private float xShift;
     private float yShift;
     private int lineSpace;
     // </editor-fold>
     // <editor-fold desc="Constructor">

     /**
      * Constructor
      */
     public LabelBreak() {
         super();
         this.setBreakType(BreakTypes.LABEL_BREAK);
         this.text = new ArrayList<>();
         this.angle = 0;
         this.setColor(Color.black);
         this.font = new Font(GlobalUtil.getDefaultFontName(), Font.PLAIN, 7);
         this.alignType = AlignType.LEFT;
         this.xShift = 0;
         this.yShift = 0;
         this.lineSpace = 5;
     }
     // </editor-fold>
     // <editor-fold desc="Get Set Methods">

     /**
      * Get text string
      *
      * @return Text string
      */
     public String getText() {
         return String.join("\n", this.text);
     }

     /**
      * Get text string list
      * @return Text string list
      */
     public List<String> getTexts() {
         return this.text;
     }

     /**
      * Set text string and fire size changed event
      *
      * @param text Text string
      */
     public void setText(String text) {
         this.text = Arrays.asList(text.split("\n"));
         this.fireSizeChangedEvent(new SizeChangedEvent(this));
     }

     /**
      * Set text string list
      *
      * @param value Text string list
      */
     public void setTexts(List<String> value) {
         this.text = value;
     }

     /**
      * Get angle
      *
      * @return Angle
      */
     public float getAngle() {
         return angle;
     }

     /**
      * Set angle
      *
      * @param angle Angle
      */
     public void setAngle(float angle) {
         this.angle = angle;
     }

     /**
      * Get font
      *
      * @return Font
      */
     public Font getFont() {
         return font;
     }

     /**
      * Set font and fire size changed event
      *
      * @param f Font
      */
     public void setFont(Font f) {
         this.font = f;
         //this.fireSizeChangedEvent(new SizeChangedEvent(this));
     }

     /**
      * Get align type
      *
      * @return Align type
      */
     public AlignType getAlignType() {
         return alignType;
     }

     /**
      * Set align type
      *
      * @param at Align type
      */
     public void setAlignType(AlignType at) {
         alignType = at;
     }

     /**
      * Get y shift
      *
      * @return Y shift
      */
     public float getYShift() {
         return yShift;
     }

     /**
      * Set y shift
      *
      * @param value Y shift
      */
     public void setYShift(float value) {
         this.yShift = value;
     }

     /**
      * Get x shift
      *
      * @return X shift
      */
     public float getXShift() {
         return xShift;
     }

     /**
      * Set x shift
      *
      * @param value X shift
      */
     public void setXShift(float value) {
         xShift = value;
     }

     /**
      * Get line space
      *
      * @return Line space
      */
     public int getLineSpace() {
         return this.lineSpace;
     }

     /**
      * Set line space
      *
      * @param value Line space
      */
     public void setLineSpace(int value) {
         this.lineSpace = value;
     }

     // </editor-fold>
     // <editor-fold desc="Methods">

     /**
      * Get property object
      *
      * @return Property object
      */
     @Override
     public Object getPropertyObject() {
         HashMap objAttr = new HashMap();
         objAttr.put("Text", "Text");
         objAttr.put("Angle", "Angle");
         objAttr.put("Color", "Color");
         objAttr.put("Font", "Font");
         //objAttr.Add("AlignType", "AlignType");
         //objAttr.Add("YShift", "YShift");
         //CustomProperty cp = new CustomProperty(this, objAttr);
         return objAttr;
     }

     /**
      * Clone
      *
      * @return LabelBreak object
      */
     @Override
     public Object clone() {
         LabelBreak aCB = new LabelBreak();
         aCB.setCaption(this.getCaption());
         aCB.setColor(this.getColor());
         aCB.setDrawShape(this.isDrawShape());
         aCB.setEndValue(this.getEndValue());
         aCB.setNoData(this.isNoData());
         aCB.setStartValue(this.getStartValue());
         aCB.setAngle(this.angle);
         aCB.setTexts(this.text);
         aCB.setFont(this.font);
         aCB.setAlignType(this.alignType);
         aCB.setYShift(this.yShift);
         aCB.setXShift(this.xShift);
         aCB.setLineSpace(this.lineSpace);

         return aCB;
     }
     // </editor-fold>
     // <editor-fold desc="Events">

     /**
      * Add size changed listener
      *
      * @param scl SizeChangedListener interface
      */
     public void addSizeChangedListener(ISizeChangedListener scl) {
         this.listeners.add(ISizeChangedListener.class, scl);
     }

     /**
      * Remove size changed listener
      *
      * @param scl SizeChangedListener interface
      */
     public void removeSizeChangedListener(ISizeChangedListener scl) {
         this.listeners.remove(ISizeChangedListener.class, scl);
     }

     /**
      * Fire size changed event
      *
      * @param event SizeChangedEvent
      */
     public void fireSizeChangedEvent(SizeChangedEvent event) {
         Object[] ls = this.listeners.getListenerList();
         for (int i = 0; i < ls.length; i = i + 2) {
             if (ls[i] == ISizeChangedListener.class) {
                 ((ISizeChangedListener) ls[i + 1]).sizeChangedEvent(event);
             }
         }
     }
     // </editor-fold>
 }