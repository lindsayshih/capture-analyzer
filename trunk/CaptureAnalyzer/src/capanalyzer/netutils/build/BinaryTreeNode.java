package capanalyzer.netutils.build;

public class BinaryTreeNode
{
	private BinaryTreeNode myLeftSon = null;
	private BinaryTreeNode myRightSon = null;
	private Object myValue = null;
	private String myType = null;
	
	public BinaryTreeNode(String type)
	{
		myType = type;
	}
	
	public BinaryTreeNode getLeftSon()
	{
		return myLeftSon;
	}
	public void setLeftSon(BinaryTreeNode theLeftSon)
	{
		myLeftSon = theLeftSon;
	}
	public BinaryTreeNode getRightSon()
	{
		return myRightSon;
	}
	public void setRightSon(BinaryTreeNode theRightSon)
	{
		myRightSon = theRightSon;
	}
	public Object getValue()
	{
		return myValue;
	}
	public void setValue(Object theValue)
	{
		myValue = theValue;
	}
	
	public boolean isLeaf()
	{
		return (myLeftSon == null) && (myRightSon == null);
	}

	public String getType()
	{
		return myType;
	}
	
	public static void printTree(BinaryTreeNode root)
	{
		if (root.isLeaf())
		{
			System.out.println(root.myValue);
			return;
		}
		
		if (root.myLeftSon != null)
		{
			System.out.print("( ");
			printTree(root.myLeftSon);
			System.out.print(" ) ");
		}
		
		System.out.print(" "+root.myType+" ");
		
		if (root.myRightSon != null)
		{
			System.out.print("( ");
			printTree(root.myRightSon);
			System.out.print(" ) ");
		}
	}
}
