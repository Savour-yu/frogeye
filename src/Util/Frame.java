package Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jfree.chart.event.ChartChangeEvent;
import org.llrp.ltk.generated.parameters.EPCData;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.types.LLRPParameter;

import GMM.GaussianMM;
import drawChart.RSSIChart;

public class Frame
{
	private static int tagNum;// the number of tag in a frame
	private static int antNum = 1;// the number of antenna, also means the dimension of a read
	private static int readLength = 10;// we define readLength as the times of reading in a frame
	private static ArrayList<String> tagEpcList = new ArrayList<String>();// we use that to store all tag EPC we monitor
	private static int countFrame = 0;// to count the num of all frame from we run the program
	private static Logger logger = Logger.getLogger("FrameLog");// log every Frame and some error
	private static int[][][] unit = new int[tagNum][(int) (readLength * 1.3)][antNum];
	private static int[][] countRead = new int[tagNum][antNum];// to count the num of read for every tag,a read contains
																// antNum
	// RSSI
	// private static int[] countAnt = new int[antNum];
	private static int countTagReport = 0;
	private static int[] countFull = new int[antNum]; // count the full RSSI in unit
	private static ArrayList<RSSIChart> charts = new ArrayList<>();
	private static boolean initedFlag = false;
	private static ArrayList<GaussianMM> gmms =  new ArrayList<>();
	public Frame()
	{

	}

	/**
	 * if the End point received a MSG, we get a TagReportData. we take the EPC,
	 * RSSI, Antenna ID from the TagReportData, then we take the EPC as a key, only
	 * consider the tag whose EPC in the tagEpcList
	 * 
	 * @param aTagReportData
	 *            may include the command Report and other Receive formation
	 * 
	 */
	public static void decodeTagReport(TagReportData aTagReportData)
	{
		// First, we try to get EPC
		LLRPParameter epcp = (LLRPParameter) aTagReportData.getEPCParameter();
		String aEPC;
		int RSSI;
		int AntennaID;
		if ((epcp != null) && (aTagReportData.getPeakRSSI() != null) && (aTagReportData.getAntennaID() != null))
		{
			if (epcp.getName().equals("EPC_96"))
			{
				EPC_96 epc96 = (EPC_96) epcp;
				aEPC = epc96.getEPC().toString();
			} else if (epcp.getName().equals("EPCData"))
			{
				EPCData epcData = (EPCData) epcp;
				aEPC = epcData.getEPC().toString();
			} else
			{
				logger.error("Could not find EPC in Tag Report");
				return;
			}
			RSSI = Integer.parseInt(aTagReportData.getPeakRSSI().getPeakRSSI().toString());
			AntennaID = Integer.parseInt(aTagReportData.getAntennaID().getAntennaID().toString());
		} else
		{
			logger.error("Could not get EPC, RSSI or AntennaID,lost a tag report");
			return;
		}

		if (tagEpcList.contains(aEPC))
		{
			logger.error(String.format("[%-5d]\t[%-3d]\t%s\t,%d,%d", countTagReport, tagEpcList.indexOf(aEPC), aEPC,
					RSSI, AntennaID));
			int index = tagEpcList.indexOf(aEPC);
			charts.get(index).receiveData(RSSI, AntennaID);
			// logger.error(String.format("%s", aEPC));

			// int index = tagEpcList.indexOf(aEPC);
			// unit[index][countRead[index][AntennaID - 1]][AntennaID - 1] = RSSI;
			// if (countRead[index][AntennaID - 1] < ((int) readLength * 1.3))
			// {
			// countRead[index][AntennaID - 1]++;
			// } else
			// {
			// countFull[AntennaID - 1]++;
			// }
			// for (int i = 0; i < antNum; i++)
			// {
			// if (countFull[i] >= tagNum * 0.8)
			// {
			// // take the first readLength unit to Guassian
			// }
			// }
		}
		// else if (RSSI > -43)
		// {
		// tagEpcList.add(aEPC);
		//
		// }
		countTagReport++;

	}

	public static void setEPCListFromFile(String filePath)
	{
		try
		{
			String encoding = "utf-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists())
			{ // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null)
				{
					tagEpcList.add(lineTxt);
				}
				read.close();
			} else
			{
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e)
		{
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		if (!initedFlag)
		{
			for (String Epc : tagEpcList)
			{
				charts.add(new RSSIChart("RSSI of tag " + Epc, "Time Serials"));
//				gmms.add(new GaussianMM(dataset, 10));
			}
			initedFlag = true;
		}
	}

	public static void main(String[] args)
	{
		Frame.setEPCListFromFile("D:\\EPCList.txt");
		for (String item : tagEpcList)
		{
			System.out.println(item);
		}

	}

}
