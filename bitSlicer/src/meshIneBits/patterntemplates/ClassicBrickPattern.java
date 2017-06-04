package meshIneBits.patterntemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.PatternParameterConfig;
import meshIneBits.util.Vector2;

/**
 * Simplest pattern possible: a grid with a rotation of 90° 1 layer on 2. There
 * is no auto-optimization implemented in this class.
 */

public class ClassicBrickPattern extends PatternTemplate {

	private Vector2 patternStart;
	private Vector2 patternEnd;
	private double bitsOffset;
	private double skirtRadius;

	public double getSkirtRadius() {
		return skirtRadius;
	}


	public Pattern createPattern(int layerNumber) {
		// Setup parameters
		bitsOffset = (double) config.get("bitsOffset").getCurrentValue();
		// Start
		Vector<Bit2D> bits = new Vector<Bit2D>();
		Vector2 coo = patternStart;
		int column = 1;
		while (coo.x <= patternEnd.x) {
			while (coo.y <= patternEnd.y) {
				// every bits have no rotation in that template
				bits.add(new Bit2D(coo, new Vector2(1, 0)));
				coo = coo.add(new Vector2(0, CraftConfig.bitWidth + bitsOffset));
			}
			coo = new Vector2(patternStart.x + (CraftConfig.bitLength + bitsOffset) * column,
					patternStart.y);
			column++;
		}
		// in this pattern 1 layer on 2 has a 90° rotation
		Vector2 rotation = new Vector2(1, 0);
		if (layerNumber % 2 == 0) {
			rotation = new Vector2(0, 1);
		}
		return new Pattern(bits, rotation);
	}

	/* Not yet implemented
	 * @see meshIneBits.patterntemplates.PatternTemplate#optimize(meshIneBits.Layer)
	 */
	@Override
	public int optimize(Layer actualState) {
		return -1;
	}

	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection) {
		double distance = 0;
		if (localDirection.x == 0) {// up or down
			distance = CraftConfig.bitWidth / 2;
		} else if (localDirection.y == 0) {// left or right
			distance = CraftConfig.bitLength / 2;
		}
		return this.moveBit(actualState, bitKey, localDirection, distance);
	}

	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance) {
		return actualState.moveBit(bitKey, localDirection, distance);
	}

	@Override
	public String getCommonName() {
		return "Classic Brick Pattern";
	}

	@Override
	public String getIconName() {
		return "p1.png";
	}
	
	@Override
	public String getDescription() {
		return "The simplest patterne: a grid with a rotation of 90° 1 layer on 2. "
				+ "There is no auto-optimization implemented in this class.";
	}
	
	@Override
	public String getHowToUse() {
		return "Choose the gap you desire.";
	}

	@Override
	public boolean ready(GeneratedPart generatedPart) {
		// Setting the skirtRadius and starting/ending points
		this.skirtRadius = generatedPart.getSkirtRadius();
		double maxiSide = Math.max(CraftConfig.bitLength, CraftConfig.bitWidth);
		this.patternStart = new Vector2(-skirtRadius - maxiSide, -skirtRadius - maxiSide);
		this.patternEnd = new Vector2(skirtRadius + maxiSide, skirtRadius + maxiSide);
		return true;
	}

	@Override
	public void initiateConfig() {
		// This template only need the distance between bits
		config.add(new PatternParameterConfig("bitsOffset", "Space between bits",
				"The horizontal and vertical gap in mm", 1.0, 100.0, 3.0, 1.0));
	}
}