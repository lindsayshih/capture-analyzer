/*
 * Created on Feb 23, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package capanalyzer.netutils.build;

/**
 * @author rbaryana
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PacketSide {
    
    private String mySide = null;
    
    private PacketSide(String theSide)
    {
        mySide = theSide;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object a) {

        if (a instanceof PacketSide)
        {
            PacketSide a1 = (PacketSide) a;
            return a1.mySide.equals(this.mySide);
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
       return mySide;
    }
    
    public String toArrow()
    {
    	if (mySide.equals("Client to Server"))
    		return "--->>";
    	
    	return "<<---";
    }
    
    public static final PacketSide CLIENT_TO_SERVER = new PacketSide("Client to Server");
    
    public static final PacketSide SERVER_TO_CLIENT = new PacketSide("Server to Client");
}
