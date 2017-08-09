package Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.jfree.chart.event.ChartChangeEvent;
import org.llrp.ltk.generated.parameters.EPCData;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.types.LLRPParameter;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import GMM.GaussianMM;
import drawChart.RSSIChart;
import runnable.TagRunnable;

public class Frame
{
	private static int tagNum;// the number of tag in a frame
	private static int antNum = 1;// the number of antenna, also means the dimension of a read
	private static int readLength = 10;// we define readLength as the times of reading in a frame
	private static ArrayList<String> tagEpcList = new ArrayList<String>();// we use that to store all tag EPC we monitor
	private static int countFrame = 0;// to count the num of all frame from we run the program
	private static Logger logger = Logger.getLogger("FrameLog");// log every Frame and some error
	private static int count = 0;

	private static double[][][] unit;
	// to count the num of read for every tag,a read contains antNum RSSI
	private static int[][] countRead;
	private static boolean[] countFull; // count the full RSSI in unit

	private static ArrayList<RSSIChart> charts = new ArrayList<>();
	private static boolean initedFlag = false;
//	private static ArrayList<GaussianMM> gmms = new ArrayList<>();
	private static int countTagReport = 0;
	private static ArrayList<Integer> antennaList = new ArrayList<>();

	private static boolean flag = true;
	private static ArrayList<ArrayBlockingQueue<ArrayList<Double>>> queues = new ArrayList<>();

	public Frame()
	{

	}

	public static ArrayList<String> getEPCList()
	{
		return tagEpcList;
	}

	/**
	 * 将单位全部默认置为-90.0
	 */
	private static void initUnit()
	{
		for (int i = 0; i < unit.length; i++)
		{
			for (int j = 0; j < unit[0].length; j++)
			{
				for (int k = 0; k < unit[0][0].length; k++)
				{
					unit[i][j][k] = -90.0;
				}
			}
		}
	}

	private static void clearRecord()
	{
		initUnit();
		// to count the num of read for every tag,a read contains antNum RSSI
		countRead = new int[tagNum][antNum];
		countFull = new boolean[tagNum]; // count the full RSSI in unit
		count = 0;
	}

	/**
	 * 初始化帧，设置EPClist，初始化单位，设置天线list
	 */
	public static void initFrame(int antNum, String EPCFilePath)
	{
		// TODO Auto-generated method stub
		if (!initedFlag)
		{

			Frame.antNum = antNum;
			for (int i = 0; i < antNum; i++)
			{
				antennaList.add(i + 1);
			}
			setEPCListFromFile(EPCFilePath);
			for (String Epc : tagEpcList)
			{
				int index = tagEpcList.indexOf(Epc);
				charts.add(new RSSIChart("RSSI of tag " + Epc, "Time Serials"));
				// gmms.add(new GaussianMM(10, antNum));
				queues.add(new ArrayBlockingQueue<ArrayList<Double>>(1000));
				new Thread(new TagRunnable(Epc, 10, antNum, queues.get(index))).start();

			}
			tagNum = tagEpcList.size();

			unit = new double[tagNum][(int) (readLength * 1.3)][antNum];
			initUnit();
			// to count the num of read for every tag,a read contains antNum RSSI
			countRead = new int[tagNum][antNum];
			countFull = new boolean[tagNum]; // count the full RSSI in unit
			logger.error("epclist:" + tagEpcList.toString());
			logger.error("antennaList:" + antennaList.toString());
			logger.error("tagNum:" + tagNum);
			initedFlag = true;
		}
	}

