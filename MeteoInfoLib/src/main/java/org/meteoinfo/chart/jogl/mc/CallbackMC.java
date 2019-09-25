/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl.mc;

import java.util.ArrayList;

/**
 * Created by Primoz on 8.7.2016.
 */
public abstract class CallbackMC implements Runnable {
    private ArrayList<float []> vertices;

    void setVertices(ArrayList<float []> vertices) {
        this.vertices = vertices;
    }

    public ArrayList<float []> getVertices() {
        return this.vertices;
    }
}
