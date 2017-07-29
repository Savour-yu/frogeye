package GMM;

import java.util.ArrayList;

public class Parameter {
	private ArrayList<ArrayList<Double>> pCentricPoint; // 均值参数k个分布的中心点，每个中心点d维
	private ArrayList<Double> pWeights; // k个GMM的权值,数组
	private ArrayList<ArrayList<ArrayList<Double>>> pSigma; // k个GMM的协方差矩阵,d*d*k
	
	public ArrayList<ArrayList<Double>> getpCentricPoint() {
		return pCentricPoint;
	}
	public void setpCentricPoint(ArrayList<ArrayList<Double>> pCentricPoint) {
		this.pCentricPoint = pCentricPoint;
	}
	public ArrayList<Double> getpWeights() {
		return pWeights;
	}
	public void setpWeights(ArrayList<Double> pWeights) {
		this.pWeights = pWeights;
	}
	public ArrayList<ArrayList<ArrayList<Double>>> getpSigma() {
		return pSigma;
	}
	public void setpSigma(ArrayList<ArrayList<ArrayList<Double>>> pSigma) {
		this.pSigma = pSigma;
	}
}