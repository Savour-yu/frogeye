package GMM;

import java.util.ArrayList;
import java.util.Comparator;

import javax.print.attribute.standard.Sides;

/***
 * 高斯模型，一个峰由权重、标准差、中心向量衡量，且我们每次把权重除以标准差的值作为优先级rank
 * 以后尝试拟合一个模型时，按照rank大小优先拟合大的，所有都未成功拟合则将rank最小的取代。
 * 
 * @author Yu
 *
 */
public class Model
{
	private static double defaultWight = 0.01;// 默认的权重
	private static double defaultVariance = 2000;// 默认的标准差

	private double weight;// 峰的权重
	private double variance;// 峰的标准差
	private ArrayList<Double> center;// 峰中心向量
	private double rank;// 排序，首先拟合rank大的，所有全部未拟合则替代rank最小的峰

	public Model(ArrayList<Double> aCenter)
	{
		weight = defaultWight;
		variance = defaultVariance;
		center = new ArrayList<Double>(aCenter);
	}

	public Model(double aWeight, double aVarance, ArrayList<Double> aCenter)
	{
		weight = aWeight;
		variance = aVarance;
		center = new ArrayList<Double>(aCenter);
	}

	public double getWeight()
	{
		return weight;
	}

	public void setWeight(double weight)
	{
		this.weight = weight;
	}

	public double getVariance()
	{
		return variance;
	}

	public void setVariance(double variance)
	{
		this.variance = variance;
	}

	public ArrayList<Double> getCenter()
	{
		return center;
	}

	public void setCenter(ArrayList<Double> center)
	{
		this.center = center;
	}

	public double getRank()
	{
		rank = weight / variance;
		return rank;
	}

	public static void sortModel(ArrayList<Model> ModelList)
	{
		
		Comparator<Model> ModelComparator = new Comparator<Model>()
		{
			public int compare(Model s1, Model s2)
			{
				// 按照Rank排序
				if (s1.getRank() > s2.getRank())
				{
					return 1;
				}
				else if (s1.getRank()<s2.getRank())) {
					return -1;
				}else {
					return 0;
				}
			}
		};
		ModelList.sort(ModelComparator);
	}

}
