import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.measure.ResultsTable;
import ij.plugin.FFT;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class Cleaner {
	private RoiManager rm = new RoiManager();
	/**Stores the original ImagePlus**/
	private ImagePlus ip = null;
	
	private ImagePlus ipresult = null;
	private ImageStack isresult = null;

	private ImageProcessor iprocifft = null;
	private ShortProcessor iprocifft16 = null;
	
	private ImagePlus ipfft = null;
	private ImagePlus ipifft = null;
	
	/**Stores the number of slices in the ImagePlus**/
	private int NSlices = 0;
	/**Stores the threshold value selected by the user**/
	private int threshold = 0;
	
	private int fftPeaks = 0;
	
	private MaximumFinder maxfind = new MaximumFinder();
	
	private double roiRadius = 6.0d;
	
	private File[] selectedFiles = null;
	
	Cleaner(ImagePlus ip, int threshold, int radius, File[] selectedFiles) {
		this.ip = ip;
		this.ipresult = new ImagePlus();
		this.threshold = threshold;
		this.roiRadius = radius;
		this.ipfft = new ImagePlus();
		this.ipifft = new ImagePlus();
		this.selectedFiles = selectedFiles;
	}
	
	public void run() {
		for(int i=0; i<selectedFiles.length; i++) {
			isresult = new ImageStack();
			ipresult = new ImagePlus();
			if(i != 0) {
				IJ.open(selectedFiles[i].toPath().toString());
				ip = WindowManager.getCurrentImage();
				ipfft = new ImagePlus();
				ipifft = new ImagePlus();
			}
			ip.setSlice(1);
			if(i == 0) {
				computeGridFilter();
			}
			NSlices = ip.getNSlices();
			
				for(int j=0; j<NSlices; j++) {
					ip.setSlice(j+1);
					
					runMethod();
				}
			
			ipresult.setStack(isresult);
			ipresult.show();
			
			IJ.saveAsTiff(ipresult, selectedFiles[i].toPath().toString().replaceAll(".tif", "_filtered.tif"));
			ip.close();
			ipresult.close();
		}
	}
	
	private void runMethod() {
		ipfft.setImage(FFT.forward(ip));
		ipfft.show();
		
		for(int i=0; i<fftPeaks; i++) {
			rm.select(ipfft, i);
			IJ.run(ipfft, "Clear", "slice");
		}
		
		ipifft.setImage(FFT.inverse(ipfft));
		ipifft.show();
		iprocifft = ipifft.getProcessor();
		offset(iprocifft);
		iprocifft16 = iprocifft.convertToShortProcessor();
		ipifft.setProcessor(iprocifft16);
		
		isresult.addSlice(ipifft.getProcessor());
		
		rm.deselect();
		
		ipfft.hide();
		ipifft.hide();
	}
	
	private void computeGridFilter() {
		ipfft.setImage(FFT.forward(ip));
		ipfft.show();
		maxfind.findMaxima(ipfft.getProcessor(), threshold, ImageProcessor.NO_THRESHOLD, MaximumFinder.LIST, true, false);
		ResultsTable rt = ResultsTable.getResultsTable();
		fftPeaks = rt.size();
		
		for(int i=0; i<fftPeaks; i++) {
			double xValue = rt.getValueAsDouble(0, i);
			double yValue = rt.getValueAsDouble(1, i);
			
			EllipseRoi roi = new EllipseRoi(xValue-roiRadius/2, yValue-roiRadius/2, xValue+roiRadius/2, yValue+roiRadius/2, 1.0);
			rm.addRoi(roi);
			rm.select(i, true, false);
		}
	}
	
	private void offset(ImageProcessor iproc) {
		double min = iproc.getMin();
		iproc.subtract(min);
	}
}
