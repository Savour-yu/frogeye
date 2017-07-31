package GMM;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.video.BackgroundSubtractorMOG2;

public class Model
{
	private int gDimension;

	private void setDimension(int d)
	{
		gDimension = d;
	}

	Long aLong = new Long(10000);

	private List<Vector> units;
	private boolean trainFlag = true;

	public void ReceiveVector(Vector unit) {
		if(units.add(unit);
	}

	

}