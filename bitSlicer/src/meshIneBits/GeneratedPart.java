package meshIneBits;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import meshIneBits.Config.CraftConfig;
import meshIneBits.PatternTemplates.PatternTemplate;
import meshIneBits.PatternTemplates.PatternTemplate1;
import meshIneBits.PatternTemplates.PatternTemplate2;
import meshIneBits.PatternTemplates.PatternTemplate3;
import meshIneBits.Slicer.Slice;
import meshIneBits.Slicer.SliceTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;

/**
 * This object is the equivalent of the part which will be printed
 *
 */
public class GeneratedPart extends Observable implements Runnable, Observer {
	private Vector<Layer> layers = new Vector<Layer>();
	private Vector<Slice> slices = new Vector<Slice>();
	private PatternTemplate patternTemplate;
	private double skirtRadius;
	private Thread t = null;
	private SliceTool slicer;
	private boolean sliced = false;

	public GeneratedPart(Model model) {
		slicer = new SliceTool(this, model);
		slicer.sliceModel();
	}

	public void buildBits2D() {

		if ((t == null) || ((t != null) && !t.isAlive())) {
			setPatternTemplate();

			this.layers.clear();

			t = new Thread(this);
			t.start();
		}
	}

	private void buildLayers() {
		@SuppressWarnings("unchecked")
		Vector<Slice> slicesCopy = (Vector<Slice>) slices.clone();
		double bitThickness = CraftConfig.bitThickness;
		double sliceHeight = CraftConfig.sliceHeight;
		double layersOffSet = CraftConfig.layersOffset;
		double z = (CraftConfig.firstSliceHeightPercent * sliceHeight) / 100;
		int layerNumber = 1;
		int progress = 0;
		int progressGoal = slicesCopy.size();
		double zBitBottom = 0;
		double zBitRoof = bitThickness;

		Logger.updateStatus("Generating Layers");
		while (!slicesCopy.isEmpty()) {
			Vector<Slice> includedSlices = new Vector<Slice>();
			while ((z <= zBitRoof) && !slicesCopy.isEmpty()) {
				if (z >= zBitBottom) {
					includedSlices.add(slicesCopy.get(0));
				}
				slicesCopy.remove(0);
				z = z + sliceHeight;
				progress++;
				Logger.setProgress(progress, progressGoal);
			}
			if (!includedSlices.isEmpty()) {
				layers.add(new Layer(includedSlices, layerNumber, GeneratedPart.this));
				layerNumber++;
			}
			zBitBottom = zBitRoof + layersOffSet;
			zBitRoof = zBitBottom + bitThickness;
		}
		Logger.updateStatus("Layer count: " + (layerNumber - 1));
	}

	public Vector<Layer> getLayers() {
		if (!isGenerated()) {
			try {
				throw new Exception("Part not generated!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.layers;
	}

	public PatternTemplate getPatternTemplate() {
		return patternTemplate;
	}

	public double getSkirtRadius() {
		return skirtRadius;
	}

	public Vector<Slice> getSlices() {
		if (!sliced) {
			try {
				throw new Exception("Part not sliced!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return slices;
	}

	public boolean isGenerated() {
		if (!layers.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isSliced() {
		return sliced;
	}

	@Override
	public void run() {
		buildLayers();
		setChanged();
		notifyObservers();
	}

	private void setPatternTemplate() {
		switch (CraftConfig.patternNumber) {
		case 1:
			patternTemplate = new PatternTemplate1(skirtRadius);
			break;
		case 2:
			patternTemplate = new PatternTemplate2(skirtRadius);
			break;
		case 3:
			patternTemplate = new PatternTemplate3(skirtRadius);
			break;
		}
	}

	/**
	 * skirtRadius is the radius of the cylinder that fully contains the part.
	 */
	private void setSkirtRadius() {

		double radius = 0;

		for (Slice s : slices) {
			for (Segment2D segment : s.getSegmentList()) {
				if (segment.start.vSize2() > radius) {
					radius = segment.start.vSize2();
				}
				if (segment.end.vSize2() > radius) {
					radius = segment.end.vSize2();
				}
			}
		}

		skirtRadius = Math.sqrt(radius);

		System.out.println("Skirt's radius: " + ((int) skirtRadius + 1) + " mm");
	}

	@Override
	public void update(Observable o, Object arg) {
		if ((o == slicer) && (arg == this)) {
			this.slices = slicer.getSlices();
			sliced = true;
			setSkirtRadius();
			setChanged();
			notifyObservers();
		}
	}

}
