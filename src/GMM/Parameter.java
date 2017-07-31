package GMM;

import java.util.ArrayList;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

public class Parameter
{
	
	private double alpha = 1/500;//learning rate 
	private double VarThreshold = 4.0 * 4.0;
	private int k = 10;// k,the number of Gaussians in mixture
	private double WeightSum = 0.9;//threshold sum of weights for background test
	private double VarianceThresholdGen = 3.0*3.0;;
	private double VarianceInit=15.0;//initial variance for new components
	private double VarianceMax = 5*VarianceInit;
	private ArrayList<ArrayList<Double>> pCentricPoint; // 均值参数k个分布的中心点，每个中心点d维
	private ArrayList<Double> pWeights; // k个GMM的权值,数组
	private ArrayList<ArrayList<ArrayList<Double>>> pSigma; // k个GMM的协方差矩阵,d*d*k

	public ArrayList<ArrayList<Double>> getpCentricPoint()
	{
		return pCentricPoint;
	}

	public void setpCentricPoint(ArrayList<ArrayList<Double>> pCentricPoint)
	{
		this.pCentricPoint = pCentricPoint;
	}

	public ArrayList<Double> getpWeights()
	{
		return pWeights;
	}

	public void setpWeights(ArrayList<Double> pWeights)
	{
		this.pWeights = pWeights;
	}

	public ArrayList<ArrayList<ArrayList<Double>>> getpSigma()
	{
		return pSigma;
	}

	public void setpSigma(ArrayList<ArrayList<ArrayList<Double>>> pSigma)
	{
		this.pSigma = pSigma;
	}

	public double getAlpha()
	{
		return alpha;
	}

	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}

//	public double getVarThreshold()
//	{
//		return VarThreshold;
//	}
//
//	public void setVarThreshold(double varThreshold)
//	{
//		VarThreshold = varThreshold;
//	}

	public int getK()
	{
		return k;
	}

	public void setK(int k)
	{
		this.k = k;
	}
}