package GMM;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import Util.GMMUtil;
import kmeans.*;

/**
 * 生成K个中心点，使用KMeans聚类传入一个数据集和K，返回K个中心向量
 * 
 * @author Yu
 *
 */
public class GenerateCenters
{
	private ArrayList<ArrayList<Double>> centers;

	public GenerateCenters(List<ArrayList<Double>> dataSet, int K)
	{
		// TODO Auto-generated constructor stub

		double[][] points = GMMUtil.toArray((ArrayList<ArrayList<Double>>) dataSet);
		kmeans_data kmdata = new kmeans_data(points, dataSet.size(), dataSet.get(0).size()); // 初始化数据结构
		kmeans_param param = new kmeans_param(); // 初始化参数结构
		param.initCenterMehtod = kmeans_param.CENTER_RANDOM; // 设置聚类中心点的初始化模式为随机模式

		// 做KMeans计算，k
		kmeans.doKmeans(K, kmdata, param);

		for (double[] row : kmdata.centers)
		{
			System.out.println();
			for (double item : row)
			{
				System.out.print(String.format("%f\t", item));
			}
		}
		setCenters(GMMUtil.toList(kmdata.centers));
	}


	public ArrayList<ArrayList<Double>> getCenters()
	{
		return centers;
	}

	private void setCenters(ArrayList<ArrayList<Double>> centers)
	{
		this.centers = centers;
	}

	public static void main(String[] args)
	{

		BufferedReader br;
		String data = null;

		List<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\wine.txt")));

			while ((data = br.readLine()) != null)
			{
				// System.out.println(data);
				String[] fields = data.split(",");
				List<Double> tmpList = new ArrayList<Double>();
				for (int i = 0; i < fields.length; i++)
					tmpList.add(Double.parseDouble(fields[i]));
				dataList.add((ArrayList<Double>) tmpList);
			}
			br.close();
		} catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GenerateCenters gCenters = new GenerateCenters(dataList, 3);
		
//		double[][] points = GMMUtil.toArray((ArrayList<ArrayList<Double>>) dataList);
//		kmeans_data kmdata = new kmeans_data(points, dataList.size(), dataList.get(0).size()); // 初始化数据结构
//		kmeans_param param = new kmeans_param(); // 初始化参数结构
//		param.initCenterMehtod = kmeans_param.CENTER_RANDOM; // 设置聚类中心点的初始化模式为随机模式
//
//		// 做kmeans计算，k=3
//		kmeans.doKmeans(3, kmdata, param);

		// 查看每个点的所属聚类标号
//		System.out.print("The labels of points is: ");
//		for (int lable : kmdata.labels)
//		{
//			System.out.print(lable + " ");
//		}
		for (ArrayList<Double> row : gCenters.centers)
		{
			System.out.println();
			for (double item : row)
			{
				System.out.print(String.format("%f\t", item));
			}
		}
	}

}
