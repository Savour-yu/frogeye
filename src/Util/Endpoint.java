package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.custom.enumerations.ImpinjInventorySearchType;
import org.llrp.ltk.generated.custom.enumerations.ImpinjLowDutyCycleMode;
import org.llrp.ltk.generated.custom.messages.IMPINJ_ENABLE_EXTENSIONS;
import org.llrp.ltk.generated.custom.messages.IMPINJ_ENABLE_EXTENSIONS_RESPONSE;
import org.llrp.ltk.generated.custom.parameters.ImpinjInventorySearchMode;
import org.llrp.ltk.generated.custom.parameters.ImpinjLowDutyCycle;
import org.llrp.ltk.generated.enumerations.AISpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.AntennaEventType;
import org.llrp.ltk.generated.enumerations.C1G2WriteResultType;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.GetReaderConfigRequestedData;
import org.llrp.ltk.generated.enumerations.ROSpecStartTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecState;
import org.llrp.ltk.generated.enumerations.ROSpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.StatusCode;
import org.llrp.ltk.generated.interfaces.AccessCommandOpSpec;
import org.llrp.ltk.generated.interfaces.AccessCommandOpSpecResult;
import org.llrp.ltk.generated.interfaces.AirProtocolInventoryCommandSettings;
import org.llrp.ltk.generated.interfaces.SpecParameter;
import org.llrp.ltk.generated.messages.ADD_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ADD_ACCESSSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.ADD_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.CLOSE_CONNECTION;
import org.llrp.ltk.generated.messages.CLOSE_CONNECTION_RESPONSE;
import org.llrp.ltk.generated.messages.ENABLE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ACCESSSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ENABLE_EVENTS_AND_REPORTS;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.GET_ACCESSSPECS;
import org.llrp.ltk.generated.messages.GET_ACCESSSPECS_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG_RESPONSE;
import org.llrp.ltk.generated.messages.GET_REPORT;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG_RESPONSE;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.STOP_ROSPEC;
import org.llrp.ltk.generated.messages.STOP_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.parameters.AISpec;
import org.llrp.ltk.generated.parameters.AISpecStopTrigger;
import org.llrp.ltk.generated.parameters.AccessCommand;
import org.llrp.ltk.generated.parameters.AccessSpec;
import org.llrp.ltk.generated.parameters.AntennaConfiguration;
import org.llrp.ltk.generated.parameters.AntennaEvent;
import org.llrp.ltk.generated.parameters.AntennaProperties;
import org.llrp.ltk.generated.parameters.C1G2InventoryCommand;
import org.llrp.ltk.generated.parameters.C1G2RFControl;
import org.llrp.ltk.generated.parameters.C1G2ReadOpSpecResult;
import org.llrp.ltk.generated.parameters.C1G2SingulationControl;
import org.llrp.ltk.generated.parameters.C1G2TagSpec;
import org.llrp.ltk.generated.parameters.C1G2TargetTag;
import org.llrp.ltk.generated.parameters.C1G2Write;
import org.llrp.ltk.generated.parameters.C1G2WriteOpSpecResult;
import org.llrp.ltk.generated.parameters.Custom;
import org.llrp.ltk.generated.parameters.EPCData;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.GeneralDeviceCapabilities;
import org.llrp.ltk.generated.parameters.InventoryParameterSpec;
import org.llrp.ltk.generated.parameters.RFReceiver;
import org.llrp.ltk.generated.parameters.RFTransmitter;
import org.llrp.ltk.generated.parameters.ROBoundarySpec;
import org.llrp.ltk.generated.parameters.ROSpec;
import org.llrp.ltk.generated.parameters.ROSpecStartTrigger;
import org.llrp.ltk.generated.parameters.ROSpecStopTrigger;
import org.llrp.ltk.generated.parameters.ReaderEventNotificationData;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.generated.parameters.TransmitPowerLevelTableEntry;
import org.llrp.ltk.generated.parameters.UHFBandCapabilities;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.BitArray_HEX;
import org.llrp.ltk.types.Integer96_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.LLRPParameter;
import org.llrp.ltk.types.SignedShort;
import org.llrp.ltk.types.TwoBitField;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;
import org.llrp.ltk.types.UnsignedShortArray_HEX;
import org.llrp.ltk.util.Util;

