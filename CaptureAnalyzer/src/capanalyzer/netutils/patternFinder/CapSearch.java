package capanalyzer.netutils.patternFinder;

/**
 * Common class for all searches.
 * @author rbaryana
 *
 */
public class CapSearch
{

	protected int myMinFlowsToCheck = 1;
	protected int myMinResultRatio = 8;
	
	public void setMinFlowsToCheck(int theMinFlowsToCheck)
	{
		myMinFlowsToCheck = theMinFlowsToCheck;
	}
	public void setMinResultRatio(int theMinResultRatio)
	{
		myMinResultRatio = theMinResultRatio;
	}

}
