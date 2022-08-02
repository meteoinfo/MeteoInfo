package org.meteoinfo.math.stats.kde;

import java.util.List;

public class Ball
{
	private static final String NAME = "Ball";

	int numPoints;

	double[] ll;
	double[] ur;
	double minBW;
	double maxBW;

	// If it's a node where I split I will have an event
	// otherwise I will have all events as list.
	Event event = null;
	List<Event> events = null;;

	Ball leftBall;
	Ball rightBall;

	/**
	 * Creates a ball from the given data - for case where no more split is needed
	 * 
	 * @param ballData
	 */
	protected Ball(List<Event> ballData)
	{
		events = ballData;
		this.numPoints = ballData.size();
	}

	/**
	 * Creating a Ball on split, saving all necessary sufficient statistics for the region.
	 * 
	 * @param ll		- Lower left corner of the region
	 * @param ur		- Upper right corner of the region
	 * @param event		- The data point on which we split
	 * @param numPoints	- #Points in that region
	 * @param leftBall	- Left child
	 * @param rightBall	- Right Child
	 * @param minBW		- Minimum BW in that region
	 * @param maxBW		- Maximum BW in that region
	 */
	protected Ball(double[] ll, double[] ur, Event event, int numPoints, Ball leftBall, Ball rightBall, double minBW, double maxBW)
	{
		this.ll = ll;
		this.ur = ur;
		this.event = event;
		this.numPoints = numPoints;
		this.leftBall = leftBall;
		this.rightBall = rightBall;
		this.minBW = minBW;
		this.maxBW = maxBW;
	}
	
	/**
	 * Minimum log probability that can be obtained from the region
	 * 
	 * @param e	- The point to estimate the log probability for
	 * @return Minimum log probability
	 */
	protected double minPdf(Event e)
	{
		double[] farthest = farthestPoint(e.getPoint());

		return BallTree.computeLogKernel(e.getPoint(), farthest, maxBW, minBW); // + FastMath.log(numPoints)
	}

	/**
	 * Maximum log probability that can be obtained from the region
	 * 
	 * @param e	- The point to estimate the log probability for
	 * @return Maximum log probability
	 */
	protected double maxPdf(Event e)
	{
		double[] closest = closestPoint(e.getPoint());
		return BallTree.computeLogKernel(e.getPoint(), closest, minBW, maxBW); // + FastMath.log(numPoints)
	}

	/**
	 * Finds the closest point in the area from the estimated point. Note that that point
	 * does not have to be a real point, it's a "point" on the edge of the region or the actual
	 * point if it's inside.
	 * 
	 * @param point	- Point we are estimating the log probability for
	 * @return	closest "point"
	 */
	private double[] closestPoint(double[] point)
	{
		if (point[0] > ur[0]) // To the right of the area
		{
			if (point[1] >= ur[1]) return ur;
			if (point[1] >= ll[1])
			{
				double[] closest = { ur[0], point[1] };
				return closest;
			}

			double[] closest = { ur[0], ll[1] };
			return closest;
		}

		if (point[0] > ll[0]) // between the two vertical edges (could be in the area)
		{
			if (point[1] > ur[1])
			{
				double[] closest = { point[0], ur[1] };
				return closest;
			}
			if (point[1] >= ll[1])
			{
				// If it's in the area, the closest possible point is
				// the point itself
				return point;
			}

			double[] closest = { point[0], ll[1] };
			return closest;
		}

		// Else - to the left ot the area
		if (point[1] > ur[1])
		{
			double[] closest = { ll[0], ur[1] };
			return closest;
		}
		if (point[1] >= ll[1])
		{
			double[] closest = { ll[0], point[1] };
			return closest;
		}

		return ll;
	}

	/**
	 * Finds the closest point in the area from the estimated point. Note that that point
	 * does not have to be a real point, it's a "point" one of the corners.
	 * 
	 * @param point	- Point we are estimating the log probability for
	 * @return furthest point
	 */
	private double[] farthestPoint(double[] point)
	{
		double midy = ur[1] - (ur[1] - ll[1]) / 2;
		double midx = ur[0] - (ur[0] - ll[0]) / 2;

		double[] ul = { ll[0], ur[1] };
		double[] lr = { ur[0], ll[1] };

		if (point[0] > midx)
		{
			if (point[1] > midy) return ll;
			return ul;
		}

		// Else - to the left ot the midx
		if (point[1] > midy) return lr;
		return ur;
	}

	private String className()
	{
		return NAME;
	}

	public static void main(String... args) throws Exception
	{

	}
}