//import Adapter.TableModel;
//import Layout.Window;
//import func.*;
/**
 * 终端，即上位机，负责连接阅读器，对阅读器进行配置，接收阅读器收到的消息
 * 
 * @author Yu
 *
 */
public class Endpoint implements LLRPEndpoint
{
	private LLRPConnection connection;
	private static String epcString = "EPC: ";
	private static Logger logger = Logger.getLogger(Endpoint.class.getName());
	// private static Logger logger2 = Logger.getLogger("aTagRespond");
	private static Logger logger3 = Logger.getLogger("epcString");
	public static String WriteResult;
	public static String ReadResult;
	public static String Dataread = null;
	private ROSpec rospec;
	private static int count1 = 0;
	private static int NOTIFYcount = 0;
	// public Vector<TagReportData> dataVector = new Vector<TagReportData>();
	// public Vector<String[]> dVector = new Vector<String[]>();

	// public static String Readernum = null;
	// TableModel tb;
	// int number = 0;

	private UnsignedInteger modelName;
	UnsignedShort maxPowerIndex;
	SignedShort maxPower;
	UnsignedShort channelIndex;
	UnsignedShort hopTableID;

	public Endpoint()
	{

	}

	public UnsignedInteger getUniqueMessageID()
	{
		// return new UnsignedInteger(MessageID++);
		Random random = new Random();
		return new UnsignedInteger(random.nextInt(60) + 1);
	}

	@Override
	public void errorOccured(String arg0)
	{
		// TODO Auto-generated method stub
		logger.error(arg0);
	}

