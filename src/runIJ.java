import java.io.File;

import ij.ImageJ;
import ij.ImagePlus;

public class runIJ {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.getProperties().setProperty("plugins.dir", System.getProperty("user.dir")+File.separator+"target"+File.separator);
				ImageJ ij=new ImageJ();
				ImagePlus ip = new ImagePlus("K:\\STAFF\\PHOTON_TEAM\\Magrini_William\\Dev\\stream.tif");
				ip.show();
				ij.exitWhenQuitting(true);
	}

}
