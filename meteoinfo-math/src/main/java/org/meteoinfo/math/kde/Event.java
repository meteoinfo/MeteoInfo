package org.meteoinfo.math.stats.kde;

import org.apache.commons.math3.util.Precision;

public class Event
{
	private static final String NAME = "Event";

	protected long userID;
	protected double[] point;
	protected double h = Precision.EPSILON;
	
	public Event(long userID, double[] point)
	{
		this.userID = userID;
		this.point = point;
	}
	
	public Event(long userID, double xVal, double yVal)
	{
		double[] loc = {xVal, yVal};
		
		this.userID = userID;
		this.point = loc;
	}
	
	public double[] getPoint() { return point; };
	public long getUserID() { return userID; };
	
	protected void setH(double h)
	{
		this.h = Math.max(this.h, h);
	}
	
	protected double getH() throws RuntimeException
	{
		if(h == -1)
		{
			throw new RuntimeException("Bandwidth value was not set");
		}
		
		return h;
	}
}