	// take the (epc,rssi,antennnaID) as input,,then make a unit(frame)
	private static void makeAUnit(String epc, double RSSI, int AntennaID)
	{
		// TODO Auto-generated method stub

		logger.error("count:" + count);
		int EPCIndex = tagEpcList.indexOf(epc);
		int AntIndex = antennaList.indexOf(AntennaID);
		unit[EPCIndex][countRead[EPCIndex][AntIndex]][AntIndex] = RSSI;
		logger.error("epcIndex:" + EPCIndex + "\tantIndex:" + AntIndex);
		// 判断什么时候完成了一帧数据，由于来的数据是不一定的
		// 对于一个 标签，无论哪根天线读取满了1.3*length次，都认为这个标签读取完成，当完成的标签超过标签数量的0.8时，我们认为一帧数据收集完成
		if ((countRead[EPCIndex][AntIndex]) < ((int) (readLength * 1.2)))
		{
			countRead[EPCIndex][AntIndex]++;
			// logger.error("success4");

		} else
		{
			// 某个标签的某根天线读满了
			countFull[EPCIndex] = true;
			// logger.error("success3");
			// countRead[EPCIndex][AntIndex] = 0;

		}
		// logger.error("success0\t" + countRead[EPCIndex][AntIndex]);
		for (int i = 0; i < tagNum; i++)
		{

			if (countFull[i])
			{
				count++;
				// logger.error("success1");
			}

		}
		// 某根天线读满的标签数量超过总数的0.8

		if (count > 0.8 * tagNum)
		{
			logger.error("success2");
			double[][][] frame = new double[tagNum][readLength][antNum];
			// 截取前面的readlength次读
			for (int i = 0; i < tagNum; i++)
			{
				for (int j = 0; j < readLength; j++)
				{
					for (int k = 0; k < antNum; k++)
					{
						frame[i][j][k] = unit[i][j][k];
					}
				}
			}
			clearRecord();
			// 将frame传给GMM
			countFrame++;
			if (countFrame < 10)
			{
				return;
			}
			for (int i = 0; i < readLength; i++)
			{

				try
				{
					queues.get(EPCIndex).put((GMMUtil.toList(frame[EPCIndex]).get(i)));
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// if (gmms.get(EPCIndex).fit(GMMUtil.toList(frame[EPCIndex]).get(i),
				// GaussianMM.trainLearningRate) < 0)
				// {
				// gmms.get(EPCIndex).repaintChart(tagEpcList.get(EPCIndex) + " no hit " +
				// countFrame);
				//
				// }

			}
			// if (countFrame % 10 == 0)
			// {
			// for (int k = 0; k < tagEpcList.size(); k++)
			// {
			// gmms.get(k).repaintChart(tagEpcList.get(k) + " normal refresh " +
			// countFrame);
			// }
			//
			// }

		}

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
		if (!initedFlag)
		{
			return;
		}

		LLRPParameter epcp = (LLRPParameter) aTagReportData.getEPCParameter();
		String aEPC;
		double RSSI;
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
			RSSI = Double.parseDouble(aTagReportData.getPeakRSSI().getPeakRSSI().toString());
			AntennaID = Integer.parseInt(aTagReportData.getAntennaID().getAntennaID().toString());
		} else
		{
			logger.error("Could not get EPC, RSSI or AntennaID,lost a tag report");
			return;
		}

		if (tagEpcList.contains(aEPC) && antennaList.contains(AntennaID))
		{
			logger.error(String.format("[%-5d]\t[%-3d]\t%s\t,%f,%d", countTagReport, tagEpcList.indexOf(aEPC), aEPC,
					RSSI, AntennaID));
			int index = tagEpcList.indexOf(aEPC);
			makeAUnit(aEPC, RSSI, AntennaID);
			charts.get(index).receiveData(RSSI, AntennaID);
			// logger.error(String.format("%s", aEPC));

			// unit[index][countRead[index][AntennaID - 1]][AntennaID - 1] = RSSI;
			// // 判断什么时候完成了一帧数据，由于来的数据是随机的
			// if (countRead[index][AntennaID - 1] < ((int) readLength * 1.2))
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
			// // take the head readLength unit to Guassian
			// }
			// }
		}
		// else if (RSSI > -46)
		// {
		// tagEpcList.add(aEPC);
		// logger.error(String.format("[%-5d]\t[%-3d]\t%s\t,%f,%d", countTagReport,
		// tagEpcList.indexOf(aEPC), aEPC,
		// RSSI, AntennaID));
		// }
		else
		{
			// logger.error(String.format("[%-5d]\t[%-3d]\t%s\t,%f,%d", countTagReport, -1,
			// aEPC, RSSI, AntennaID));
		}
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

	}

	// private static void initGMM()
	// {
	// for(int i =0;i<tagEpcList.size();i++) {
	// gmms.add(new GaussianMM(10, antNum));
	// }
	// }

	public static void main(String[] args)
	{
		Frame.setEPCListFromFile("D:\\EPCList.txt");
		for (String item : tagEpcList)
		{
			System.out.println(item);
		}

	}

}
