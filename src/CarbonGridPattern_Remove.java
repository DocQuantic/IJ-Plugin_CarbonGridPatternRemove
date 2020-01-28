import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class CarbonGridPattern_Remove implements PlugIn{
	private ImagePlus ip = null;
	private JFileChooser fileChooser = new JFileChooser();
	private FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Tiff files", "tif");
	private File[] selectedFiles = null;
	
	@Override
	public void run(String arg) {
		fileChooser.setCurrentDirectory(new File("D:/Users1/"));
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(fileFilter);
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFiles = fileChooser.getSelectedFiles();
		}
		else {
			return;
		}
		IJ.open(selectedFiles[0].toPath().toString());
		ip = WindowManager.getCurrentImage();
		if(ip==null) {
			IJ.error("Error", "No opened image.");
		}else {
			ip.setSlice(1);
			displayGUI();
		}
	}
	
	private void displayGUI() {
		GenericDialog gd = new GenericDialog("Pattern Remove");
		gd.addNumericField("Threshold", 20, 0);
		gd.addNumericField("Radius", 8, 0);
		
		gd.showDialog();
		
		if(gd.wasOKed()) {
			int threshold = (int) gd.getNextNumber();
			int radius = (int) gd.getNextNumber();
			
			runCleaner(threshold, radius);
		}
	}
	
	public void runCleaner(int threshold, int radius) {
		Cleaner cleaner = new Cleaner(ip, threshold, radius, selectedFiles);
		cleaner.run();
	}
}
