
import java.util.ArrayList;

import Util.Frame;
import drawChart.GMMProbabilityChart;
import drawChart.RSSIChart;
import runnable.EndPointRunnable;

public class MainProcess
{
	private ArrayList<GMMProbabilityChart> GMMcharts = new ArrayList<>();
	private ArrayList<RSSIChart> rssiCharts = new ArrayList<>();

	public static void main(String[] args)
	{
		MainProcess m = new MainProcess();
		Thread thread = new Thread(new EndPointRunnable("192.168.1.117"));

//		for (String Epc : Frame.getEPCList())
//		{
//			m.rssiCharts.add(new RSSIChart("RSSI of tag " + Epc, "Time Serials"));
//		}
		thread.start();

	}
}
