import java.awt.Rectangle;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class PreviewFilter_ implements PlugInFilter{

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_16+DOES_STACKS+NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		byte[] pixels = (byte[])ip.getPixels();
		int width = ip.getWidth();
		Rectangle r = ip.getRoi();
		
		int offset, i;
		for(int y=r.y; y<(r.y+r.height); y++) {
			offset = y*width;
			for(int x=r.x; x<(r.x+r.width); x++){
				i=offset+x;
				pixels[i]=(byte)(255-pixels[i]);
			}
		}
	}

}