	/**
	 * 连接阅读器，通过IP连接，即阅读器与终端上位机处于同一个局域网内
	 * 
	 * @param ip
	 */
	public void connect(String ip)
	{
		// create client-initiated LLRP connection

		connection = new LLRPConnector(this, ip);

		// connect to reader
		// LLRPConnector.connect waits for successful
		// READER_EVENT_NOTIFICATION from reader
		try
		{
			logger.info("Initiate LLRP connection to reader");
			((LLRPConnector) connection).connect();
		} catch (LLRPConnectionAttemptFailedException e1)
		{
			e1.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * 断开与阅读器的连接
	 */
	public void disconnect()
	{
		LLRPMessage response;
		CLOSE_CONNECTION close = new CLOSE_CONNECTION();
		close.setMessageID(getUniqueMessageID());
		try
		{
			// don't wait around too long for close
			response = connection.transact(close, 4000);

			// check whether ROSpec addition was successful
			StatusCode status = ((CLOSE_CONNECTION_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("CLOSE_CONNECTION was successful");
			} else
			{
				logger.info(response.toXMLString());
				logger.info("CLOSE_CONNECTION Failed ... continuing anyway");
			}

		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("CLOSE_CONNECTION: Received invalid response message");
		} catch (TimeoutException ex)
		{
			logger.info("CLOSE_CONNECTION Timeouts ... continuing anyway");
		}
	}

	public void enableImpinjExtensions()
	{
		LLRPMessage response;

		try
		{
			logger.info("IMPINJ_ENABLE_EXTENSIONS ...");
			IMPINJ_ENABLE_EXTENSIONS ena = new IMPINJ_ENABLE_EXTENSIONS();
			ena.setMessageID(getUniqueMessageID());

			response = connection.transact(ena, 10000);

			StatusCode status = ((IMPINJ_ENABLE_EXTENSIONS_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("IMPINJ_ENABLE_EXTENSIONS was successful");
			} else
			{
				logger.info(response.toXMLString());
				logger.info("IMPINJ_ENABLE_EXTENSIONS Failure");
				System.exit(1);
			}
		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("Could not process IMPINJ_ENABLE_EXTENSIONS response");
			System.exit(1);
		} catch (TimeoutException ex)
		{
			logger.error("Timeout Waiting for IMPINJ_ENABLE_EXTENSIONS response");
			System.exit(1);
		}
	}

	public void factoryDefault()
	{

		LLRPMessage response;

		try
		{
			// factory default the reader
			logger.info("SET_READER_CONFIG with factory default ...");
			SET_READER_CONFIG set = new SET_READER_CONFIG();
			set.setMessageID(getUniqueMessageID());
			set.setResetToFactoryDefault(new Bit(true));
			response = connection.transact(set, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((SET_READER_CONFIG_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("SET_READER_CONFIG Factory Default was successful");
			} else
			{
				logger.info(response.toXMLString());
				logger.info("SET_READER_CONFIG Factory Default Failure");
				System.exit(1);
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * 连接阅读器后查看阅读器基本信息
	 */
	public void getReaderCapabilities()
	{
		LLRPMessage response;
		GET_READER_CAPABILITIES_RESPONSE gresp;

		GET_READER_CAPABILITIES get = new GET_READER_CAPABILITIES();
		GetReaderCapabilitiesRequestedData data = new GetReaderCapabilitiesRequestedData(
				GetReaderCapabilitiesRequestedData.All);
		get.setRequestedData(data);
		get.setMessageID(getUniqueMessageID());
		logger.info("Sending GET_READER_CAPABILITIES message  ...");
		try
		{
			response = connection.transact(get, 10000);

			// check whether GET_CAPABILITIES addition was successful
			gresp = (GET_READER_CAPABILITIES_RESPONSE) response;
			StatusCode status = gresp.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("GET_READER_CAPABILITIES was successful");

				// get the info we need
				GeneralDeviceCapabilities dev_cap = gresp.getGeneralDeviceCapabilities();
				if ((dev_cap == null) || (!dev_cap.getDeviceManufacturerName().equals(new UnsignedInteger(25882))))
				{
					logger.error("DocSample2 must use Impinj model Reader, not "
							+ dev_cap.getDeviceManufacturerName().toString());
					System.exit(1);
				}

				modelName = dev_cap.getModelName();
				logger.info("Found Impinj reader model " + modelName.toString());

				// get the max power level
				if (gresp.getRegulatoryCapabilities() != null)
				{
					UHFBandCapabilities band_cap = gresp.getRegulatoryCapabilities().getUHFBandCapabilities();

					List<TransmitPowerLevelTableEntry> pwr_list = band_cap.getTransmitPowerLevelTableEntryList();

					TransmitPowerLevelTableEntry entry = pwr_list.get(pwr_list.size() - 1);

					maxPowerIndex = entry.getIndex();
					maxPower = entry.getTransmitPowerValue();
					// LLRP sends power in dBm * 100
					double d = ((double) maxPower.intValue()) / 100;

					logger.info("Max power " + d + " dBm at index " + maxPowerIndex.toString());
				}
			} else
			{
				logger.info(response.toXMLString());
				logger.info("GET_READER_CAPABILITIES failures");
				System.exit(1);
			}
		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("Could not display response string");
		} catch (TimeoutException ex)
		{
			logger.error("Timeout waiting for GET_READER_CAPABILITIES response");
			System.exit(1);
		}
	}

	/**
	 * 获取阅读器的配置信息
	 *
	 * 
	 */
	public void getReaderConfiguration()
	{
		LLRPMessage response;
		GET_READER_CONFIG_RESPONSE gresp;

		GET_READER_CONFIG get = new GET_READER_CONFIG();
		GetReaderConfigRequestedData data = new GetReaderConfigRequestedData(GetReaderConfigRequestedData.All);
		get.setRequestedData(data);
		get.setMessageID(getUniqueMessageID());
		get.setAntennaID(new UnsignedShort(0));
		get.setGPIPortNum(new UnsignedShort(0));
		get.setGPOPortNum(new UnsignedShort(0));
		logger.info("Sending GET_READER_CONFIG message  ...");
		try
		{
			response = connection.transact(get, 10000);

			// check whether GET_CAPABILITIES addition was successful
			gresp = (GET_READER_CONFIG_RESPONSE) response;
			StatusCode status = gresp.getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("GET_READER_CONFIG was successful");

				List<AntennaConfiguration> alist = gresp.getAntennaConfigurationList();

				if (!alist.isEmpty())
				{
					AntennaConfiguration a_cfg = alist.get(0);
					channelIndex = a_cfg.getRFTransmitter().getChannelIndex();
					hopTableID = a_cfg.getRFTransmitter().getHopTableID();
					RFTransmitter rfTransmitter = a_cfg.getRFTransmitter();
					// rfTransmitter.getTransmitPower().toString();

					List<AirProtocolInventoryCommandSettings> iclist = a_cfg
							.getAirProtocolInventoryCommandSettingsList();
					C1G2InventoryCommand i_com = (C1G2InventoryCommand) iclist.get(0);
					ImpinjInventorySearchMode searchmode = (ImpinjInventorySearchMode) i_com.getCustomList().get(0);
					ImpinjLowDutyCycle lowDutyCycle = (ImpinjLowDutyCycle) i_com.getCustomList().get(3);
					C1G2RFControl rfControl = i_com.getC1G2RFControl();
					C1G2SingulationControl singulationControl = i_com.getC1G2SingulationControl();

					logger.info("ChannelIndex " + channelIndex.toString() + " hopTableID " + hopTableID.toString()
							+ " *****RFpower*******:" + rfTransmitter.getTransmitPower().toString() + " RFSensitity:"
							+ a_cfg.getRFReceiver().getReceiverSensitivity().toString() + " SearchMode:"
							+ searchmode.getInventorySearchMode().toString() + " LowDutyCircle:"
							+ lowDutyCycle.getLowDutyCycleMode().toString() + "\n"
							+ " [0:Max Throughput 1:Hybrid Mode(High throughput(M=2)) 2:Dense Reader(M=4) 3:Dense Reader(M=8) 4��Max Miller(High throughput(M=4)) 5:Dense Reader(M=4)2 1000:AutoSet 1001��AutoSet Single Interrogator=1000 now]"
							+ "\n" + " RFModeIndex: " + rfControl.getModeIndex().toString() + " Tari: "
							+ rfControl.getTari().toString() + " Session: " + singulationControl.getSession().toString()
							+ " TagPopulation(estimate): " + singulationControl.getTagPopulation().toString()
							+ " TagTransitTime(estimate)" + singulationControl.getTagTransitTime().toString());
				} else
				{
					logger.error("Could not find antenna configuration");
					System.exit(1);
				}
				List<AntennaProperties> pList = gresp.getAntennaPropertiesList();
				if (!pList.isEmpty())
				{
					AntennaProperties a_pro1 = pList.get(0);

					logger.info("AntennaID: " + a_pro1.getAntennaID().toString() + " ConnectedStatus: "
							+ a_pro1.getAntennaConnected().toString() + " AntennaGain: "
							+ a_pro1.getAntennaGain().toString());
					AntennaProperties a_pro2 = pList.get(1);
					logger.info("AntennaID: " + a_pro2.getAntennaID().toString() + " ConnectedStatus: "
							+ a_pro2.getAntennaConnected().toString() + " AntennaGain: "
							+ a_pro2.getAntennaGain().toString());

				} else
				{
					logger.error("Could not find antenna configuration");
					System.exit(1);
				}
			} else
			{
				logger.info(response.toXMLString());
				logger.info("GET_READER_CONFIG failures");
				System.exit(1);
			}
		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("Could not display response string");
		} catch (TimeoutException ex)
		{
			logger.error("Timeout waiting for GET_READER_CONFIG response");
			System.exit(1);
		}
	}

	public ADD_ROSPEC buildROSpecFromObjects()
	{
		logger.info("Building ADD_ROSPEC message from scratch ...");
		ADD_ROSPEC addRoSpec = new ADD_ROSPEC();
		addRoSpec.setMessageID(getUniqueMessageID());

		rospec = new ROSpec();

		// set up the basic info for the RO Spec.
		rospec.setCurrentState(new ROSpecState(ROSpecState.Disabled));
		rospec.setPriority(new UnsignedByte(0));
		rospec.setROSpecID(getUniqueMessageID());
		// rospec.setROSpecID(new UnsignedInteger(12345));

		// set the start and stop conditions for the ROSpec.
		// For now, we will start and stop manually
		ROBoundarySpec boundary = new ROBoundarySpec();
		ROSpecStartTrigger start = new ROSpecStartTrigger();
		ROSpecStopTrigger stop = new ROSpecStopTrigger();
		start.setROSpecStartTriggerType(new ROSpecStartTriggerType(ROSpecStartTriggerType.Null));
		stop.setROSpecStopTriggerType(new ROSpecStopTriggerType(ROSpecStopTriggerType.Null));
		stop.setDurationTriggerValue(new UnsignedInteger(0));
		boundary.setROSpecStartTrigger(start);
		boundary.setROSpecStopTrigger(stop);
		rospec.setROBoundarySpec(boundary);

		// set up what we want to do in the ROSpec. In this case
		// build the simples inventory on all channels using defaults
		AISpec aispec = new AISpec();

		// what antennas to use.
		UnsignedShortArray ants = new UnsignedShortArray();
		ants.add(new UnsignedShort(0)); // 0 means all antennas
		aispec.setAntennaIDs(ants);

		// set up the AISpec stop condition and options for inventory
		AISpecStopTrigger aistop = new AISpecStopTrigger();
		aistop.setAISpecStopTriggerType(new AISpecStopTriggerType(AISpecStopTriggerType.Null));
		aistop.setDurationTrigger(new UnsignedInteger(0));
		aispec.setAISpecStopTrigger(aistop);

		// set up any override configuration. none in this case
		InventoryParameterSpec ispec = new InventoryParameterSpec();
		ispec.setAntennaConfigurationList(null);
		ispec.setInventoryParameterSpecID(new UnsignedShort(23));
		ispec.setProtocolID(new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));
		List<InventoryParameterSpec> ilist = new ArrayList<InventoryParameterSpec>();
		ilist.add(ispec);

		aispec.setInventoryParameterSpecList(ilist);
		List<SpecParameter> slist = new ArrayList<SpecParameter>();
		slist.add(aispec);
		rospec.setSpecParameterList(slist);

		addRoSpec.setROSpec(rospec);

		return addRoSpec;
	}

	public ADD_ROSPEC buildROSpecFromFile()
	{
		logger.info("Loading ADD_ROSPEC message from file ADD_ROSPEC.xml ...");
		try
		{
			LLRPMessage addRospec = Util.loadXMLLLRPMessage(new File("D:\\ADD_ROSPEC.xml"));
			// TODO make sure this is an ADD_ROSPEC message
			return (ADD_ROSPEC) addRospec;
		} catch (FileNotFoundException ex)
		{
			logger.error("Could not find file");
			System.exit(1);
		} catch (IOException ex)
		{
			logger.error("IO Exception on file");
			System.exit(1);
		} catch (JDOMException ex)
		{
			logger.error("Unable to convert LTK-XML to DOM");
			System.exit(1);
		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("Unable to convert LTK-XML to Internal Object");
			System.exit(1);
		}
		return null;
	}

	/**
	 * set the reader by the config document
	 * 
	 * @param config
	 *            :build by a xml document
	 * @return nothing
	 */
	public void setReaderConfiguration(SET_READER_CONFIG config)
	{

		try
		{
			connection.transact(config, 10000);
		} catch (TimeoutException e)
		{
			logger.error("Timeout waiting for SET_READER_CONFIG response");
			System.exit(1);
		}
	}

	/**
	 * set the reader by default config file
	 */
	public void setReaderConfiguration()
	{
		LLRPMessage response;

		logger.info("Loading SET_READER_CONFIG message from file SET_READER_CONFIG.xml ...");
		try
		{
			LLRPMessage setConfig;

			// TODO make sure this is an SET_READER_CONFIG message
			try
			{
				setConfig = Util.loadXMLLLRPMessage(new File("D:\\config.xml"));
			} catch (Exception e)
			{
				// TODO: handle exception
				setConfig = Util.loadXMLLLRPMessage(new File("D:\\SET_READER_CONFIG.xml"));
			}

			response = connection.transact(setConfig, 10000);

			// check whetherSET_READER_CONFIG addition was successful
			StatusCode status = ((SET_READER_CONFIG_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("SET_READER_CONFIG was successful");
			} else
			{
				logger.info(response.toXMLString());
				logger.info("SET_READER_CONFIG failures");
				System.exit(1);
			}

		} catch (TimeoutException ex)
		{
			logger.error("Timeout waiting for SET_READER_CONFIG response");
			System.exit(1);
		} catch (FileNotFoundException ex)
		{
			logger.error("Could not find file");
			System.exit(1);
		} catch (IOException ex)
		{
			logger.error("IO Exception on file");
			System.exit(1);
		} catch (JDOMException ex)
		{
			logger.error("Unable to convert LTK-XML to DOM");
			System.exit(1);
		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("Unable to convert LTK-XML to Internal Object");
			System.exit(1);
		}
	}

	public void addRoSpec(boolean xml)
	{
		LLRPMessage response;

		ADD_ROSPEC addRospec = null;

		if (xml)
		{
			addRospec = buildROSpecFromFile();
		} else
		{
			addRospec = buildROSpecFromObjects();
		}

		addRospec.setMessageID(getUniqueMessageID());
		rospec = addRospec.getROSpec();

		logger.info("Sending ADD_ROSPEC message  ...");
		try
		{
			response = connection.transact(addRospec, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((ADD_ROSPEC_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("ADD_ROSPEC was successful");
			} else
			{
				logger.info(response.toXMLString());
				logger.info("ADD_ROSPEC failures");
				System.exit(1);
			}
		} catch (InvalidLLRPMessageException ex)
		{
			logger.error("Could not display response string");
		} catch (TimeoutException ex)
		{
			logger.error("Timeout waiting for ADD_ROSPEC response");
			System.exit(1);
		}
	}

	public void enable()
	{
		LLRPMessage response;
		try
		{
			// factory default the reader
			logger.info("ENABLE_ROSPEC ...");
			ENABLE_ROSPEC ena = new ENABLE_ROSPEC();
			ena.setMessageID(getUniqueMessageID());
			ena.setROSpecID(rospec.getROSpecID());

			response = connection.transact(ena, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((ENABLE_ROSPEC_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("ENABLE_ROSPEC was successful");
			} else
			{
				logger.error(response.toXMLString());
				logger.info("ENABLE_ROSPEC_RESPONSE failed ");
				System.exit(1);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start()
	{
		LLRPMessage response;
		try
		{
			logger.info("START_ROSPEC ...");
			START_ROSPEC start = new START_ROSPEC();
			start.setMessageID(getUniqueMessageID());
			start.setROSpecID(rospec.getROSpecID());

			response = connection.transact(start, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((START_ROSPEC_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("START_ROSPEC was successful");
			} else
			{
				logger.error(response.toXMLString());
				logger.info("START_ROSPEC_RESPONSE failed ");
				System.exit(1);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void stop()
	{
		LLRPMessage response;
		try
		{
			logger.info("STOP_ROSPEC ...");
			STOP_ROSPEC stop = new STOP_ROSPEC();
			stop.setMessageID(getUniqueMessageID());
			stop.setROSpecID(rospec.getROSpecID());

			response = connection.transact(stop, 10000);

			// check whether ROSpec addition was successful
			StatusCode status = ((STOP_ROSPEC_RESPONSE) response).getLLRPStatus().getStatusCode();
			if (status.equals(new StatusCode("M_Success")))
			{
				logger.info("STOP_ROSPEC was successful");
			} else
			{
				logger.error(response.toXMLString());
				logger.info("STOP_ROSPEC_RESPONSE failed ");
				System.exit(1);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void logOneCustom(Custom cust)
	{

		if (cust.getVendorIdentifier().intValue() != (25882))
		{
			logger.error("Non Impinj Extension Found in message");
			return;
		}
	}

	/**
	 * we use that method
	 * 
	 * @param tr
	 */
	public void logOneTagReport(TagReportData tr)
	{
		// As an example here, we'll just get the stuff out of here and
		// for a super long string
		epcString = "";

		// epc is not optional, so we should fail if we can't find it
		// String epcString = "EPC: ";
		LLRPParameter epcp = (LLRPParameter) tr.getEPCParameter();
		if (epcp != null)
		{
			if (epcp.getName().equals("EPC_96"))
			{
				EPC_96 epc96 = (EPC_96) epcp;
				epcString += epc96.getEPC().toString();
			} else if (epcp.getName().equals("EPCData"))
			{
				EPCData epcData = (EPCData) epcp;
				epcString += epcData.getEPC().toString();
			}
		} else
		{
			logger.error("Could not find EPC in Tag Report");
			return;

		}

		// all of these values are optional, so check their non-nullness first
		if (tr.getTagSeenCount() != null)
		{
			epcString += "\n" + " SeenCount: " + tr.getTagSeenCount().getTagCount().toString();

		}

		if (tr.getChannelIndex() != null)
		{
			epcString += "\n" + " ChanIndex: " + tr.getChannelIndex().getChannelIndex().toString();
		}
		if (tr.getPeakRSSI() != null)
		{
			epcString += "\n" + " RSSI: " + tr.getPeakRSSI().getPeakRSSI().toString();
		}
		if (tr.getFirstSeenTimestampUTC() != null)
		{
			epcString += "\n" + " FirstSeen: " + tr.getFirstSeenTimestampUTC().getMicroseconds().toString();
		}

		if (tr.getInventoryParameterSpecID() != null)
		{
			epcString += "\n" + " ParamSpecID: "
					+ tr.getInventoryParameterSpecID().getInventoryParameterSpecID().toString();
		}

		if (tr.getLastSeenTimestampUTC() != null)
		{
			epcString += "\n" + " LastTime: " + tr.getLastSeenTimestampUTC().getMicroseconds().toString();
		}

		if (tr.getAntennaID() != null)
		{
			epcString += "\n" + " Antenna: " + tr.getAntennaID().getAntennaID().toString();
		}

		if (tr.getROSpecID() != null)
		{
			epcString += "\n" + " ROSpecID: " + tr.getROSpecID().getROSpecID().toString();
		}

//		count++;
		logger3.fatal(epcString);

	}

	@Override
	public void messageReceived(LLRPMessage message)
	{

		// convert all messages received to LTK-XML representation
		// and print them to the console

		logger.info("Received " + message.getName() + " message asychronously");

		if (message.getTypeNum() == RO_ACCESS_REPORT.TYPENUM)
		{
			// REPORTcount++;

			RO_ACCESS_REPORT report1 = (RO_ACCESS_REPORT) message;

			List<TagReportData> tdlist1 = report1.getTagReportDataList();
			for (TagReportData tr : tdlist1)
			{

				Frame.decodeTagReport(tr);
				// logOneTagReport(tr);
				count1++;
			}
			// List<Custom> clist1 = report1.getCustomList();
			// for (Custom cust : clist1)
			// {
			// logOneCustom(cust);
			// }
		} else if (message.getTypeNum() == READER_EVENT_NOTIFICATION.TYPENUM)
		{
			// TODO
			NOTIFYcount++;

			logger.info(NOTIFYcount + ":there are some READER_EVENT_NPTIFICATION occured!");
			READER_EVENT_NOTIFICATION notification = (READER_EVENT_NOTIFICATION) message;
			ReaderEventNotificationData notifdata = notification.getReaderEventNotificationData();

			notifdata.getAntennaEvent();
		}

	}

	public void enable_notification()
	{

		logger.info("ENABLE_EVENT_AND_REPORTS");
		ENABLE_EVENTS_AND_REPORTS eer = new ENABLE_EVENTS_AND_REPORTS();
		eer.setMessageID(getUniqueMessageID());

		try
		{
			connection.transact(eer, 10000);
		} catch (TimeoutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		Frame.initFrame(2, "D:\\EPCList.txt");

		BasicConfigurator.configure();

		if (args.length < 1)
		{
			System.out.print("Must pass reader hostname or IP as agument 1");
		}

		// Only show root events from the base logger
		Logger.getRootLogger().setLevel(Level.ERROR);
		Endpoint example = new Endpoint();
		logger.setLevel(Level.INFO);

		example.connect("192.168.1.117");
		example.enableImpinjExtensions();
		example.factoryDefault();
		example.getReaderCapabilities();
		example.getReaderConfiguration();
		example.setReaderConfiguration();
		example.getReaderConfiguration();
		example.enable_notification();
		example.addRoSpec(false);
		// example.addAccessSpec();
		example.enable();

		example.start();
		Scanner aScanner = new Scanner(System.in);
		//一直阻塞直到输入exit则退出
		while (!aScanner.nextLine().equals("exit"))
		{
		}
		aScanner.close();



		example.stop();
		example.disconnect();
		// logger2.fatal("The count of how many times tag were read:" + count1);
		logger3.fatal("The count of how many times tag were read:" + count1);
		System.exit(0);
	}
}
