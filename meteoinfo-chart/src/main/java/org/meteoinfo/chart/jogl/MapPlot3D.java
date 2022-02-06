package org.meteoinfo.chart.jogl;

import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.Extent;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionUtil;

public class MapPlot3D extends Plot3DGL {
    private ProjectionInfo projInfo;
    private Extent lonLatExtent;

    /**
     * Constructor
     */
    public MapPlot3D() {
        super();
        this.projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
    }

    /**
     * Constructor
     * @param projInfo Projection info
     */
    public MapPlot3D(ProjectionInfo projInfo) {
        super();
        this.projInfo = projInfo;
    }

    /**
     * Get projection info
     * @return Projection info
     */
    public ProjectionInfo getProjInfo() {
        return this.projInfo;
    }

    /**
     * Set projection info
     * @param value Projection info
     */
    public void setProjInfo(ProjectionInfo value) {
        this.projInfo = value;
    }

    /**
     * Add a graphic
     *
     * @param graphic The graphic
     * @param proj The graphic projection
     */
    @Override
    public void addGraphic(Graphic graphic, ProjectionInfo proj) {
        if (proj.equals(this.projInfo)) {
            super.addGraphic(graphic);
        } else {
            Graphic nGraphic = ProjectionUtil.projectClipGraphic(graphic, proj, this.projInfo);
            super.addGraphic(nGraphic);
        }
    }

    /**
     * Set draw extent
     *
     * @param value Extent
     */
    @Override
    public void setDrawExtent(Extent3D value) {
        super.setDrawExtent(value);

        if (this.projInfo.isLonLat()) {
            this.lonLatExtent = new Extent(value.minX, value.maxX, value.minY, value.maxY);
        } else {
            //this.lonLatExtent = ProjectionUtil.getProjectionExtent()
        }
    }
}
