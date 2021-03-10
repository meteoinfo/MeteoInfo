package org.meteoinfo.chart.jogl;

import org.meteoinfo.chart.plot3d.GraphicCollection3D;

import java.util.*;

public class ParticleGraphics extends GraphicCollection3D {

    /**
     * Inner Particle class
     */
    static class Particle {
        float x, y, z;    //position
        float[] rgba;    //color
    }

    private float pointSize;
    private HashMap<Integer, List> particles;

    /**
     * Constructor
     */
    public ParticleGraphics() {
        super();
        this.pointSize = 2;
        this.particles = new HashMap<>();
    }

    /**
     * Get point size
     * @return Size Single point size
     */
    public float getPointSize() {
        return pointSize;
    }

    /**
     * Set point size
     * @param value Single point size
     */
    public void setPointSize(float value) {
        pointSize = value;
    }

    /**
     * Get particles
     * @return Particles
     */
    public HashMap<Integer, List> getParticles() {
        return particles;
    }

    /**
     * Set particles
     * @param value Particles
     */
    public void setParticles(HashMap<Integer, List> value) {
        particles = value;
    }

    /**
     * Add a particle
     * @param particle The particle
     */
    public void addParticle(int key, Particle particle) {
        if (this.particles.containsKey(key)) {
            this.particles.get(key).add(particle);
        } else {
            List<Particle> list = new ArrayList();
            list.add(particle);
            this.particles.put(key, list);
        }
    }

    /**
     * Get sorted particle list
     * @return Particle list
     */
    public List<Map.Entry<Integer, List>> getParticleList() {
        List<Map.Entry<Integer, List>> list = new ArrayList<Map.Entry<Integer, List>>(particles.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, List>>() {
            public int compare(Map.Entry<Integer, List> mapping1, Map.Entry<Integer, List> mapping2) {
                return mapping2.getKey() - mapping1.getKey();
            }
        });

        return list;
    }
}
