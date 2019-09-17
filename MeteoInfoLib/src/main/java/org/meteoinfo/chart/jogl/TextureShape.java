/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.IOException;
import org.meteoinfo.shape.ImageShape;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class TextureShape extends ImageShape{
    private Texture texture;
    private String fileName;
    
    /**
     * Constructor
     */
    public TextureShape() {
        super();
    }
    
    /**
     * Constructor
     * @param texture Texture
     */
    public TextureShape(Texture texture) {
        super();
        this.texture = texture;
    }
    
    /**
     * Get texture
     * @return Texture
     */
    public Texture getTexture() {
        return this.texture;
    }
    
    /**
     * Set texture
     * @param value Texture 
     */
    public void setTexture(Texture value) {
        this.texture = value;
    }
    
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.TEXTURE;
    }
    
    /**
     * Get file name
     * @return File name
     */
    public String getFileName() {
        return this.fileName;
    }
    
    /**
     * Set file name
     * @param value File name
     */
    public void setFileName(String value) {
        this.fileName = value;
    }
    
    /**
     * Load texture from file
     * @throws IOException 
     */
    public void loadTexture() throws IOException {
        this.texture = TextureIO.newTexture(new File(fileName), true);
    }
}
