/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.graphic;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.joml.Vector3f;
import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.chart.jogl.pipe.Pipe;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.legend.PolygonBreak;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.ShapeTypes;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author yaqiang
 */
public class SurfaceGraphics extends GraphicCollection3D {
    private PointZ[][] vertices;
    private int[][] legendIndex;
    private boolean faceInterp;
    private boolean edgeInterp;
    private boolean mesh;
    private Transform transform;
    private Vector3f[][] tVertices;
    private Vector3f[][] normals;
    private BufferedImage image;
    private Texture texture;
    private int textureID;
    private GL2 gl;
    
    /**
     * Constructor
     */
    public SurfaceGraphics() {
        super();        
        this.allQuads = true;
        this.singleLegend = false;
        this.faceInterp = false;
        this.edgeInterp = false;
        this.mesh = false;
        this.usingLight = true;
        this.image = null;
        this.gl = null;
    }
    
    /**
     * Get vertices
     * @return 
     */
    public PointZ[][] getVertices() {
        return this.vertices;
    }
    
    /**
     * Set vertices
     * @param value Vertices 
     */
    public void setVertices(PointZ[][] value) {
        this.vertices = value;
        double xmin, ymin, zmin, xmax, ymax, zmax;
        xmin = ymin = zmin = Double.MAX_VALUE;
        xmax = ymax = zmax = Double.MIN_VALUE;
        PointZ p;
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                p = value[i][j];
                xmin = xmin > p.X ? p.X : xmin;
                ymin = ymin > p.Y ? p.Y : ymin;
                zmin = zmin > p.Z ? p.Z : zmin;
                xmax = xmax < p.X ? p.X : xmax;
                ymax = ymax < p.Y ? p.Y : ymax;
                zmax = zmax < p.Z ? p.Z : zmax;
            }
        }
        Extent3D extent = new Extent3D(xmin, xmax, ymin, ymax, zmin, zmax);
        this.setExtent(extent);
    }
    
    /**
     * Get vertex
     * @param i Vertex index i
     * @param j Vertex index j
     * @return Vertex
     */
    public PointZ getVertex(int i, int j) {
        return this.vertices[i][j];
    }

    /**
     * Get transformed vertex
     * @param i Index i
     * @param j Index j
     * @return Transformed vertex
     */
    public Vector3f getTVertex(int i, int j) {
        return this.tVertices[i][j];
    }

    /**
     * Get vertex normal
     * @param i Index i
     * @param j Index j
     * @return Vertex normal
     */
    public Vector3f getNormal(int i, int j) {
        return this.normals[i][j];
    }

    /**
     * get if user interpolated coloring for each face
     * @return Boolean
     */
    public boolean isFaceInterp() {
        return this.faceInterp;
    }

    /**
     * Set if use interpolated coloring for each face
     * @param value Boolean
     */
    public void setFaceInterp(boolean value) {
        this.faceInterp = value;
    }

    /**
     * get if user interpolated coloring for each edge
     * @return Boolean
     */
    public boolean isEdgeInterp() {
        return this.edgeInterp;
    }

    /**
     * Set if use interpolated coloring for each edge
     * @param value Boolean
     */
    public void setEdgeInterp(boolean value) {
        this.edgeInterp = value;
    }

    /**
     * Get if is mesh
     * @return Boolean
     */
    public boolean isMesh() {
        return this.mesh;
    }

    /**
     * Set if is mesh
     * @param value Boolean
     */
    public void setMesh(boolean value) {
        this.mesh = value;
    }
    
    /**
     * Get dimension 1
     * @return Dimension 1
     */
    public int getDim1() {
        return this.vertices.length;
    }
    
    /**
     * Get dimension 2
     * @return Dimension 2
     */
    public int getDim2() {
        return this.vertices[0].length;
    }
    
    /**
     * Set legend scheme
     *
     * @param value Legend scheme
     */
    @Override
    public void setLegendScheme(LegendScheme value) {
        this.legendScheme = value;
        this.updateLegendIndex();
    }

    /**
     * Get image
     * @return The image
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Set image
     * @param value The image
     */
    public void setImage(BufferedImage value) {
        this.image = value;
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

    /**
     * Get texture id
     * @return Texture id
     */
    public int getTextureID() {
        return this.textureID;
    }

    /**
     * Check if the legend has multiple colors
     *
     * @return Multiple colors or not
     */
    public boolean isMultiColors() {
        return this.getLegendScheme().getBreakNum() > 1;
    }
    
    /**
     * Update legend index
     */
    public void updateLegendIndex() {
        int dim1 = this.getDim1();
        int dim2 = this.getDim2();
        this.legendIndex = new int[dim1][dim2];
        if (this.legendScheme.getBreakNum() > 1) {
            for (int i = 0; i < dim1; i++) {
                for (int j = 0; j < dim2; j++) {
                    this.legendIndex[i][j] = this.legendScheme.legendBreakIndex(this.vertices[i][j].M);
                }
            }
        }
    }
    
    /**
     * Get legend break of a vertex
     * @param i Vertex index i
     * @param j Vertex index j
     * @return Legend break
     */
    public ColorBreak getLegendBreak(int i, int j) {
        return this.legendScheme.getLegendBreak(this.legendIndex[i][j]);
    }
    
    /**
     * Get RGBA array of a vertex
     * @param i Vertex index i
     * @param j Vertex index j
     * @return RGBA float array
     */
    public float[] getRGBA(int i, int j) {
        return this.legendScheme.getLegendBreak(this.legendIndex[i][j]).getColor().getRGBComponents(null);
    }

    /**
     * Get RGBA array of a vertex - edge
     * @param i Vertex index i
     * @param j Vertex index j
     * @return RGBA float array
     */
    public float[] getEdgeRGBA(int i, int j) {
        PolygonBreak pb = (PolygonBreak) this.legendScheme.getLegendBreak(this.legendIndex[i][j]);
        return pb.getOutlineColor().getRGBComponents(null);
    }

    @Override
    public LegendScheme getLegendScheme() {
        if (this.mesh) {
            LegendScheme ls = this.legendScheme.convertTo(ShapeTypes.IMAGE, true);
            return ls;
        } else {
            return this.legendScheme;
        }
    }

    /**
     * Transform
     * @param transform The transform
     */
    public void transform(Transform transform) {
        if (this.transform != null) {
            if (this.transform.equals(transform))
                return;
        }
        this.transform = (Transform) transform.clone();

        int dim1 = this.getDim1();
        int dim2 = this.getDim2();
        this.tVertices = new Vector3f[dim1][dim2];
        PointZ p;
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                p = vertices[i][j];
                tVertices[i][j] = new Vector3f(transform.transform_x((float)p.X), transform.transform_y((float)p.Y),
                        transform.transform_z((float)p.Z));
            }
        }

        //Calculate normals
        this.normals = new Vector3f[dim1][dim2];
        Vector3f v, left, right, up, down;
        Vector3f normal, nLeftUp, nLeftDown, nRightUp, nRightDown;
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                v = tVertices[i][j];
                left = j > 0 ? tVertices[i][j - 1] : null;
                right = j < dim2 - 1 ? tVertices[i][j + 1] : null;
                down = i > 0 ? tVertices[i - 1][j] : null;
                up = i < dim1 - 1 ? tVertices[i + 1][j] : null;
                nLeftUp = (left == null || up == null) ? new Vector3f() :
                        left.sub(v, new Vector3f()).cross(up.sub(v, new Vector3f()));
                nLeftDown = (left == null || down == null) ? new Vector3f() :
                        down.sub(v, new Vector3f()).cross(left.sub(v, new Vector3f()));
                nRightUp = (right == null || up == null) ? new Vector3f() :
                        up.sub(v, new Vector3f()).cross(right.sub(v, new Vector3f()));
                nRightDown = (right == null || down == null) ? new Vector3f() :
                        right.sub(v, new Vector3f()).cross(down.sub(v, new Vector3f()));
                normal = nLeftUp.add(nLeftDown).add(nRightUp).add(nRightDown).normalize();
                this.normals[i][j] = normal;
            }
        }
    }

    /**
     * Update texture from image
     * @param gl The JOGL GL2 object
     */
    public void updateTexture(GL2 gl) {
        if (this.gl == null || !this.gl.equals(gl)) {
            this.texture = AWTTextureIO.newTexture(gl.getGLProfile(), this.image, true);
            this.textureID = this.texture.getTextureObject(gl);
            this.gl = gl;
        }
    }
}
