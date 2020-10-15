/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

import java.awt.Color;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class Lighting {

    private boolean enable;
    private int light;
    float[] ambient;
    float[] diffuse;
    float[] specular;
    float[] position;
    float[] mat_ambient;
    float[] mat_diffuse;
    float[] mat_specular;
    float[] mat_emission;
    float mat_shininess;
    
    /**
     * Constructor
     */
    public Lighting() {
        this(true);
    }

    /**
     * Constructor
     * @param far Far light position or not
     */
    public Lighting(boolean far) {        
        this.enable = false;
        this.light = GL2.GL_LIGHT1;
        //this.ambient = new float[]{0.f, 0.f, 0.f, 1.f};
        this.ambient = new float[]{0.2f, 0.2f, 0.2f, 1.f};
        this.diffuse = new float[]{1.f, 1.f, 1.f, 1.f};
        this.specular = new float[]{1.f, 1.f, 1.f, 1.f};
        if (far)
            this.position = new float[]{0.f, 0.f, 1.f, 0.f};
        else
            this.position = new float[]{1.f, 1.f, 2.f, 1.f};
        this.mat_ambient = new float[]{0.2f, 0.2f, 0.2f, 1.f};
        this.mat_diffuse = new float[]{0.8f, 0.8f, 0.8f, 1.f};
        this.mat_specular = new float[]{ 0.0f, 0.0f, 0.0f, 1.0f };
        this.mat_emission = new float[]{ 0.0f, 0.0f, 0.0f, 1.0f };
        this.mat_shininess = 50.0f;
    }

    /**
     * Get enable lighting or not
     *
     * @return Boolean
     */
    public boolean isEnable() {
        return this.enable;
    }

    /**
     * Set enable lighting or not
     *
     * @param value Boolean
     */
    public void setEnable(boolean value) {
        this.enable = value;
    }

    /**
     * Get ambient
     *
     * @return Ambient
     */
    public float[] getAmbient() {
        return this.ambient;
    }

    /**
     * Set ambient
     *
     * @param value Ambient
     */
    public void setAmbient(float[] value) {
        this.ambient = value;
    }
    
    /**
     * Set ambient
     *
     * @param value Ambient
     */
    public void setAmbient(List value) {
        if (value.size() < 4) {
            return;
        }

        this.ambient = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
            (float) value.get(3)};
    }

    /**
     * Set ambient
     *
     * @param value Color
     */
    public void setAmbient(Color value) {
        this.ambient = value.getRGBComponents(null);
    }

    /**
     * Get diffuse
     *
     * @return Diffuse
     */
    public float[] getDiffuse() {
        return this.diffuse;
    }

    /**
     * Set diffuse
     *
     * @param value Diffuse
     */
    public void setDiffuse(float[] value) {
        this.diffuse = value;
    }
    
    /**
     * Set diffuse
     *
     * @param value Diffuse
     */
    public void setDiffuse(List value) {
        if (value.size() < 4) {
            return;
        }

        this.diffuse = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
            (float) value.get(3)};
    }

    /**
     * Set diffuse
     *
     * @param value Color
     */
    public void setDiffuse(Color value) {
        this.diffuse = value.getRGBComponents(null);
    }

    /**
     * Get specular
     *
     * @return Specular
     */
    public float[] getSpecular() {
        return this.specular;
    }

    /**
     * Set specular
     *
     * @param value Specular
     */
    public void setSpecular(float[] value) {
        this.specular = value;
    }
    
    /**
     * Set specular
     *
     * @param value Specular
     */
    public void setSpecular(List value) {
        if (value.size() < 4) {
            return;
        }

        this.specular = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
            (float) value.get(3)};
    }

    /**
     * Set specular
     *
     * @param value Color
     */
    public void setSpecular(Color value) {
        this.specular = value.getRGBComponents(null);
    }

    /**
     * Get position
     *
     * @return Position
     */
    public float[] getPosition() {
        return this.position;
    }

    /**
     * Set position
     *
     * @param value Position
     */
    public void setPosition(float[] value) {
        this.position = value;
    }

    /**
     * Set position
     *
     * @param value Position
     */
    public void setPosition(List value) {
        if (value.size() < 4) {
            return;
        }

        this.position = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
            (float) value.get(3)};
    }
    
    /**
     * Set material ambient light
     * @param value Material ambient light
     */
    public void setMat_Ambient(float[] value) {
        this.mat_ambient = value;
    }
    
    /**
     * Set material ambient light
     *
     * @param value Material ambient light
     */
    public void setMat_Ambient(List value) {
        if (value.size() < 4) {
            return;
        }

        this.mat_ambient = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
            (float) value.get(3)};
    }

    /**
     * Set material diffuse light
     * @param value Material diffuse light
     */
    public void setMat_Diffuse(float[] value) {
        this.mat_diffuse = value;
    }

    /**
     * Set material diffuse light
     *
     * @param value Material diffuse light
     */
    public void setMat_Diffuse(List value) {
        if (value.size() < 4) {
            return;
        }

        this.mat_diffuse = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
                (float) value.get(3)};
    }
    
    /**
     * Set material specular light
     * @param value Material specular light
     */
    public void setMat_Specular(float[] value) {
        this.mat_specular = value;
    }
    
    /**
     * Set material specular light
     *
     * @param value Material specular light
     */
    public void setMat_Specular(List value) {
        if (value.size() < 4) {
            return;
        }

        this.mat_specular = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
            (float) value.get(3)};
    }

    /**
     * Set material emission light
     * @param value Material emission light
     */
    public void setMat_Emission(float[] value) {
        this.mat_emission = value;
    }

    /**
     * Set material emission light
     *
     * @param value Material emission light
     */
    public void setMat_Emission(List value) {
        if (value.size() < 4) {
            return;
        }

        this.mat_emission = new float[]{(float) value.get(0), (float) value.get(1), (float) value.get(2),
                (float) value.get(3)};
    }
    
    /**
     * Set material shininess
     * @param value Material shininess
     */
    public void setMat_Shininess(float value) {
        this.mat_shininess = value;
    }

    /**
     * Set light position
     * @param gl GL2
     */
    public void setPosition(GL2 gl) {
        gl.glLightfv(this.light, GL2.GL_POSITION, position, 0);
    }

    /**
     * Start the lighting
     *
     * @param gl GL2
     */
    public void start(GL2 gl) {
        gl.glShadeModel(GL2.GL_SMOOTH);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(this.light);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        //gl.glEnable(GL2.GL_AUTO_NORMAL);
        //gl.glEnable(GLLightingFunc.GL_NORMALIZE);

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, ambient, 0);
        //gl.glLightfv(this.light, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(this.light, GL2.GL_SPECULAR, specular, 0);
        gl.glLightfv(this.light, GL2.GL_DIFFUSE, diffuse, 0);
        //gl.glLightfv(this.light, GL2.GL_POSITION, position, 0);
        
        //Material
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, mat_shininess);
    }
    
    /**
     * Stop light
     * @param gl GL2
     */
    public void stop(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(this.light);
        //gl.glDisable(GL2.GL_AUTO_NORMAL);
        //gl.glDisable(GLLightingFunc.GL_NORMALIZE);
        //gl.glPopAttrib();
    }
}
