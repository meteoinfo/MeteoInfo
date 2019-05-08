package org.meteoinfo.math.stats.kde;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.util.FastMath;

import com.google.common.primitives.Doubles;

public class Helpers
{
	private static Random ms_rand = new Random(System.currentTimeMillis());
	
	public static int nextInt(int n)
	{
		return ms_rand.nextInt(n);
	}
	
	public static double euclidianDistance(double[] point1, double[] point2)
	{
		double sum = 0;
		for(int d = 0 ; d < point1.length ; d++)
		{
			sum += FastMath.pow(point1[d] - point2[d], 2);
		}
		
		
		return FastMath.sqrt(sum);
	}
	
	/**
	 * Helps avoiding underflow
	 * http://machineintelligence.tumblr.com/post/4998477107/the-log-sum-exp-trick
	 * 
	 * @param valsLogSpace
	 * @return
	 */
	public static double logSumExp(List<Double> valsLogSpace)
	{
		return logSumExp(Doubles.toArray(valsLogSpace));
	}
	
	/**
	 * Helps avoiding underflow
	 * http://machineintelligence.tumblr.com/post/4998477107/the-log-sum-exp-trick
	 * 
	 * @param valsLogSpace
	 * @return
	 */
	public static double logSumExp(double[] valsLogSpace)
	{
		double A = max(valsLogSpace);

		double sum = 0;
		for (double v : valsLogSpace)
		{
			double expVal = v - A;
			sum += Math.exp(expVal);
		}

		return A + Math.log(sum);
	}
	
	public static double[] makeSequence(double start, double end, int length)
	{
		if(start > end)
		{
			System.err.println("Cant make sequance with start > end");
			return null;
		}
		
		double[] seq = new double[length];
		double by = (end - start) / (length - 1);
		double value = start;
		for (int i = 0; i < length; i++, value += by)
		{
			seq[i] = value;
		}
		return seq;
	}
	
	public static int maxInd(double[] a)
	{
		double max = Double.NEGATIVE_INFINITY;
		int maxInd = -1;
		for(int i = 0 ; i < a.length ; i++)
		{
			
			if(a[i] > max)
			{
				maxInd = i;
				max = a[i];
			}
		}
		
		return maxInd;
	}
	
	public static double max(double[] a)
	{
		return a[maxInd(a)];
	}
}
