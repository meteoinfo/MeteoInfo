package org.meteoinfo.math.stats.kde;

import java.util.List;
import org.apache.commons.math3.util.FastMath;
import org.meteoinfo.math.stats.kde.kdtree.KDTree;

public class KDE
{
	private static final String NAME = "KDE";
	private BallTree ballTree; 
	private int numPoints;
	
	/**
	 * Creates a Kernel Density Estimation for the data using the fixed bandwidth method.
	 * See: http://www.datalab.uci.edu/papers/kernel_KDD2014.pdf 
	 * 
	 * @param data	- The observed data
	 * @param h		- The fixed bandwidth
	 * @return A "trained" fixed-KDE model
	 */
	public static KDE trainFixedKDE(List<Event> data, double h)
	{
		for(Event e : data)
		{
			e.setH(h);
		}
		
		return new KDE(data);
	}

	/**
	 * Creates a Kernel Density Estimation for the data using the adaptive bandwidth method
	 * with the default value of (h0 = 0).
	 * See: http://www.datalab.uci.edu/papers/kernel_KDD2014.pdf 
	 * 
	 * @param data	- The observed data
	 * @param K		- K'th nearest neighbor
	 * @return A "trained" adaptive-KDE model
	 */
	public static KDE trainAdaptiveKDE(List<Event> data, int K)
	{
		return trainAdaptiveKDE(data, 1, K);
	}
	
	/**
	 * Creates a Kernel Density Estimation for the data using the adaptive bandwidth method.
	 * See: http://www.datalab.uci.edu/papers/kernel_KDD2014.pdf 
	 * 
	 * @param data	- The observed data
	 * @param h0	- h0 value (default 1)
	 * @param K		- K'th nearest neighbor
	 * @return A "trained" adaptive-KDE model
	 */
	public static KDE trainAdaptiveKDE(List<Event> data, double h0, int K)
	{
		String funcName = "trainAdaptiveKDE";
//		EtmPoint tm = TM.createPoint(String.format("%s.%s",className(), funcName));
		
		// The following KD tree is not the one for computing the 
		// pdf faster but a real KD-Tree for finding the k'th nearest
		// neighbor in the adaptive method. Using the Stanford ML java
		// library.
		KDTree kdTree = new KDTree(2);	// 2 dimentions.

		// First just creating the KD-tree to help find nearest neighbors
		for(Event e : data)
		{
			insertPoint(kdTree, e.getPoint());
		}
		
		// Now actually finding the k'th nearest neighbor
		for(Event e : data)
		{
			Object[] objectnn = kdTree.nearest(e.getPoint(), K+1); // +1 because I don't want to include the point itself
			double[] lastnn = (double[]) objectnn[objectnn.length - 1];
			
			double h = h0 * Helpers.euclidianDistance(e.getPoint(), lastnn);
			
			e.setH(h);
		}
//		tm.collect();
		return new KDE(data);
	}
	
	/**
	 * The KDTree class (implemented in the Stanford ML library works as a key value map.
	 * Because of that, it cannot store two values with the same key. To overcome that, we
	 * will move the key randomly by a really small number (while keeping the actual value).
	 * 
	 * @param tree	- The tree to insert it to
	 * @param point	- The value to insert to the tree		
	 */
	private static void insertPoint(KDTree tree, double[] point)
	{
		double[] tempPont = new double[point.length];
		System.arraycopy(point, 0, tempPont, 0, point.length);
		while (tree.search(tempPont) != null) // MAJOR HACK!
		{
			tempPont[0] = point[0] + Helpers.nextInt(100000) * 1E-15;
			tempPont[1] = point[1] + Helpers.nextInt(100000) * 1E-15;
		}

		// Note that in terms of data, we will keep the real point's data
		// but just insert it in a random key.
		tree.insert(tempPont, point);
	}
	
	/**
	 * Private constructor, to create an instance use the static method.
	 * 
	 * @param hTrainedData
	 */
	private KDE(List<Event> hTrainedData)
	{
//		EtmPoint tm = TM.createPoint(String.format("%s.%s",className(), "creatingBallTree"));
		this.ballTree = new BallTree(hTrainedData, 200);
//		tm.collect();
		
		this.numPoints = hTrainedData.size();
	}
	
	/**
	 * Computed the log probability of event e. The computation is done by using the kd tree.
	 * See: http://www.datalab.uci.edu/papers/kernel_KDD2014.pdf
	 * 
	 * @param e
	 * @return Log probability for event e
	 */
	public double logPdf(Event e)
	{
		List<Double> logValues = ballTree.logPdfRecurse(e);
		double sum = Helpers.logSumExp(logValues);
		return sum - FastMath.log(numPoints);
	}
	
	private static String className()
	{
		return NAME;
	}

	public static void main(String... args) throws Exception
	{

	}
}
