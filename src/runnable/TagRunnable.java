package runnable;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import GMM.GaussianMM;

public class TagRunnable implements Runnable
{
	private GaussianMM mog;
	private ArrayBlockingQueue<ArrayList<Double>> queue;
	private int count = 0;
	private String epc;

	public TagRunnable(String epc, int K, int dimension, ArrayBlockingQueue<ArrayList<Double>> queue)
	{
		// TODO Auto-generated constructor stub
		this.queue = queue;
		mog = new GaussianMM(K, dimension);
		this.epc = epc;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		while (true)
		{
			try
			{
				ArrayList<Double> vector = queue.take();
				count++;
				if (mog.fit(vector, GaussianMM.trainLearningRate) < 0)
				{
					mog.repaintChart(epc + " no hit " + count);

				}
				if (count % 10 == 0)
				{
					mog.repaintChart(epc + " refresh " + count);
				}
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
