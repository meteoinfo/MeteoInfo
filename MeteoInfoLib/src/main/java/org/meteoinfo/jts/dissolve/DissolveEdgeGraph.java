package org.meteoinfo.jts.dissolve;

import org.meteoinfo.jts.edgegraph.EdgeGraph;
import org.meteoinfo.jts.edgegraph.HalfEdge;
import org.meteoinfo.jts.geom.Coordinate;


/**
 * A graph containing {@link DissolveHalfEdge}s.
 * 
 * @author Martin Davis
 *
 */
class DissolveEdgeGraph extends EdgeGraph
{
  protected HalfEdge createEdge(Coordinate p0)
  {
    return new DissolveHalfEdge(p0);
  }
  

}
