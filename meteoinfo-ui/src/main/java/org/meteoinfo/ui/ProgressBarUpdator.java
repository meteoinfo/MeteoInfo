 /* Copyright 2012 - Yaqiang Wang,
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
package org.meteoinfo.ui;

/**
 *
 * @author yaqiang
 */
public class ProgressBarUpdator implements Runnable {

    /**
     * Progress bar that shows the current status
     */
    private javax.swing.JProgressBar jpb = null;
    /**
     * Progress bar value
     */
    private Integer value = null;

    /**
     * Constructor
     * @param jpb The progress bar this has to update
     */
    public ProgressBarUpdator(javax.swing.JProgressBar jpb) {
        this.jpb = jpb;
        jpb.setMaximum(100);
    }

    /**
     * Sets the value to the progress bar
     * @param value Value to set
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Action of the thread will be executed here. The value of the progress bar will be set here.
     */
    @Override
    public void run() {
        do {
            if (value != null) {
                jpb.setValue((int) Math.round(Math.floor(value.intValue() * 100 / jpb.getMaximum())));
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while (value == null || value.intValue() < jpb.getMaximum());
    }
} 