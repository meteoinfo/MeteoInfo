package org.meteoinfo.math.stats.kde;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

public class BallTree
{
	private static final String NAME = "BallTree";
	private Ball headBall;
	int minParent;

	public static int numSkipped = 0;
	
	/**
	 * Creates a Ball Tree (k-d tree) for the data to reduce the speed of the log probability computation.
	 * See: http://www.datalab.uci.edu/papers/kernel_KDD2014.pdf
	 * @param data		- The data to create the BallTree for 
	 * @param minParent	- Minimum number of nodes in the area to split to split a region to sub regions.
	 */
	protected BallTree(List<Event> data, int minParent)
	{
		this.minParent = minParent;
		this.headBall = splitBall(data, 0);
	}

	/**
	 * Recursively creates the Ball Tree.
	 * See: http://www.datalab.uci.edu/papers/kernel_KDD2014.pdf 
	 * 
	 * @param data		- The data in the tree 
	 * @param feature	- The feature to split on (longitude or latitude)
	 * @return The Ball we split on.
	 */
	private Ball splitBall(List<Event> data, int feature)
	{
		int numPoints = data.size();

		if (numPoints <= minParent) { return new Ball(data); }

		double[] ll = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
		double[] ur = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

		double minBW = Double.MAX_VALUE;
		double maxBW = -1;

		TreeMap<DoubleEqual, Event> sorted = new TreeMap<DoubleEqual, Event>();

		for (Event e : data)
		{
			// In this run I will actually compute all the suffucient statistics
			// of the node.

			minBW = Math.min(minBW, e.getH());
			maxBW = Math.max(maxBW, e.getH());

			double[] point = e.getPoint();

			ll[0] = Math.min(ll[0], point[0]);
			ll[1] = Math.min(ll[1], point[1]);

			ur[0] = Math.max(ur[0], point[0]);
			ur[1] = Math.max(ur[1], point[1]);

			DoubleEqual de = new DoubleEqual(point[feature]);
			sorted.put(de, e);
		}

		// In the second run, I just want to split the data to left and right.
		int medIndex = (int) Math.ceil((double) numPoints / 2) - 1;

		List<Event> rightBranchData = new ArrayList<Event>(medIndex);
		List<Event> leftBranchData = new ArrayList<Event>(medIndex);

		Event medianEvent = null;

		int i = -1;
		for (Event e : sorted.values())
		{
			i++;
			if (i == medIndex)
			{
				medianEvent = e;
				continue;
			}

			if (i < medIndex) leftBranchData.add(e);
			else rightBranchData.add(e);
		}

		Ball leftBranch = splitBall(leftBranchData, (feature + 1) % 2);
		Ball rightBranch = splitBall(rightBranchData, (feature + 1) % 2);

		return new Ball(ll, ur, medianEvent, numPoints, leftBranch, rightBranch, minBW, maxBW);
	}

	/**
	 * Computing the log probability in a recursive way on the tree. We return a list
	 * here and not the actual number so we can use the logSumExp trick (Link: ??) to
	 * avoid underflow.
	 * 
	 * @param e	- Event to estimate the log probability for
	 * @return	list of all log probabilities that were computed.
	 */
	protected List<Double> logPdfRecurse(Event e)
	{
		return logPdfRecurse(e, headBall);
	}
	
	/**
	 * Computing the log probability in a recursive way on the tree. We return a list
	 * here and not the actual number so we can use the logSumExp trick (Link: ??) to
	 * avoid underflow.
	 * 
	 * @param e	- Event to estimate the log probability for
	 * @param ball - The region to start from.
	 * @return	list of all log probabilities that were computed.
	 */
	private static List<Double> logPdfRecurse(Event e, Ball ball)
	{
		List<Double> logValues = new ArrayList<Double>();
		if(ball.events != null)
		{
			// This is a leaf ball - compute pdf of all events
			logValues.addAll(computeLogKernel(e.getPoint(), ball.events));
			return logValues;
		}
		
		// Else, we need to check if we need to recurse further down or this
		// is a good stopping point
		double minPdf = ball.minPdf(e);
		double maxPdf = ball.maxPdf(e);
		
		if(FastMath.exp(maxPdf) - FastMath.exp(minPdf) < 0.001)
		{
			// No recursion needed 
			logValues.add(FastMath.log(ball.numPoints) + (maxPdf + minPdf)/2);
			
			numSkipped += ball.numPoints;
			
			return logValues;
		}

		// No recursion is definitely needed
		logValues.add(computeLogKernel(e.getPoint(), ball.event.getPoint(), ball.event.getH()));
		logValues.addAll(logPdfRecurse(e, ball.leftBall));
		logValues.addAll(logPdfRecurse(e, ball.rightBall));
		
		return logValues;
	}

	protected static List<Double> computeLogKernel(double[] y, List<Event> samples)
	{
		List<Double> logValues = new ArrayList<Double>(samples.size());
		for (Event s : samples)
		{
			logValues.add(computeLogKernel(y, s.getPoint(), s.getH()));
		}

		return logValues;
	}
	
	protected static double computeLogKernel(double[] y, double[] s, double h)
	{
		return computeLogKernel(y, s, h, h);
	}

	/**
	 * The actual math for computing the log probability contribution for point y from point s.
	 * 
	 * @param y - The point we estimate for.
	 * @param s	- Observed point in the model
	 * @param hMax	- The maximum bandwidth in this region
	 * @param hMin	- The minimum bandwidth in this region
	 * @return log probability contribution of 's' to 'y'.
	 */
	protected static double computeLogKernel(double[] y, double[] s, double hMax, double hMin)
	{
		double invH = 1d / hMin;

		double ones = (y[0] - s[0]);
		double twos = (y[1] - s[1]);

		double expVal = ones * (invH * ones) + twos * (invH * twos);

		double firstVal = -FastMath.log(2 * FastMath.PI) - FastMath.log(hMax);
		double value = firstVal + -0.5 * expVal;

		return value;
	}

	private static class DoubleEqual implements Comparable<DoubleEqual>
	{
		Double val;

		public DoubleEqual(double v)
		{
			this.val = v;
		}

		@Override
		public int compareTo(DoubleEqual o)
		{
			if (this.val >= o.val) return 1;

			return -1;
		}

	}

	private String className()
	{
		return NAME;
	}
}
