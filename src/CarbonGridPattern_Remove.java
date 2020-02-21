import java.awt.AWTEvent;
import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.EllipseRoi;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.FFT;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class CarbonGridPattern_Remove implements ExtendedPlugInFilter, DialogListener{
	
	private ImagePlus imp;
	private ImagePlus impFFT;
	private ImagePlus impiFFT;
	private ImagePlus impRes;
	private ImageProcessor ipiFFT;
	private ImageProcessor ipiFFT16;
	private ImageStack isRes;
	
	private Color color;
	
	private ResultsTable rt;
	public RoiManager rm = new RoiManager();
	
	private boolean preview = true;
	private boolean previewCheckState = false;
	
	private PlugInFilterRunner pluginFilterRunner;
	
	private GenericDialog gd;
	
	private MaximumFinder maxfind = new MaximumFinder();
	
	private double xValue;
	private double yValue;
	
	private int threshold;
	private int roiRadius;
	
	private int frame;
	
	private int NSlices;
	private int flags = DOES_ALL + DOES_STACKS;
	private int fftPeaks;
	
	private double avg;
//	private JFileChooser fileChooser = new JFileChooser();
//	private FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Tiff files", "tif");
//	private File[] selectedFiles = null;

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp=imp;
		IJ.register(CarbonGridPattern_Remove.class);
		if(imp!=null) {
			if(imp.isHyperStack()) {
				IJ.error("Grid Pattern Remover", "Sorry, but this plugin does not work with hyperstacks.");
				return DONE;
			}
			
			impFFT = new ImagePlus();
			impiFFT = new ImagePlus();
			isRes = new ImageStack();
			impRes = new ImagePlus();
			
			color = new Color(0, 0, 0);
			impFFT.setColor(color);		
			
			NSlices = imp.getStackSize();
		}
		
		return flags;
	}

	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		preview = !IJ.isMacro();
		this.pluginFilterRunner=pfr;
		
		imp.setSlice(1);
		
		gd = new NonBlockingGenericDialog("Pattern Remove");
		gd.addNumericField("Threshold", 20, 0);
		gd.addNumericField("Radius", 6, 0);
		gd.addPreviewCheckbox(pfr);
		
		gd.addDialogListener(this);
		
		gd.showDialog();
		
		if(gd.wasCanceled()) {
			return DONE;
		}
		
		preview = !gd.wasOKed();
		
		return flags;
	}

	@Override
	public void setNPasses(int nPasses) {
		if(preview) {
			frame = imp.getCurrentSlice();
		}else {
			frame = 1;
		}
		frame--;
	}
	
	@Override
	public void run(ImageProcessor ip) {
		imp.setSlice(frame);
		if(preview) {
			computeGridFilter();
			clean(imp);
			displayResult();
		}
		else {
			if(frame==0) {
				computeGridFilter();
			}
			
			clean(imp);
			
			if(frame==NSlices-1) {
				displayResult();
			}
			
			frame++;
		}
		
		rm.close();
		IJ.run("Clear Results", "");
	}

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		threshold = (int) gd.getNextNumber();
		roiRadius = (int) gd.getNextNumber();
		
		boolean currentPreviewState = gd.getPreviewCheckbox().getState();

		if (currentPreviewState != previewCheckState) {
			previewCheckState = currentPreviewState;
		}
		
		return true;
	}
	
	public void computeGridFilter() {		
		impFFT.setImage(FFT.forward(imp));
		impFFT.show();
		maxfind.findMaxima(impFFT.getProcessor(), threshold, ImageProcessor.NO_THRESHOLD, MaximumFinder.LIST, true, false);
		rt = ResultsTable.getResultsTable();
		fftPeaks = rt.size();
		
		for(int i=0; i<fftPeaks; i++) {
			xValue = rt.getValueAsDouble(0, i);
			yValue = rt.getValueAsDouble(1, i);

			EllipseRoi roi = new EllipseRoi(xValue-roiRadius/2, yValue-roiRadius/2, xValue+roiRadius/2, yValue+roiRadius/2, 1.0);
			rm.addRoi(roi);
			rm.select(i, true, false);
		}
		impFFT.hide();
	}
	
	private void clean(ImagePlus imp) {
		impFFT.show();
		impFFT.setImage(FFT.forward(imp));
		
		for(int i=0; i<fftPeaks; i++) {
			rm.select(impFFT, i);
			IJ.run(impFFT, "Clear", "slice");
		}
		
		impiFFT.setImage(FFT.inverse(impFFT));
		impiFFT.show();
		ipiFFT = impiFFT.getProcessor();
		offset(ipiFFT);
		ipiFFT16 = ipiFFT.convertToShortProcessor();
		impiFFT.setProcessor(ipiFFT16);
		
		isRes.addSlice(impiFFT.getProcessor());
		
		rm.deselect();
		
		impFFT.hide();
		impiFFT.hide();
	}
	
	private void offset(ImageProcessor ip) {
		double min = ip.getMin();
		ip.subtract(min);
	}
	
	private void displayResult() {
		impRes.setStack(isRes);
		impRes.show();
	}
	
//	@Override
//	public void run(ImageProcessor ip) {
//		fileChooser.setCurrentDirectory(new File("K:\\STAFF\\PHOTON_TEAM\\Magrini_William\\Dev\\"));
//		fileChooser.setMultiSelectionEnabled(true);
//		fileChooser.setFileFilter(fileFilter);
//		int result = fileChooser.showOpenDialog(null);
//		if (result == JFileChooser.APPROVE_OPTION) {
//			selectedFiles = fileChooser.getSelectedFiles();
//		}
//		else {
//			return;
//		}
//		IJ.open(selectedFiles[0].toPath().toString());
//		if(ip==null) {
//			IJ.error("Error", "No opened image.");
//		}else {
//			displayGUI();
//		}
//	}
	
//	private void displayGUI() {
//		GenericDialog gd = new GenericDialog("Pattern Remove");
//		gd.addNumericField("Threshold", 20, 0);
//		gd.addNumericField("Radius", 8, 0);
//		gd.addPreviewCheckbox(pfr);
//		
//		gd.showDialog();
//		
//		if(gd.wasOKed()) {
//			int threshold = (int) gd.getNextNumber();
//			int radius = (int) gd.getNextNumber();
//
//			for(int i=0; i<selectedFiles.length; i++) {
//				Cleaner cleaner = null;
//				
//				if(i!=0) {
//					IJ.open(selectedFiles[i].toPath().toString());
//					ip = WindowManager.getCurrentImage();
//					cleaner = new Cleaner(ip, threshold, radius);
//				}
//				else {
//					cleaner = new Cleaner(ip, threshold, radius);
//					cleaner.computeGridFilter();
//				}
//				
//				
//				cleaner.run();
//				
//				IJ.saveAsTiff(cleaner.ipresult, selectedFiles[i].toPath().toString().replaceAll(".tif", "_filtered.tif"));
//				ip.close();
//				cleaner.ipresult.close();
//				cleaner.rm.close();
//				IJ.run("Clear Results", "");
//			}
//		}
//	}
	
}
