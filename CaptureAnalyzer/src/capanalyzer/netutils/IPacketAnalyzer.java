package capanalyzer.netutils;

import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.files.CaptureFileBlock;

public interface IPacketAnalyzer
{
	public abstract void processPacket(CaptureFileBlock theFullPacket, long thePacketOffset);
	
	public abstract void finalizeFlow(FiveTuple theFlowTuple); 
}
