package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.PointD;

public abstract class Transform {

    public abstract boolean isValid();

    public abstract PointD transform(double x, double y);

    public abstract Transform inverted();
}
