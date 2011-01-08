package capanalyzer.netutils;

public class MinMaxAvgSum <T>
{
	private T min;
	private T max;
	private T average;
	private T sum;
	
	public MinMaxAvgSum(T min, T max, T average, T sum)
	{
		this.min = min;
		this.max = max;
		this.average = average;
		this.sum = sum;
	}
	
	/**
	 * @return the min
	 */
	public T getMin()
	{
		return min;
	}
	/**
	 * @return the max
	 */
	public T getMax()
	{
		return max;
	}
	/**
	 * @return the average
	 */
	public T getAverage()
	{
		return average;
	}
	/**
	 * @return the sum
	 */
	public T getSum()
	{
		return sum;
	}
}
